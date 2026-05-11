package me.myproject.MODEL;

import java.util.Date;

public class TaiKhoan {
    private String accountId;
    private String ID_Ref;
    private String userName;
    private VaiTro role;
    private Boolean accountStatus = true; // Default value for account status
    private Date createdAt; // Default value for creation date
    private String token;
    private String refreshToken;

    public TaiKhoan() {
        // Default constructor
    }

    public TaiKhoan(String userName, String passWord, VaiTro role, String accountId) {
        this.userName = userName;
        // this.passWord = passWord;
        this.role = role;
        this.accountId = accountId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public VaiTro getRole() {
        return role;
    }


    public String getID_VaiTro() {
        return role.getRoleId();
    }

    public void setID_VaiTro(String iD_VaiTro) {
        role.setRoleId(iD_VaiTro);
    }



    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Boolean getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Boolean accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getID_Ref() {
        return ID_Ref;
    }

    public void setID_Ref(String ID_Ref) {
        this.ID_Ref = ID_Ref;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
