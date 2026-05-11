# BookCar — Web Frontend

Ứng dụng đặt xe trực tuyến, xây dựng bằng **React + Vite + TailwindCSS + Zustand**.

---

## 🚀 Khởi động nhanh

```bash
# 1. Cài dependencies
npm install

# 2. Sao chép biến môi trường
cp .env.example .env

# 3. Cấu hình .env
VITE_API_BASE_URL=http://localhost:8080   # URL Backend Spring Boot
VITE_WS_URL=ws://localhost:8080/ws        # WebSocket endpoint

# 4. Chạy dev server
npm run dev
# → http://localhost:3000
```

---

## 📁 Cấu trúc thư mục

```
src/
├── assets/                 # Hình ảnh, fonts, SVG
├── components/
│   ├── Elements/           # Button, Input, Modal, Spinner
│   └── Form/               # FormField, Checkbox
├── config/                 # Hằng số toàn cục, API keys
├── features/               # ⭐ Trái tim dự án — chia theo module
│   ├── auth/api/           # authApi.js
│   ├── booking/api/        # bookingApi.js, masterDataApi.js
│   ├── chat/               # chatApi.js, ChatDialog.jsx
│   ├── customer/api/       # customerApi.js
│   ├── driver/api/         # driverApi.js
│   └── payment/api/        # paymentApi.js, walletApi.js
├── hooks/                  # useAuth, useDebounce, useWebSocket
├── layouts/                # AuthLayout, MainLayout
├── pages/
│   ├── auth/               # LoginPage, RegisterCustomer/Driver, ForgotPassword
│   ├── customer/           # Home, Booking, Tracking, History, Rating, Profile
│   └── driver/             # Dashboard, AvailableTrips, CurrentTrip, Revenue, Wallet, Profile
├── routes/                 # AppRoutes.jsx, ProtectedRoute.jsx
├── services/               # apiClient.js (Axios + interceptors)
├── store/                  # rootStore.js (Zustand)
└── utils/                  # formatDate.js, currency.js, cn.js
```

---

## 🛠️ Công nghệ

| Thư viện       | Mục đích                              |
|----------------|---------------------------------------|
| React 18       | UI Framework                          |
| Vite 5         | Bundler — dev server siêu nhanh       |
| TailwindCSS 3  | Utility-first styling                 |
| Zustand 5      | Global state management               |
| React Router 6 | Client-side routing                   |
| Axios          | HTTP client + interceptors            |
| React Hook Form + Zod | Form validation                |
| STOMP/SockJS   | WebSocket (realtime booking/chat)     |
| Recharts       | Biểu đồ doanh thu                     |
| React Hot Toast | Notifications                        |
| date-fns       | Xử lý ngày giờ (locale VI)            |

---

## 👤 Roles & Routes

| Role     | Đường dẫn                    |
|----------|------------------------------|
| CUSTOMER | `/customer/home`             |
| DRIVER   | `/driver/dashboard`          |
| ADMIN    | `/admin/dashboard`           |

---

## 🔌 Kết nối Backend

Tất cả API calls đi qua `src/services/apiClient.js`:
- **Auto attach** `Authorization: Bearer <token>` cho mọi request
- **Auto refresh** token khi nhận 401
- **Auto redirect** `/login` khi refresh thất bại

### WebSocket
Kết nối STOMP qua SockJS tại `VITE_WS_URL/ws`.

Topics được lắng nghe:
- `/topic/booking/{id}` — cập nhật trạng thái chuyến
- `/topic/chat/{id}`    — tin nhắn realtime
- `/topic/available-bookings` — chuyến mới cho tài xế

---

## 📦 Build production

```bash
npm run build
# Output: dist/
```

---

## 🗺️ Roadmap

- [ ] Google Maps integration (ChonDiemView)
- [ ] Admin dashboard đầy đủ
- [ ] Push notifications (FCM)
- [ ] Dark/Light theme toggle
- [ ] PWA support
- [ ] i18n (Tiếng Việt / English)
