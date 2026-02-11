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
}