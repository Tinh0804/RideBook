package model;

public class PhuongThucThanhToan {
    private String ID_ThanhToan;
    private String loaiHinhThanhToan;
    private boolean trangThaiTT;

    public PhuongThucThanhToan(String ID_ThanhToan, String loaiHinhThanhToan, boolean trangThaiTT) {
        this.ID_ThanhToan = ID_ThanhToan;
        this.loaiHinhThanhToan = loaiHinhThanhToan;
        this.trangThaiTT = trangThaiTT;
    }

    public PhuongThucThanhToan() {
		// TODO Auto-generated constructor stub
	}

	public String getID_ThanhToan() {
        return ID_ThanhToan;
    }

    public void setID_ThanhToan(String ID_ThanhToan) {
        this.ID_ThanhToan = ID_ThanhToan;
    }

    public String getLoaiHinhThanhToan() {
        return loaiHinhThanhToan;
    }

    public void setLoaiHinhThanhToan(String loaiHinhThanhToan) {
        this.loaiHinhThanhToan = loaiHinhThanhToan;
    }

    public boolean isTrangThaiTT() {
        return trangThaiTT;
    }

    public void setTrangThaiTT(boolean trangThaiTT) {
        this.trangThaiTT = trangThaiTT;
    }
    
}
