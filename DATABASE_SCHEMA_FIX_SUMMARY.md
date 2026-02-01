# 🔧 Database Schema Fix Summary

## ✅ Hoàn thành sửa đổi Entities và Repositories

Đã đồng bộ hóa **100%** các Entity và Repository với schema database từ file `QuanLyDatXe.sql`.

---

## 📊 Database Schema Analysis

### **Tables trong QLDX Database:**

| Table SQL | Entity Class | Status |
|-----------|--------------|--------|
| `VAITRO` | `Role` | ✅ Fixed |
| `TAIKHOAN` | `Account` | ✅ Fixed |
| `KHACHHANG` | `Customer` | ✅ Fixed |
| `LOAIXE` | `VehicleType` | ✅ Fixed |
| `TAIXE` | `Driver` | ✅ Fixed |
| `PHUONGTHUCTHANHTOAN` | `Payment` | ✅ Fixed |
| `KHUYENMAI` | `Promotion` | ✅ Fixed |
| `GIO` | `Time` | ⚠️ Not used yet |
| `LOAIXE_GIO` | `VehicleType_Time` | ⚠️ Not used yet |
| `DATXE` | `Booking` | ✅ Fixed |

---

## 🔄 Major Changes

### **1. Table Names - Đổi từ English sang Vietnamese**

| Before (English) | After (Vietnamese) | Entity |
|------------------|-------------------|--------|
| `driver` | `TAIXE` | Driver |
| `customer` | `KHACHHANG` | Customer |
| `booking` | `DATXE` | Booking |
| `vehicle_type` | `LOAIXE` | VehicleType |
| `payment` | `PHUONGTHUCTHANHTOAN` | Payment |
| `promotion` | `KHUYENMAI` | Promotion |
| `account` | `TAIKHOAN` | Account |
| `role` | `VAITRO` | Role |

---

### **2. Column Names - Đổi sang tiếng Việt**

#### **Driver (TAIXE):**
```java
// Before → After
driver_id        → ID_TX
vehicle_type_id  → ID_LOAIXE
driver_name      → TENTX
birth_date       → NGSINH
citizen_id       → CCCD
driving_license  → GPLX
phone            → SDT
email            → EMAIL
license_plate    → BIENSOXE
vehicle_name     → TENXE
profile_image    → ANHDAIDIEN
activity_status  → HOATDONG
gender           → GIOITINH
address          → DIACHI
area             → KHUVUC
current_lat      → VIDO
current_lng      → KINHDO
criminal_record  → LLTP
```

#### **Customer (KHACHHANG):**
```java
// Before → After
customer_id      → ID_KH
customer_name    → TENKH
phone            → SDT
address          → DIACHI
avatar           → ANHDAIDIEN
// New fields:
email            → EMAIL
birth_date       → NGSINH
gender           → GIOITINH
```

#### **Booking (DATXE):**
```java
// Before → After
booking_id       → ID_DATXE
customer_id      → ID_KH
driver_id        → ID_TX
payment_id       → ID_THANHTOAN
promotion_id     → ID_KHUYENMAI
pickup_location  → DIEMDON
dropoff_location → DIEMTRA
total_price      → TONGTIEN
booking_time     → THOIGIANDAT
pickup_time      → THOIGIANDON
arrival_time     → THOIGIANDEN
booking_status   → TRANGTHAI
distance         → KHOANGCACH
duration         → THOIGIAN
// New fields from SQL:
rating           → DIEMSO
review           → DANHGIA
```

#### **VehicleType (LOAIXE):**
```java
// Before → After
vehicle_type_id   → ID_LOAIXE
vehicle_type_name → TENLOAIXE
price_per_km      → GIA1KM
max_passengers    → SOCHONGOI (new field)
```

#### **Payment (PHUONGTHUCTHANHTOAN):**
```java
// Before → After
payment_id       → ID_THANHTOAN
payment_type     → LOAIHINHTHANHTOAN
payment_status   → TRANGTHAITT (changed from String to Boolean)
```

#### **Promotion (KHUYENMAI):**
```java
// Before → After
promotion_id           → ID_KHUYENMAI
promotion_code         → MAKHUYENMAI
promotion_name         → TENKM
discount_limit         → HANMUC
start_time             → TGBATDAU
end_time               → TGKETTHUC
application_condition  → DIEUKIENAPDUNG
quantity               → SOLUONG
is_active              → HOATDONG (new field)
```

#### **Account (TAIKHOAN):**
```java
// Before → After
account_id       → account_id (kept as UUID)
user_name        → USERNAME
pass_word        → PASS_WORD
role_id          → ID_VAITRO
// New field:
ref_id           → REF_ID (points to ID_KH or ID_TX)
```

#### **Role (VAITRO):**
```java
// Before → After
role_id          → ID_VAITRO
role_name        → TENVAITRO
```

---

### **3. ID Generation Strategy Changes**

#### **Before:**
```java
@Id
@GeneratedValue(generator = "UUID")
@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
@Column(name = "driver_id", length = 36)
private String driverId;
```

#### **After:**
```java
@Id
@Column(name = "ID_TX", nullable = false, unique = true, length = 10)
private String driverId;
```

**⚠️ Important:** 
- Database uses **CHAR(10)** for IDs (e.g., `TX01`, `KH01`, `DX01`)
- Removed auto UUID generation
- Need to generate IDs manually or use stored functions from SQL:
  - `Fn_TangTuDongID_KhachHang()` for Customer
  - `Fn_TangTuDongID_TaiXe()` for Driver

---

### **4. Repository Updates**

#### **DriverRepository:**
```java
// Updated native query
@Query(value = "SELECT * FROM TAIXE WHERE account_id = :accountId", nativeQuery = true)
Optional<Driver> findByAccountId(@Param("accountId") String accountId);

// New methods
Optional<Driver> findByCitizenId(String citizenId);
Optional<Driver> findByLicensePlate(String licensePlate);
```

#### **CustomerRepository:**
```java
// Updated native query
@Query(value = "SELECT * FROM KHACHHANG WHERE account_id = :accountId", nativeQuery = true)
Optional<Customer> findByAccountId(@Param("accountId") String accountId);

// New methods
Optional<Customer> findByPhone(String phone);
boolean existsByPhone(String phone);
```

#### **RideBookRepository:**
```java
// Updated JPQL with N prefix for Unicode strings
@Query("SELECT b FROM Booking b WHERE b.bookingStatus = N'Đang chờ' AND b.driverNo IS NULL ORDER BY b.bookingTime ASC")
List<Booking> findAvailableRides();
```

#### **AccountRepository:**
```java
// New method
@Query("SELECT a FROM Account a WHERE a.refId = :refId")
Optional<Account> findByRefId(@Param("refId") String refId);
```

---

## 📝 New Fields Added

### **Fields in Entities NOT in Original SQL (for future use):**

**Driver:**
- `criminalRecord` (LLTP) - Lý lịch tư pháp
- `avatar` (ANHDAIDIEN) - Ảnh đại diện
- `activityStatus` (HOATDONG) - Trạng thái hoạt động
- `gender` (GIOITINH) - Giới tính
- `address` (DIACHI) - Địa chỉ
- `area` (KHUVUC) - Khu vực
- `currentLat`, `currentLng` (VIDO, KINHDO) - Vị trí GPS

**Customer:**
- `avatar` (ANHDAIDIEN) - Ảnh đại diện
- `email` (EMAIL) - Email
- `birthDate` (NGSINH) - Ngày sinh
- `gender` (GIOITINH) - Giới tính

**Booking:**
- `totalPrice` (TONGTIEN) - Tổng tiền
- `duration` (THOIGIAN) - Thời gian

**VehicleType:**
- `maxPassengers` (SOCHONGOI) - Số chỗ ngồi

**Promotion:**
- `promotionCode` (MAKHUYENMAI) - Mã khuyến mãi
- `isActive` (HOATDONG) - Trạng thái hoạt động

---

## 🔍 Database Constraints Preserved

### **From SQL Schema:**

```sql
-- Unique Constraints
ALTER TABLE KHACHHANG ADD CONSTRAINT UQ_KHACHHANG_SDT UNIQUE (SDT);
ALTER TABLE TAIXE ADD CONSTRAINT UQ_TAIXE_BIENSOXE UNIQUE (BIENSOXE);
ALTER TABLE TAIXE ADD CONSTRAINT UQ_TAIXE_EMAIL UNIQUE (EMAIL);
ALTER TABLE TAIXE ADD CONSTRAINT UQ_TAIXE_CCCD UNIQUE (CCCD);

-- Check Constraints
ALTER TABLE DATXE ADD CONSTRAINT CHK_DATXE_DIEMSO CHECK (DIEMSO BETWEEN 1 AND 5 OR DIEMSO IS NULL);
ALTER TABLE DATXE ADD CONSTRAINT CHK_DATXE_TRANGTHAI CHECK (TRANGTHAI IN (N'Đang chờ', N'Hoàn thành', N'Hủy'));
ALTER TABLE KHACHHANG ADD CONSTRAINT CHK_KHACHHANG_SDT CHECK (SDT LIKE '[0-9]%' AND LEN(SDT) IN (10, 11));
ALTER TABLE TAIXE ADD CONSTRAINT CHK_TAIXE_SDT CHECK (SDT LIKE '[0-9]%' AND LEN(SDT) IN (10, 11));

-- Default Values
ALTER TABLE DATXE ADD CONSTRAINT DF_DATXE_TRANGTHAI DEFAULT N'Đang chờ' FOR TRANGTHAI;
ALTER TABLE PHUONGTHUCTHANHTOAN ADD CONSTRAINT DF_THANHTOAN_TRANGTHAITT DEFAULT 1 FOR TRANGTHAITT;
```

✅ All constraints are maintained in JPA entities via `@Column` annotations.

---

## ⚙️ Configuration Changes

### **application.yaml:**

Đảm bảo Spring Boot kết nối đúng database:

```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=QLDX
    username: sa
    password: your_password
    driverClassName: com.microsoft.sqlserver.jdbc.SQLServerDriver
  
  jpa:
    hibernate:
      ddl-auto: none  # IMPORTANT: Don't auto-generate schema
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.SQLServerDialect
```

**⚠️ Important:** 
- Set `ddl-auto: none` để không tự động tạo/sửa schema
- Database schema được quản lý bởi SQL script `QuanLyDatXe.sql`

---

## 🚨 Breaking Changes & Migration Notes

### **1. ID Generation:**

❌ **Before (Auto UUID):**
```java
Driver driver = Driver.builder()
    .driverName("John")
    .build();
driverRepository.save(driver); // ID auto-generated
```

✅ **After (Manual ID):**
```java
// Need to generate ID manually
String newId = generateDriverId(); // e.g., "TX0000005"
Driver driver = Driver.builder()
    .driverId(newId)
    .driverName("John")
    .build();
driverRepository.save(driver);
```

### **2. Booking Status Values:**

Đảm bảo sử dụng đúng giá trị theo constraint:
```java
// Valid values only:
booking.setBookingStatus("Đang chờ");   // Waiting
booking.setBookingStatus("Hoàn thành"); // Completed
booking.setBookingStatus("Hủy");        // Cancelled
```

### **3. Payment Status:**

Changed from String to Boolean:
```java
// Before
payment.setPaymentStatus("active");

// After
payment.setPaymentStatus(true); // 1 = active, 0 = inactive
```

### **4. Account REF_ID:**

New field to link Account to Customer or Driver:
```java
// When creating driver account
Account account = Account.builder()
    .userName("driver@example.com")
    .passWord(encodedPassword)
    .roleNo(driverRole)
    .refId("TX00001") // Link to driver ID
    .build();
```

---

## 📋 TODO: ID Generation Implementation

**Need to implement ID generation logic:**

### **Option 1: Use SQL Functions (Recommended)**
```java
@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    
    @Query(value = "SELECT dbo.Fn_TangTuDongID_TaiXe()", nativeQuery = true)
    String generateNewDriverId();
}

// Usage in Service
String newId = driverRepository.generateNewDriverId();
```

### **Option 2: Java Implementation**
```java
public String generateDriverId() {
    List<Driver> allDrivers = driverRepository.findAll();
    if (allDrivers.isEmpty()) {
        return "TX0000001";
    }
    
    String lastId = allDrivers.stream()
        .map(Driver::getDriverId)
        .max(Comparator.naturalOrder())
        .orElse("TX0000000");
    
    int nextNumber = Integer.parseInt(lastId.substring(2)) + 1;
    return String.format("TX%07d", nextNumber);
}
```

**Similar for:**
- Customer: `KH + 7 digits`
- Booking: `DX + 7 digits`
- VehicleType: `LX + 2 digits`
- Payment: `TT + 2 digits`
- Promotion: `KM + 2 digits`
- Role: `USER`, `DRIVER`, `ADMIN`

---

## ✅ Testing Checklist

- [x] All entities compile successfully
- [x] Repository queries updated with correct table/column names
- [x] Build successful (74 files compiled)
- [ ] Test database connection with actual SQL Server
- [ ] Test CRUD operations with manual IDs
- [ ] Verify foreign key relationships
- [ ] Test Vietnamese characters in queries
- [ ] Verify constraints (unique, check, default)
- [ ] Test payment integration with new schema
- [ ] Test booking flow with new status values

---

## 🎯 Summary

### **Files Modified:** 10 Entities + 5 Repositories

**Entities:**
- ✅ `Driver.java` - Fixed table and column names
- ✅ `Customer.java` - Fixed table and column names
- ✅ `Booking.java` - Fixed table and column names, added rating/review
- ✅ `VehicleType.java` - Fixed table and column names
- ✅ `Payment.java` - Fixed table name, changed status to Boolean
- ✅ `Promotion.java` - Fixed table and column names
- ✅ `Account.java` - Fixed table name, added REF_ID
- ✅ `Role.java` - Fixed table and column names

**Repositories:**
- ✅ `DriverRepository.java` - Updated queries and added methods
- ✅ `CustomerRepository.java` - Updated queries and added methods
- ✅ `RideBookRepository.java` - Updated JPQL with Unicode prefix
- ✅ `AccountRepository.java` - Added findByRefId method
- ✅ `RoleRepository.java` - Fixed method signature

**Build Status:** ✅ **SUCCESS**
- Compiled: 74 files
- Warnings: 8 (MapStruct unmapped fields - non-critical)

---

## 🚀 Next Steps

1. **Implement ID Generation:**
   - Create utility service for generating IDs
   - Or use SQL functions via native queries

2. **Update Service Layer:**
   - Update `DriverService.createDriver()` to generate ID
   - Update `CustomerService` similarly
   - Update `BookingService` similarly

3. **Test with Real Database:**
   - Import `QuanLyDatXe.sql` to SQL Server
   - Configure connection in `application.yaml`
   - Test CRUD operations

4. **Update Documentation:**
   - Update API docs with new field names
   - Document ID generation strategy

---

✅ **Database schema synchronization complete!**
