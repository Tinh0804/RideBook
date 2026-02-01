
package model;
public class TaiKhoan {
    private String SDT;
    private String matKhau;
    private String ID_VaiTro;
    private String ID_NguoiDung;
    private Boolean trangThaiTK;

    public String getUserName() {
        return SDT;
    }

    public void setUserName(String userName) {
        this.SDT = userName;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getID_VaiTro() {
        return ID_VaiTro;
    }

    public void setID_VaiTro(String iD_VaiTro) {
        ID_VaiTro = iD_VaiTro;
    }

    public String getID_NguoiDung() {
        return ID_NguoiDung;
    }

    public void setID_NguoiDung(String ID_NguoiDung) {
       this.ID_NguoiDung = ID_NguoiDung;
    }

    public TaiKhoan(String userName, String matKhau, String ID_VaiTro, String ID_NguoiDung) {
        this.SDT = userName;
        this.matKhau = matKhau;
        this.ID_VaiTro = ID_VaiTro;
        this.ID_NguoiDung = ID_NguoiDung;
        this.trangThaiTK=true;
    }

	public TaiKhoan() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		String json = String.format("{\n"
				+ "                \"userName\": \"%s\",\n"
				+ "                \"matKhau\": %s,\n"
				+ "                \"id_VaiTro\":\"%s\",\n"
				+ "                \"ID_NguoiDung\": %s\n"
				+ "                }",this.SDT, this.matKhau,this.ID_VaiTro,this.ID_NguoiDung);
		return json;
		
	}

	public Boolean getTrangThaiTK() {
		return trangThaiTK;
	}

	public void setTrangThaiTK(Boolean trangThaiTK) {
		this.trangThaiTK = trangThaiTK;
	}
}
