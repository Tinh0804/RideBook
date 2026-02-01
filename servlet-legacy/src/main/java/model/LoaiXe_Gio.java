package model;
public class LoaiXe_Gio {
    private String ID_LoaiXe;
    private String ID_Gio;
    private double phuThu;

    public LoaiXe_Gio(String ID_LoaiXe, String ID_Gio, double phuThu) {
        this.ID_LoaiXe = ID_LoaiXe;
        this.ID_Gio = ID_Gio;
        this.phuThu = phuThu;
    }
    public String getID_LoaiXe() {
        return ID_LoaiXe;
    }
    public void setID_LoaiXe(String iD_LoaiXe) {
        ID_LoaiXe = iD_LoaiXe;
    }
    public String getID_Gio() {
        return ID_Gio;
    }
    public void setID_Gio(String iD_Gio) {
        ID_Gio = iD_Gio;
    }
    public double getPhuThu() {
        return phuThu;
    }
    public void setPhuThu(double phuThu) {
        this.phuThu = phuThu;
    }



}
