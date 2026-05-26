package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.net.URI;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import me.myproject.BUSINESSLOGIC.WalletBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.Enum.PaymentMethod;
import me.myproject.Utilities.StompWebSocketClient;

public class ViTienTaiXeView extends FrameMain {
    private TaiKhoan tk;
    private JLabel lblBalance;
    private JButton btnDeposit, btnWithdraw,btnHistory;
    private WalletBSL walletBSL;
    private String currentWalletId = null;
    
    // Thêm client WebSocket
    private StompWebSocketClient webSocketClient;

    public ViTienTaiXeView(TaiKhoan tk) {
        super("Ví Tài Xế");
        this.tk = tk;
        this.walletBSL = new WalletBSL();
        initUI();
        loadWalletData();
        
        // Khởi tạo WebSocket lắng nghe kết quả nạp tiền
        initWebSocket();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(new Color(245, 245, 245));

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.blueHeader);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 60));
        
        JLabel lblTitle = new JLabel("QUẢN LÝ VÍ & THU NHẬP", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        JButton btnBack = new JButton("  Thoát");

        btnBack.setFont(new Font("Arial", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(ColorMain.blueHeader);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBack.addActionListener(e -> {
            this.dispose(); // Sẽ tự động gọi hàm dispose() được override bên dưới
        });

        headerPanel.add(btnBack, BorderLayout.WEST);

        // 2. CENTER - CARD SỐ DƯ
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245, 245, 245));
        
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(30, 50, 30, 50)
        ));

        JLabel lblBalanceTitle = new JLabel("Số dư hiện tại");
        lblBalanceTitle.setFont(new Font("Arial", Font.PLAIN, 16));
        lblBalanceTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBalanceTitle.setForeground(Color.GRAY);

        lblBalance = new JLabel("Đang tải...");
        lblBalance.setFont(new Font("Arial", Font.BOLD, 36));
        lblBalance.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBalance.setForeground(new Color(46, 204, 113));

        cardPanel.add(lblBalanceTitle);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblBalance);
        
        centerPanel.add(cardPanel);

        // 3. BOTTOM - NÚT CHỨC NĂNG
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        bottomPanel.setBackground(new Color(245, 245, 245));

        btnDeposit = new JButton("NẠP TIỀN");
        btnDeposit.setFont(new Font("Arial", Font.BOLD, 14));
        btnDeposit.setBackground(new Color(52, 152, 219));
        btnDeposit.setForeground(Color.BLACK); // Đổi sang chữ trắng cho dễ nhìn
        btnDeposit.setPreferredSize(new Dimension(180, 45));
        btnDeposit.setFocusPainted(false);
        btnDeposit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeposit.addActionListener(e -> handleDeposit());
        btnDeposit.setEnabled(false);

        btnHistory = new JButton("LỊCH SỬ GIAO DỊCH");
        btnHistory.setFont(new Font("Arial", Font.BOLD, 14));
        btnHistory.setBackground(new Color(155, 89, 182));
        btnHistory.setForeground(Color.BLACK);
        btnHistory.setPreferredSize(new Dimension(180, 45));
        btnHistory.setFocusPainted(false);
        btnHistory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHistory.addActionListener(e -> handleTransactionHistory());
        btnHistory.setEnabled(false);

        btnWithdraw = new JButton("RÚT TIỀN");
        btnWithdraw.setFont(new Font("Arial", Font.BOLD, 14));
        btnWithdraw.setBackground(new Color(231, 76, 60));
        btnWithdraw.setForeground(Color.BLACK); // Đổi sang chữ trắng
        btnWithdraw.setPreferredSize(new Dimension(180, 45));
        btnWithdraw.setFocusPainted(false);
        btnWithdraw.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnWithdraw.addActionListener(e -> handleWithdraw());
        btnWithdraw.setEnabled(false);

        

        bottomPanel.add(btnDeposit);
        bottomPanel.add(btnWithdraw);
        bottomPanel.add(btnHistory);

        // Lắp ráp
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        // Hiển thị frame
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    // ==========================================
    // LOGIC WEBSOCKET (MỚI THÊM)
    // ==========================================
    private void initWebSocket() {
        String driverId = tk.getID_Ref(); 
        if (driverId == null || driverId.isBlank()) return;

        String destination = "/topic/driver/" + driverId;
        System.out.println("[Ví Tài Xế WS] Subscribing to " + destination);
        
        webSocketClient = new StompWebSocketClient(destination, this::handleWebSocketMessage);
        webSocketClient.connect(AppConfig.WS_URL);
    }

    private void handleWebSocketMessage(String message) {
        if (message == null || message.isBlank()) return;

        System.out.println("[Ví Tài Xế WS] Nhận tin nhắn: " + message);

        // Xử lý khi có tin nhắn nạp tiền thành công
        if (message.startsWith("TOPUP_SUCCESS:")) {
            SwingUtilities.invokeLater(() -> {
                // Lấy lại dữ liệu ví mới nhất từ Database
                loadWalletData();
                JOptionPane.showMessageDialog(this, "Thanh toán thành công! Số dư của bạn đã được cập nhật.", "Nạp Tiền Thành Công", JOptionPane.INFORMATION_MESSAGE);
            });
        } 
        // Xử lý khi giao dịch thất bại hoặc bị hủy
        else if (message.startsWith("TOPUP_FAILED:")) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Giao dịch nạp tiền đã bị hủy hoặc thất bại.", "Nạp Tiền Thất Bại", JOptionPane.WARNING_MESSAGE);
            });
        }
    }

    @Override
    public void dispose() {
        // Đóng kết nối WebSocket khi tắt màn hình ví để tránh rò rỉ bộ nhớ
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        super.dispose();
    }
    // ==========================================

    private void loadWalletData() {
        new Thread(() -> {
            Map<String, Object> response = walletBSL.getWalletInfo();

            SwingUtilities.invokeLater(() -> {
                try {
                    if (response != null && response.get("status").toString().startsWith("200") 
                        && response.containsKey("result")) {
                        
                        Map<String, Object> result = (Map<String, Object>) response.get("result");
                        if (result.containsKey("walletId")) {
                            this.currentWalletId = result.get("walletId").toString();
                            btnDeposit.setEnabled(true);
                            btnWithdraw.setEnabled(true);
                            btnHistory.setEnabled(true);
                        }

                        if (result != null && result.containsKey("balance")) {
                            Double balance = Double.valueOf(result.get("balance").toString());
                            lblBalance.setText(String.format("%,.0f VNĐ", balance));
                        }
                    } else {
                        lblBalance.setText("0 VNĐ");
                        String msg = (response != null && response.containsKey("message")) 
                                    ? response.get("message").toString() 
                                    : "Không thể kết nối đến máy chủ!";
                        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    lblBalance.setText("Lỗi định dạng");
                }
                
                this.revalidate();
                this.repaint();
            });
        }).start();
    }

    private void handleDeposit() {
        if (this.currentWalletId == null) {
            JOptionPane.showMessageDialog(this, 
                "Dữ liệu ví chưa tải xong, vui lòng đợi!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            loadWalletData();
            return;
        }

        String[] options = {"MoMo", "VNPAY"};
        String paymentMethod = (String) JOptionPane.showInputDialog(
                this,
                "Chọn phương thức thanh toán:",
                "Phương thức",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (paymentMethod == null) return;

        String input = JOptionPane.showInputDialog(
                this, 
                "Nhập số tiền muốn nạp (VNĐ):", 
                "Nạp Tiền", 
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null || input.trim().isEmpty()) return;

        try {
            Double amount = Double.parseDouble(input.trim());
            if (amount <= 0) throw new NumberFormatException();

            btnDeposit.setEnabled(false);
            btnDeposit.setText("Đang xử lý...");

            new Thread(() -> {
                Map<String, Object> response;

                if (paymentMethod.equals("MoMo")) {
                    response = walletBSL.deposit(this.currentWalletId, amount, PaymentMethod.MOMO);
                } else {
                    response = walletBSL.deposit(this.currentWalletId, amount, PaymentMethod.VNPAY);
                }

                SwingUtilities.invokeLater(() -> {
                    btnDeposit.setEnabled(true);
                    btnDeposit.setText("NẠP TIỀN");

                    try {
                        if (response != null && response.get("status").toString().startsWith("200")) {
                            Map<String, Object> resultObj = (Map<String, Object>) response.get("result");
                            if (resultObj != null && resultObj.containsKey("paymentUrl")) {
                                String url = resultObj.get("paymentUrl").toString();
                                openWebBrowser(url);

                                JOptionPane.showMessageDialog(this,
                                        "Đã mở trình duyệt web. Vui lòng hoàn tất thanh toán trên trình duyệt, ứng dụng sẽ tự động cập nhật số dư.",
                                        "Thanh toán",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "Không tìm thấy link thanh toán!",
                                        "Lỗi",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            String msg = (response != null && response.containsKey("message"))
                                    ? response.get("message").toString()
                                    : "Lỗi hệ thống";
                            JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                                "Lỗi xử lý dữ liệu: " + e.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });

            }).start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Số tiền không hợp lệ!",
                    "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleWithdraw() {
        String input = JOptionPane.showInputDialog(this, "Nhập số tiền muốn rút về Ngân hàng (VNĐ):", "Rút Tiền", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            Double amount = Double.parseDouble(input.trim());
            if (amount <= 0) throw new NumberFormatException();

            btnWithdraw.setEnabled(false);
            btnWithdraw.setText("Đang xử lý...");

            new Thread(() -> {
                Map<String, Object> response = walletBSL.withdraw(this.currentWalletId, amount);
                SwingUtilities.invokeLater(() -> {
                    btnWithdraw.setEnabled(true);
                    btnWithdraw.setText("RÚT TIỀN");

                    if (response != null && response.get("status").toString().equals("200")) {
                        JOptionPane.showMessageDialog(this, "Rút tiền thành công! Tiền sẽ được chuyển về thẻ ngân hàng.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        loadWalletData(); 
                    } else {
                        String msg = response != null ? (String) response.get("message") : "Lỗi hệ thống";
                        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openWebBrowser(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng copy link sau vào trình duyệt để thanh toán:\n" + url);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleTransactionHistory() {
       if (this.currentWalletId == null) {
            JOptionPane.showMessageDialog(this, 
                "Dữ liệu ví chưa tải xong, vui lòng đợi!", 
                "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Mở View Lịch sử giao dịch và truyền ID ví sang
        new LichSuGiaoDichView(this.currentWalletId).setVisible(true);
    }
}