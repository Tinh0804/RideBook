
package me.myproject.MODEL;

import java.util.Date;

public class KhuyenMai {
    private String ID_KhuyenMai;
    private String tenKhuyenMai;
    private double hanMuc;
    private Date thoiGianBatDau;
    private Date thoiGianKetThuc;
    private String dieuKienApDung;
    private int soLuong;

    public KhuyenMai() {
        // Default constructor
    }

    public KhuyenMai(String ID_KhuyenMai, String tenKhuyenMai, double hanMuc,Date thoiGianBatDau, Date thoiGianKetThuc,String dieuKienApDung,int soLuong) {
        this.ID_KhuyenMai = ID_KhuyenMai;
        this.dieuKienApDung = dieuKienApDung;
        this.hanMuc = hanMuc;
        this.soLuong = soLuong;
        this.tenKhuyenMai = tenKhuyenMai;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    

    public String getID_KhuyenMai() {
        return ID_KhuyenMai;
    }

    public void setID_KhuyenMai(String ID_KhuyenMai) {
        this.ID_KhuyenMai = ID_KhuyenMai;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public void setTenKhuyenMai(String tenKhuyenMai) {
        this.tenKhuyenMai = tenKhuyenMai;
    }

    public double getHanMuc() {
        return hanMuc;
    }

    public void setHanMuc(double hanMuc) {
        this.hanMuc = hanMuc;
    }

    public Date getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Date thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public Date getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(Date thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public String getDieuKienApDung() {
        return dieuKienApDung;
    }

    public void setDieuKienApDung(String dieuKienApDung) {
        this.dieuKienApDung = dieuKienApDung;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
    
}
