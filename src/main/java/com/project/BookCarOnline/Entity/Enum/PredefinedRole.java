package com.project.BookCarOnline.Entity.Enum;

import java.util.Map;

public enum PredefinedRole {
    ADMIN,
    CUSTOMER,
    DRIVER;

    private static final Map<PredefinedRole, String> roleDescriptions = Map.of(
            ADMIN, "11111111-2222-3333-4444-555555555555",
            CUSTOMER, "USER",
            DRIVER, "DRIVER"
    );

    public String getDescription() {
        return roleDescriptions.get(this);
    }
}
