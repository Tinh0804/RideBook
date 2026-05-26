package me.myproject.Utilities.Enum;

public enum RoleName {
    CUSTOMER("Khách hàng"),
    DRIVER("Tài xế"),
    ADMIN("Quản trị viên");

    private final String roleName;

    RoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
