import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 1. ĐÃ ĐỔI TÊN METRIC SANG "stomp_*" ĐỂ KHÔNG TRÙNG VỚI HỆ THỐNG K6
const stompErrors = new Rate('stomp_errors');
const stompSessionDuration = new Trend('stomp_session_duration');
const messagesReceived = new Counter('stomp_messages_received');

export const options = {
  scenarios: {
    // KỊCH BẢN: 150 User kết nối WebSocket và duy trì liên tục trong 45s
    stomp_realtime_test: {
      executor: 'ramping-vus',
      startVUs: 1,
      stages: [
        { duration: '10s', target: 50 },  // Mở dần 50 kết nối TCP duy trì
        { duration: '10s', target: 300 }, // Đẩy lên 150 kết nối đồng thời
        { duration: '20s', target: 300 }, // Giữ 150 kết nối liên tục, vừa nghe vừa phát
        { duration: '5s', target: 0 },    // Ngắt kết nối êm ái
      ],
      exec: 'testStompWebSocket',
    },
  },
  thresholds: {
    'stomp_errors': ['rate<0.01'], // Tỷ lệ rớt kết nối STOMP phải dưới 1%
  },
};

// Đảm bảo URL này khớp đúng với cấu hình Registry trong WebSocketConfig của bạn
const WS_URL = 'ws://localhost:8080/RideBook/ws/websocket';

export function testStompWebSocket() {
  const startTime = Date.now();

  const res = ws.connect(WS_URL, null, function (socket) {
    socket.on('open', () => {
      // BƯỚC 1: Gửi frame STOMP CONNECT để bắt tay với Spring Boot WebSocket Broker
      const connectFrame = "CONNECT\naccept-version:1.1,1.2\nhost:localhost\n\n\x00";
      socket.send(connectFrame);
    });

    socket.on('message', (data) => {
      // BƯỚC 2: Nhận frame phản hồi từ Server
      if (data.indexOf("CONNECTED") === 0) {
        // Đã kết nối STOMP thành công -> SUBSCRIBE vào topic thông báo
        const subscribeFrame = "SUBSCRIBE\nid:sub-0\ndestination:/topic/websocket_notifications\n\n\x00";
        socket.send(subscribeFrame);

        // BƯỚC 3: Giả lập gửi tin nhắn định kỳ (Tài xế gửi vị trí GPS mỗi 2 giây)
        const sendInterval = setInterval(() => {
          const payload = JSON.stringify({
            lat: 16.06437 + (Math.random() * 0.001),
            lng: 108.22052 + (Math.random() * 0.001),
            status: "MOVING"
          });
          // Gửi frame SEND vào endpoint trên Server
          const sendFrame = `SEND\ndestination:/app/driver/location\ncontent-type:application/json\n\n${payload}\x00`;
          socket.send(sendFrame);
        }, 2000);

        // Duy trì phiên làm việc trong 15 giây rồi ngắt
        socket.setTimeout(() => {
          clearInterval(sendInterval);
          const disconnectFrame = "DISCONNECT\n\n\x00";
          socket.send(disconnectFrame);
          socket.close();
        }, 15000);
      } else if (data.indexOf("MESSAGE") === 0) {
        // Nhận được tin nhắn broadcast từ server
        messagesReceived.add(1);
      }
    });

    socket.on('error', (e) => {
      if (e.error() != "websocket: close sent") {
        console.error('WebSocket Error: ', e.error());
        stompErrors.add(1);
      }
    });

    socket.on('close', () => {
      stompSessionDuration.add(Date.now() - startTime);
    });
  });

  const is101 = check(res, {
    '✔ WebSocket handshake status là 101 Switching Protocols': (r) => r && r.status === 101,
  });

  if (!is101 && res) {
    console.error(`[HANDSHAKE THẤT BẠI] Status: ${res.status} | URL: ${WS_URL} | Lỗi: ${res.error || 'N/A'}`);
  }

  sleep(1);
}