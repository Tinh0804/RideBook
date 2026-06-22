# 🚗 RideBook (BookCar Online)

**Nền tảng đặt xe trực tuyến đa nền tảng kết nối hành khách và tài xế theo thời gian thực.**

Dự án là một hệ thống đặt xe công nghệ (tương tự Grab/Gojek) cung cấp giải pháp toàn diện bao gồm: ứng dụng Web cho Hành khách (đặt xe, theo dõi vị trí) & Tài xế (nhận chuyến, quản lý doanh thu), và ứng dụng Desktop dành cho Quản trị viên điều hành hệ thống. Hệ thống tập trung xử lý điều phối chuyến đi theo thời gian thực (real-time) với độ trễ thấp và tích hợp thanh toán ví điện tử.

---

## 📸 Demo / Screenshots

*(Dành cho nhà tuyển dụng: Dưới đây là giao diện thực tế của hệ thống. Vui lòng xem video demo chi tiết tại: `[Link YouTube/Drive của bạn]`)*

| 📱 Giao diện Khách hàng | 🚕 Giao diện Tài xế | 💻 Admin Dashboard |
|:---:|:---:|:---:|
| ![Customer App](docs/screenshots/customer.png) <br> *Đặt xe & Theo dõi bản đồ* | ![Driver App](docs/screenshots/driver.png) <br> *Nhận chuyến & Doanh thu* | ![Admin App](docs/screenshots/admin.png) <br> *Quản lý & Thống kê* |

---

## 🛠️ Công nghệ (Tech Stack)

Dự án được xây dựng theo kiến trúc Client-Server với hệ sinh thái công nghệ hiện đại:

**Backend (API Server)**
* **Framework:** Java 21, Spring Boot 3.5
* **Bảo mật & Xác thực:** Spring Security, OAuth2 (Google/Facebook Login), JWT (JSON Web Tokens)
* **Real-time:** Spring WebSocket (STOMP)
* **Cơ sở dữ liệu (Database):** 
  * **PostgreSQL:** Lưu trữ dữ liệu quan hệ (người dùng, chuyến đi, giao dịch).
  * **MongoDB:** Lưu trữ dữ liệu phi cấu trúc (lịch sử chat, logs).
  * **Redis:** Caching dữ liệu, quản lý session WebSocket, lưu OTP.
* **Tích hợp bên thứ 3:** Firebase Admin SDK (Cloud Storage), VNPay API, MoMo API, Google Maps API.

**Frontend (Web App)**
* **Core:** React 18, Vite 5
* **State Management:** Zustand 5
* **Styling & UI:** TailwindCSS 3
* **Map & Realtime:** Leaflet (React-Leaflet) hiển thị bản đồ, STOMP/SockJS client để nhận event realtime.
* **Form & Validation:** React Hook Form, Zod.
* **Charts:** Recharts để vẽ biểu đồ doanh thu.

**Desktop App (Admin)**
* **Công nghệ:** Java 11 (Swing/JavaFX)
* **Database:** SQL Server (sử dụng MSSQL JDBC)
* **Báo cáo:** Apache POI (Xuất file Excel), JFreeChart (Biểu đồ).

**DevOps**
* **Deployment:** Docker & Docker Compose (Container hóa toàn bộ Databases).
* **Build tool:** Maven, npm.

---

## ✨ Điểm nổi bật (Features)

* **🚀 Điều phối chuyến đi Real-time (WebSocket):** Hệ thống tự động tìm kiếm và phát sóng (broadcast) yêu cầu đặt xe tới các tài xế gần nhất theo thời gian thực mà không cần reload trang. Xử lý đồng thời trạng thái chuyến xe giữa khách và tài xế.
* **🔐 Xác thực đa phương thức:** Đăng nhập an toàn qua SĐT/Email bằng JWT Token; tích hợp SSO OAuth2 (Đăng nhập nhanh bằng Google, Facebook).
* **💬 Live Chat Tốc độ cao:** Tích hợp tính năng nhắn tin trực tiếp giữa khách hàng và tài xế trong ứng dụng (lưu trữ trên MongoDB giúp truy xuất lịch sử cực nhanh).
* **💳 Hệ thống Ví điện tử & Thanh toán nội bộ:** Xây dựng hệ thống Wallet khép kín. Người dùng nạp tiền qua cổng thanh toán **VNPay** hoặc **MoMo**, tiền cước được tự động trừ và chia hoa hồng (commission) cho nền tảng.
* **🗺️ Tích hợp Bản đồ Trực quan:** Ứng dụng Google Maps API / Leaflet để gợi ý địa điểm, tính toán khoảng cách/giá tiền và hiển thị xe di chuyển trên bản đồ.
* **📈 Quản lý Quản trị viên Đa tầng:** Admin có thể theo dõi biến động doanh thu, quản lý hồ sơ đăng ký của tài xế và kiểm soát các loại phương tiện (Vehicle Types) một cách tập trung.

---

## 🚀 Hướng dẫn Cài đặt & Chạy thử

Yêu cầu hệ thống: **Java 21, Node.js 18+, Docker.**

### Bước 1: Khởi chạy Cơ sở dữ liệu (Docker)
Hệ thống sử dụng Docker Compose để khởi chạy đồng thời PostgreSQL, MongoDB, Redis và SQL Server.
```bash
git clone <URL_REPOSITOTY_CỦA_BẠN>
cd BookCar/Backend
docker-compose up -d
```

### Bước 2: Cấu hình và Khởi động Backend
1. Tạo một project trên Firebase Console, lấy file service account và lưu vào đường dẫn `Backend/src/main/resources/firebase/firebase-adminsdk.json`.
2. Kiểm tra file `application.yaml`, đảm bảo các thông số Database (username/password) khớp với cấu hình `docker-compose.yml`.
3. Chạy Backend bằng Maven:
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```
*(Server sẽ chạy tại: `http://localhost:8080/RideBook`)*

### Bước 3: Cấu hình và Khởi động Frontend
1. Di chuyển sang thư mục WebAPP và cài đặt dependencies:
```bash
cd ../WebAPP
npm install
```
2. Tạo file biến môi trường:
```bash
cp .env.example .env
```
*(Mở file `.env` và kiểm tra `VITE_API_BASE_URL=http://localhost:8080/RideBook`)*
3. Khởi động Web App:
```bash
npm run dev
```
*(Mở trình duyệt truy cập: `http://localhost:3000`)*

### Bước 4: Trải nghiệm
Hệ thống có sẵn các tài khoản mặc định (hoặc bạn có thể tự đăng ký mới):

| Vai trò | Username | Password | Tên hiển thị |
|---------|----------|----------|--------------|
| **Admin** | `admin` | `admin123` (hoặc `admin`) | Quản trị viên hệ thống |
| **Customer** | `customer1` | `pass123` | Nguyễn Văn A |
| **Driver** | `driver1` | `driver123` | Lê Văn C |

* Mở 2 trình duyệt ẩn danh: một đóng vai trò Khách hàng (Customer) để đặt xe, một đóng vai trò Tài xế (Driver) để nhận chuyến và trải nghiệm luồng realtime.

---
*Dự án được xây dựng với niềm đam mê kiến tạo sản phẩm phần mềm chất lượng, áp dụng các chuẩn thiết kế và công nghệ thực tế nhất!*
