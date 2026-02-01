package model;
import java.util.Date;
public class BangGia {
    private String ID_BangGia;
    private String ID_LoaiXe;
    private Date thoiGianBatDau;
    private Date thoiGianKetThuc;
    private double gia1KM;

    public BangGia(String ID_BangGia, String ID_LoaiXe, Date ThoiGianBatDau, Date ThoiGianKetThuc, double gia1KM) {
        this.ID_BangGia = ID_BangGia;
        this.ID_LoaiXe = ID_LoaiXe;
        this.thoiGianBatDau = ThoiGianBatDau;
        this.thoiGianKetThuc = ThoiGianKetThuc;
        this.gia1KM = gia1KM;

    }

    public String getID_BangGia() {
        return ID_BangGia;
    }

    public void setID_BangGia(String ID_BangGia) {
        this.ID_BangGia = ID_BangGia;
    }

    public String getID_LoaiXe() {
        return ID_LoaiXe;
    }

    public void setID_LoaiXe(String ID_LoaiXe) {
        this.ID_LoaiXe = ID_LoaiXe;
    }

    public Date getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Date ThoiGianBatDau) {
        this.thoiGianBatDau = ThoiGianBatDau;
    }

    public Date getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(Date ThoiGianKetThuc) {
        this.thoiGianKetThuc = ThoiGianKetThuc;
    }

    public double getGia1KM() {
        return gia1KM;
    }

    public void setGia1KM(double gia1KM) {
        this.gia1KM = gia1KM;
    }
    
    
}
