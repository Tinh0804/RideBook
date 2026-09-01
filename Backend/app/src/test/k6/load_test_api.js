
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  // Tăng tải êm ái để Tomcat làm nóng Thread Pool, không bị sốc tải 0 giây
  scenarios: {
    cv_benchmark_600_rps: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '10s', target: 50 },  // Warm-up: Làm nóng kết nối TCP
        { duration: '10s', target: 150 }, // Đẩy tải lên 150 VUs
        { duration: '20s', target: 150 }, // Giữ tải đỉnh trong 20s để lấy số liệu RPS
        { duration: '5s', target: 0 },
      ],
      exec: 'estimatePriceTest',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'], // Lỗi phải < 1%
  },
};

const BASE_URL = 'http://localhost:8080/RideBook';

export function estimatePriceTest() {
  const payload = JSON.stringify({
    pickupLocation: "12 Hà Huy Tập, Thanh Khê, Đà Nẵng",
    dropoffLocation: "Đại học Sư Phạm Kỹ Thuật, Đại học Đà Nẵng",
    vehicleTypeId: "d4e5f6a7-b8c9-0123-4567-890abcdef014",
    pickupLat: 16.06437,
    pickupLng: 108.22052,
    dropoffLat: 16.07619,
    dropoffLng: 108.15609
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      // ÉP TÁI SỬ DỤNG KẾT NỐI TCP ĐỂ TRÁNH NGHẼN PORT Ở LOCALHOST
      'Connection': 'keep-alive',
    },
  };

  const res = http.post(`${BASE_URL}/bookings/estimate-price`, payload, params);

  check(res, {
    'status là 200': (r) => r.status === 200,
  });
  // if (res.status !== 200) {
  //   console.error(`[LỖI PHÁT HIỆN] Status: ${res.status} | Body: ${res.body.slice(0, 150)}`);
  // }

  // Nghỉ 10ms (0.01s) - Nhịp tối ưu cho Keep-Alive TCP
  sleep(0.01); 
}