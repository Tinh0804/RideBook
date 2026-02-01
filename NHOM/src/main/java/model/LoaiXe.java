package model;

public class LoaiXe {
    private String ID_LoaiXe;
    private String tenLoaiXe;
    private double gia1KM;


    public String getID_LoaiXe() {
        return ID_LoaiXe;
    }


    public void setID_LoaiXe(String iD_LoaiXe) {
        ID_LoaiXe = iD_LoaiXe;
    }


    public String getTenLoaiXe() {
        return tenLoaiXe;
    }


    public void setTenLoaiXe(String tenLoaiXe) {
        this.tenLoaiXe = tenLoaiXe;
    }


    public double getGia1KM() {
        return gia1KM;
    }


    public void setGia1KM(double gia1km) {
        gia1KM = gia1km;
    }


    public LoaiXe(String ID_LoaiXe, String tenLoaiXe, double gia1KM) {
        this.ID_LoaiXe = ID_LoaiXe;
        this.tenLoaiXe = tenLoaiXe;
        this.gia1KM = gia1KM;
    }


	public LoaiXe() {
		// TODO Auto-generated constructor stub
	}
    
}
