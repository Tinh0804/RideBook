
package model;
public class VaiTro {
    private String ID_VaiTro;
    private String tenVaiTro;

    public String getID_VaiTro() {
        return ID_VaiTro;
    }

    public void setID_VaiTro(String iD_VaiTro) {
        ID_VaiTro = iD_VaiTro;
    }

    public String getTenVaiTro() {
        return tenVaiTro;
    }

    public void setTenVaiTro(String tenVaiTro) {
        this.tenVaiTro = tenVaiTro;
    }

    public VaiTro(String ID_VaiTro, String tenVaiTro) {
        this.ID_VaiTro = ID_VaiTro;
        this.tenVaiTro = tenVaiTro;
    }
}
