package com.cyancoder.ssouser.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The revoke/restore contract is what makes a withdrawn grant recoverable, so
 * pin the state transitions the service relies on.
 */
class RoleAssignmentRevocationTest {

    private UserClientRoleAssignmentEntity grant() {
        UserClientRoleAssignmentEntity entity = new UserClientRoleAssignmentEntity();
        entity.setUsername("acme-head");
        entity.setClientId("cyan-panel");
        entity.setRoleKey("client-admin");
        entity.setGrantedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setGrantedBy("owner");
        return entity;
    }

    @Test
    void newAssignmentIsActiveByDefault() {
        assertTrue(grant().isActive());
    }

    @Test
    void revokingKeepsTheRowAndRecordsWhoDidIt() {
        UserClientRoleAssignmentEntity entity = grant();

        entity.setActive(false);
        entity.setRevokedAt(Instant.parse("2026-02-01T00:00:00Z"));
        entity.setRevokedBy("owner");

        assertFalse(entity.isActive());
        assertEquals("client-admin", entity.getRoleKey());
        assertEquals("owner", entity.getRevokedBy());
        // The original grant metadata survives the revocation.
        assertEquals("owner", entity.getGrantedBy());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), entity.getGrantedAt());
    }

    @Test
    void restoringClearsRevocationSoTheGrantReadsAsCurrent() {
        UserClientRoleAssignmentEntity entity = grant();
        entity.setActive(false);
        entity.setRevokedAt(Instant.parse("2026-02-01T00:00:00Z"));
        entity.setRevokedBy("owner");

        // Re-granting reuses the same row rather than creating a duplicate.
        entity.setActive(true);
        entity.setRevokedAt(null);
        entity.setRevokedBy(null);
        entity.setGrantedAt(Instant.parse("2026-03-01T00:00:00Z"));
        entity.setGrantedBy("support");

        assertTrue(entity.isActive());
        assertNull(entity.getRevokedAt());
        assertNull(entity.getRevokedBy());
        assertNotNull(entity.getGrantedAt());
        assertEquals("support", entity.getGrantedBy());
    }
}
