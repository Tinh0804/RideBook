package me.myproject.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class TokenStore {
    private static final String TOKEN_FILE = System.getProperty("user.home") + "/.datxe_token";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_PROFILE_ID = "profile_id";
    private static final String KEY_USER_NAME = "user_name";

    private TokenStore() {
    }

    public static void saveTokens(String accessToken, String refreshToken) throws IOException {
        saveAuthState(accessToken, refreshToken, null, null, null);
    }

    public static void saveAuthState(String accessToken, String refreshToken, String role, String profileId, String userName) throws IOException {
        Properties props = new Properties();
        if (accessToken != null) {
            props.setProperty(KEY_ACCESS_TOKEN, accessToken);
        }
        if (refreshToken != null) {
            props.setProperty(KEY_REFRESH_TOKEN, refreshToken);
        }
        if (role != null) {
            props.setProperty(KEY_ROLE, role);
        }
        if (profileId != null) {
            props.setProperty(KEY_PROFILE_ID, profileId);
        }
        if (userName != null) {
            props.setProperty(KEY_USER_NAME, userName);
        }
        Path path = Paths.get(TOKEN_FILE);
        try (OutputStream os = Files.newOutputStream(path)) {
            props.store(os, "DatXe tokens");
        }
    }

    public static Optional<String> loadAccessToken() throws IOException {
        return loadToken(KEY_ACCESS_TOKEN);
    }

    public static Optional<String> loadRefreshToken() throws IOException {
        return loadToken(KEY_REFRESH_TOKEN);
    }

    public static Optional<String> loadRole() throws IOException {
        return loadToken(KEY_ROLE);
    }

    public static Optional<String> loadProfileId() throws IOException {
        return loadToken(KEY_PROFILE_ID);
    }

    public static Optional<String> loadUserName() throws IOException {
        return loadToken(KEY_USER_NAME);
    }

    public static void clearTokens() throws IOException {
        Path path = Paths.get(TOKEN_FILE);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    private static Optional<String> loadToken(String key) throws IOException {
        Path path = Paths.get(TOKEN_FILE);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(path)) {
            props.load(is);
        }
        return Optional.ofNullable(props.getProperty(key));
    }
}
