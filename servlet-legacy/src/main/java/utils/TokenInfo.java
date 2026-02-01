package utils;

import model.TaiKhoan;

public class TokenInfo {
    private long createTime;
    private TaiKhoan taiKhoan;

    public TokenInfo(long createTime,TaiKhoan taiKhoan) {
        this.createTime = System.currentTimeMillis();
        this.setTaiKhoan(taiKhoan);
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isExpired(long expireDurationMillis) {
        long now = System.currentTimeMillis();
        return (now - createTime) > expireDurationMillis;
    }

	public TaiKhoan getTaiKhoan() {
		return taiKhoan;
	}

	public void setTaiKhoan(TaiKhoan taiKhoan) {
		this.taiKhoan = taiKhoan;
	}
}

