package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.AuthManager;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.StompWebSocketClient;

public class ChoTaiXeView extends FrameMain {
    private final TaiKhoan taiKhoan;
    private StompWebSocketClient webSocketClient;
    private JLabel statusLabel;
    private JLabel driverLabel;
    private FrameMain viewPrevious;

    public ChoTaiXeView(TaiKhoan taiKhoan,FrameMain viewPrevious) {
        super("Đang tìm tài xế");
        this.taiKhoan = taiKhoan;
        this.viewPrevious = viewPrevious;
        init();
        initWebSocket();
    }

    private void init() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        statusLabel = new JLabel("Đang tìm tài xế phù hợp...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(statusLabel, BorderLayout.CENTER);

        driverLabel = new JLabel("", SwingConstants.CENTER);
        driverLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        panel.add(driverLabel, BorderLayout.NORTH);

        JPanel dotPanel = new JPanel();
        dotPanel.setBackground(Color.WHITE);
        JLabel dots = new JLabel("● ● ●");
        dots.setFont(new Font("Arial", Font.BOLD, 16));
        dots.setForeground(new Color(0, 160, 160));
        dotPanel.add(dots);
        panel.add(dotPanel, BorderLayout.WEST);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);

        btnClose.addActionListener(e -> {
            if (webSocketClient != null) webSocketClient.disconnect();
            this.viewPrevious.setVisible(true); // Hiển thị lại View trước đó khi đóng ChoTaiXeView
            dispose();
        });
    }

    private void initWebSocket() {
        String customerId = resolveCustomerId();
        if (customerId == null || customerId.isBlank()) {
            return;
        }
        String destination = "/topic/customer/" + customerId;
        webSocketClient = new StompWebSocketClient(destination, this::handleMessage);
        webSocketClient.connect(AppConfig.WS_URL);
    }

    private void handleMessage(String message) {
        System.out.println("[CLIENT WS] Nhận tin nhắn: " + message);
        if (message == null || message.isBlank()) return;

        // 1. Trường hợp không tìm thấy tài xế
        if (message.contains("NO_DRIVER_FOUND")) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Rất tiếc, không có tài xế nào gần đây");
                statusLabel.setForeground(Color.RED);
            });
            return;
        }

        // 2. Trường hợp đã có tài xế nhận chuyến
        if (message.contains("DRIVER_ASSIGNED")) {
            try {
                // Giả sử format: DRIVER_ASSIGNED:bookingId:driverName:phone
                String[] parts = message.split(":");
                if (parts.length >= 4) {
                    String driverName = parts[2].trim();
                    String driverPhone = parts[3].trim();

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("ĐÃ TÌM THẤY TÀI XẾ!");
                        statusLabel.setForeground(new Color(0, 128, 0)); // Màu xanh lá
                        driverLabel.setText("<html><center>Tài xế: <b>" + driverName + "</b><br>SĐT: " + driverPhone + "</center></html>");
                        
                        // Vẽ lại giao diện
                        revalidate();
                        repaint();
                        
                        // Transition to Active Trip View
                        if (webSocketClient != null) webSocketClient.disconnect();
                        dispose();
                        String bookingId = parts[1].trim();
                        new DangXuLyChuyenUserView(taiKhoan, bookingId);
                    });
                }
            } catch (Exception e) {
                System.err.println("Lỗi parse tin nhắn tài xế: " + e.getMessage());
            }
        }
    }

    private String resolveCustomerId() {
        if (taiKhoan != null && taiKhoan.getID_Ref() != null && !taiKhoan.getID_Ref().isBlank()) {
            return taiKhoan.getID_Ref();
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
            System.out.println("[WS] Đang đóng kết nối khi thoát View...");
            webSocketClient.disconnect();
        }
        super.dispose();
    }
}
