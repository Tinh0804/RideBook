package com.project.ridebook.keycloak;

record LegacyAccount(
        String accountId,
        String username,
        String passwordHash,
        boolean enabled,
        String role,
        String profileId,
        String email,
        String displayName) {
}
