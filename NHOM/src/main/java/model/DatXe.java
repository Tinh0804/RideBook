package model;

import java.sql.Timestamp;

public class DatXe {
	private String ID_DatXe;
    private String ID_KH;
    private String ID_TX;
    private String ID_ThanhToan;
    private String ID_KhuyenMai;
    private String DiemTra;
    private String DiemDon;
    private Double GiaTien;
    private Timestamp thoiGianDat;
    private Timestamp thoiGianDon;
    private Timestamp thoiGianDen;
    private String trangThai;
    private double khoangCach;
    private int diemSo;
    private String danhGia;

    public DatXe(String ID_ChuyenXe, String ID_KhachHang, String ID_TaiXe, String ID_ThanhToan, String ID_KhuyenMai,
             String DiemTra, String DiemDon, Double GiaTien,Timestamp thoiGianDat, Timestamp thoiGianDon, Timestamp thoiGianDen,
            String trangThai, double khoangCach, int diemSo, String danhGia) {
        this.ID_DatXe = ID_ChuyenXe;
        this.ID_KH = ID_KhachHang;
        this.ID_TX = ID_TaiXe;
        this.ID_ThanhToan = ID_ThanhToan;
        this.ID_KhuyenMai = ID_KhuyenMai;
        this.DiemTra = DiemTra;
        this.DiemDon = DiemDon;
        this.GiaTien = GiaTien;
        this.thoiGianDat = thoiGianDat;
        this.thoiGianDon = thoiGianDon;
        this.thoiGianDen = thoiGianDen;
        this.trangThai = trangThai;
        this.khoangCach = khoangCach;
        this.diemSo = diemSo;
        this.danhGia = danhGia;
    }

    public DatXe() {
	}

	public String getID_DatXe() {
        return ID_DatXe;
    }

    public void setID_DatXe(String ID_ChuyenXe) {
        this.ID_DatXe = ID_ChuyenXe;
    }

    public String getID_KhachHang() {
        return ID_KH;
    }

    public void setID_KhachHang(String ID_KhachHang) {
        this.ID_KH = ID_KhachHang;
    }

    public String getID_TaiXe() {
        return ID_TX;
    }

    public void setID_TaiXe(String ID_TaiXe) {
        this.ID_TX = ID_TaiXe;
    }

    public String getID_ThanhToan() {
        return ID_ThanhToan;
    }

    public void setID_ThanhToan(String ID_ThanhToan) {
        this.ID_ThanhToan = ID_ThanhToan;
    }

    public String getID_KhuyenMai() {
        return ID_KhuyenMai;
    }

    public void setID_KhuyenMai(String ID_KhuyenMai) {
        this.ID_KhuyenMai = ID_KhuyenMai;
    }


    public String getDiemTra() {
        return DiemTra;
    }

    public void setDiemTra(String DiemTra) {
        this.DiemTra = DiemTra;
    }

    public String getDiemDon() {
        return DiemDon;
    }

    public void setDiemDon(String DiemDon) {
        this.DiemDon = DiemDon;
    }
    
    public Double getGiaTien() {
        return GiaTien;
    }

    public void setGiaTien(Double GiaTien) {
        this.GiaTien = GiaTien;
    }

    public Timestamp getThoiGianDat() {
        return thoiGianDat;
    }

    public void setThoiGianDat(java.sql.Timestamp thoiGianDat) {
        this.thoiGianDat = thoiGianDat;
    }

    public Timestamp getThoiGianDon() {
        return thoiGianDon;
    }

    public void setThoiGianDon(Timestamp thoiGianDon) {
        this.thoiGianDon = thoiGianDon;
    }

    public Timestamp getThoiGianDen() {
        return thoiGianDen;
    }

    public void setThoiGianDen(Timestamp thoiGianDen) {
        this.thoiGianDen = thoiGianDen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public double getKhoangCach() {
        return khoangCach;
    }

    public void setKhoangCach(double khoangCach) {
        this.khoangCach = khoangCach;
    }

    public int getDiemSo() {
        return diemSo;
    }

    public void setDiemSo(int diemSo) {
        this.diemSo = diemSo;
    }

    public String getDanhGia() {
        return danhGia;
    }

    public void setDanhGia(String danhGia) {
        this.danhGia = danhGia;
    }
}
