
package model;

import java.sql.Timestamp;

public class KhuyenMai {
    private String ID_KhuyenMai;
    private String tenKM;
    private double hanMuc;
    private Timestamp tgBatDau;
    private Timestamp tgKetThuc;
    private String dieuKienApDung;
    private int soLuong;

    public KhuyenMai(String ID_KhuyenMai, String tenKM, double hanMuc,Timestamp tgBatDau, Timestamp tgKetThuc,String dieuKienApDung,int soLuong) {
        this.ID_KhuyenMai = ID_KhuyenMai;
        this.dieuKienApDung = dieuKienApDung;
        this.hanMuc = hanMuc;
        this.soLuong = soLuong;
        this.tenKM = tenKM;
        this.tgBatDau = tgBatDau;
        this.tgKetThuc = tgKetThuc;
    }

    

    public KhuyenMai() {
		// TODO Auto-generated constructor stub
	}



	public String getID_KhuyenMai() {
        return ID_KhuyenMai;
    }

    public void setID_KhuyenMai(String ID_KhuyenMai) {
        this.ID_KhuyenMai = ID_KhuyenMai;
    }

    public String getTenKM() {
        return tenKM;
    }

    public void setTenKM(String tenKM) {
        this.tenKM = tenKM;
    }

    public double getHanMuc() {
        return hanMuc;
    }

    public void setHanMuc(double hanMuc) {
        this.hanMuc = hanMuc;
    }

    public Timestamp getTGBatDau() {
        return tgBatDau;
    }

    public void setTGBatDau(Timestamp tgBatDau) {
        this.tgBatDau = tgBatDau;
    }

    public Timestamp getTGKetThuc() {
        return tgKetThuc;
    }

    public void setTGKetThuc(Timestamp tgKetThuc) {
        this.tgKetThuc = tgKetThuc;
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
