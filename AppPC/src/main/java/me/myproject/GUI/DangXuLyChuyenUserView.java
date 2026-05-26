package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import me.myproject.BUSINESSLOGIC.DriverChuyenDiBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.AuthManager;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.Enum.BookingStatus;
import me.myproject.Utilities.MapUtil;
import me.myproject.Utilities.StompWebSocketClient;

public class DangXuLyChuyenUserView extends FrameMain {
    private TaiKhoan tk;
    private String bookingId;
    private String driverId; // Used for chat

    private JPanel headerPanel;
    private JLabel lblHeaderTitle;
    private JLabel lblDriverName, lblDriverPhone;
    private JLabel lblPickupAddress, lblDropoffAddress;
    private JLabel lblPrice;
    private JButton btnChat;
    private JPanel mapPanel;
    private JLabel mapLabel;

    private StompWebSocketClient webSocketClient;

    private final Color COLOR_ACCEPTED = new Color(33, 150, 243); // Blue
    private final Color COLOR_ARRIVED = new Color(255, 152, 0);   // Orange
    private final Color COLOR_IN_PROGRESS = new Color(244, 67, 54); // Red

    public DangXuLyChuyenUserView(TaiKhoan tk, String bookingId) {
        super("Hành trình của bạn");
        this.tk = tk;
        this.bookingId = bookingId;

        initUI();
        loadBookingDetails();
        initWebSocket();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(new Color(248, 249, 250));

        String fontName = "Segoe UI";

        // 1. HEADER
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_ACCEPTED);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 65));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        lblHeaderTitle = new JLabel("TÀI XẾ ĐANG ĐẾN");
        lblHeaderTitle.setFont(new Font(fontName, Font.BOLD, 18));
        lblHeaderTitle.setForeground(Color.WHITE);
        lblHeaderTitle.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(lblHeaderTitle, BorderLayout.CENTER);

        // 2. MAP
        mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        mapLabel = new JLabel("Đang tải bản đồ...", JLabel.CENTER);
        mapLabel.setFont(new Font(fontName, Font.ITALIC, 14));
        mapLabel.setForeground(Color.GRAY);
        mapPanel.add(mapLabel, BorderLayout.CENTER);

        // 3. BOTTOM INFO
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setBackground(Color.WHITE);
        bottomContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(224, 224, 224)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Driver Card
        JPanel driverCard = new JPanel(new BorderLayout());
        driverCard.setBackground(Color.WHITE);
        
        JPanel driverInfo = new JPanel();
        driverInfo.setLayout(new BoxLayout(driverInfo, BoxLayout.Y_AXIS));
        driverInfo.setBackground(Color.WHITE);
        
        lblDriverName = new JLabel("Đang tải dữ liệu...");
        lblDriverName.setFont(new Font(fontName, Font.BOLD, 17));
        lblDriverName.setForeground(new Color(33, 33, 33));
        
        lblDriverPhone = new JLabel("SĐT: ...");
        lblDriverPhone.setFont(new Font(fontName, Font.PLAIN, 14));
        lblDriverPhone.setForeground(new Color(117, 117, 117));
        
        driverInfo.add(lblDriverName);
        driverInfo.add(Box.createVerticalStrut(5));
        driverInfo.add(lblDriverPhone);

        btnChat = new JButton("💬 Chat với Tài xế");
        btnChat.setBackground(new Color(76, 175, 80));
        btnChat.setForeground(Color.WHITE);
        btnChat.setFocusPainted(false);
        btnChat.setBorderPainted(false);
        btnChat.setOpaque(true);
        btnChat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChat.setPreferredSize(new Dimension(160, 40));
        btnChat.addActionListener(e -> openChat());

        driverCard.add(driverInfo, BorderLayout.CENTER);
        driverCard.add(btnChat, BorderLayout.EAST);

        // Route Card
        JPanel routeCard = new JPanel(new GridLayout(2, 1, 0, 12));
        routeCard.setBackground(Color.WHITE);
        routeCard.setBorder(new EmptyBorder(20, 0, 20, 0));

        lblPickupAddress = new JLabel("<html><span style='color:#4CAF50;'>● Đón:</span> ...</html>");
        lblPickupAddress.setFont(new Font(fontName, Font.PLAIN, 15));
        
        lblDropoffAddress = new JLabel("<html><span style='color:#F44336;'>● Đến:</span> ...</html>");
        lblDropoffAddress.setFont(new Font(fontName, Font.PLAIN, 15));
        
        routeCard.add(lblPickupAddress);
        routeCard.add(lblDropoffAddress);

        // Price Card
        JPanel priceCard = new JPanel(new BorderLayout());
        priceCard.setBackground(Color.WHITE);
        JLabel lblPriceTitle = new JLabel("Tổng cước phí:");
        lblPriceTitle.setFont(new Font(fontName, Font.BOLD, 15));
        lblPriceTitle.setForeground(new Color(97, 97, 97));
        
        lblPrice = new JLabel("0 đ");
        lblPrice.setFont(new Font(fontName, Font.BOLD, 20));
        lblPrice.setForeground(new Color(0, 150, 136));
        
        priceCard.add(lblPriceTitle, BorderLayout.WEST);
        priceCard.add(lblPrice, BorderLayout.EAST);

        bottomContainer.add(driverCard);
        bottomContainer.add(routeCard);
        bottomContainer.add(priceCard);

        this.add(headerPanel, BorderLayout.NORTH);
        this.add(mapPanel, BorderLayout.CENTER);
        this.add(bottomContainer, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private void openChat() {
        if (driverId == null) {
            JOptionPane.showMessageDialog(this, "Chưa có thông tin Tài xế");
            return;
        }
        String myId = resolveCustomerId();
        ChatDialog chatDialog = new ChatDialog(this, bookingId, myId, driverId,false);
        chatDialog.setVisible(true);
    }

    private void loadBookingDetails() {
        new Thread(() -> {
            try {
                DriverChuyenDiBSL bsl = new DriverChuyenDiBSL();
                Map<String, Object> response = bsl.layThongTinChuyen(bookingId);

                if (response != null && response.get("result") != null) {
                    Map<String, Object> result = (Map<String, Object>) response.get("result");

                    SwingUtilities.invokeLater(() -> {
                        String name = result.getOrDefault("driverName", "Tài xế").toString();
                        String phone = result.getOrDefault("driverPhone", "Chưa cập nhật").toString();
                        driverId = result.getOrDefault("driverId", "").toString();
                        
                        String pickup = result.getOrDefault("pickupLocation", "Không rõ").toString();
                        String dropoff = result.getOrDefault("dropoffLocation", "Không rõ").toString();
                        
                        Object priceObj = result.get("totalPrice");
                        double price = (priceObj instanceof Number) ? ((Number) priceObj).doubleValue() : 0.0;

                        lblDriverName.setText(name.isEmpty() ? "Tài xế" : name);
                        lblDriverPhone.setText("SĐT: " + phone);
                        lblPickupAddress.setText("<html><span style='color:#4CAF50; font-size:16px;'>●</span> <b>Đón:</b> " + pickup + "</html>");
                        lblDropoffAddress.setText("<html><span style='color:#F44336; font-size:16px;'>●</span> <b>Đến:</b> " + dropoff + "</html>");
                        lblPrice.setText(String.format("%,.0f VNĐ", price));

                        try {
                            mapLabel.setText(""); 
                            mapLabel.setIcon(new MapUtil(pickup, dropoff).getIcon());
                        } catch (Exception mapEx) {
                            mapLabel.setText("Không thể tải bản đồ");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

   private void initWebSocket() {
        String customerId = resolveCustomerId();
        if (customerId == null || customerId.isBlank()) {
            System.out.println("[WS] Không có customerId, bỏ qua WebSocket");
            return;
        }

        // Đóng connection cũ nếu đang tồn tại (rất quan trọng)
        if (webSocketClient != null) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }

        String destination = "/topic/customer/" + customerId;
        
        System.out.println("[WS] DangXuLyChuyenUserView đang subscribe: " + destination);

        webSocketClient = new StompWebSocketClient(destination, this::handleMessage);
        webSocketClient.connect(AppConfig.WS_URL);
    }

    private void handleMessage(String message) {
        if (message == null || message.isBlank()) return;

        System.out.println("[WS User Nhận] " + message);

        // Cách tốt hơn: kiểm tra định dạng chung thay vì ghép bookingId cứng
        if (message.startsWith("STATUS_UPDATE:")) {
            String[] parts = message.split(":", 3);   // tách tối đa 3 phần

            if (parts.length >= 3) {
                String receivedBookingId = parts[1];
                String newStatus = parts[2];

                System.out.println("[WS] BookingId nhận được: " + receivedBookingId);
                System.out.println("[WS] Trạng thái mới: " + newStatus);

                // So sánh với bookingId hiện tại của khách hàng
                if (bookingId != null && bookingId.equals(receivedBookingId)) {
                    SwingUtilities.invokeLater(() -> updateUIStatus(newStatus));
                } else {
                    System.out.println("[WS] Bỏ qua vì bookingId không khớp: " + receivedBookingId);
                }
            }
        }
    }

   private void updateUIStatus(String status) {
        // Cẩn thận loại bỏ khoảng trắng hoặc dấu ngoặc kép thừa nếu JSON/WebSocket gửi nhầm
        String cleanStatus = status.trim().replace("\"", ""); 
        
        // Sử dụng .name() để chuyển Enum thành String trước khi so sánh
        if (BookingStatus.ARRIVED.name().equals(cleanStatus)) {
            headerPanel.setBackground(COLOR_ARRIVED);
            lblHeaderTitle.setText("TÀI XẾ ĐÃ ĐẾN NƠI");
        } else if (BookingStatus.IN_PROGRESS.name().equals(cleanStatus)) {
            headerPanel.setBackground(COLOR_IN_PROGRESS);
            lblHeaderTitle.setText("ĐANG DI CHUYỂN");
        } else if (BookingStatus.COMPLETED.name().equals(cleanStatus)) {
            JOptionPane.showMessageDialog(this, "Chuyến đi đã hoàn thành! Cảm ơn bạn.");
            dispose();
            new TrangChuUserView(tk);
        }
        
        // Cập nhật lại giao diện ngay lập tức
        this.revalidate();
        this.repaint();
    }

    private String resolveCustomerId() {
        if (tk != null && tk.getID_Ref() != null && !tk.getID_Ref().isBlank()) {
            return tk.getID_Ref();
        }
        TaiKhoan restored = AuthManager.tryRestoreSession();
        if (restored != null && restored.getID_Ref() != null && !restored.getID_Ref().isBlank()) {
            return restored.getID_Ref();
        }
        return null;
    }

    @Override
    public void dispose() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
            webSocketClient = null;
        }
        super.dispose();
    }
}
