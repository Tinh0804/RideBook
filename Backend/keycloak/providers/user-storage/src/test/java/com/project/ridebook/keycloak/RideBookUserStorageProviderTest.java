package com.project.ridebook.keycloak;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RideBookUserStorageProviderTest {
    @Test
    void verifiesSpringCompatibleBcryptAndRejectsInvalidHashes() {
        String hash = BCrypt.hashpw("old-password", BCrypt.gensalt(10));

        assertTrue(RideBookUserStorageProvider.verifyPassword("old-password", hash));
        assertFalse(RideBookUserStorageProvider.verifyPassword("wrong-password", hash));
        assertFalse(RideBookUserStorageProvider.verifyPassword("old-password", "not-bcrypt"));
    }
}
