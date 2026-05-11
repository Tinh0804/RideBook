package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import me.myproject.BUSINESSLOGIC.DriverChuyenDiBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.AuthManager;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.MapUtil;
import me.myproject.Utilities.StompWebSocketClient;

public class NhanChuyenView extends FrameMain implements ActionListener {
    private TaiKhoan taiKhoan;
    private JPanel mainPanel, contentPanel, headerPanel;
    
    // UI Panels cho CardLayout
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private final String CARD_SEARCHING = "SEARCHING";
    private final String CARD_FOUND = "FOUND";
    
    // UI Elements
    private JLabel lblTitle, lblLocation;
    private JButton btnBack, btnRefresh;
    
    // Panel Trạng thái: FOUND (Có chuyến)
    private JLabel lblCountdown;
    private JLabel lblBookingInfo, lblCustomerInfo, lblPriceInfo, lblAddressInfo;
    private JButton btnAcceptRide, btnDeclineRide;
    
    // Timer & Data
    private String currentBookingId;
    private Timer countdownTimer;
    private int timeLeft = 30; // 30 giây đếm ngược
    private StompWebSocketClient webSocketClient;
    private JLabel mapLabel;
    
    public NhanChuyenView(TaiKhoan taiKhoan) {
        super("Nhận Chuyến - Ứng Dụng Đặt Xe");
        this.taiKhoan = taiKhoan;
        init();
        initWebSocket();
    }
    
    public NhanChuyenView() {
        this(null);
    }
    
    private void init() {
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(new Color(245, 245, 245));
        
        createHeader();
        createContent();
        
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    
    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.blueHeader);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 70));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Left
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);
        
        btnBack = new JButton();
        URL backUrl = getClass().getResource("/me/myproject/IMAGE/back.png");
        if(backUrl != null) btnBack.setIcon(new ImageIcon(backUrl));
        styleButton(btnBack, ColorMain.blueHeader, Color.WHITE);
        btnBack.addActionListener(this);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        lblTitle = new JLabel("NHẬN CHUYẾN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        
        lblLocation = new JLabel("Khu vực: Đang định vị...");
        lblLocation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLocation.setForeground(new Color(224, 224, 224));
        
        titlePanel.add(lblTitle);
        titlePanel.add(lblLocation);
        
        leftPanel.add(btnBack);
        leftPanel.add(titlePanel);
        
        // Right
        btnRefresh = new JButton("Tải lại");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        styleButton(btnRefresh, new Color(0, 150, 136), Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(90, 35));
        btnRefresh.addActionListener(this);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        this.add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createContent() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // --- 1. CARD PANEL (Trái) ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        cardPanel.add(createSearchingPanel(), CARD_SEARCHING);
        cardPanel.add(createRideFoundPanel(), CARD_FOUND);
        
        // --- 2. MAP PANEL (Phải) ---
        JPanel mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        JLabel lblMapTitle = new JLabel("Bản đồ tuyến đường", JLabel.CENTER);
        lblMapTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMapTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        mapPanel.add(lblMapTitle, BorderLayout.NORTH);
        
        mapLabel = new JLabel("Chưa có chuyến", JLabel.CENTER);
        mapPanel.add(mapLabel, BorderLayout.CENTER);
        
        // --- TOP LAYOUT ---
        JPanel topContainer = new JPanel(new GridLayout(1, 2, 15, 0));
        topContainer.setOpaque(false);
        topContainer.add(cardPanel);
        topContainer.add(mapPanel);
        
        mainPanel.add(topContainer, BorderLayout.CENTER);
        
        // --- BOTTOM LAYOUT (Stats + Tips) ---
        JPanel bottomContainer = new JPanel(new BorderLayout(15, 0));
        bottomContainer.setOpaque(false);
        bottomContainer.setPreferredSize(new Dimension(this.getWidth(), 150));
        bottomContainer.add(createStatsPanel(), BorderLayout.WEST);
        bottomContainer.add(createTipsPanel(), BorderLayout.CENTER);
        
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);
        this.add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSearchingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        panel.add(Box.createVerticalGlue());
        
        JLabel iconSearching = new JLabel(new ImageIcon(getClass().getResource("/me/myproject/IMAGE/back.png"))); // Thay icon radar vào đây
        iconSearching.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblStatus = new JLabel("Đang tìm kiếm chuyến đi...");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatus.setForeground(new Color(66, 66, 66));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSubStatus = new JLabel("Vui lòng giữ ứng dụng mở");
        lblSubStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubStatus.setForeground(new Color(158, 158, 158));
        lblSubStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(iconSearching);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblStatus);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblSubStatus);
        
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    
    private JPanel createRideFoundPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblAlert = new JLabel("🔥 CÓ CHUYẾN MỚI!");
        lblAlert.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAlert.setForeground(new Color(244, 67, 54));
        lblAlert.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblCountdown = new JLabel("Tự động từ chối sau: 30s");
        lblCountdown.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCountdown.setForeground(new Color(255, 152, 0));
        lblCountdown.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Thông tin cuốc xe"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        lblCustomerInfo = new JLabel("Khách hàng: ...");
        lblPriceInfo = new JLabel("Giá tiền: ...");
        lblPriceInfo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPriceInfo.setForeground(new Color(76, 175, 80));
        
        lblAddressInfo = new JLabel("<html><b>Đón:</b> ...<br><b>Đến:</b> ...</html>");
        lblBookingInfo = new JLabel("Mã chuyến: ...");
        
        infoPanel.add(lblCustomerInfo);
        infoPanel.add(lblPriceInfo);
        infoPanel.add(lblAddressInfo);
        infoPanel.add(lblBookingInfo);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        
        btnAcceptRide = new JButton("NHẬN CHUYẾN");
        styleButton(btnAcceptRide, new Color(76, 175, 80), Color.WHITE);
        btnAcceptRide.setPreferredSize(new Dimension(150, 45));
        btnAcceptRide.addActionListener(e -> acceptCurrentRide());
        
        btnDeclineRide = new JButton("TỪ CHỐI");
        styleButton(btnDeclineRide, new Color(158, 158, 158), Color.WHITE);
        btnDeclineRide.setPreferredSize(new Dimension(150, 45));
        btnDeclineRide.addActionListener(e -> declineCurrentRide("Bạn đã từ chối chuyến xe."));
        
        btnPanel.add(btnAcceptRide);
        btnPanel.add(btnDeclineRide);
        
        panel.add(lblAlert);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblCountdown);
        panel.add(Box.createVerticalStrut(20));
        panel.add(infoPanel);
        panel.add(Box.createVerticalGlue());
        panel.add(btnPanel);
        
        return panel;
    }
    
    // --- CÁC HÀM TẠO PANEL THỐNG KÊ (Giữ nguyên cấu trúc nhưng làm đẹp CSS) ---
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220,220,220), 1));
        panel.setPreferredSize(new Dimension(300, 150));
        JLabel lblTitle = new JLabel(" Thống kê hôm nay");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(lblTitle, BorderLayout.NORTH);
        // ... (Tuỳ chỉnh thêm giao diện Stats nếu cần)
        return panel;
    }
    private JPanel createTipsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220,220,220), 1));
        JLabel lblTitle = new JLabel(" Mẹo hoạt động");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(lblTitle, BorderLayout.NORTH);
        return panel;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ==========================================
    // LOGIC & WEBSOCKET & TIMER
    // ==========================================

    private void initWebSocket() {
        String driverId = resolveDriverId();
        if (driverId == null || driverId.isBlank()) return;
        
        String destination = "/topic/driver/" + driverId;
        webSocketClient = new StompWebSocketClient(destination, this::handleMessage);
        webSocketClient.connect(AppConfig.WS_URL);
    }

    public void handleMessage(String message) {
        System.out.println("[WS] Nhận tin nhắn: " + message);

        if (message != null && message.contains("NEW_RIDE:")) {
            String bookingId = message.replace("NEW_RIDE:", "").trim();
            this.currentBookingId = bookingId;
            
            // Gọi API tải dữ liệu chuyến
            new Thread(() -> loadBookingInfo(bookingId)).start();
            
        } else if (message != null && message.contains("CANCEL_RIDE:")) {
            // Khách hàng huỷ chuyến khi tài xế đang suy nghĩ
            String canceledId = message.replace("CANCEL_RIDE:", "").trim();
            if (currentBookingId != null && currentBookingId.equals(canceledId)) {
                SwingUtilities.invokeLater(() -> declineCurrentRide("Khách hàng đã hủy chuyến xe này!"));
            }
        }
    }

    private void loadBookingInfo(String bookingId) {
        try {
            DriverChuyenDiBSL bsl = new DriverChuyenDiBSL();
            Map<String, Object> response = bsl.layThongTinChuyen(bookingId);

            if (response != null && response.get("result") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = (Map<String, Object>) response.get("result");

                SwingUtilities.invokeLater(() -> {
                    // 1. Cập nhật UI
                    lblBookingInfo.setText("Mã chuyến: " + bookingId.substring(0, 8));
                    lblCustomerInfo.setText("Khách hàng: " + result.getOrDefault("customerPhone", "Ẩn danh"));
                    
                    Object priceObj = result.get("totalPrice");
                    double price = (priceObj instanceof Number) ? ((Number) priceObj).doubleValue() : 0.0;
                    lblPriceInfo.setText(String.format("Giá: %,.0f VNĐ", price));

                    String pickup = String.valueOf(result.getOrDefault("pickupLocation", "Không rõ"));
                    String dropoff = String.valueOf(result.getOrDefault("dropoffLocation", "Không rõ"));
                    lblAddressInfo.setText("<html><span style='color:#4CAF50;'>● Đón:</span> " + pickup + 
                                           "<br><span style='color:#F44336;'>● Đến:</span> " + dropoff + "</html>");

                    try {
                        mapLabel.setText("");
                        mapLabel.setIcon(new MapUtil(pickup, dropoff).getIcon());
                    } catch (Exception ex) {
                        mapLabel.setText("Không thể tải bản đồ");
                    }

                    // 2. Chuyển CardLayout sang màn hình Nhận chuyến
                    cardLayout.show(cardPanel, CARD_FOUND);
                    
                    // 3. Khởi động đếm ngược 30s
                    startCountdown();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        
        timeLeft = 30;
        lblCountdown.setText("Tự động từ chối sau: " + timeLeft + "s");
        
        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            lblCountdown.setText("Tự động từ chối sau: " + timeLeft + "s");
            
            if (timeLeft <= 0) {
                countdownTimer.stop();
                declineCurrentRide("Hết thời gian nhận chuyến. Hệ thống tự động bỏ qua.");
            }
        });
        countdownTimer.start();
    }

    private void acceptCurrentRide() {
        if (countdownTimer != null) countdownTimer.stop();
        if (currentBookingId == null) return;
        
        String driverId = resolveDriverId();
        btnAcceptRide.setText("Đang xử lý...");
        btnAcceptRide.setEnabled(false);

        new Thread(() -> {
            try {
                DriverChuyenDiBSL bsl = new DriverChuyenDiBSL();
                Map<String, Object> response = bsl.nhanChuyen(currentBookingId, driverId);
                Number status = (Number) response.get("status");
                
                SwingUtilities.invokeLater(() -> {
                    if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                        if (webSocketClient != null) webSocketClient.disconnect();
                        this.dispose();
                        new DangXuLyChuyenView(taiKhoan, currentBookingId);
                    } else {
                        JOptionPane.showMessageDialog(this, "Chuyến này đã bị hủy hoặc có người khác nhận.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        resetToSearchingState();
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Lỗi kết nối mạng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    resetToSearchingState();
                });
            }
        }).start();
    }

    private void declineCurrentRide(String reasonMessage) {
        if (countdownTimer != null) countdownTimer.stop();
        currentBookingId = null;
        
        JOptionPane.showMessageDialog(this, reasonMessage, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        
        // Bạn có thể quay về Trang Chủ hoặc đưa về trạng thái "Đang tìm kiếm"
        // Ở đây tuân theo yêu cầu: "tự huỷ bỏ màn hình" -> về trang chủ
        if (webSocketClient != null) webSocketClient.disconnect();
        this.dispose();
        new TrangChuDriverView(taiKhoan);
    }

    private void resetToSearchingState() {
        if (countdownTimer != null) countdownTimer.stop();
        currentBookingId = null;
        btnAcceptRide.setText("NHẬN CHUYẾN");
        btnAcceptRide.setEnabled(true);
        mapLabel.setIcon(null);
        mapLabel.setText("Chưa có chuyến");
        cardLayout.show(cardPanel, CARD_SEARCHING);
    }

    private String resolveDriverId() {
        if (taiKhoan != null && taiKhoan.getID_Ref() != null && !taiKhoan.getID_Ref().isBlank()) {
            return taiKhoan.getID_Ref();
        }
        TaiKhoan restored = AuthManager.tryRestoreSession();
        if (restored != null) return restored.getID_Ref();
        return null;
    }

    @Override
    public void dispose() {
        if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();
        if (webSocketClient != null) webSocketClient.disconnect();
        super.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            if (webSocketClient != null) webSocketClient.disconnect();
            this.dispose();
            new TrangChuDriverView(taiKhoan);
        } else if (e.getSource() == btnRefresh) {
            btnRefresh.setBackground(new Color(0, 120, 109));
            Timer colorTimer = new Timer(500, evt -> btnRefresh.setBackground(new Color(0, 150, 136)));
            colorTimer.setRepeats(false);
            colorTimer.start();
            resetToSearchingState();
        }
    }
}