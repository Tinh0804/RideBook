
package me.myproject.MODEL;
public class VaiTro {
    private String roleId;
    private String roleName;

    public VaiTro() {
        // Default constructor
    }

    public VaiTro(String roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getTenVaiTro() {
        return roleName;
    }

    public void setTenVaiTro(String tenVaiTro) {
        this.roleName = tenVaiTro;
    }
}
