package com.project.ridebook.keycloak;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public final class RideBookUserStorageProviderFactory
        implements UserStorageProviderFactory<RideBookUserStorageProvider> {
    public static final String PROVIDER_ID = "ridebook-legacy-users";
    private String url;
    private String username;
    private String password;

    @Override
    public void init(Config.Scope config) {
        url = requiredEnvironment("RIDEBOOK_DB_URL", "DB_URL");
        username = requiredEnvironment("RIDEBOOK_DB_USERNAME", "DB_USERNAME");
        password = requiredEnvironment("RIDEBOOK_DB_PASSWORD", "DB_PASSWORD");
    }

    @Override
    public RideBookUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new RideBookUserStorageProvider(
                session, model, new LegacyAccountRepository(url, username, password));
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    private String requiredEnvironment(String name, String fallbackName) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getenv(fallbackName);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " or " + fallbackName + " is required");
        }
        return value;
    }
}
