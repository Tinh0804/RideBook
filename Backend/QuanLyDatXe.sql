USE master;
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'QLDX')
BEGIN
    ALTER DATABASE QLDX SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QLDX;
END;
--lệnh tạo data base
CREATE DATABASE QLDX
GO
--lệnh sử dụng databse, mọi câu lệnh phía dưới sử dụng cho DB này
USE QLDX
GO
CREATE TABLE VAITRO(
    ID_VAITRO CHAR(6) PRIMARY KEY,
    TENVAITRO NVARCHAR(100),
)
CREATE TABLE TAIKHOAN (
    SDT VARCHAR(11),
    MATKHAU VARCHAR(100) NOT NULL,
    ID_VAITRONO CHAR(6) NOT NULL,
    ID_NGUOIDUNG CHAR(5),
    TRANGTHAITK BIT,
    PRIMARY KEY (SDT, ID_VAITRONO),
    CONSTRAINT FK_VAITRO FOREIGN KEY (ID_VAITRONO)
        REFERENCES VAITRO(ID_VAITRO),
    
);

CREATE TABLE KHACHHANG (
    ID_KH CHAR(5) PRIMARY KEY,
    TENKH NVARCHAR(100),
    SDT VARCHAR(15),
    DIACHI NVARCHAR(400),
);
CREATE TABLE LOAIXE (
    ID_LOAIXE CHAR(5) PRIMARY KEY,
    TENLOAIXE NVARCHAR(100),
    GIA1KM DECIMAL(10, 2)
);
CREATE TABLE TAIXE (
    ID_TX CHAR(5) PRIMARY KEY,
    ID_LOAIXENO CHAR(5),
    TENTX NVARCHAR(100),
    NGSINH DATE,
    CCCD VARCHAR(200),
    GPLX VARCHAR(200),
    SDT VARCHAR(15),
    EMAIL VARCHAR(100),
    BIENSOXE VARCHAR(20),
    TENXE NVARCHAR(100),
    -- DIACHI NVARCHAR(200),
    TRANGTHAIHD BIT,
    ANHDAIDIEN VARCHAR(200),
    GIOITINH NVARCHAR(20),
    DIACHI NVARCHAR(400),
    KHUVUC NVARCHAR(100),
    LLTP VARCHAR(200)
    CONSTRAINT FK_TAIXE_LOAIXE FOREIGN KEY (ID_LOAIXENO)
        REFERENCES LOAIXE(ID_LOAIXE)
        ON UPDATE CASCADE 
        ON DELETE SET NULL
);
CREATE TABLE PHUONGTHUCTHANHTOAN (
    ID_THANHTOAN CHAR(5) PRIMARY KEY,
    LOAIHINHTHANHTOAN NVARCHAR(100),
    TRANGTHAITT BIT
);
CREATE TABLE KHUYENMAI (
    ID_KHUYENMAI CHAR(5) PRIMARY KEY,
    TENKM NVARCHAR(100),
    HANMUC DECIMAL(10,2),
    TGBATDAU DATETIME,
    TGKETTHUC DATETIME,
    DIEUKIENAPDUNG NVARCHAR(200),
    SOLUONG INT,
    LOAIGIAMGIA VARCHAR(100),
    MUCGIA FLOAT,
    GIACHUYENTOITHIEU FLOAT,
    GIOHANMOTKHACH INT
);

CREATE TABLE KHACHHANG_KHUYENMAI (
    ID_KHKM    VARCHAR(36) NOT NULL,
    ID_KHNO   VARCHAR(36) NOT NULL,
    ID_KHUYENMAINO  VARCHAR(36) NOT NULL,
    TRANGTHAI NVARCHAR(20) NOT NULL,
    THOIGIANLUU DATETIME2 NOT NULL,
    THOIGIANDUNG DATETIME2 NULL,
    CONSTRAINT PK_KHACHHANG_KHUYENMAI PRIMARY KEY (ID_KHKM),
    CONSTRAINT FK_KHACHHANG_KHUYENMAI_CUSTOMER FOREIGN KEY (ID_KHNO) REFERENCES KHACHHANG(ID_KH),
    CONSTRAINT FK_KHACHHANG_KHUYENMAI_PROMOTION FOREIGN KEY (ID_KHUYENMAINO) REFERENCES KHUYENMAI(ID_KHUYENMAI)
);
CREATE TABLE GIO (
    ID_GIO CHAR(5) PRIMARY KEY,
    THOIGIANBATDAU TIME,
    THOIGIANKETTHUC TIME
);
CREATE TABLE LOAIXE_GIO (
    ID_LOAIXENO CHAR(5),
    ID_GIONO CHAR(5),
    PHUTHU FLOAT,
    PRIMARY KEY (ID_LOAIXENO, ID_GIONO),
    CONSTRAINT FK_LOAIXE_GIO_LOAIXE FOREIGN KEY (ID_LOAIXENO)
        REFERENCES LOAIXE(ID_LOAIXE)
        ON UPDATE CASCADE 
        ON DELETE CASCADE,
    CONSTRAINT FK_LOAIXE_GIO_GIO FOREIGN KEY (ID_GIONO)
        REFERENCES GIO(ID_GIO)
        ON UPDATE CASCADE 
        ON DELETE CASCADE
);
CREATE TABLE DATXE (
    ID_DATXE CHAR(5) PRIMARY KEY,
    ID_KHNO CHAR(5),
    ID_TXNO CHAR(5),
    ID_THANHTOANNO CHAR(5),
    ID_KHUYENMAINO CHAR(5),
    DIEMDON NVARCHAR(200),
    DIEMTRA NVARCHAR(200),
    GIATIEN DECIMAL,
    THOIGIANDAT DATETIME,
    THOIGIANDON DATETIME,
    THOIGIANDEN DATETIME,
    TRANGTHAI NVARCHAR(50),
    KHOANGCACH FLOAT,
    DIEMSO INT,
    DANHGIA NVARCHAR(500),
    CONSTRAINT FK_DATXE_KH FOREIGN KEY (ID_KHNO)
        REFERENCES KHACHHANG(ID_KH)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT FK_DATXE_TX FOREIGN KEY (ID_TXNO)
        REFERENCES TAIXE(ID_TX)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT FK_DATXE_THANHTOAN FOREIGN KEY (ID_THANHTOANNO)
        REFERENCES PHUONGTHUCTHANHTOAN(ID_THANHTOAN)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT FK_DATXE_KHUYENMAI FOREIGN KEY (ID_KHUYENMAINO)
        REFERENCES KHUYENMAI(ID_KHUYENMAI)
        ON UPDATE CASCADE ON DELETE SET NULL
);
-- KHUYENMAI: HANMUC ≥ 0
ALTER TABLE KHUYENMAI
ADD CONSTRAINT CHK_KHUYENMAI_HANMUC CHECK (HANMUC >= 0);

-- KHUYENMAI: SOLUONG ≥ 0
ALTER TABLE KHUYENMAI
ADD CONSTRAINT CHK_KHUYENMAI_SOLUONG CHECK (SOLUONG >= 0);

-- DATXE: DIEMSO từ 1 đến 5 (nếu không đánh giá thì NULL)
ALTER TABLE DATXE
ADD CONSTRAINT CHK_DATXE_DIEMSO CHECK (DIEMSO BETWEEN 1 AND 5 OR DIEMSO IS NULL);

-- DATXE: TRANGTHAI chỉ nhận các giá trị hợp lệ (ví dụ: 'Đang thực hiện', 'Hoàn thành', 'Hủy')
ALTER TABLE DATXE
ADD CONSTRAINT CHK_DATXE_TRANGTHAI CHECK (TRANGTHAI IN (N'Chờ tài xế nhận', N'Đang thực hiện', N'Hoàn thành', N'Đã huỷ'));

-- PHUONGTHUCTHANHTOAN: TRANGTHAITT là 0 hoặc 1
ALTER TABLE PHUONGTHUCTHANHTOAN
ADD CONSTRAINT CHK_THANHTOAN_TRANGTHAITT CHECK (TRANGTHAITT IN (0, 1));

-- KHACHHANG: SĐT là duy nhất
ALTER TABLE KHACHHANG
ADD CONSTRAINT UQ_KHACHHANG_SDT UNIQUE (SDT);

-- TAIXE: BIENSOXE là duy nhất
ALTER TABLE TAIXE
ADD CONSTRAINT UQ_TAIXE_BIENSOXE UNIQUE (BIENSOXE);

-- TAIXE: EMAIL là duy nhất
ALTER TABLE TAIXE
ADD CONSTRAINT UQ_TAIXE_EMAIL UNIQUE (EMAIL);

-- TAIXE: CCCD là duy nhất
ALTER TABLE TAIXE
ADD CONSTRAINT UQ_TAIXE_CCCD UNIQUE (CCCD);

-- TAIXE: Mặc định trạng thái hoạt động của DRIVER khi mới tạo là  0(không hoạt động)
ALTER TABLE TAIXE
ADD CONSTRAINT DF_TAIXE_TRANGTHAIHD DEFAULT 0 FOR TRANGTHAIHD

ALTER TABLE TAIXE
ADD CONSTRAINT DF_TAIXE_GIOITINH DEFAULT N'Nam' FOR GIOITINH

ALTER TABLE TAIXE
ADD CONSTRAINT DF_TAIXE_LLTP DEFAULT NULL FOR LLTP
-- PHUONGTHUCTHANHTOAN: mặc định TRANGTHAITT = 1 (Chưa thanh toán)
ALTER TABLE PHUONGTHUCTHANHTOAN
ADD CONSTRAINT DF_THANHTOAN_TRANGTHAITT DEFAULT 1 FOR TRANGTHAITT;

-- DATXE: mặc định TRANGTHAI = 'Chờ'
ALTER TABLE DATXE
ADD CONSTRAINT DF_DATXE_TRANGTHAI DEFAULT N'Đang thực hiện' FOR TRANGTHAI;

-- DATXE: mặc định DIEMSO = NULL (chưa đánh giá)
ALTER TABLE DATXE
ADD CONSTRAINT DF_DATXE_DIEMSO DEFAULT NULL FOR DIEMSO;

-- DATXE: DANHGIA mặc định là NULL
ALTER TABLE DATXE
ADD CONSTRAINT DF_DATXE_DANHGIA DEFAULT NULL FOR DANHGIA;

-- KHACHHANG: SDT phải là chuỗi số và có độ dài 10 hoặc 11
ALTER TABLE KHACHHANG
ADD CONSTRAINT CHK_KHACHHANG_SDT CHECK (
    SDT LIKE '[0-9]%' AND LEN(SDT) IN (10, 11)
);

-- TAIXE: SDT phải là chuỗi số và có độ dài 10 hoặc 11
ALTER TABLE TAIXE
ADD CONSTRAINT CHK_TAIXE_SDT CHECK (
    SDT LIKE '[0-9]%' AND LEN(SDT) IN (10, 11)
);
--TAIKHOAN: Mặc định trạng thái cho các tài khoản là 1(mở)
ALTER TABLE TAIKHOAN 
ADD CONSTRAINT DF_TAIKHOAN_TRANGTHAITK DEFAULT 1 FOR TRANGTHAITK;

INSERT INTO LOAIXE (ID_LOAIXE, TENLOAIXE, GIA1KM) 
VALUES
	('LX001', N'Xe máy', 5500),
	('LX002', N'Xe ô tô 4 chỗ', 10000),
	('LX003', N'Xe ô tô 7 chỗ', 12000),
	('LX004', N'Xe ga', 7000);

INSERT INTO KHACHHANG (ID_KH, TENKH, SDT, DIACHI) 
VALUES
	('KH001', N'Hùng Nghĩa', '0901234567', N'112 Trần Cao Vân,Hà Nội'),
	('KH002', N'Duy Phương', '0912345678', N'100 Hai Bà Trưng,Hồ Chí Minh'),
	('KH003', N'Vy Hậu', '0987654321', N'55 Lê Duẩn, Đà Nẵng'),
	('KH004', N'Lê Tỉnh', '0934567890', N'28 Nguyễn Văn Linh,Đà Nẵng'),
    ('KH005', N'Nguyễn Lương Bin', '0355160341', N'123 Lê Lợi, Quận Hoàn Kiếm, Hà Nội'),
    ('KH006', N'Nguyễn Vy Hậu', '0355160346', N'45 Nguyễn Huệ, Quận Hải Châu, Đà Nẵng'),
    ('KH007', N'Phạm Quốc Anh', '0946789013', N'78 Trần Phú, Quận 1, TP.HCM'),
    ('KH008', N'Lê Minh Tuấn', '0957890124', N'56 Giải Phóng, Quận Đống Đa, Hà Nội'),
    ('KH009', N'Hoàng Thị Mai', '0968901235', N'89 Phạm Văn Đồng, Quận Cầu Giấy, Hà Nội'),
    ('KH010', N'Vũ Văn Nam', '0979012346', N'12 Hà Huy Tập, Quận Hải Châu, Đà Nẵng'),
    ('KH011', N'Đỗ Thị Hồng', '0980123457', N'34 Nguyễn Văn Cừ, Quận 5, TP.HCM'),
    ('KH012', N'Bùi Văn Long', '0991234568', N'67 Lý Thường Kiệt, Quận Hoàn Kiếm, Hà Nội'),
    ('KH013', N'Ngô Thị Thủy', '0902345679', N'90 Lê Đại Hành, Quận Thanh Khê, Đà Nẵng'),
    ('KH014', N'Phan Văn Tâm', '0913456780', N'23 Võ Thị Sáu, Quận 3, TP.HCM'),
    ('KH015', N'Đặng Thị Ngọc', '0924567891', N'45 Kim Mã, Quận Ba Đình, Hà Nội'),
    ('KH016', N'Trương Văn Hòa', '0935678903', N'78 Ông Ích Khiêm, Quận Thanh Khê, Đà Nẵng'),
    ('KH017', N'Lý Thị Hà', '0946789014', N'101 Nguyễn Thị Minh Khai, Quận 1, TP.HCM'),
    ('KH018', N'Nguyễn Văn Quang', '0957890125', N'56 Nguyễn Trãi, Quận Thanh Xuân, Hà Nội'),
    ('KH019', N'Trần Thị Minh', '0968901236', N'89 Lê Duẩn, Quận Hải Châu, Đà Nẵng'),
    ('KH020', N'Phạm Văn Đức', '0979012347', N'12 Cách Mạng Tháng Tám, Quận 3, TP.HCM'),
    ('KH021', N'Lê Thị Bích', '0980123458', N'34 Tây Sơn, Quận Đống Đa, Hà Nội'),
    ('KH022', N'Hoàng Văn Thành', '0991234569', N'67 Núi Trúc, Quận Ba Đình, Hà Nội'),
    ('KH023', N'Vũ Thị Hương', '0902345680', N'90 Nguyễn Đình Chiểu, Quận 1, TP.HCM'),
    ('KH024', N'Đỗ Văn Khánh', '0913456791', N'23 Trần Cao Vân, Quận Thanh Khê, Đà Nẵng');

INSERT INTO TAIXE (ID_TX, ID_LOAIXENO, TENTX, NGSINH, CCCD, GPLX, SDT, EMAIL, BIENSOXE, TENXE, TRANGTHAIHD,ANHDAIDIEN,GIOITINH,DIACHI,KHUVUC,LLTP) 
VALUES
	('TX001', 'LX001', N'Nguyễn Bin', '1990-05-10', '123456789012', '92B2-12345', '0911000001', 'tx1@example.com', '29A-12345', N'Yamaha Sirius', 1,NULL,DEFAULT,N'112 Trần Cao Vân,Đà Nẵng',N'Đà Nẵng',DEFAULT),
	('TX002', 'LX002', N'Thành Long', '1988-03-20', '234567890123', '92B1-23456', '0911000002', 'tx2@example.com', '30B-23456', N'Toyota Vios', 1,NULL,N'Nữ',N'10 Hà Huy Tập,Đà Nẵng',N'Đà Nẵng',DEFAULT),
	('TX003', 'LX003', N'Thị Hậu', '1992-09-15', '345678901234', '43B2-34567', '0911000003', 'tx3@example.com', '31C-34567', N'Kia Rondo', DEFAULT,NULL,DEFAULT,N'30 Lý Tự Trọng,Hà Nội',N'Hà Nội',DEFAULT),
    ('TX004', 'LX001', N'Nguyễn Văn Dũng', '1991-07-12', '456789012345', '92B2-45678', '0911000004', 'tx4@example.com', '29A-45678', N'Honda Wave', DEFAULT, NULL, DEFAULT, N'45 Lê Lợi, Quận Hoàn Kiếm, Hà Nội', N'Hà Nội', DEFAULT),
    ('TX005', 'LX002', N'Trần Thị Hoa', '1989-04-25', '567890123456', '92B1-56789', '0911000005', 'tx5@example.com', '30B-56789', N'Toyota Innova', 1, NULL, N'Nữ', N'12 Nguyễn Huệ, Quận Hải Châu, Đà Nẵng', N'Đà Nẵng', DEFAULT),
    ('TX006', 'LX003', N'Phạm Văn Tài', '1993-11-30', '678901234567', '43B2-67890', '0911000006', 'tx6@example.com', '31C-67890', N'Hyundai Accent', 1, NULL, DEFAULT, N'78 Trần Phú, Quận 1, TP.HCM', N'TP.HCM', DEFAULT),
    ('TX007', 'LX001', N'Lê Thị Kim', '1990-02-14', '789012345678', '92B2-78901', '0911000007', 'tx7@example.com', '29A-78901', N'Yamaha Sirius', 1, NULL, N'Nữ', N'56 Giải Phóng, Quận Đống Đa, Hà Nội', N'Hà Nội', DEFAULT),
    ('TX008', 'LX002', N'Hoàng Văn Nam', '1992-08-05', '890123456789', '92B1-89012', '0911000008', 'tx8@example.com', '30B-89012', N'Kia Morning', DEFAULT, NULL, DEFAULT, N'89 Phạm Văn Đồng, Quận Thanh Khê, Đà Nẵng', N'Đà Nẵng', DEFAULT),
    ('TX009', 'LX003', N'Vũ Thị Hằng', '1987-12-20', '901234567890', '43B2-90123', '0911000009', 'tx9@example.com', '31C-90123', N'Mazda 3', DEFAULT, NULL, N'Nữ', N'34 Nguyễn Văn Cừ, Quận 5, TP.HCM', N'TP.HCM', DEFAULT),
    ('TX010', 'LX001', N'Đỗ Văn Long', '1994-03-10', '012345678901', '92B2-01234', '0911000010', 'tx10@example.com', '29A-01234', N'Honda Air Blade', 1, NULL, DEFAULT, N'67 Lý Thường Kiệt, Quận Hoàn Kiếm, Hà Nội', N'Hà Nội', DEFAULT),
    ('TX011', 'LX002', N'Bùi Thị Lan', '1991-09-15', '123456788012', '92B1-12345', '0911000011', 'tx11@example.com', '30B-12345', N'Toyota Vios', DEFAULT, NULL, N'Nữ', N'90 Lê Đại Hành, Quận Thanh Khê, Đà Nẵng', N'Đà Nẵng', DEFAULT),
    ('TX012', 'LX003', N'Ngô Văn Tâm', '1988-06-25', '234567898123', '43B2-23456', '0911000012', 'tx12@example.com', '31C-23456', N'Honda CR-V', DEFAULT, NULL, DEFAULT, N'23 Võ Thị Sáu, Quận 3, TP.HCM', N'TP.HCM', DEFAULT),
    ('TX013', 'LX001', N'Phan Thị Ngọc', '1993-01-30', '345678801234', '92B2-34567', '0911000013', 'tx13@example.com', '29A-34567', N'Yamaha Exciter', 1, NULL, N'Nữ', N'45 Kim Mã, Quận Ba Đình, Hà Nội', N'Hà Nội', DEFAULT),
    ('TX014', 'LX002', N'Trương Văn Hòa', '1990-10-05', '456788012345', '92B1-45678', '0911000014', 'tx14@example.com', '30B-45678', N'Hyundai Tucson', DEFAULT, NULL, DEFAULT, N'78 Ông Ích Khiêm, Quận Thanh Khê, Đà Nẵng', N'Đà Nẵng', DEFAULT),
    ('TX015', 'LX003', N'Lý Văn Quang', '1989-05-18', '567890183456', '43B2-56789', '0911000015', 'tx15@example.com', '31C-56789', N'Kia Seltos', DEFAULT, NULL, DEFAULT, N'101 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', N'TP.HCM', DEFAULT),
    ('TX016', 'LX001', N'Nguyễn Thị Minh', '1992-02-28', '678981234567', '92B2-67890', '0911000016', 'tx16@example.com', '29A-67890', N'Honda Vision', 1, NULL, N'Nữ', N'56 Nguyễn Trãi, Quận Thanh Xuân, Hà Nội', N'Hà Nội', DEFAULT),
    ('TX017', 'LX002', N'Trần Văn Đức', '1991-11-10', '789012385678', '92B1-78901', '0911000017', 'tx17@example.com', '30B-78901', N'Toyota Camry', DEFAULT, NULL, DEFAULT, N'89 Lê Duẩn, Quận Hải Châu, Đà Nẵng', N'Đà Nẵng', DEFAULT),
    ('TX018', 'LX003', N'Phạm Thị Bích', '1990-07-22', '890123856789', '43B2-89012', '0911000018', 'tx18@example.com', '31C-89012', N'Mazda DX-5', 1, NULL, N'Nữ', N'12 Cách Mạng Tháng Tám, Quận 3, TP.HCM', N'TP.HCM', DEFAULT),
    ('TX019', 'LX001', N'Lê Văn Thành', '1988-04-15', '901234587890', '92B2-90123', '0911000019', 'tx19@example.com', '29A-90123', N'Yamaha Jupiter', DEFAULT, NULL, DEFAULT, N'34 Tây Sơn, Quận Đống Đa, Hà Nội', N'Hà Nội', DEFAULT),
    ('TX020', 'LX002', N'Hoàng Thị Hương', '1993-12-01', '012385678901', '92B1-01234', '0911000020', 'tx20@example.com', '30B-01234', N'Kia Cerato', DEFAULT, NULL, N'Nữ', N'67 Núi Trúc, Quận Ba Đình, Hà Nội', N'Hà Nội', DEFAULT),
    ('TX021', 'LX003', N'Vũ Văn Khánh', '1991-08-20', '123426789012', '43B2-12345', '0911000021', 'tx21@example.com', '31C-12345', N'Honda City', 1, NULL, DEFAULT, N'90 Nguyễn Đình Chiểu, Quận 1, TP.HCM', N'TP.HCM', DEFAULT),
    ('TX022', 'LX001', N'Đỗ Thị Hà', '1989-03-05', '234567890183', '92B2-23456', '0911000022', 'tx22@example.com', '29A-23456', N'Honda Lead', DEFAULT, NULL, N'Nữ', N'23 Trần Cao Vân, Quận Thanh Khê, Đà Nẵng', N'Đà Nẵng', DEFAULT),
    ('TX023', 'LX002', N'Bùi Văn Tùng', '1992-06-12', '345678981234', '92B1-34567', '0911000023', 'tx23@example.com', '30B-34567', N'Toyota Corolla', 1, NULL, DEFAULT, N'45 Lê Lợi, Quận Hoàn Kiếm, Hà Nội', N'Hà Nội', DEFAULT);

INSERT INTO PHUONGTHUCTHANHTOAN (ID_THANHTOAN, LOAIHINHTHANHTOAN, TRANGTHAITT) 
VALUES
	('TT001', N'Tiền mặt', 1),
	('TT002', N'Chuyển khoản', 1),
	('TT003', N'Momo', 1),
	('TT004', N'ZaloPay', 1);

INSERT INTO KHUYENMAI (ID_KHUYENMAI, TENKM, HANMUC, TGBATDAU, TGKETTHUC, DIEUKIENAPDUNG, SOLUONG, LOAIGIAMGIA, MUCGIAM, GIACHUYENTOITHIEU, GIOIHANMOTKHACH)
VALUES 
('KM001', N'Giảm 20% tối đa 50k', 50000.00, '2025-01-01 00:00:00', '2025-12-31 23:59:59', N'Áp dụng mọi chuyến', 100, 'PERCENTAGE', 20.0, 100000.00, 2),
('KM002', N'Giảm 30k cố định', 30000.00, '2025-02-01 00:00:00', '2025-06-30 23:59:59', N'Chỉ áp dụng cho xe 4 chỗ', 50, 'FIXED_AMOUNT', 30000.0, 50000.00, 1),
('KM003', N'Giảm 50% tối đa 100k', 100000.00, '2025-03-01 00:00:00', '2025-05-31 23:59:59', N'Khách hàng mới', 30, 'PERCENTAGE', 50.0, 150000.00, 1),
('KM004', N'Giảm 15k cho chuyến đầu', 15000.00, '2025-04-01 00:00:00', '2025-07-31 23:59:59', N'Lần đầu sử dụng', 200, 'FIXED_AMOUNT', 15000.0, 0, 1),
('KM005', N'Giảm 10% không giới hạn', NULL, '2025-05-01 00:00:00', '2025-08-31 23:59:59', N'Áp dụng toàn bộ', 999, 'PERCENTAGE', 10.0, 0, NULL);

-- Chèn các mã khuyến mãi đã lưu cho khách hàng (THOIGIANLUU = thời điểm lưu)
INSERT INTO KHACHHANG_KHUYENMAI (ID_KHKM, ID_KHNO, ID_KHUYENMAINO, TRANGTHAI, THOIGIANLUU, THOIGIANDUNG)
VALUES
('KMKH-001-001', '6d3802dc-29c1-499c-a3b4-9e5571de13e5', 'KM001', 'SAVED', '2025-03-15 10:00:00', NULL),
('KMKH-001-002', '6d3802dc-29c1-499c-a3b4-9e5571de13e5', 'KM002', 'SAVED', '2025-03-20 14:30:00', NULL),
('KMKH-001-003', '6d3802dc-29c1-499c-a3b4-9e5571de13e5', 'KM003', 'USED',   '2025-03-25 09:15:00', '2025-04-01 18:20:00'),
('KMKH-002-001', '6d3802dc-29c1-499c-a3b4-9e5571de13e5', 'KM001', 'SAVED', '2025-03-18 11:45:00', NULL),
('KMKH-002-002', '6d3802dc-29c1-499c-a3b4-9e5571de13e5', 'KM004', 'EXPIRED','2025-01-10 08:00:00', NULL),
('KMKH-002-003', '6d3802dc-29c1-499c-a3b4-9e5571de13e5', 'KM005', 'SAVED', '2025-05-01 12:00:00', NULL);

INSERT INTO GIO (THOIGIANBATDAU, THOIGIANKETTHUC, ID_GIO, TEN)
VALUES
-- G1: Giờ thấp điểm (khuya - sáng sớm)
('22:00:00', '05:00:00', 'G1', N'Giờ thấp điểm (22h-5h)'),

-- G2: Giờ bình thường buổi sáng
('05:00:01', '08:00:00', 'G2', N'Giờ bình thường sáng'),

-- G3: Giờ cao điểm sáng
('08:00:01', '10:00:00', 'G3', N'Giờ cao điểm sáng'),

-- G4: Giờ cao điểm chiều tối
('16:30:00', '19:30:00', 'G4', N'Giờ cao điểm chiều tối'),

-- Thêm vài khung giờ khác để phong phú
('10:00:01', '12:00:00', 'G5', N'Giữa trưa'),
('12:00:01', '16:29:00', 'G6', N'Giờ thấp điểm chiều'),
('19:30:01', '21:59:00', 'G7', N'Giờ bình thường tối');


INSERT INTO LOAIXE_GIO (ID_LOAIXENO, ID_GIONO, PHUTHU) 
VALUES
	('LX001', 'G1', 1.0),
	('LX002', 'G4', 1.5),
	('LX003', 'G3', 1.0),
	('LX004', 'G2', 1.1),
	('LX002', 'G1', 1.8),
	('LX003', 'G1', 2.0),
	('LX004', 'G1', 1.7),
	('LX001', 'G3', 1.5),
	('LX002', 'G3', 1.7),
	('LX004', 'G3', 1.4),
	('LX001', 'G4', 1.6),
	('LX003', 'G4', 1.8),
	('LX004', 'G4', 1.5),
	('LX001', 'G2', 1.0),
	('LX002', 'G2', 1.3),
	('LX003', 'G2', 1.2),
	('LX001', 'G5', 1.0),
	('LX002', 'G5', 1.3),
	('LX003', 'G5', 1.1),
	('LX004', 'G5', 0.95),
	('LX001', 'G7', 1.0),
	('LX002', 'G7', 1.3),
	('LX003', 'G7', 1.1),
	('LX004', 'G7', 0.9),
	('LX001', 'G6', 0.85),
	('LX002', 'G6', 1.1),
	('LX003', 'G6', 0.9),
	('LX004', 'G6', 0.8);
	

INSERT INTO DATXE (ID_DATXE, ID_KHNO, ID_TXNO, ID_THANHTOANNO, ID_KHUYENMAINO, DIEMDON, DIEMTRA,GIATIEN, THOIGIANDAT, THOIGIANDON, THOIGIANDEN, TRANGTHAI, KHOANGCACH, DIEMSO, DANHGIA) 
VALUES
    ('DX001', 'KH001', 'TX001', 'TT001', 'KM001', N'123 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', N'45 Giải Phóng, Hai Bà Trưng, Hà Nội',80000, '2023-01-05 08:00', '2023-01-05 08:10', '2023-01-05 08:40', N'Hoàn thành', 10.5, 5, N'Rất tốt'),
    ('DX002', 'KH002', 'TX002', 'TT002', 'KM002', N'56 Lê Lợi, Quận 1, TP.HCM', N'78 Nguyễn Văn Cừ, Bình Thạnh, TP.HCM', 50000,'2023-02-07 09:00', '2023-02-07 09:15', '2023-02-07 09:50', N'Hoàn thành', 8.2, 4, N'Hài lòng'),
    ('DX003', 'KH003', 'TX003', 'TT003', NULL, N'12 Nguyễn Huệ, Hải Châu, Đà Nẵng', N'34 Phạm Văn Đồng, Sơn Trà, Đà Nẵng', 35000,'2023-03-10 10:00', '2023-03-10 10:20', '2023-03-10 11:00', N'Đang thực hiện', 12.6, NULL, NULL),
    ('DX004', 'KH004', 'TX002', 'TT004', 'KM003', N'89 Đinh Tiên Hoàng, Quận 1, TP.HCM', N'101 Phạm Hùng, Gò Vấp, TP.HCM', 60000,'2023-04-12 14:00', '2023-04-12 14:15', '2023-04-12 14:45', N'Đã huỷ', 6.8, NULL, NULL),
    ('DX005', 'KH005', 'TX004', 'TT001', 'KM001', N'45 Lý Thường Kiệt, Ba Đình, Hà Nội', N'67 Nguyễn Văn Huyên, Cầu Giấy, Hà Nội', 85000,'2023-05-15 07:00', '2023-05-15 07:10', '2023-05-15 07:40', N'Đã huỷ', 9.5, 5, N'Hài lòng'),
    ('DX006', 'KH006', NULL, 'TT002', NULL, N'23 Lê Đại Hành, Hải Châu, Đà Nẵng', N'56 Trần Phú, Hải Châu, Đà Nẵng', 120000,'2023-06-18 08:00', '2023-06-18 08:15', '2023-06-18 08:45', N'Chờ tài xế nhận', 7.8, NULL, NULL),
    ('DX007', 'KH007', 'TX006', 'TT003', 'KM002', N'78 Võ Thị Sáu, Quận 3, TP.HCM', N'12 Nguyễn Thị Minh Khai, Quận 1, TP.HCM', 15000,'2023-07-20 09:00', '2023-07-20 09:20', '2023-07-20 10:00', N'Hoàn thành', 11.2, 5, N'Rất tốt'),
    ('DX008', 'KH008', 'TX007', 'TT004', 'KM003', N'34 Kim Mã, Ba Đình, Hà Nội', N'56 Nguyễn Trãi, Thanh Xuân, Hà Nội', 43000,'2023-08-22 10:00', '2023-08-22 10:15', '2023-08-22 10:45', N'Đã huỷ', 5.6, NULL, NULL),
    ('DX009', 'KH009', 'TX008', 'TT001', NULL, N'67 Ông Ích Khiêm, Thanh Khê, Đà Nẵng', N'89 Bạch Đằng, Hải Châu, Đà Nẵng', 12000,'2023-09-25 11:00', '2023-09-25 11:10', '2023-09-25 11:30', N'Hoàn thành', 6.3, 4, N'Hài lòng'),
    ('DX010', 'KH010', 'TX009', 'TT002', 'KM001', N'101 Cách Mạng Tháng Tám, Quận 3, TP.HCM', N'123 Lê Văn Sỹ, Tân Bình, TP.HCM', 32000,'2023-10-28 12:00', '2023-10-28 12:15', '2023-10-28 12:50', N'Đang thực hiện', 8.9, NULL, NULL),
    ('DX011', 'KH011', 'TX010', 'TT003', 'KM002', N'45 Tây Sơn, Đống Đa, Hà Nội', N'78 Phạm Ngọc Thạch, Đống Đa, Hà Nội',12000,'2024-01-03 13:00', '2024-01-03 13:20', '2024-01-03 14:00', N'Hoàn thành', 7.4, 5, N'Rất tốt'),
    ('DX012', 'KH012', 'TX011', 'TT004', NULL, N'12 Trần Cao Vân, Hải Châu, Đà Nẵng', N'34 Nguyễn Văn Linh, Hải Châu, Đà Nẵng', 15000,'2024-02-06 14:00', '2024-02-06 14:10', '2024-02-06 14:30', N'Đã huỷ', 5.8, NULL, NULL),
    ('DX013', 'KH013', 'TX012', 'TT001', 'KM003', N'56 Nguyễn Đình Chiểu, Quận 1, TP.HCM', N'89 Điện Biên Phủ, Bình Thạnh, TP.HCM', 18000,'2024-03-09 15:00', '2024-03-09 15:15', '2024-03-09 15:45', N'Hoàn thành', 7.2, 4, N'Hài lòng'),
    ('DX014', 'KH014', NULL, 'TT002', 'KM001', N'67 Lê Duẩn, Hoàn Kiếm, Hà Nội', N'101 Hàng Bông, Hoàn Kiếm, Hà Nội', 35000,'2024-04-12 16:00', '2024-04-12 16:10', '2024-04-12 16:30', N'Chờ tài xế nhận', 6.1, NULL, NULL),
    ('DX015', 'KH015', 'TX014', 'TT003', NULL, N'23 Núi Trúc, Ba Đình, Hà Nội', N'45 Nguyễn Khánh Toàn, Cầu Giấy, Hà Nội', 13000,'2024-05-15 17:00', '2024-05-15 17:15', '2024-05-15 17:50', N'Hoàn thành', 8.7, 5, N'Rất tốt'),
    ('DX016', 'KH016', 'TX015', 'TT004', 'KM002', N'78 Nguyễn Thị Minh Khai, Quận 3, TP.HCM', N'12 Nam Kỳ Khởi Nghĩa, Quận 1, TP.HCM', 32000,'2024-06-18 18:00', '2024-06-18 18:10', '2024-06-18 18:30', N'Đã huỷ', 5.3, NULL, NULL),
    ('DX017', 'KH017', 'TX016', 'TT001', 'KM003', N'34 Nguyễn Trãi, Thanh Xuân, Hà Nội', N'56 Lê Văn Lương, Thanh Xuân, Hà Nội', 40000,'2024-07-21 19:00', '2024-07-21 19:15', '2024-07-21 19:45', N'Hoàn thành', 8.4, 4, N'Hài lòng'),
    ('DX018', 'KH018', 'TX017', 'TT002', NULL, N'89 Lê Duẩn, Hải Châu, Đà Nẵng', N'123 Võ Nguyên Giáp, Sơn Trà, Đà Nẵng', 52000,'2024-08-24 20:00', '2024-08-24 20:20', '2024-08-24 21:00', N'Đang thực hiện', 15.6, NULL, NULL),
    ('DX019', 'KH019', 'TX018', 'TT003', 'KM001', N'12 Đinh Tiên Hoàng, Quận 1, TP.HCM', N'34 Phạm Ngọc Thạch, Quận 3, TP.HCM', 18000,'2024-09-27 21:00', '2024-09-27 21:15', '2024-09-27 21:50', N'Hoàn thành', 9.8, 5, N'Rất tốt'),
    ('DX020', 'KH020', 'TX019', 'TT004', 'KM002', N'45 Giải Phóng, Hai Bà Trưng, Hà Nội', N'67 Nguyễn Văn Cừ, Long Biên, Hà Nội', 20000,'2024-10-30 22:00', '2024-10-30 22:10', '2024-10-30 22:40', N'Đã huỷ', 7.9, NULL, NULL),
    ('DX021', 'KH021', 'TX020', 'TT001', NULL, N'78 Núi Trúc, Ba Đình, Hà Nội', N'101 Trần Hưng Đạo, Hoàn Kiếm, Hà Nội', 25000,'2025-01-02 07:00', '2025-01-02 07:10', '2025-01-02 07:30', N'Hoàn thành', 5.4, 4, N'Hài lòng'),
    ('DX022', 'KH022', 'TX021', 'TT002', 'KM003', N'123 Nguyễn Đình Chiểu, Quận 3, TP.HCM', N'45 Trường Chinh, Tân Bình, TP.HCM', 30000,'2025-02-05 08:00', '2025-02-05 08:15', '2025-02-05 08:50', N'Đang thực hiện', 12.3, NULL, NULL),
    ('DX023', 'KH023', 'TX022', 'TT003', 'KM001', N'56 Trần Cao Vân, Hải Châu, Đà Nẵng', N'78 Nguyễn Văn Linh, Hải Châu, Đà Nẵng', 32000,'2025-03-08 09:00', '2025-03-08 09:20', '2025-03-08 10:00', N'Hoàn thành', 6.5, 5, N'Rất tốt'),
    ('DX024', 'KH024', 'TX023', 'TT004', NULL, N'12 Lê Lợi, Hoàn Kiếm, Hà Nội', N'34 Hàng Bài, Hoàn Kiếm, Hà Nội', 35000,'2025-04-11 10:00','2025-04-11 10:10', '2025-04-11 10:30', N'Đã huỷ', 4.7, NULL, NULL),
    ('DX025', 'KH002', NULL, 'TT001', 'KM002', N'89 Phạm Văn Đồng, Sơn Trà, Đà Nẵng', N'101 Bạch Đằng, Hải Châu, Đà Nẵng', 42000,'2025-05-14 11:00', '2025-05-14 11:10', '2025-05-14 11:30', N'Chờ tài xế nhận', 6.3, NULL, NULL),
    ('DX026', 'KH003', 'TX002', 'TT002', 'KM001', N'67 Võ Thị Sáu, Quận 3, TP.HCM', N'123 Lê Văn Sỹ, Tân Bình, TP.HCM', 17000,'2025-06-17 12:00', '2025-06-17 12:15', '2025-06-17 12:50', N'Đang thực hiện', 7.2, NULL, NULL),
    ('DX027', 'KH004', 'TX003', 'TT003', NULL, N'45 Giải Phóng, Hai Bà Trưng, Hà Nội', N'78 Nguyễn Văn Cừ, Long Biên, Hà Nội', 22000,'2025-07-20 13:00', '2025-07-20 13:20', '2025-07-20 14:00', N'Hoàn thành', 8.5, 4, N'Hài lòng'),
    ('DX028', 'KH005', 'TX004', 'TT004', 'KM003', N'12 Ông Ích Khiêm, Thanh Khê, Đà Nẵng', N'34 Trần Phú, Hải Châu, Đà Nẵng', 19000,'2025-08-23 14:00', '2025-08-23 14:10', '2025-08-23 14:30', N'Hoàn thành', 5.8, NULL, NULL),
    ('DX029', 'KH007', NULL, 'TT001', 'KM002', N'56 Nguyễn Văn Cừ, Quận 5, TP.HCM', N'89 Phạm Hùng, Quận 7, TP.HCM', 67000,'2025-09-26 15:00', '2025-09-26 15:15', '2025-09-26 15:45', N'Chờ tài xế nhận', 8.9, NULL, NULL),
    ('DX030', 'KH007', 'TX006', 'TT002', NULL, N'78 Lý Thường Kiệt, Hoàn Kiếm, Hà Nội', N'101 Phạm Ngọc Thạch, Đống Đa, Hà Nội', 32000,'2025-10-29 16:00', '2025-10-29 16:20', '2025-10-29 17:00', N'Đang thực hiện', 7.4, NULL, NULL);
    -- (dbo.Fn_TangTuDongID_DatXe(), 'KH004', 'TX003', 'TT001', 'KM003', N'Bến xe miền Đông', N'Gò Vấp', '2025-04-19 14:00', '2025-04-19 14:15', '2025-04-19 14:45', N'Hoàn thành', 6.8, NULL, NULL)
INSERT INTO VAITRO(ID_VAITRO,TENVAITRO) 
VALUES
	('USER',N'Khách Hàng'),
	('DRIVER',N'Tài Xế'),
	('ADMIN',N'Quản trị viên')
SELECT * FROM DATXE WHERE TRANGTHAI = N'Chờ tài xế nhận' AND ID_TXNO IS NULL
INSERT INTO TAIKHOAN (SDT, MATKHAU, ID_VAITRONO, ID_NGUOIDUNG, TRANGTHAITK)
VALUES
    -- Accounts for KH001 to KH004 (Customers, added)
    ('0987687686', '12345', 'USER', 'KH001', 1),
    ('0912345678', '12345', 'USER', 'KH002', 1),
    ('0987654321', '12345', 'USER', 'KH003', 1),
    ('0934567890', '12345', 'USER', 'KH004', 1),
    -- Accounts for KH005 to KH024 (Customers, verified)
    ('0355160341', '12345', 'USER', 'KH005', 1),
    ('0935678902', '12345', 'USER', 'KH006', 1),
    ('0946789013', '12345', 'USER', 'KH007', 0),
    ('0957890124', '12345', 'USER', 'KH008', 1),
    ('0968901235', '12345', 'USER', 'KH009', 1),
    ('0979012346', '12345', 'USER', 'KH010', 0),
    ('0980123457', '12345', 'USER', 'KH011', 1),
    ('0991234568', '12345', 'USER', 'KH012', 1),
    ('0902345679', '12345', 'USER', 'KH013', 0),
    ('0913456780', '12345', 'USER', 'KH014', 1),
    ('0924567891', '12345', 'USER', 'KH015', 1),
    ('0935678903', '12345', 'USER', 'KH016', 0),
    ('0946789014', '12345', 'USER', 'KH017', 1),
    ('0957890125', '12345', 'USER', 'KH018', 1),
    ('0968901236', '12345', 'USER', 'KH019', 0),
    ('0979012347', '12345', 'USER', 'KH020', 1),
    ('0980123458', '12345', 'USER', 'KH021', 1),
    ('0991234569', '12345', 'USER', 'KH022', 0),
    ('0902345680', '12345', 'USER', 'KH023', 1),
    ('0913456791', '12345', 'USER', 'KH024', 1),
    -- Accounts for TX001 to TX003 (Drivers, added)
    ('0911000001', 'tx001', 'DRIVER', 'TX001', 1),
    ('0911000002', 'tx002', 'DRIVER', 'TX002', 1),
    ('0911000003', 'tx003', 'DRIVER', 'TX003', 1),
    -- Accounts for TX004 to TX023 (Drivers, verified)
    ('0911000004', 'tx004', 'DRIVER', 'TX004', 1),
    ('0911000005', 'tx005', 'DRIVER', 'TX005', 0),
    ('0911000006', 'tx006', 'DRIVER', 'TX006', 1),
    ('0911000007', 'tx007', 'DRIVER', 'TX007', 1),
    ('0911000008', 'tx008', 'DRIVER', 'TX008', 0),
    ('0911000009', 'tx009', 'DRIVER', 'TX009', 1),
    ('0911000010', 'tx010', 'DRIVER', 'TX010', 1),
    ('0911000011', 'tx011', 'DRIVER', 'TX011', 0),
    ('0911000012', 'tx012', 'DRIVER', 'TX012', 1),
    ('0911000013', 'tx013', 'DRIVER', 'TX013', 1),
    ('0911000014', 'tx014', 'DRIVER', 'TX014', 0),
    ('0911000015', 'tx015', 'DRIVER', 'TX015', 1),
    ('0911000016', 'tx016', 'DRIVER', 'TX016', 1),
    ('0911000017', 'tx017', 'DRIVER', 'TX017', 0),
    ('0911000018', 'tx018', 'DRIVER', 'TX018', 1),
    ('0911000019', 'tx019', 'DRIVER', 'TX019', 1),
    ('0911000020', 'tx020', 'DRIVER', 'TX020', 0),
    ('0911000021', 'tx021', 'DRIVER', 'TX021', 1),
    ('0911000022', 'tx022', 'DRIVER', 'TX022', 1),
    ('0911000023', 'tx023', 'DRIVER', 'TX023', 0),
    -- Admin account (unchanged)
    ('0355160346', '12345', 'ADMIN', NULL, 1);


CREATE PROCEDURE Pr_Find10DriverCloserCustomer
    @lat DECIMAL(9,6),
    @lng DECIMAL(9,6)
AS
BEGIN
    SELECT TOP 10 
        ID_TX, 
        TENTX, 
        SDT,
        (6371 * ACOS(
            COS(RADIANS(@lat)) 
            * COS(RADIANS(VIDO)) 
            * COS(RADIANS(KINHDO) - RADIANS(@lng)) 
            + SIN(RADIANS(@lat)) 
            * SIN(RADIANS(VIDO))
        )) AS distance
    FROM TAIXE
    WHERE TRANGTHAIHD = 1
      AND (6371 * ACOS(
            COS(RADIANS(@lat)) 
            * COS(RADIANS(VIDO)) 
            * COS(RADIANS(KINHDO) - RADIANS(@lng)) 
            + SIN(RADIANS(@lat)) 
            * SIN(RADIANS(VIDO))
        )) <= 5
    ORDER BY distance ASC;
END;
-- Index cho việc kiểm tra trạng thái chuyến xe
CREATE INDEX idx_Booking_Status_Driver ON DATXE (TRANGTHAI, ID_TXNO);

-- Index cho tọa độ tài xế để tính toán Haversine nhanh hơn
CREATE INDEX idx_Driver_Location ON TAIXE (VIDO, KINHDO) WHERE TRANGTHAIHD = 1;


CREATE PROCEDURE Pr_FindAvailableDriversCloserCustomer
    @lat FLOAT,
    @lng FLOAT,
    @radius FLOAT,
    @vehical_type VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT d.*, 
        (6371 * acos(cos(radians(@lat)) * cos(radians(d.VIDO)) 
        * cos(radians(d.KINHDO) - radians(@lng)) + sin(radians(@lat)) 
        * sin(radians(d.VIDO)))) AS Distance
    FROM TAIXE d,LOAIXE lx
    JOIN LOAIXE lx 
        ON d.ID_LXNO = lx.ID_LOAIXE
    WHERE d.TRANGTHAIHD = 1   -- Đang bật app 
    AND NOT EXISTS (
        SELECT 1 FROM DATXE b 
        WHERE b.ID_TXNO = d.ID_TX 
        AND b.TRANGTHAI IN (N'Đang chờ', N'Đang thực hiện') -- Dùng N để hỗ trợ tiếng Việt
    )
    AND (6371 * acos(cos(radians(@lat)) * cos(radians(d.VIDO)) 
        * cos(radians(d.KINHDO) - radians(@lng)) + sin(radians(@lat)) 
        * sin(radians(d.VIDO)))) <= @radius
    ORDER BY Distance ASC;
END;

EXEC  Pr_FindAvailableDriversCloserCustomer 16.06657,108.19113,3
--------------------------------------------------------------------------------------HÀM_THỦ TỤC_TRIGGER----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
go
CREATE TRIGGER Tr_KhachHang_Delete
ON KHACHHANG
AFTER DELETE
AS
BEGIN
    DELETE FROM TAIKHOAN
    WHERE ID_NGUOIDUNG IN (SELECT ID_KH FROM DELETED)
    AND ID_VAITRONO = 'USER';
END;

go
CREATE TRIGGER Tr_TaiXe_Delete
ON TAIXE
AFTER DELETE
AS
BEGIN
    DELETE FROM TAIKHOAN
    WHERE ID_NGUOIDUNG IN (SELECT ID_TX FROM DELETED)
    AND ID_VAITRONO = 'DRIVER';
END;

-- 1. Hàm tạo mã tự động cho khách hàng
GO
CREATE FUNCTION Fn_TangTuDongID_KhachHang()
RETURNS CHAR(5)
AS 
BEGIN
    DECLARE @Tam CHAR(3)
    SELECT @Tam = FORMAT(COALESCE(CAST(RIGHT(MAX(ID_KH), 3) AS INT), 0) + 1, 'D3')
    FROM KHACHHANG
    WHERE ID_KH LIKE 'KH[0-9][0-9][0-9]' -- Đảm bảo định dạng đúng
    RETURN 'KH' + @Tam
END;
GO
PRINT dbo.Fn_TangTuDongID_KhachHang() 

-- 2. Hàm tạo mã tự động cho tài xế
GO
CREATE FUNCTION Fn_TangTuDongID_TaiXe()
RETURNS CHAR(5)
AS 
BEGIN
    DECLARE @Tam CHAR(3)
    SELECT @Tam = FORMAT(COALESCE(CAST(RIGHT(MAX(ID_TX), 3) AS INT), 0) + 1, 'D3')
    FROM TAIXE
    WHERE ID_TX LIKE 'TX[0-9][0-9][0-9]' -- Đảm bảo định dạng đúng
    RETURN 'TX' + @Tam
END;
GO
PRINT dbo.Fn_TangTuDongID_TaiXe();

-- 3. Hàm tạo mã tự động cho chuyến xe
GO
CREATE FUNCTION Fn_TangTuDongID_DatXe()
RETURNS CHAR(5)
AS 
BEGIN
    DECLARE @Tam CHAR(3)
    SELECT @Tam = FORMAT(COALESCE(CAST(RIGHT(MAX(ID_DATXE), 3) AS INT), 0) + 1, 'D3')
    FROM DATXE
    WHERE ID_DATXE LIKE 'DX[0-9][0-9][0-9]' -- Đảm bảo định dạng đúng
    RETURN 'DX' + @Tam
END;
GO
PRINT dbo.Fn_TangTuDongID_DatXe();

-- 4. Hàm kiểm tra đăng nhập cho user -> trả về message thông báo
GO
CREATE FUNCTION Fn_KiemTraDangNhap(
    @SDT VARCHAR(11),
    @MATKHAU VARCHAR(100),
    @ID_VAITRO CHAR(6)
)
RETURNS TABLE
AS
RETURN
(
    SELECT 
        CASE 
            WHEN NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE SDT = @SDT) THEN 'PhoneNotFound'
            WHEN NOT EXISTS (SELECT 1 FROM TAIKHOAN 
                             WHERE SDT = @SDT AND MATKHAU = @MATKHAU AND ID_VAITRONO = @ID_VAITRO) THEN 'InvalidCredentials'
            WHEN NOT EXISTS (SELECT 1 FROM TAIKHOAN 
                             WHERE SDT = @SDT AND MATKHAU = @MATKHAU AND ID_VAITRONO = @ID_VAITRO AND TRANGTHAITK = 1) THEN 'BlockedAccount'
            ELSE 'Success'
        END AS Status,
        CASE 
            WHEN NOT EXISTS (SELECT 1 FROM TAIKHOAN WHERE SDT = @SDT) THEN N'Số điện thoại không tồn tại'
            WHEN NOT EXISTS (SELECT 1 FROM TAIKHOAN 
                             WHERE SDT = @SDT AND MATKHAU = @MATKHAU AND ID_VAITRONO = @ID_VAITRO) THEN N'Mật khẩu hoặc vai trò không đúng'
            WHEN NOT EXISTS (SELECT 1 FROM TAIKHOAN 
                             WHERE SDT = @SDT AND MATKHAU = @MATKHAU AND ID_VAITRONO = @ID_VAITRO AND TRANGTHAITK = 1) THEN N'Tài khoản của bạn đã bị khoá'
            ELSE N'Đăng nhập thành công'
        END AS Message,
        (SELECT ID_NGUOIDUNG 
         FROM TAIKHOAN 
         WHERE SDT = @SDT AND MATKHAU = @MATKHAU AND ID_VAITRONO = @ID_VAITRO) AS UserId,
        (SELECT ID_VAITRONO 
         FROM TAIKHOAN 
         WHERE SDT = @SDT AND MATKHAU = @MATKHAU AND ID_VAITRONO = @ID_VAITRO) AS RoleId,
        (SELECT TRANGTHAITK
         FROM TAIKHOAN 
         WHERE SDT = @SDT AND MATKHAU = @MATKHAU AND ID_VAITRONO = @ID_VAITRO) AS AccountStatus
);
GO
SELECT * FROM dbo.Fn_KiemTraDangNhap('0902345679', '12345', 'USER');

-- 5. Thủ tục thêm khách hàng 
GO
CREATE PROCEDURE Pr_ThemKhachHang
    @TENKH NVARCHAR(100),
    @SDT VARCHAR(15),
    @DIACHI NVARCHAR(200),
    @MATKHAU VARCHAR(100)
AS 
BEGIN 
    BEGIN TRY 
        IF EXISTS (SELECT 1 FROM KHACHHANG WHERE SDT = @SDT)
        BEGIN
            SELECT 
                NULL AS ID_KH,
                400 AS Status,
                N'Số điện thoại đã tồn tại.' AS Message;
            RETURN;
        END

        BEGIN TRANSACTION;
        DECLARE @ID_KH CHAR(5);
        SET @ID_KH = dbo.Fn_TangTuDongID_KhachHang();
        INSERT INTO KHACHHANG 
        VALUES (@ID_KH, @TENKH, @SDT, @DIACHI);
        INSERT INTO TAIKHOAN 
        VALUES (@SDT, @MATKHAU, 'USER', @ID_KH, 1);
        -- Trả về kết quả
        SELECT @ID_KH AS ID_KH, 200 AS Status, N'Thêm khách hàng thành công.' AS Message;
        COMMIT TRANSACTION;

    END TRY
    BEGIN CATCH
        -- Rollback nếu có lỗi
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        -- Trả về lỗi dưới dạng kết quả
        SELECT NULL AS ID_KH, 401 AS Status, ERROR_MESSAGE() AS Message;
    END CATCH
END;
GO
EXEC dbo.Pr_ThemKhachHang @TENKH = N'Nguyễn Văn A',@SDT = '0901234865',@DIACHI = N'123 Đường Láng, Hà Nội',@MATKHAU = 'password123';

-- 6. Thủ tục thêm tài xế
GO 
CREATE PROCEDURE Pr_ThemTaiXe
    @ID_LOAIXENO CHAR(5),
    @TENTX NVARCHAR(100),
    @NGSINH DATE,
    @CCCD VARCHAR(200),
    @GPLX VARCHAR(200),
    @SDT VARCHAR(15),
    @EMAIL VARCHAR(100),
    @MATKHAU VARCHAR(100),
    @BIENSOXE VARCHAR(20),
    @TENXE NVARCHAR(100),
    @ANHDAIDIEN NVARCHAR(200),
    @GIOITINH NVARCHAR(20),
    @DIACHI NVARCHAR(100),
    @KHUVUC NVARCHAR(50),
    @LLTP VARCHAR(200)
AS 
BEGIN
    BEGIN TRY 
        IF EXISTS (SELECT 1 FROM TAIXE WHERE SDT = @SDT)
            BEGIN
                SELECT NULL AS ID_TX, 400 AS Status, N'Số điện thoại đã tồn tại.' AS Message
                RETURN
            END
        IF EXISTS (SELECT 1 FROM TAIXE WHERE BIENSOXE = @BIENSOXE)
            BEGIN
                SELECT NULL AS ID_TX, 400 AS Status, N'Biển số xe đã tồn tại.' AS Message;
                RETURN;
            END

        IF EXISTS (SELECT 1 FROM TAIXE WHERE CCCD = @CCCD)
            BEGIN
                SELECT NULL AS ID_TX, 400 AS Status, N'Căn cước công dân đã tồn tại.' AS Message;
                RETURN;
            END

        IF EXISTS (SELECT 1 FROM TAIXE WHERE GPLX = @GPLX)
            BEGIN
                SELECT NULL AS ID_TX, 400 AS Status, N'Giấy phép lái xe đã tồn tại.' AS Message;
                RETURN;
            END

        IF EXISTS (SELECT 1 FROM TAIXE WHERE EMAIL = @EMAIL)
            BEGIN
                SELECT NULL AS ID_TX, 400 AS Status, N'Email đã tồn tại.' AS Message;
                RETURN;
            END
        BEGIN TRANSACTION;

            DECLARE @ID_TX CHAR(5)
            SET @ID_TX = dbo.Fn_TangTuDongID_TaiXe()
            INSERT INTO TAIXE
            VALUES (@ID_TX, @ID_LOAIXENO, @TENTX, @NGSINH, @CCCD, @GPLX, @SDT, @EMAIL, @BIENSOXE, @TENXE, DEFAULT,@ANHDAIDIEN,@GIOITINH,@DIACHI,@KHUVUC,@LLTP);
            INSERT INTO TAIKHOAN 
            VALUES (@SDT, @MATKHAU, 'DRIVER', @ID_TX,1);
            --Trả về kết quả
            SELECT @ID_TX AS ID_TX, 'Success' AS Status, N'Thêm tài xế thành công' AS Message
        COMMIT TRANSACTION;
        END TRY 
    BEGIN CATCH
        -- Rollback nếu có lỗi
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        -- Trả về lỗi dưới dạng kết quả
        SELECT NULL AS ID_TX, 'Error' AS Status, N'Lỗi.Vui lòng thử lại sau' AS Message;
    END CATCH
END;
GO
EXEC Pr_ThemTaiXe @ID_LOAIXENO = 'LX001',@TENTX = N'Trần Văn B',@NGSINH = '1990-01-01',@CCCD = '048385005446',@GPLX = 'GPLX43456',@SDT = '049275012', @EMAIL = 'travap@example.com', @MATKHAU = 'password456', @BIENSOXE = '29A-12835', @TENXE = N'Toyota Vios',@ANHDAIDIEN=null,@GIOITINH=N'Nam',@DIACHI=N'48 Cao Thắng,Đà Nẵng',@KHUVUC=N'Đà Nẵng',@LLTP=null;

--7.Cập nhật tài xế
go
CREATE PROCEDURE Pr_CapNhatTaiXe
    @ID_TX CHAR(5),
    @ID_LOAIXENO CHAR(5),
    @TENTX NVARCHAR(100),
    @NGSINH DATE,
    @CCCD VARCHAR(200),
    @GPLX VARCHAR(200),
    @SDT VARCHAR(15),
    @EMAIL VARCHAR(100),
    @BIENSOXE VARCHAR(20),
    @TENXE NVARCHAR(100),
    @TRANGTHAIHD BIT,
    @ANHDAIDIEN NVARCHAR(200),
    @GIOITINH NVARCHAR(20),
    @DIACHI NVARCHAR(100),
    @KHUVUC NVARCHAR(100),
    @LLTP VARCHAR(200)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Status VARCHAR(10);
    DECLARE @Message NVARCHAR(255);

    -- Kiểm tra tài xế tồn tại
    IF NOT EXISTS (SELECT 1 FROM TAIXE WHERE ID_TX = @ID_TX)
    BEGIN
        SET @Status = 'ERROR';
        SET @Message = N'Tài xế với ID ' + @ID_TX + N' không tồn tại.';
        SELECT @Status AS Status, @Message AS Message;
        RETURN;
    END

    -- Kiểm tra dữ liệu đầu vào
    IF @TENTX IS NULL OR LTRIM(RTRIM(@TENTX)) = ''
    BEGIN
        SET @Status = 'ERROR';
        SET @Message = N'Tên tài xế không được để trống.';
        SELECT @Status AS Status, @Message AS Message;
        RETURN;
    END

    IF @SDT IS NULL OR LTRIM(RTRIM(@SDT)) = '' OR @SDT NOT LIKE '[0-9]%'
    BEGIN
        SET @Status = 'ERROR';
        SET @Message = N'Số điện thoại không hợp lệ.';
        SELECT @Status AS Status, @Message AS Message;
        RETURN;
    END

    -- Kiểm tra ID_LOAIXENO (khóa ngoại)
    IF @ID_LOAIXENO IS NOT NULL AND NOT EXISTS (SELECT 1 FROM LOAIXE WHERE ID_LOAIXE = @ID_LOAIXENO)
    BEGIN
        SET @Status = 'ERROR';
        SET @Message = N'Loại xe với ID ' + @ID_LOAIXENO + N' không tồn tại.';
        SELECT @Status AS Status, @Message AS Message;
        RETURN;
    END

    BEGIN TRY
        -- Cập nhật tài xế
        UPDATE TAIXE
        SET
            ID_LOAIXENO = @ID_LOAIXENO,
            TENTX = @TENTX,
            NGSINH = @NGSINH,
            CCCD = ISNULL(@CCCD, CCCD),           -- Giữ giá trị cũ nếu NULL
            GPLX = ISNULL(@GPLX, GPLX),           -- Giữ giá trị cũ nếu NULL
            SDT = @SDT,
            EMAIL = @EMAIL,
            BIENSOXE = @BIENSOXE,
            TENXE = @TENXE,
            TRANGTHAIHD = @TRANGTHAIHD,
            ANHDAIDIEN = ISNULL(@ANHDAIDIEN, ANHDAIDIEN) ,-- Giữ giá trị cũ nếu NULL
            GIOITINH = @GIOITINH,
            DIACHI = @DIACHI,
            @KHUVUC = @KHUVUC,
            LLTP =ISNULL(@LLTP, LLTP)
        WHERE ID_TX = @ID_TX;

        SET @Status = 'OK';
        SET @Message = N'Cập nhật tài xế thành công.';
    END TRY
    BEGIN CATCH
        SET @Status = 'ERROR';
        SET @Message = N'Lỗi khi cập nhật tài xế: ' + ERROR_MESSAGE();
    END CATCH

    -- Trả về kết quả
    SELECT @Status AS Status, @Message AS Message;
END
go
-- 8.Cập nhật khách hàng
CREATE PROCEDURE Pr_CapNhatKhachHang
    @ID_KH CHAR(5),
    @TENKH NVARCHAR(100),
    @SDT VARCHAR(15),
    @DIACHI NVARCHAR(400),
    @MATKHAU VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @Status INT, @Message NVARCHAR(200);

    BEGIN TRY
        -- Kiểm tra SDT có bị trùng với khách hàng khác không
        IF EXISTS (
            SELECT 1 
            FROM KHACHHANG 
            WHERE SDT = @SDT 
            AND ID_KH != @ID_KH
        )
        BEGIN
            SET @Status = 400;
            SET @Message = N'Số điện thoại đã tồn tại cho khách hàng khác';
        END
        ELSE
        BEGIN
            -- Bắt đầu transaction
            BEGIN TRANSACTION;

            -- Cập nhật thông tin khách hàng
            UPDATE KHACHHANG
            SET 
                TENKH = @TENKH,
                SDT = @SDT,
                DIACHI = @DIACHI
            WHERE ID_KH = @ID_KH;

            -- Cập nhật mật khẩu nếu có
            IF @MATKHAU IS NOT NULL
            BEGIN
                UPDATE TAIKHOAN
                SET MATKHAU = @MATKHAU
                WHERE ID_NGUOIDUNG = @ID_KH AND ID_VAITRONO='USER' 
            END

            -- Commit transaction
            COMMIT TRANSACTION;

            SET @Status = 200;
            SET @Message = N'Cập nhật khách hàng thành công';
        END
    END TRY
    BEGIN CATCH
        -- Rollback nếu có lỗi
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        SET @Status = 400;
        SET @Message = N'Lỗi: ' + ERROR_MESSAGE();
    END CATCH

    -- Trả về kết quả
    SELECT @Status AS Status, @Message AS Message;
END;
EXECUTE Pr_CapNhatKhachHang @ID_KH='KH002',@TENKH='Tinh',@SDT='0123456789',@DIACHI='123 Tran cao Van,da nang',@MATKHAU='54321'
-- 7. Kiểm ta tính hợp lệ của khuyến mãi
GO
CREATE FUNCTION Fn_KiemTraKhuyenMai
(
    @ID_KHUYENMAI CHAR(5),
    @ThoiGianDat DATETIME
)
RETURNS NVARCHAR(100)
AS
BEGIN
    DECLARE @Result NVARCHAR(100);
    DECLARE @SoLuong INT;
    DECLARE @TGBatDau DATETIME;
    DECLARE @TGKetThuc DATETIME;

    SELECT @SoLuong = SOLUONG, @TGBatDau = TGBATDAU, @TGKetThuc = TGKETTHUC
    FROM KHUYENMAI
    WHERE ID_KHUYENMAI = @ID_KHUYENMAI;

    IF @SoLuong <= 0
        SET @Result = N'Khuyến mãi đã hết số lượng.';
    ELSE IF @ThoiGianDat < @TGBatDau OR @ThoiGianDat > @TGKetThuc
        SET @Result = N'Khuyến mãi không trong thời gian áp dụng.';
    ELSE
        SET @Result = N'Khuyến mãi hợp lệ.';
    RETURN @Result;
END;
GO
-- Test:
SELECT dbo.Fn_KiemTraKhuyenMai('KM001', '2025-04-07 08:00') AS Result;

-- 8. Tính tổng tiền cho mỗi chuyến xe
GO 
CREATE FUNCTION Fn_TinhTongTien(
    @ID_DATXE CHAR(5)
)
RETURNS DECIMAL(10,2)
AS 
BEGIN 
    DECLARE @ChiPhi DECIMAL(10, 2);
    DECLARE @KhoangCach FLOAT;
    DECLARE @Gia1KM DECIMAL(10, 2);
    DECLARE @PhuThu FLOAT;
    DECLARE @HanMucKM DECIMAL(10, 2);
    DECLARE @ThoiGianDat DATETIME;
    DECLARE @ID_LOAIXE CHAR(5);
    DECLARE @ID_KHUYENMAI CHAR(5);
    DECLARE @KiemTraKM NVARCHAR(100);

    SELECT @KhoangCach = D.KHOANGCACH, @ThoiGianDat = D.THOIGIANDAT, @ID_LOAIXE = T.ID_LOAIXENO, @ID_KHUYENMAI = ID_KHUYENMAINO
    FROM DATXE D, TAIXE T 
    WHERE T.ID_TX = D.ID_TXNO AND D.ID_DATXE = @ID_DATXE;

    SELECT @Gia1KM = GIA1KM
    FROM LOAIXE
    WHERE ID_LOAIXE = @ID_LOAIXE;

    SET @PhuThu = 1; 
    SELECT @PhuThu = PHUTHU
    FROM LOAIXE_GIO, GIO
    WHERE ID_LOAIXENO = @ID_LOAIXE AND ID_GIO = ID_GIONO
    AND CAST(@ThoiGianDat AS TIME) BETWEEN THOIGIANBATDAU AND THOIGIANKETTHUC;

    IF @ID_KHUYENMAI IS NOT NULL
    BEGIN
        SET @KiemTraKM = dbo.Fn_KiemTraKhuyenMai(@ID_KHUYENMAI, @ThoiGianDat);
        IF @KiemTraKM = N'Khuyến mãi hợp lệ.'
        BEGIN
            DECLARE @DieuKien NVARCHAR(200);
            DECLARE @SoLuong INT;
            SELECT @HanMucKM = HANMUC, @DieuKien = DIEUKIENAPDUNG, @SoLuong = SOLUONG
            FROM KHUYENMAI
            WHERE ID_KHUYENMAI = @ID_KHUYENMAI;

            IF @DieuKien = N'Cho đơn từ 50K'
            BEGIN
                SET @ChiPhi = @KhoangCach * @Gia1KM * @PhuThu;
                IF @ChiPhi < 50000
                    SET @HanMucKM = 0; 
            END
            ELSE IF @DieuKien = N'Chỉ áp dụng cuối tuần'
            BEGIN
                IF DATEPART(WEEKDAY, @ThoiGianDat) NOT IN (1, 7) -- Chủ nhật = 1
                    SET @HanMucKM = 0;
            END
            ELSE IF @DieuKien = N'Không giới hạn' OR @DieuKien = N'Tất cả đơn'
            BEGIN
                SET @HanMucKM = @HanMucKM; 
            END
            ELSE
                SET @HanMucKM = 0;
        END
        ELSE
        BEGIN
            SET @HanMucKM = 0;
        END
    END

    SET @ChiPhi = (@KhoangCach * @Gia1KM * @PhuThu) - ISNULL(@HanMucKM, 0);
    IF @ChiPhi < 0 SET @ChiPhi = 0;
    RETURN @ChiPhi;
END;
GO
--Test
SELECT dbo.Fn_TinhTongTien('DX001') AS ChiPhi;
GO
CREATE FUNCTION Fn_TinhGiaTien(
    @KhoangCach FLOAT,
    @ID_KHUYENMAI CHAR(5),
    @ID_LOAIXE CHAR(5)
)
RETURNS DECIMAL(10,2)
AS 
BEGIN 
    DECLARE @ChiPhi DECIMAL(10, 2);
    DECLARE @Gia1KM DECIMAL(10, 2);
    DECLARE @PhuThu FLOAT;
    DECLARE @HanMucKM DECIMAL(10, 2);
    DECLARE @ThoiGianDat DATETIME;
    DECLARE @KiemTraKM NVARCHAR(100);

    SELECT @Gia1KM = GIA1KM
    FROM LOAIXE
    WHERE ID_LOAIXE = @ID_LOAIXE;

    SET @PhuThu = 1; 
    SELECT @PhuThu = PHUTHU
    FROM LOAIXE_GIO, GIO
    WHERE ID_LOAIXENO = @ID_LOAIXE AND ID_GIO = ID_GIONO
    AND CAST(@ThoiGianDat AS TIME) BETWEEN THOIGIANBATDAU AND THOIGIANKETTHUC;

    IF @ID_KHUYENMAI IS NOT NULL
    BEGIN
        SET @KiemTraKM = dbo.Fn_KiemTraKhuyenMai(@ID_KHUYENMAI, @ThoiGianDat);
        IF @KiemTraKM = N'Khuyến mãi hợp lệ.'
        BEGIN
            DECLARE @DieuKien NVARCHAR(200);
            DECLARE @SoLuong INT;
            SELECT @HanMucKM = HANMUC, @DieuKien = DIEUKIENAPDUNG, @SoLuong = SOLUONG
            FROM KHUYENMAI
            WHERE ID_KHUYENMAI = @ID_KHUYENMAI;

            IF @DieuKien = N'Cho đơn từ 50K'
            BEGIN
                SET @ChiPhi = @KhoangCach * @Gia1KM * @PhuThu;
                IF @ChiPhi < 50000
                    SET @HanMucKM = 0; 
            END
            ELSE IF @DieuKien = N'Chỉ áp dụng cuối tuần'
            BEGIN
                IF DATEPART(WEEKDAY, @ThoiGianDat) NOT IN (1, 7) -- Chủ nhật = 1
                    SET @HanMucKM = 0;
            END
            ELSE IF @DieuKien = N'Không giới hạn' OR @DieuKien = N'Tất cả đơn'
            BEGIN
                SET @HanMucKM = @HanMucKM; 
            END
            ELSE
                SET @HanMucKM = 0;
        END
        ELSE
        BEGIN
            SET @HanMucKM = 0;
        END
    END

    SET @ChiPhi = (@KhoangCach * @Gia1KM * @PhuThu) - ISNULL(@HanMucKM, 0);
    IF @ChiPhi < 0 SET @ChiPhi = 0;
    RETURN @ChiPhi;
END;
GO
-- 9. Kiểm tra tài xế có rãnh tại thời điểm đặt xe không (lọc tài xế rảnh khi tìm danh sách tài xế phù hợp.)
GO
CREATE FUNCTION Fn_KiemTraTrangThaiTaiXe
(
    @ID_TX CHAR(5),
    @ThoiGianDat DATETIME
)
RETURNS BIT
AS
BEGIN
    IF EXISTS ( SELECT 1
                FROM DATXE
                WHERE ID_TXNO = @ID_TX AND TRANGTHAI IN (N'Chờ tài xế nhận', N'Đang thực hiện')
                AND @ThoiGianDat BETWEEN THOIGIANDAT AND ISNULL(THOIGIANDEN, DATEADD(HOUR, 2, THOIGIANDAT)))
        RETURN 0; -- Tài xế bận
    RETURN 1; -- Tài xế rãnh
END;
GO
-- Test
SELECT dbo.Fn_KiemTraTrangThaiTaiXe('TX001', '2025-04-18 10:00') AS TaiXeSanSang;

-- 10. Đặt xe
GO
CREATE PROCEDURE Pr_DatXe
    @ID_KH CHAR(5),
    @ID_THANHTOAN CHAR(5),
    @ID_KHUYENMAI CHAR(5),
    @DIEMDON NVARCHAR(200),
    @DIEMTRA NVARCHAR(200),
    @THOIGIANDAT DATETIME = NULL, 
    @KHOANGCACH FLOAT,
    @ID_LOAIXE CHAR(5)
AS 
BEGIN 
    BEGIN TRY
        -- Kiểm tra khuyến mãi (nếu có)
        IF @THOIGIANDAT IS NULL
            SET @THOIGIANDAT = GETDATE();
        DECLARE @KhuyenMaiStatus NVARCHAR(100) = N'Không sử dụng khuyến mãi';
        IF @ID_KHUYENMAI IS NOT NULL
        BEGIN
            DECLARE @KiemTraKM NVARCHAR(100);
            SET @KiemTraKM = dbo.Fn_KiemTraKhuyenMai(@ID_KHUYENMAI, @THOIGIANDAT);
            IF @KiemTraKM != N'Khuyến mãi hợp lệ.'
                BEGIN
                    SET @KhuyenMaiStatus = @KiemTraKM;
                    SET @ID_KHUYENMAI = NULL;
                END
            ELSE
                SET @KhuyenMaiStatus = N'Khuyến mãi hợp lệ';
        END

        -- Tạo ID tự động cho DATXE
        DECLARE @ID_DATXE CHAR(5);
        SET @ID_DATXE = dbo.Fn_TangTuDongID_DatXe()
        -- Chọn một tài xế rảnh ngẫu nhiên
        DECLARE @ID_TX CHAR(5);
        SELECT TOP 1 @ID_TX = T.ID_TX
        FROM TAIXE T
        INNER JOIN LOAIXE L ON T.ID_LOAIXENO = L.ID_LOAIXE
        WHERE T.ID_LOAIXENO = @ID_LOAIXE 
        AND dbo.Fn_KiemTraTrangThaiTaiXe(T.ID_TX, @THOIGIANDAT) = 1
        ORDER BY NEWID(); -- Chọn ngẫu nhiên
        -- Kiểm tra nếu không có tài xế rảnh
        IF @ID_TX IS NULL
            BEGIN
                SELECT NULL AS ID_DATXE, 'NoDriverAvailable' AS Status, N'Hiện tại không có tài xế rảnh. Vui lòng thử lại sau.' AS Message, NULL AS ID_TX, @KhuyenMaiStatus AS KhuyenMaiStatus;
                RETURN;
            END
        BEGIN TRANSACTION;
            -- Tạo chuyến xe
            INSERT INTO DATXE (ID_DATXE, ID_KHNO, ID_THANHTOANNO, ID_KHUYENMAINO, DIEMDON, DIEMTRA, GIATIEN, THOIGIANDAT, KHOANGCACH, TRANGTHAI)
            VALUES (@ID_DATXE, @ID_KH, @ID_THANHTOAN, @ID_KHUYENMAI, @DIEMDON, @DIEMTRA, dbo.Fn_TinhGiaTien(@KhoangCach, @ID_KHUYENMAI, @ID_LOAIXE), GETDATE(), @KHOANGCACH, N'Chờ tài xế nhận');
            -- Cập nhật số lượng khuyến mãi
            IF @ID_KHUYENMAI IS NOT NULL
                BEGIN
                    UPDATE KHUYENMAI
                    SET SOLUONG = SOLUONG - 1
                    WHERE ID_KHUYENMAI = @ID_KHUYENMAI;
                END
            -- Trả về thông tin chuyến xe
            SELECT @ID_DATXE AS ID_DATXE, 'Success' AS Status, N'Đặt xe thành công' AS Message, @ID_TX AS ID_TX, @KhuyenMaiStatus AS KhuyenMaiStatus;
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        -- Trả về lỗi dưới dạng kết quả
        SELECT NULL AS ID_DATXE, 'Error' AS Status, ERROR_MESSAGE() AS Message, NULL AS ID_TaiXe, @KhuyenMaiStatus AS KhuyenMaiStatus;
    END CATCH
END;
GO
EXEC dbo.Pr_DatXe @ID_KH = 'KH003', @ID_THANHTOAN = 'TT001', @ID_KHUYENMAI = 'KM004', @DIEMDON = N'12 Trần Cao Vân,Quận Thanh Khê,Đà Nẵng', @DIEMTRA = N'456 Nguyễn Trãi,Quận Hải Châu,TP.HCM',@KHOANGCACH = 10.0, @ID_LOAIXE = 'LX001';
-- go
-- CREATE TRIGGER trg_AfterInsert_DATXE
-- ON DATXE
-- AFTER INSERT
-- AS
-- BEGIN
--     -- Cập nhật GIATIEN cho các bản ghi vừa chèn
--     UPDATE DATXE
--     SET GIATIEN = dbo.Fn_TinhTongTien(i.ID_DATXE)
--     FROM DATXE d
--     INNER JOIN inserted i ON d.ID_DATXE = i.ID_DATXE;
-- END;
GO 
-- CREATE PROCEDURE Pr_ThongKeChuyenNgay
-- AS
-- BEGIN
--     DECLARE @NgayHienTai DATE = CAST(GETDATE() AS DATE);
--     SELECT *
--     FROM DATXE 
--     WHERE  CAST(THOIGIANDAT AS DATE) = @NgayHienTai
--     ORDER BY THOIGIANDAT;
-- END;
CREATE PROCEDURE Pr_ThongKeChuyenNgay
    @Ngay DATE  -- Tham số ngày do người dùng truyền vào
AS
BEGIN
    SELECT *
    FROM DATXE 
    WHERE CAST(THOIGIANDAT AS DATE) = @Ngay
    ORDER BY THOIGIANDAT;
END;
GO
DECLARE @Ngay DATE = CAST(GETDATE() AS DATE);
EXEC Pr_ThongKeChuyenNgay @Ngay
GO
CREATE PROCEDURE Pr_ThongKeChuyenThang
    @ThoiGian DATETIME
AS
BEGIN
    DECLARE @Thang INT = MONTH(@ThoiGian);
    DECLARE @Nam INT = YEAR(@ThoiGian);

    SELECT *
    FROM DATXE 
    WHERE MONTH(THOIGIANDAT) = @Thang 
      AND YEAR(THOIGIANDAT) = @Nam
    ORDER BY THOIGIANDAT;
END;
GO
DECLARE @Ngay DATE = CAST(GETDATE() AS DATE);
EXEC Pr_ThongKeChuyenThang @Ngay ;
GO
Create PROCEDURE Pr_ChuyenXeGanNhat
(@soDong as Int)
AS
BEGIN
    SELECT top (@soDong)  DX.ID_DATXE,TX.TENTX,KH.TENKH,DIEMDON,DIEMTRA,DIEMSO,THOIGIANDAT,THOIGIANDEN,THOIGIANDON,TRANGTHAI,KHOANGCACH,DANHGIA,dbo.Fn_TinhTongTien(DX.ID_DATXE) as giaTien 
    FROM DATXE DX,KHACHHANG KH,PHUONGTHUCTHANHTOAN PTTT,TAIXE TX
    WHERE DX.ID_KHNO=KH.ID_KH AND PTTT.ID_THANHTOAN=DX.ID_THANHTOANNO AND TX.ID_TX=DX.ID_TXNO
    ORDER BY THOIGIANDAT DESC;  -- Hoặc bạn có thể dùng THOIGIANDON nếu muốn
END;
GO

-- 14. Mở/Khoá tài khoản theo mã
GO
CREATE PROCEDURE Pr_MoKhoaTaiKhoan
    @ID_Ma CHAR(6),      
    @HanhDong BIT    
AS
BEGIN
    DECLARE @Message NVARCHAR(50)
    DECLARE @Status VARCHAR(20)
    IF EXISTS (SELECT 1 FROM TAIKHOAN WHERE ID_NGUOIDUNG = @ID_Ma)
    BEGIN
        UPDATE TAIKHOAN
        SET TRANGTHAITK = @HanhDong
        WHERE ID_NGUOIDUNG = @ID_Ma;

        IF @HanhDong = 1
        BEGIN
            SET @Message = N'Tài khoản đã được mở thành công.';
        END
        ELSE
        BEGIN
            SET @Message = N'Tài khoản đã bị khóa thành công.';
        END
        SET @Status = 'Success'
    END
    ELSE
    BEGIN
        SET @Message = N'Không tìm thấy hoặc không tồn tại người dùng này.';
        SET @Status = 'NotFound'
    END
    SELECT @Status AS 'Status', @Message AS 'Message'
END;
GO 
EXECUTE Pr_MoKhoaTaiKhoan @ID_Ma = 'TX001', @HanhDong = 0;
-- 15.Kiểm tra trạng thái tài khoản
GO
CREATE FUNCTION Fn_KiemTraTTTK (
    @sdt CHAR(11),
    @idVaiTro VARCHAR(6)
)
RETURNS BIT
AS
BEGIN
    DECLARE @trangThai BIT

    SELECT @trangThai = TrangThaiTK
    FROM TAIKHOAN
    WHERE SDT = @sdt AND ID_VAITRONO = @idVaiTro
    IF @trangThai IS NULL
        SET @trangThai = 0 

    RETURN @trangThai
END
GO
-- 16.Bật/tắt trạng thái hoạt động của tài xế
CREATE PROCEDURE Pr_CapNhatTrangThaiTaiXe
    @ID_TX CHAR(5),
    @TrangThai BIT
AS
BEGIN
    DECLARE @Message NVARCHAR(100)
    DECLARE @Status VARCHAR(20)

    IF EXISTS (SELECT 1 FROM TAIXE WHERE ID_TX = @ID_TX)
    BEGIN
        UPDATE TAIXE
        SET TRANGTHAIHD = @TrangThai
        WHERE ID_TX = @ID_TX;

        SET @Status = 'Success'
        IF @TrangThai = 1
            SET @Message = N'Tài xế đã được bật hoạt động.'
        ELSE
            SET @Message = N'Tài xế đã được tắt hoạt động.'
    END
    ELSE
    BEGIN
        SET @Status = 'NotFound'
        SET @Message = N'Không tìm thấy tài xế có ID này.'
    END

    SELECT @Status AS 'Status', @Message AS 'Message'
END;
GO
EXECUTE Pr_CapNhatTrangThaiTaiXe @ID_TX='TX024', @TrangThai=1
go
-- 17.Thống kê dữ liệu của chuyến xe theo trạng thái
CREATE FUNCTION Fn_SoChuyenXeTheoTrangThai
(@trangThai NVARCHAR(200))
RETURNS INT
AS
BEGIN
    DECLARE @soLuong INT
    SELECT @soLuong=Count(ID_DATXE) 
    FROM DATXE 
    WHERE TRANGTHAI=@trangThai
    RETURN @soLuong
END
go
print dbo.Fn_SoChuyenXeTheoTrangThai (N'Đang thực hiện')
-- 18.Cập nhật trạng thái chuyến xe
go
CREATE PROCEDURE Pr_CapNhatTrangThaiChuyenXe
    @ID_DATXE CHAR(5),
    @TRANGTHAI NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Result NVARCHAR(200);

    -- Kiểm tra chuyến xe có tồn tại không
    IF EXISTS (SELECT 1 FROM DATXE WHERE ID_DATXE = @ID_DATXE)
    BEGIN
        BEGIN TRY
            -- Cập nhật trạng thái
            UPDATE DATXE
            SET TRANGTHAI = @TRANGTHAI
            WHERE ID_DATXE = @ID_DATXE;

            SET @Result = N'Cập nhật trạng thái thành công!';
        END TRY
        BEGIN CATCH
            SET @Result = N'Lỗi cập nhật: ' + ERROR_MESSAGE();
        END CATCH
    END
    ELSE
    BEGIN
        SET @Result = N'Không tìm thấy chuyến xe!';
    END

    -- Trả kết quả
    SELECT @Result AS Result;
END
EXEC Pr_CapNhatTrangThaiChuyenXe 'DX001', N'Hoàn thành';
go

-- 19.Danh sách tài xế có thể nhận chuyến
CREATE PROCEDURE Pr_DSTaiXeCoTheNhanChuyen
AS 
BEGIN
    SELECT *
FROM 
    TAIXE TX
WHERE 
    TX.TRANGTHAIHD = 1  -- Tài xế đang hoạt động
    AND NOT EXISTS (
        SELECT 1
        FROM DATXE DX
        WHERE DX.ID_TXNO = TX.ID_TX
        AND DX.TRANGTHAI IN ('Đang thực hiện')  -- Trạng thái cuốc xe chưa hoàn thành
    );
END
EXEC Pr_DSTaiXeCoTheNhanChuyen
go
-- 20.Gán Chuyến Xe cho Tài xế phù hợp
CREATE PROCEDURE Pr_GanChuyenXeChoTaiXePhuHop
    @BookingID CHAR(5),
    @DriverID CHAR(5)
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @Message NVARCHAR(100);
    
    BEGIN TRY
        -- Check if booking exists and is in 'Chờ tài xế nhận' status
        IF EXISTS (
            SELECT 1 
            FROM DATXE 
            WHERE ID_DATXE = @BookingID 
            AND TRANGTHAI = N'Chờ tài xế nhận'
        )
        BEGIN
            -- Check if driver exists
            IF EXISTS (
                SELECT 1 
                FROM DATXE
                WHERE ID_TXNO = @DriverID AND TRANGTHAI LIKE N'Đang thực hiện'
            )
            BEGIN
                SET @Message = N'Bạn đang còn chuyến chưa hoàn thành';
            END
            ELSE
            BEGIN
                -- Update booking with driver and change status
                UPDATE DATXE
                SET ID_TXNO = @DriverID,
                    TRANGTHAI = N'Đang thực hiện'
                WHERE ID_DATXE = @BookingID;
                
                SET @Message = N'Nhận thành công chuyến' 
            END
        END
        ELSE
        BEGIN
            SET @Message = N'Chuyến xe không tồn tại hoặc không ở trạng thái chờ tài xế nhận';
        END
    END TRY
    BEGIN CATCH
        SET @Message = N'Lỗi khi gán tài xế: ' + ERROR_MESSAGE();
    END CATCH
    
    SELECT @Message AS Result;
END;
EXEC Pr_GanChuyenXeChoTaiXePhuHop 'DX025','TX006'
-- 21.Lấy số chuyến đi theo tháng(đối số năm)
go
CREATE PROCEDURE Pr_SoChuyenDiTheoThang
    @nam INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Create a table with all months (1–12)
    WITH Months AS (
        SELECT 1 AS month
        UNION SELECT 2
        UNION SELECT 3
        UNION SELECT 4
        UNION SELECT 5
        UNION SELECT 6
        UNION SELECT 7
        UNION SELECT 8
        UNION SELECT 9
        UNION SELECT 10
        UNION SELECT 11
        UNION SELECT 12
    )
    SELECT 
        m.month AS Thang,
        COUNT(DX.ID_DATXE) AS soChuyenXe
    FROM Months m
    LEFT JOIN DATXE DX 
        ON MONTH(DX.THOIGIANDAT) = m.month 
        AND YEAR(DX.THOIGIANDAT) = @nam
    GROUP BY m.month
    ORDER BY m.month;
END;

EXEC Pr_SoChuyenDiTheoThang 2025
go
-- 22.Thống kê chuyến xe theo khu vực
CREATE PROCEDURE Pr_TongChuyenTheoKhuVuc
AS
BEGIN
    SET NOCOUNT ON;

    SELECT 
        SUM(CASE 
            WHEN DX.diemDon LIKE N'%Đà Nẵng%' OR DX.diemDon LIKE N'%DN%' THEN 1 
            ELSE 0 
        END) AS DaNang,SUM(CASE 
                            WHEN DX.diemDon LIKE N'%TP.HCM%' OR DX.diemDon LIKE N'%HCM%' OR DX.diemDon LIKE N'%Sài Gòn%' THEN 1 
                            ELSE 0 
        END) AS "TP.HCM",SUM(CASE 
                            WHEN DX.diemDon LIKE N'%Hà Nội%' OR DX.diemDon LIKE N'%HN%' THEN 1 
                            ELSE 0 
        END) AS HaNoi
    FROM DATXE DX
   
END;
go
-- 23.Tỷ lệ dùng khuyến mãi
CREATE PROCEDURE Pr_ThongKeTyLeKhuyenMai
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @tongChuyen FLOAT;
    SELECT @tongChuyen = COUNT(*)
    FROM DATXE

    SET @tongChuyen = CASE WHEN @tongChuyen = 0 THEN 1 ELSE @tongChuyen END;
    SELECT 
        KM.ID_KHUYENMAI AS id_khuyenMai,
        KM.TENKM AS tenKM,
        ISNULL(COUNT(DX.ID_DATXE), 0) AS soLuong,
        ROUND(ISNULL(COUNT(DX.ID_DATXE) * 100.0 / @tongChuyen, 0), 2) AS tyLe
    FROM 
        KHUYENMAI KM
    LEFT JOIN 
        DATXE DX ON KM.ID_KHUYENMAI = DX.ID_KHUYENMAINO
    GROUP BY 
        KM.ID_KHUYENMAI, KM.TENKM
    ORDER BY 
        soLuong DESC, KM.ID_KHUYENMAI;
END;
EXEC Pr_ThongKeTyLeKhuyenMai
go
-- 24.Thống kê trạng thái chuyến xe
CREATE PROCEDURE Pr_ThongKeTrangThaiChuyen
AS 
BEGIN
    SET NOCOUNT ON;

    WITH Statuses AS (
        SELECT N'Chờ tài xế nhận' AS trangThai
        UNION SELECT N'Đang thực hiện'
        UNION SELECT N'Hoàn thành'
        UNION SELECT N'Đã huỷ'
    )
    -- Count trips for each status, including zeros
    SELECT 
        s.trangthai,
        ISNULL(COUNT(DX.ID_DATXE), 0) AS SoLuong
    FROM 
        Statuses s
    LEFT JOIN 
        DATXE DX ON s.trangthai = DX.TRANGTHAI
    GROUP BY 
        s.trangthai
    ORDER BY 
        s.trangthai;
END;
go
-- 25.Tính tổng doanh thu theo năm
CREATE FUNCTION Fn_TinhTongDoanhThuTheoNam(
    @Nam INT
)
RETURNS DECIMAL(10,2)
AS 
BEGIN 
    DECLARE @TongDoanhThu DECIMAL(10,2);

    SELECT @TongDoanhThu = SUM(dbo.Fn_TinhTongTien(ID_DATXE))
    FROM DATXE
    WHERE YEAR(THOIGIANDAT) = @Nam
    -- AND TRANGTHAI = N'Đang thực hiện'; 

    RETURN ISNULL(@TongDoanhThu, 0);
END;
go
print dbo.Fn_TinhTongDoanhThuTheoNam(2025)
go
-- 26.Thống kê theo phương thức thanh toán
CREATE PROCEDURE Pr_ThongKePhuongThucThanhToan
    (@nam INT)
AS
BEGIN
    SELECT 
        T.LOAIHINHTHANHTOAN AS PhuongThucThanhToan,
        COUNT(D.ID_DATXE) AS SoLuongChuyen,
        ISNULL(SUM(dbo.Fn_TinhTongTien(D.ID_DATXE)), 0) AS TongDoanhThu
    FROM DATXE D
    JOIN PHUONGTHUCTHANHTOAN T ON D.ID_THANHTOANNO = T.ID_THANHTOAN
    WHERE YEAR(D.THOIGIANDAT) = @nam
    GROUP BY T.LOAIHINHTHANHTOAN;
END;
go
EXEC Pr_ThongKePhuongThucThanhToan 2025
--27.Tính số khách hàng mới tháng này
go
CREATE FUNCTION Fn_KhachHangMoiThangNay()
RETURNS INT
AS
BEGIN

    RETURN (SELECT COUNT(DISTINCT dx.ID_KHNO) as khachHangMoiTrongThangNay
            FROM DATXE dx
            WHERE dx.THOIGIANDAT >= DATEADD(DAY, -30, GETDATE())
            AND dx.THOIGIANDAT = (
                SELECT MIN(dx2.THOIGIANDAT)
                FROM DATXE dx2
                WHERE dx2.ID_KHNO = dx.ID_KHNO
            ))
    
END
go
--28.Tính đánh giá trung bình
CREATE FUNCTION Fn_TinhDanhGiaTrungBinh()
RETURNS FLOAT
AS
BEGIN
    RETURN (SELECT AVG(CAST(DIEMSO AS FLOAT)) danhGiaTB
            FROM DATXE
            WHERE DIEMSO IS NOT NULL)
END
--29.Tính tỷ lệ hài lòng với chuyến
go
CREATE FUNCTION Fn_TyLeHaiLongCuaKhachHang()
RETURNS FLOAT
AS
BEGIN
    RETURN (SELECT 
            CASE 
                WHEN COUNT(*) > 0 THEN 
                    (COUNT(CASE WHEN DIEMSO >= 4 THEN 1 END) * 100.0) / COUNT(*)
                ELSE 0
            END
            FROM DATXE
            WHERE DIEMSO IS NOT NULL)
END
go
CREATE PROCEDURE Pr_TongChuyenDi_ChiTieu_KhachHang
    @ID_KH CHAR(5)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 
        ISNULL(COUNT(dx.ID_DATXE), 0) AS TongChuyenXe,
        ISNULL(SUM(dbo.Fn_TinhTongTien(dx.ID_DATXE)), 0.00) AS TongChiTieu
    FROM KHACHHANG kh
    LEFT JOIN DATXE dx ON dx.ID_KHNO = kh.ID_KH
    WHERE kh.ID_KH = @ID_KH;
END;
EXEC Pr_TongChuyenDi_ChiTieu_KhachHang 'KH002'

go
-- 30.Lấy mật khẩu tài khoản
CREATE FUNCTION Fn_LayMatKhau
(
    @ID_VAITRONO CHAR(6),
    @ID_NGUOIDUNG CHAR(5)
)
RETURNS VARCHAR(100)
AS
BEGIN
    DECLARE @MatKhau VARCHAR(100);

    SELECT @MatKhau = MATKHAU
    FROM TAIKHOAN
    WHERE ID_VAITRONO = @ID_VAITRONO
    AND ID_NGUOIDUNG = @ID_NGUOIDUNG;

    RETURN @MatKhau;
END;
go
CREATE PROCEDURE CalculateCustomerRates
    @TimePeriod NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @TotalCustomers FLOAT;
    DECLARE @RebookingCustomers FLOAT;
    DECLARE @InactiveCustomers FLOAT;
    DECLARE @NewCustomers FLOAT;
    DECLARE @StartDate DATETIME;

    -- Xác định ngày bắt đầu dựa trên TimePeriod
    SET @StartDate = CASE @TimePeriod
        WHEN 'Tháng này' THEN DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0) -- Ngày đầu tháng
        WHEN 'Quý này' THEN DATEADD(QUARTER, DATEDIFF(QUARTER, 0, GETDATE()), 0) -- Ngày đầu quý
        WHEN 'Năm này' THEN DATEADD(YEAR, DATEDIFF(YEAR, 0, GETDATE()), 0) -- Ngày đầu năm
        ELSE DATEADD(MONTH, DATEDIFF(MONTH, 0, GETDATE()), 0) -- Mặc định: Tháng này
    END;

    -- Tính số khách hàng mới riêng biệt
    WITH FirstRide AS (
        SELECT 
            ID_KHNO,
            MIN(THOIGIANDAT) AS FirstRideDate
        FROM DATXE
        GROUP BY ID_KHNO
    ),
    NewCustomers AS (
        SELECT ID_KHNO
        FROM FirstRide
        WHERE FirstRideDate >= @StartDate
    )
    SELECT 
        @NewCustomers = COUNT(*) * 1.0
    FROM NewCustomers;

    -- CTE để đếm số chuyến xe cho mỗi khách hàng (cho RebookingRate)
    WITH CustomerRideCount AS (
        SELECT 
            KH.ID_KH,
            COUNT(DX.ID_DATXE) AS RideCount
        FROM KHACHHANG KH
        LEFT JOIN DATXE DX ON KH.ID_KH = DX.ID_KHNO
        GROUP BY KH.ID_KH
    ),
    -- CTE để lấy khách hàng có chuyến trong khoảng thời gian (cho InactiveRate)
    RecentRides AS (
        SELECT DISTINCT DX.ID_KHNO
        FROM DATXE DX
        WHERE DX.THOIGIANDAT >= @StartDate
    )
    -- Tính toán các số liệu còn lại
    SELECT 
        @TotalCustomers = COUNT(*) * 1.0,
        @RebookingCustomers = SUM(CASE WHEN CRC.RideCount >= 2 THEN 1 ELSE 0 END) * 1.0,
        @InactiveCustomers = SUM(CASE WHEN RR.ID_KHNO IS NULL THEN 1 ELSE 0 END) * 1.0
    FROM KHACHHANG KH
    LEFT JOIN CustomerRideCount CRC ON KH.ID_KH = CRC.ID_KH
    LEFT JOIN RecentRides RR ON KH.ID_KH = RR.ID_KHNO;

    -- Xử lý trường hợp không có khách hàng
    IF @TotalCustomers = 0
    BEGIN
        SELECT 
            RebookingRate = 0.00,
            InactiveRate = 0.00,
            NewCustomerRate = 0.00;
        RETURN;
    END;

    -- Nếu không có khách hàng mới, gán giá trị 0
    IF @NewCustomers IS NULL
        SET @NewCustomers = 0;

    -- Trả về kết quả
    SELECT 
        DatLaiXe = ROUND((@RebookingCustomers / @TotalCustomers) * 100, 2),
        KhongHoatDong = ROUND((@InactiveCustomers / @TotalCustomers) * 100, 2),
        NewCustomerRate = ROUND((@NewCustomers / @TotalCustomers) * 100, 2);
END;
EXECUTE CalculateCustomerRates N'Tháng này'
select * from TAIXE
select * from LOAIXE
select * from DATXE
select * from TAIKHOAN
select * from KHACHHANG
select * from KHUYENMAI

SELECT MATKHAU FROM TAIKHOAN WHERE ID_VAITRONO='USER' AND ID_NGUOIDUNG='KH005'


            
            