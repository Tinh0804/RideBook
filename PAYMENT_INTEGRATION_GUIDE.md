# 💳 Payment Integration Guide - VNPay & MoMo

## 📋 Tổng quan

Dự án đã tích hợp **2 cổng thanh toán** phổ biến nhất tại Việt Nam:
- **VNPay** - Cổng thanh toán trực tuyến
- **MoMo** - Ví điện tử

---

## 🏗️ Kiến trúc Payment

```
BookingController → BookingService → PaymentService (VNPay/MoMo)
                         ↓
                  Payment Gateway
                         ↓
                  Callback/IPN
                         ↓
                  PaymentController
                         ↓
                  Update Booking Status
```

---

## ⚙️ Cấu hình

### 1. **VNPay Configuration**

Đăng ký tài khoản tại: https://sandbox.vnpayment.vn/

Cập nhật trong `application.yaml`:
```yaml
vnpay:
    tmn-code: "YOUR_VNPAY_TMN_CODE"
    hash-secret: "YOUR_VNPAY_HASH_SECRET"
    api-url: "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
    return-url: "http://localhost:8080/RideBook/payments/vnpay/return"
```

**Sandbox Credentials (for testing):**
- TMN Code: Lấy từ VNPay sandbox dashboard
- Hash Secret: Lấy từ VNPay sandbox dashboard

### 2. **MoMo Configuration**

Đăng ký tài khoản tại: https://developers.momo.vn/

Cập nhật trong `application.yaml`:
```yaml
momo:
    partner-code: "YOUR_MOMO_PARTNER_CODE"
    access-key: "YOUR_MOMO_ACCESS_KEY"
    secret-key: "YOUR_MOMO_SECRET_KEY"
    api-url: "https://test-payment.momo.vn/v2/gateway/api/create"
    return-url: "http://localhost:8080/RideBook/payments/momo/return"
    notify-url: "http://localhost:8080/RideBook/payments/momo/callback"
```

**Test Credentials (for testing):**
- Partner Code: `MOMOBKUN20180529`
- Access Key: `klm05TvNBzhg7h7j`
- Secret Key: `at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa`

---

## 🚀 API Endpoints

### **VNPay APIs**

#### 1. Create VNPay Payment
```http
POST /payments/vnpay/create
Content-Type: application/json

{
  "bookingId": "BOOKING123",
  "amount": 100000,
  "orderInfo": "Thanh toan chuyen xe BOOKING123",
  "returnUrl": "http://localhost:3000/payment-result",
  "locale": "vn"
}
```

**Response:**
```json
{
  "status": 201,
  "message": "Tạo thanh toán VNPay thành công",
  "result": {
    "status": "SUCCESS",
    "message": "Tạo link thanh toán VNPay thành công",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
    "orderId": "BOOKING123_1234567890",
    "amount": 100000,
    "paymentMethod": "VNPAY"
  }
}
```

#### 2. VNPay Callback (IPN)
```http
GET /payments/vnpay/callback?vnp_Amount=10000000&vnp_BankCode=...
```

Endpoint này được VNPay gọi tự động khi thanh toán hoàn tất.

#### 3. VNPay Return URL
```http
GET /payments/vnpay/return?vnp_Amount=10000000&vnp_ResponseCode=00...
```

Endpoint này được gọi khi khách hàng quay lại từ VNPay.

#### 4. Query VNPay Transaction
```http
GET /payments/vnpay/query/{orderId}?transactionDate=20240126200000
```

---

### **MoMo APIs**

#### 1. Create MoMo Payment
```http
POST /payments/momo/create
Content-Type: application/json

{
  "bookingId": "BOOKING123",
  "amount": 100000,
  "orderInfo": "Thanh toan chuyen xe BOOKING123",
  "returnUrl": "http://localhost:3000/payment-result",
  "notifyUrl": "http://your-server.com/payments/momo/callback"
}
```

**Response:**
```json
{
  "status": 201,
  "message": "Tạo thanh toán MoMo thành công",
  "result": {
    "status": "SUCCESS",
    "message": "Tạo link thanh toán MoMo thành công",
    "paymentUrl": "https://test-payment.momo.vn/v2/gateway/pay?t=...",
    "orderId": "BOOKING123_1234567890",
    "amount": 100000,
    "paymentMethod": "MOMO"
  }
}
```

#### 2. MoMo Callback (IPN)
```http
POST /payments/momo/callback
Content-Type: application/json

{
  "partnerCode": "MOMO",
  "orderId": "BOOKING123_1234567890",
  "requestId": "...",
  "amount": 100000,
  "resultCode": 0,
  "message": "Successful.",
  ...
}
```

#### 3. MoMo Return URL
```http
GET /payments/momo/return?partnerCode=MOMO&orderId=...
```

#### 4. Query MoMo Transaction
```http
GET /payments/momo/query/{orderId}?requestId=REQUEST123
```

#### 5. Refund MoMo Transaction
```http
POST /payments/momo/refund/{orderId}?requestId=REQ123&amount=50000&description=Hoan tien
```

---

## 🔄 Payment Flow

### **Frontend Flow (Khách hàng)**

1. **Khách hàng chọn phương thức thanh toán:**
   - VNPay hoặc MoMo

2. **Frontend gọi API tạo payment:**
   ```javascript
   const response = await fetch('/payments/vnpay/create', {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({
       bookingId: 'BOOKING123',
       amount: 100000,
       orderInfo: 'Thanh toan chuyen xe',
       returnUrl: 'http://localhost:3000/payment-result'
     })
   });
   
   const data = await response.json();
   // Redirect to payment URL
   window.location.href = data.result.paymentUrl;
   ```

3. **Khách hàng thanh toán:**
   - Được chuyển đến VNPay/MoMo
   - Nhập thông tin thanh toán
   - Xác nhận thanh toán

4. **Khách hàng quay lại ứng dụng:**
   - VNPay/MoMo redirect về `returnUrl`
   - Frontend nhận kết quả và hiển thị

### **Backend Flow (Server)**

1. **Nhận callback từ VNPay/MoMo (IPN):**
   - Xác thực chữ ký
   - Cập nhật trạng thái booking
   - Lưu thông tin giao dịch

2. **Xử lý return URL:**
   - Tương tự callback
   - Trả về kết quả cho frontend

---

## 🔒 Security

### **VNPay Security**

1. **HMAC SHA-512 Signature:**
   ```java
   String hashData = buildQueryString(vnpParams);
   String signature = hmacSHA512(hashSecret, hashData);
   ```

2. **Verify Callback:**
   ```java
   String receivedSignature = params.get("vnp_SecureHash");
   String calculatedSignature = hmacSHA512(hashSecret, hashData);
   if (!calculatedSignature.equals(receivedSignature)) {
       // Invalid signature
   }
   ```

### **MoMo Security**

1. **HMAC SHA-256 Signature:**
   ```java
   String rawSignature = "accessKey=" + accessKey +
                        "&amount=" + amount + ...;
   String signature = hmacSHA256(secretKey, rawSignature);
   ```

2. **Verify Callback:**
   ```java
   String receivedSignature = params.get("signature");
   String calculatedSignature = hmacSHA256(secretKey, rawSignature);
   if (!calculatedSignature.equals(receivedSignature)) {
       // Invalid signature
   }
   ```

---

## 📊 Response Codes

### **VNPay Response Codes**

| Code | Ý nghĩa |
|------|---------|
| 00 | Giao dịch thành công |
| 07 | Trừ tiền thành công. Nghi ngờ gian lận |
| 09 | Thẻ chưa đăng ký Internet Banking |
| 10 | Xác thực thông tin sai quá 3 lần |
| 11 | Hết hạn chờ thanh toán |
| 12 | Thẻ bị khóa |
| 24 | Khách hàng hủy giao dịch |
| 51 | Tài khoản không đủ số dư |
| 65 | Vượt quá hạn mức giao dịch |
| 75 | Ngân hàng đang bảo trì |
| 99 | Lỗi khác |

### **MoMo Result Codes**

| Code | Ý nghĩa |
|------|---------|
| 0 | Giao dịch thành công |
| 9000 | Chờ người dùng xác nhận |
| 1000 | Đã khởi tạo, chờ thanh toán |
| 1001 | Tài khoản không đủ tiền |
| 1002 | Giao dịch bị từ chối |
| 1003 | Giao dịch đã bị hủy |
| 1004 | Vượt quá hạn mức |
| 1005 | URL/QR code hết hạn |
| 1006 | Người dùng từ chối |
| 4010 | Tài khoản không tồn tại |
| 4100 | Hết thời gian xác nhận |
| 99 | Lỗi không xác định |

---

## 🧪 Testing

### **VNPay Sandbox Test Cards**

Sử dụng thẻ test sau trên VNPay sandbox:

**Ngân hàng NCB:**
- Số thẻ: `9704198526191432198`
- Tên chủ thẻ: `NGUYEN VAN A`
- Ngày phát hành: `07/15`
- Mật khẩu OTP: `123456`

### **MoMo Test Account**

Sử dụng app MoMo test hoặc MoMo sandbox credentials.

### **Test Scenarios**

1. **Thanh toán thành công:**
   ```bash
   curl -X POST http://localhost:8080/RideBook/payments/vnpay/create \
     -H "Content-Type: application/json" \
     -d '{
       "bookingId": "BOOK001",
       "amount": 100000,
       "orderInfo": "Test payment"
     }'
   ```

2. **Thanh toán thất bại:**
   - Nhập sai OTP hoặc hủy thanh toán

3. **Query transaction:**
   ```bash
   curl http://localhost:8080/RideBook/payments/vnpay/query/BOOK001_123?transactionDate=20240126200000
   ```

---

## 🔧 Troubleshooting

### **VNPay Issues**

1. **Invalid signature:**
   - Kiểm tra `hashSecret` trong config
   - Đảm bảo sort params theo alphabet
   - Không encode hash data

2. **Timeout:**
   - VNPay payment URL có hiệu lực 15 phút
   - Tăng expire time nếu cần

### **MoMo Issues**

1. **Invalid signature:**
   - Kiểm tra `secretKey` trong config
   - Đảm bảo raw signature string đúng format

2. **API Error:**
   - Kiểm tra `apiUrl` (test vs production)
   - Verify MoMo credentials

3. **Callback không nhận được:**
   - Đảm bảo `notifyUrl` public accessible
   - Sử dụng ngrok cho local testing

---

## 📱 Frontend Integration Example

### **React/Next.js Example**

```typescript
// Create payment
async function createPayment(bookingId: string, amount: number, method: 'vnpay' | 'momo') {
  const response = await fetch(`/payments/${method}/create`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      bookingId,
      amount,
      orderInfo: `Thanh toan chuyen xe ${bookingId}`,
      returnUrl: `${window.location.origin}/payment-result`
    })
  });
  
  const data = await response.json();
  
  if (data.status === 201) {
    // Redirect to payment gateway
    window.location.href = data.result.paymentUrl;
  }
}

// Handle payment result
function PaymentResult() {
  const params = new URLSearchParams(window.location.search);
  const vnpResponseCode = params.get('vnp_ResponseCode');
  const momoResultCode = params.get('resultCode');
  
  if (vnpResponseCode === '00' || momoResultCode === '0') {
    return <div>Thanh toán thành công!</div>;
  } else {
    return <div>Thanh toán thất bại!</div>;
  }
}
```

---

## 🚨 Important Notes

1. **Production vs Sandbox:**
   - Sandbox: Cho testing, sử dụng fake credentials
   - Production: Cho live, cần credentials thật

2. **HTTPS Required:**
   - Production phải dùng HTTPS
   - Callback URL phải public accessible

3. **IPN vs Return URL:**
   - **IPN (Instant Payment Notification):** Server-to-server, đáng tin cậy
   - **Return URL:** Browser redirect, có thể bị giả mạo
   - **Luôn xử lý payment status từ IPN, không tin Return URL**

4. **Idempotency:**
   - Xử lý duplicate callbacks
   - Check order status trước khi update

5. **Logging:**
   - Log tất cả payment requests/responses
   - Log signatures cho debugging

---

## ✅ Checklist Deployment

- [ ] Đã đăng ký tài khoản VNPay/MoMo production
- [ ] Đã cập nhật credentials trong `application.yaml`
- [ ] Đã thay sandbox URLs thành production URLs
- [ ] Đã cấu hình HTTPS
- [ ] Callback URLs public accessible
- [ ] Đã test thanh toán thành công
- [ ] Đã test thanh toán thất bại
- [ ] Đã test refund (MoMo)
- [ ] Đã implement payment logging
- [ ] Đã handle duplicate callbacks

---

## 📚 References

- **VNPay Documentation:** https://sandbox.vnpayment.vn/apis/
- **MoMo Documentation:** https://developers.momo.vn/
- **VNPay Sandbox:** https://sandbox.vnpayment.vn/
- **MoMo Test Environment:** https://test-payment.momo.vn/

---

✅ **Payment Integration Complete!**
