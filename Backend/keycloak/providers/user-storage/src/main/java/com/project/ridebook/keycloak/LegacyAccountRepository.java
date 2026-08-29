package com.project.ridebook.keycloak;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

final class LegacyAccountRepository {
    private static final String SELECT = """
            SELECT a.account_id, a.user_name, a.pass_word, a.account_status,
                   r.role_name,
                   COALESCE(c.customer_id, d.driver_id, a.account_id) AS profile_id,
                   COALESCE(c.email, d.email) AS email,
                   COALESCE(c.customer_name, d.driver_name, a.user_name) AS display_name
              FROM account a
              JOIN role r ON r.role_id = a.role_id
              LEFT JOIN customer c ON c.account_id = a.account_id
              LEFT JOIN driver d ON d.account_id = a.account_id
            """;

    private final String url;
    private final String username;
    private final String password;

    LegacyAccountRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    Optional<LegacyAccount> findByUsername(String value) {
        return find(SELECT + " WHERE lower(a.user_name) = lower(?) LIMIT 1", value);
    }

    Optional<LegacyAccount> findByEmail(String value) {
        return find(SELECT + " WHERE lower(COALESCE(c.email, d.email)) = lower(?) LIMIT 1", value);
    }

    private Optional<LegacyAccount> find(String sql, String value) {
        // ponytail: one connection per login lookup; add a small pool only when login load requires it.
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setQueryTimeout(5);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("RideBook user store is unavailable", exception);
        }
    }

    private LegacyAccount map(ResultSet result) throws SQLException {
        return new LegacyAccount(
                result.getString("account_id"),
                result.getString("user_name"),
                result.getString("pass_word"),
                result.getBoolean("account_status"),
                result.getString("role_name"),
                result.getString("profile_id"),
                result.getString("email"),
                result.getString("display_name"));
    }
}
