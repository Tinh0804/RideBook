package model;
import java.sql.Time;

public class Gio {
    private String ID_Gio;
    private Time thoiGianBatDau;
    private Time thoiGianKetThuc;

    public String getID_Gio() {
        return ID_Gio;
    }

    public void setID_Gio(String iD_Gio) {
        ID_Gio = iD_Gio;
    }

    public Time getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(Time thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public Time getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(Time thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public Gio(String ID_Gio, Time thoiGianBatDau, Time thoiGianKetThuc) {
        this.ID_Gio = ID_Gio;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

	public Gio() {
		// TODO Auto-generated constructor stub
	}
}
