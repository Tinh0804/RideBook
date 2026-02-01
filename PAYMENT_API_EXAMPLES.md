# 💳 Payment API Examples

## Quick Start Examples

### VNPay Payment Flow

#### 1. Create VNPay Payment
```bash
curl -X POST http://localhost:8080/RideBook/payments/vnpay/create \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "BOOKING123",
    "amount": 100000,
    "orderInfo": "Thanh toan chuyen xe BOOKING123",
    "locale": "vn"
  }'
```

**Response:**
```json
{
  "status": 201,
  "message": "Tạo thanh toán VNPay thành công",
  "result": {
    "status": "SUCCESS",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=10000000&vnp_Command=pay&...",
    "orderId": "BOOKING123_1706281234567",
    "amount": 100000,
    "paymentMethod": "VNPAY"
  }
}
```

#### 2. VNPay Callback Example
```
GET /payments/vnpay/callback?vnp_Amount=10000000&vnp_BankCode=NCB&vnp_ResponseCode=00&vnp_SecureHash=abc123...
```

---

### MoMo Payment Flow

#### 1. Create MoMo Payment
```bash
curl -X POST http://localhost:8080/RideBook/payments/momo/create \
  -H "Content-Type: application/json" \
  -d '{
    "bookingId": "BOOKING123",
    "amount": 100000,
    "orderInfo": "Thanh toan chuyen xe BOOKING123"
  }'
```

**Response:**
```json
{
  "status": 201,
  "message": "Tạo thanh toán MoMo thành công",
  "result": {
    "status": "SUCCESS",
    "paymentUrl": "https://test-payment.momo.vn/v2/gateway/pay?t=TU9NT1...",
    "orderId": "BOOKING123_1706281234567",
    "amount": 100000,
    "paymentMethod": "MOMO"
  }
}
```

#### 2. MoMo Callback Example
```bash
POST /payments/momo/callback
Content-Type: application/json

{
  "partnerCode": "MOMO",
  "orderId": "BOOKING123_1706281234567",
  "requestId": "1706281234567",
  "amount": 100000,
  "orderInfo": "Thanh toan chuyen xe BOOKING123",
  "resultCode": 0,
  "message": "Successful.",
  "payType": "qr",
  "transId": "2897483021",
  "signature": "abc123..."
}
```

---

## JavaScript/TypeScript Examples

### React Example

```typescript
// PaymentButton.tsx
import { useState } from 'react';

interface PaymentButtonProps {
  bookingId: string;
  amount: number;
  method: 'vnpay' | 'momo';
}

export function PaymentButton({ bookingId, amount, method }: PaymentButtonProps) {
  const [loading, setLoading] = useState(false);

  const handlePayment = async () => {
    setLoading(true);
    try {
      const response = await fetch(`/payments/${method}/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          bookingId,
          amount,
          orderInfo: `Thanh toan chuyen xe ${bookingId}`,
          returnUrl: `${window.location.origin}/payment-result`,
        }),
      });

      const data = await response.json();

      if (data.status === 201) {
        // Redirect to payment gateway
        window.location.href = data.result.paymentUrl;
      } else {
        alert('Lỗi tạo thanh toán: ' + data.message);
      }
    } catch (error) {
      console.error('Payment error:', error);
      alert('Lỗi kết nối');
    } finally {
      setLoading(false);
    }
  };

  return (
    <button onClick={handlePayment} disabled={loading}>
      {loading ? 'Đang xử lý...' : `Thanh toán qua ${method.toUpperCase()}`}
    </button>
  );
}
```

### Payment Result Page

```typescript
// PaymentResult.tsx
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';

export function PaymentResult() {
  const [searchParams] = useSearchParams();
  const [result, setResult] = useState<'success' | 'failed' | 'pending'>('pending');

  useEffect(() => {
    // Check VNPay result
    const vnpResponseCode = searchParams.get('vnp_ResponseCode');
    if (vnpResponseCode) {
      setResult(vnpResponseCode === '00' ? 'success' : 'failed');
      return;
    }

    // Check MoMo result
    const momoResultCode = searchParams.get('resultCode');
    if (momoResultCode) {
      setResult(momoResultCode === '0' ? 'success' : 'failed');
      return;
    }
  }, [searchParams]);

  if (result === 'pending') {
    return <div>Đang xử lý kết quả thanh toán...</div>;
  }

  if (result === 'success') {
    return (
      <div className="success">
        <h2>✅ Thanh toán thành công!</h2>
        <p>Cảm ơn bạn đã sử dụng dịch vụ.</p>
        <button onClick={() => window.location.href = '/bookings'}>
          Xem lịch sử đặt xe
        </button>
      </div>
    );
  }

  return (
    <div className="failed">
      <h2>❌ Thanh toán thất bại!</h2>
      <p>Vui lòng thử lại hoặc chọn phương thức thanh toán khác.</p>
      <button onClick={() => window.history.back()}>
        Quay lại
      </button>
    </div>
  );
}
```

---

## Flutter/Dart Example

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:url_launcher/url_launcher.dart';

class PaymentService {
  static const String baseUrl = 'http://localhost:8080/RideBook';

  Future<void> createVNPayPayment(String bookingId, int amount) async {
    final response = await http.post(
      Uri.parse('$baseUrl/payments/vnpay/create'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'bookingId': bookingId,
        'amount': amount,
        'orderInfo': 'Thanh toan chuyen xe $bookingId',
      }),
    );

    if (response.statusCode == 201) {
      final data = jsonDecode(response.body);
      final paymentUrl = data['result']['paymentUrl'];
      
      // Launch payment URL
      if (await canLaunch(paymentUrl)) {
        await launch(paymentUrl);
      }
    } else {
      throw Exception('Failed to create payment');
    }
  }

  Future<void> createMoMoPayment(String bookingId, int amount) async {
    final response = await http.post(
      Uri.parse('$baseUrl/payments/momo/create'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'bookingId': bookingId,
        'amount': amount,
        'orderInfo': 'Thanh toan chuyen xe $bookingId',
      }),
    );

    if (response.statusCode == 201) {
      final data = jsonDecode(response.body);
      final paymentUrl = data['result']['paymentUrl'];
      
      // Launch payment URL
      if (await canLaunch(paymentUrl)) {
        await launch(paymentUrl);
      }
    } else {
      throw Exception('Failed to create payment');
    }
  }
}

// Usage in Widget
class PaymentButton extends StatelessWidget {
  final String bookingId;
  final int amount;
  final String method; // 'vnpay' or 'momo'

  const PaymentButton({
    required this.bookingId,
    required this.amount,
    required this.method,
  });

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: () async {
        final service = PaymentService();
        try {
          if (method == 'vnpay') {
            await service.createVNPayPayment(bookingId, amount);
          } else {
            await service.createMoMoPayment(bookingId, amount);
          }
        } catch (e) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Lỗi: $e')),
          );
        }
      },
      child: Text('Thanh toán qua ${method.toUpperCase()}'),
    );
  }
}
```

---

## Postman Collection

### VNPay Create Payment
```json
{
  "name": "VNPay Create Payment",
  "request": {
    "method": "POST",
    "header": [
      {
        "key": "Content-Type",
        "value": "application/json"
      }
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n  \"bookingId\": \"BOOKING123\",\n  \"amount\": 100000,\n  \"orderInfo\": \"Thanh toan chuyen xe BOOKING123\",\n  \"locale\": \"vn\"\n}"
    },
    "url": {
      "raw": "http://localhost:8080/RideBook/payments/vnpay/create",
      "protocol": "http",
      "host": ["localhost"],
      "port": "8080",
      "path": ["RideBook", "payments", "vnpay", "create"]
    }
  }
}
```

### MoMo Create Payment
```json
{
  "name": "MoMo Create Payment",
  "request": {
    "method": "POST",
    "header": [
      {
        "key": "Content-Type",
        "value": "application/json"
      }
    ],
    "body": {
      "mode": "raw",
      "raw": "{\n  \"bookingId\": \"BOOKING123\",\n  \"amount\": 100000,\n  \"orderInfo\": \"Thanh toan chuyen xe BOOKING123\"\n}"
    },
    "url": {
      "raw": "http://localhost:8080/RideBook/payments/momo/create",
      "protocol": "http",
      "host": ["localhost"],
      "port": "8080",
      "path": ["RideBook", "payments", "momo", "create"]
    }
  }
}
```

---

## Python Example

```python
import requests

class PaymentService:
    BASE_URL = "http://localhost:8080/RideBook"
    
    @staticmethod
    def create_vnpay_payment(booking_id, amount, order_info):
        url = f"{PaymentService.BASE_URL}/payments/vnpay/create"
        payload = {
            "bookingId": booking_id,
            "amount": amount,
            "orderInfo": order_info,
            "locale": "vn"
        }
        
        response = requests.post(url, json=payload)
        
        if response.status_code == 201:
            data = response.json()
            return data['result']['paymentUrl']
        else:
            raise Exception(f"Payment creation failed: {response.text}")
    
    @staticmethod
    def create_momo_payment(booking_id, amount, order_info):
        url = f"{PaymentService.BASE_URL}/payments/momo/create"
        payload = {
            "bookingId": booking_id,
            "amount": amount,
            "orderInfo": order_info
        }
        
        response = requests.post(url, json=payload)
        
        if response.status_code == 201:
            data = response.json()
            return data['result']['paymentUrl']
        else:
            raise Exception(f"Payment creation failed: {response.text}")

# Usage
if __name__ == "__main__":
    service = PaymentService()
    
    # Create VNPay payment
    vnpay_url = service.create_vnpay_payment(
        booking_id="BOOKING123",
        amount=100000,
        order_info="Thanh toan chuyen xe BOOKING123"
    )
    print(f"VNPay URL: {vnpay_url}")
    
    # Create MoMo payment
    momo_url = service.create_momo_payment(
        booking_id="BOOKING123",
        amount=100000,
        order_info="Thanh toan chuyen xe BOOKING123"
    )
    print(f"MoMo URL: {momo_url}")
```

---

✅ **Ready to integrate!**
