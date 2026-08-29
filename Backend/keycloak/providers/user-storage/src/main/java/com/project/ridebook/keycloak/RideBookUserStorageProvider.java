package com.project.ridebook.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class RideBookUserStorageProvider implements UserStorageProvider, UserLookupProvider,
        CredentialInputValidator, CredentialInputUpdater {
    private final KeycloakSession session;
    private final ComponentModel model;
    private final LegacyAccountRepository repository;
    private final Map<String, UserModel> loadedUsers = new HashMap<>();

    RideBookUserStorageProvider(
            KeycloakSession session, ComponentModel model, LegacyAccountRepository repository) {
        this.session = session;
        this.model = model;
        this.repository = repository;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        return loadedUsers.computeIfAbsent(username.toLowerCase(), ignored ->
                repository.findByUsername(username).map(account -> adapter(realm, account)).orElse(null));
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        return repository.findByUsername(new StorageId(id).getExternalId())
                .map(account -> adapter(realm, account))
                .orElse(null);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return repository.findByEmail(email).map(account -> adapter(realm, account)).orElse(null);
    }

    private UserModel adapter(RealmModel realm, LegacyAccount account) {
        return new LegacyUserAdapter(session, realm, model, account);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType)
                && repository.findByUsername(user.getUsername()).map(LegacyAccount::passwordHash).isPresent();
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel credential)) {
            return false;
        }
        Optional<LegacyAccount> account = repository.findByUsername(user.getUsername());
        return account.filter(LegacyAccount::enabled)
                .map(LegacyAccount::passwordHash)
                .filter(hash -> verifyPassword(credential.getValue(), hash))
                .isPresent();
    }

    static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null || !hash.startsWith("$2")) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (supportsCredentialType(input.getType())) {
            throw new ReadOnlyException("Use RideBook password recovery to update this credential");
        }
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    @Override
    public void close() {
    }
}
