package me.myproject.MODEL;

public class PhuongThucThanhToan {
    private String ID_ThanhToan;
    private String loaiHinhThucThanhToan;
    private boolean trangThaiThanhToan;

    public PhuongThucThanhToan() {
        // Default constructor
    }

    public PhuongThucThanhToan(String ID_ThanhToan, String loaiHinhThucThanhToan, boolean trangThaiThanhToan) {
        this.ID_ThanhToan = ID_ThanhToan;
        this.loaiHinhThucThanhToan = loaiHinhThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getID_ThanhToan() {
        return ID_ThanhToan;
    }

    public void setID_ThanhToan(String ID_ThanhToan) {
        this.ID_ThanhToan = ID_ThanhToan;
    }

    public String getLoaiHinhThucThanhToan() {
        return loaiHinhThucThanhToan;
    }

    public void setLoaiHinhThucThanhToan(String loaiHinhThucThanhToan) {
        this.loaiHinhThucThanhToan = loaiHinhThucThanhToan;
    }

    public boolean isTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(boolean trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }
    
}
