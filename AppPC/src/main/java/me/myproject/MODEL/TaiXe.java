package me.myproject.MODEL;

import java.util.Date;

public class TaiXe {
    // Các trường ánh xạ trực tiếp từ JSON "result"
    private String driverId;
    private String driverName;
    private String birthDate; // Server trả về String "yyyy-MM-dd"
    private String citizenId;
    private String drivingLicense;
    private String criminalRecord;
    private String phone;
    private String email;
    private String licensePlate;
    private String vehicleName;
    private String avatar;
    private Boolean activityStatus;
    private String gender;
    private String address;
    private String area;
    private Double currentLat;
    private Double currentLng;

    // Default Constructor
    public TaiXe() {}

    // Getter và Setter
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }

    public String getDrivingLicense() { return drivingLicense; }
    public void setDrivingLicense(String drivingLicense) { this.drivingLicense = drivingLicense; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public Boolean getActivityStatus() { return activityStatus; }
    public void setActivityStatus(Boolean activityStatus) { this.activityStatus = activityStatus; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // Bạn có thể thêm các getter/setter cho các trường còn lại nếu cần dùng trong Swing
    public Double getCurrentLat() { return currentLat; }
    public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }
    public Double getCurrentLng() { return currentLng; }
    public void setCurrentLng(Double currentLng) { this.currentLng = currentLng; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender;} 
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}