package model;

import java.util.Date;

public class TaiXe {
    private String ID_TX;
    private String ID_LoaiXe;
    private String tenTX;
    private Date NGSINH;
    private String CCCD;
    private String GPLX;
    private String LLTP;
    private String SDT;
    private String EMAIL;
    private String bienSoXe;
    private String tenXe;
    private String anhDaiDien;
    private Boolean trangThaiHD;
    private String gioiTinh;
    private String diaChi;
    private String khuVuc;
    
    
    public TaiXe(String ID_TaiXe, String ID_LoaiXe, String tenTaiXe, Date ngaySinh, String CCCD, String GPLX,String LLTP, String SDT, String email, String bienSoXe, String tenXe,Boolean trangThaiHD,String anhDaiDien,String gioiTinh,String diaChi,String khuVuc) {
        this.ID_TX = ID_TaiXe;
        this.ID_LoaiXe = ID_LoaiXe;
        this.tenTX = tenTaiXe;
        this.NGSINH = ngaySinh;
        this.CCCD = CCCD;
        this.GPLX = GPLX;
        this.SDT = SDT;
        this.EMAIL = email;
        this.bienSoXe = bienSoXe;
        this.tenXe = tenXe;
        this.setTrangThaiHD(trangThaiHD);
        this.anhDaiDien=anhDaiDien;
        this.LLTP=LLTP;
        this.gioiTinh=gioiTinh;
        this.diaChi=diaChi;
        this.khuVuc=khuVuc;
    }

    public TaiXe() {
		// TODO Auto-generated constructor stub
	}

	public String getID_TaiXe() {
        return ID_TX;
    }

    public void setID_TaiXe(String ID_TaiXe) {
        this.ID_TX = ID_TaiXe;
    }

    public String getID_LoaiXe() {
        return ID_LoaiXe;
    }

    public void setID_LoaiXe(String ID_LoaiXe) {
        this.ID_LoaiXe = ID_LoaiXe;
    }

    public String getTenTaiXe() {
        return tenTX;
    }

    public void setTenTaiXe(String tenTaiXe) {
        this.tenTX = tenTaiXe;
    }

    public Date getNgaySinh() {
        return NGSINH;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.NGSINH = ngaySinh;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public String getGPLX() {
        return GPLX;
    }

    public void setGPLX(String GPLX) {
        this.GPLX = GPLX;
    }

    public String getSDT() {
        return SDT;
    }

    public void setSDT(String SDT) {
        this.SDT = SDT;
    }

    public String getEmail() {
        return EMAIL;
    }

    public void setEmail(String email) {
        this.EMAIL = email;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getTenXe() {
        return tenXe;
    }

    public void setTenXe(String tenXe) {
        this.tenXe = tenXe;
    }

	public String getAnhDaiDien() {
		return anhDaiDien;
	}

	public void setAnhDaiDien(String anhDaiDien) {
		this.anhDaiDien = anhDaiDien;
	}

	public Boolean getTrangThaiHD() {
		return trangThaiHD;
	}

	public void setTrangThaiHD(Boolean trangThaiHD) {
		this.trangThaiHD = trangThaiHD;
	}

	public String getLLTP() {
		return LLTP;
	}

	public void setLLTP(String lLTP) {
		LLTP = lLTP;
	}

	public String getGioiTinh() {
		return gioiTinh;
	}

	public void setGioiTinh(String gioiTinh) {
		this.gioiTinh = gioiTinh;
	}

	public String getDiaChi() {
		return diaChi;
	}

	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
	}

	public String getKhuVuc() {
		return khuVuc;
	}

	public void setKhuVuc(String khuVuc) {
		this.khuVuc = khuVuc;
	}
    
}
