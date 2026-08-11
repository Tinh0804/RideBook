package com.project.ridebook.keycloak;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

final class LegacyUserAdapter extends AbstractUserAdapter {
    private final LegacyAccount account;

    LegacyUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, LegacyAccount account) {
        super(session, realm, model);
        this.account = account;
    }

    @Override
    public String getUsername() {
        return account.username();
    }

    @Override
    public boolean isEnabled() {
        return account.enabled();
    }

    @Override
    public String getEmail() {
        return account.email();
    }

    @Override
    public String getFirstName() {
        return account.displayName();
    }

    @Override
    public String getFirstAttribute(String name) {
        return switch (name) {
            case "account_id" -> account.accountId();
            case "profile_id" -> account.profileId();
            default -> super.getFirstAttribute(name);
        };
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        String value = getFirstAttribute(name);
        return value == null ? Stream.empty() : Stream.of(value);
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> attributes = new HashMap<>(super.getAttributes());
        attributes.put("account_id", List.of(account.accountId()));
        attributes.put("profile_id", List.of(account.profileId()));
        return attributes;
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        RoleModel role = realm.getRole(account.role());
        return role == null ? Stream.empty() : Stream.of(role);
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }
}
