package com.project.BookCarOnline.Entity.Enum;

import java.util.Map;

public enum PredefinedRole {
    ADMIN(RoleName.ADMIN),
    CUSTOMER(RoleName.CUSTOMER),
    DRIVER(RoleName.DRIVER);

    private final String roleName;

    PredefinedRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    // Các hằng số static để dùng trong @PreAuthorize
    public static class RoleName {
        public static final String ADMIN = "ADMIN";
        public static final String CUSTOMER = "CUSTOMER";
        public static final String DRIVER = "DRIVER";
    }
    public static final String HAS_ROLE_ADMIN    = "hasRole('ADMIN')";
    public static final String HAS_ROLE_DRIVER   = "hasRole('DRIVER')";
    public static final String HAS_ROLE_CUSTOMER = "hasRole('CUSTOMER')";
}