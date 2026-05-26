package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import me.myproject.BUSINESSLOGIC.DriverChuyenDiBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.Enum.TripState;
import me.myproject.Utilities.MapUtil;

public class DangXuLyChuyenView extends FrameMain {
    private TaiKhoan tk;
    private String bookingId;
    
    // UI Components
    private JPanel headerPanel;
    private JLabel lblHeaderTitle;
    private JLabel lblCustomerName, lblCustomerPhone;
    private JLabel lblPickupAddress, lblDropoffAddress;
    private JLabel lblPrice;
    private JLabel lblPaymentStatus; // Label mới hiển thị trạng thái thanh toán
    private JButton btnAction, btnCall, btnChat;
    private JPanel mapPanel;
    private JLabel mapLabel;
    
    private String customerId;

    private TripState currentState = TripState.ACCEPTED;

    // Định nghĩa bảng màu cho từng trạng thái
    private final Color COLOR_ACCEPTED = new Color(33, 150, 243); // Xanh dương
    private final Color COLOR_ARRIVED = new Color(255, 152, 0);   // Cam
    private final Color COLOR_IN_PROGRESS = new Color(244, 67, 54); // Đỏ
    private final Color COLOR_PROCESSING = new Color(158, 158, 158); // Xám

    public DangXuLyChuyenView(TaiKhoan tk, String bookingId) {
        super("Đang Thực Hiện Chuyến Xe");
        this.tk = tk;
        this.bookingId = bookingId;
        
        initUI();
        loadBookingDetails();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(new Color(248, 249, 250)); // Màu nền sáng, mịn hơn

        String fontName = "Segoe UI"; // Font hiện đại

        // 1. HEADER
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_ACCEPTED);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 65));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        lblHeaderTitle = new JLabel("ĐANG ĐI ĐÓN KHÁCH");
        lblHeaderTitle.setFont(new Font(fontName, Font.BOLD, 18));
        lblHeaderTitle.setForeground(Color.WHITE);
        lblHeaderTitle.setHorizontalAlignment(JLabel.CENTER);
        headerPanel.add(lblHeaderTitle, BorderLayout.CENTER);

        // 2. BẢN ĐỒ (Center)
        mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        mapLabel = new JLabel("Đang tải bản đồ...", JLabel.CENTER);
        mapLabel.setFont(new Font(fontName, Font.ITALIC, 14));
        mapLabel.setForeground(Color.GRAY);
        mapPanel.add(mapLabel, BorderLayout.CENTER);

        // 3. THÔNG TIN KHÁCH HÀNG & CHUYẾN ĐI (Bottom)
        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.Y_AXIS));
        bottomContainer.setBackground(Color.WHITE);
        bottomContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(224, 224, 224)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // 3.1 Card Thông tin Khách hàng
        JPanel customerCard = new JPanel(new BorderLayout());
        customerCard.setBackground(Color.WHITE);
        
        JPanel customerInfo = new JPanel();
        customerInfo.setLayout(new BoxLayout(customerInfo, BoxLayout.Y_AXIS));
        customerInfo.setBackground(Color.WHITE);
        
        lblCustomerName = new JLabel("Đang tải dữ liệu...");
        lblCustomerName.setFont(new Font(fontName, Font.BOLD, 17));
        lblCustomerName.setForeground(new Color(33, 33, 33));
        
        lblCustomerPhone = new JLabel("SĐT: ...");
        lblCustomerPhone.setFont(new Font(fontName, Font.PLAIN, 14));
        lblCustomerPhone.setForeground(new Color(117, 117, 117));
        
        customerInfo.add(lblCustomerName);
        customerInfo.add(Box.createVerticalStrut(5));
        customerInfo.add(lblCustomerPhone);

        btnCall = new JButton("📞 Gọi");
        styleButton(btnCall, new Color(76, 175, 80), Color.WHITE);
        btnCall.setPreferredSize(new Dimension(120, 40));

        btnChat = new JButton("💬  Chat");
        styleButton(btnChat, new Color(0, 150, 136), Color.WHITE);
        btnChat.setPreferredSize(new Dimension(120, 40));
        btnChat.addActionListener(e -> openChat());

        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(btnCall);
        actionPanel.add(btnChat);

        customerCard.add(customerInfo, BorderLayout.CENTER);
        customerCard.add(actionPanel, BorderLayout.EAST);

        // 3.2 Card Lộ trình
        JPanel routeCard = new JPanel(new GridLayout(2, 1, 0, 12));
        routeCard.setBackground(Color.WHITE);
        routeCard.setBorder(new EmptyBorder(20, 0, 20, 0));

        lblPickupAddress = new JLabel("<html><span style='color:#4CAF50;'>● Đón:</span> ...</html>");
        lblPickupAddress.setFont(new Font(fontName, Font.PLAIN, 15));
        
        lblDropoffAddress = new JLabel("<html><span style='color:#F44336;'>● Đến:</span> ...</html>");
        lblDropoffAddress.setFont(new Font(fontName, Font.PLAIN, 15));
        
        routeCard.add(lblPickupAddress);
        routeCard.add(lblDropoffAddress);

        // 3.3 Card Giá tiền & Trạng thái thanh toán
        JPanel priceCard = new JPanel(new BorderLayout());
        priceCard.setBackground(Color.WHITE);
        
        JLabel lblPriceTitle = new JLabel("Tổng cước phí:");
        lblPriceTitle.setFont(new Font(fontName, Font.BOLD, 15));
        lblPriceTitle.setForeground(new Color(97, 97, 97));
        
        // Tạo Panel phụ chứa Giá tiền và Trạng thái thanh toán nằm phía bên phải
        JPanel rightPricePanel = new JPanel();
        rightPricePanel.setLayout(new BoxLayout(rightPricePanel, BoxLayout.Y_AXIS));
        rightPricePanel.setBackground(Color.WHITE);
        
        lblPrice = new JLabel("0 đ");
        lblPrice.setFont(new Font(fontName, Font.BOLD, 20));
        lblPrice.setForeground(new Color(0, 150, 136));
        lblPrice.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        lblPaymentStatus = new JLabel("Đang kiểm tra...");
        lblPaymentStatus.setFont(new Font(fontName, Font.BOLD, 13));
        lblPaymentStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        rightPricePanel.add(lblPrice);
        rightPricePanel.add(Box.createVerticalStrut(3));
        rightPricePanel.add(lblPaymentStatus);
        
        priceCard.add(lblPriceTitle, BorderLayout.WEST);
        priceCard.add(rightPricePanel, BorderLayout.EAST);

        // 3.4 Nút Hành Động (Trạng thái)
        btnAction = new JButton("ĐÃ ĐẾN ĐIỂM ĐÓN");
        btnAction.setFont(new Font(fontName, Font.BOLD, 16));
        styleButton(btnAction, COLOR_ACCEPTED, Color.WHITE);
        btnAction.setPreferredSize(new Dimension(this.getWidth(), 55));
        btnAction.addActionListener(e -> processNextState());

        // Gộp vào Bottom Container
        bottomContainer.add(customerCard);
        bottomContainer.add(routeCard);
        bottomContainer.add(priceCard);
        bottomContainer.add(Box.createVerticalStrut(20));
        bottomContainer.add(btnAction);

        // Lắp ráp Layout
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(mapPanel, BorderLayout.CENTER);
        this.add(bottomContainer, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void openChat() {
        if (customerId == null) {
            JOptionPane.showMessageDialog(this, "Chưa xác định được Khách hàng", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String myId = tk.getID_Ref();
        ChatDialog chatDialog = new ChatDialog(this, bookingId, myId, customerId,true);
        chatDialog.setVisible(true);
    }

    private void loadBookingDetails() {
        new Thread(() -> {
            try {
                DriverChuyenDiBSL bsl = new DriverChuyenDiBSL();
                Map<String, Object> response = bsl.layThongTinChuyen(bookingId);

                if (response != null && response.get("result") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.get("result");

                    SwingUtilities.invokeLater(() -> {
                        String name = result.getOrDefault("customerName", "Khách hàng mới").toString();
                        String phone = result.getOrDefault("customerPhone", "Chưa cập nhật").toString();
                        String pickup = result.getOrDefault("pickupLocation", "Không rõ").toString();
                        String dropoff = result.getOrDefault("dropoffLocation", "Không rõ").toString();
                        customerId = result.getOrDefault("customerId", "").toString();
                        
                        Object priceObj = result.get("totalPrice");
                        double price = (priceObj instanceof Number) ? ((Number) priceObj).doubleValue() : 0.0;
                        
                        // Xử lý hiển thị thanh toán
                        String isPaidStr = result.getOrDefault("paymentStatus", "false").toString();
                        boolean isPaid = isPaidStr.equalsIgnoreCase("true") || 
                                         isPaidStr.equalsIgnoreCase("SUCCESS") || 
                                         isPaidStr.equalsIgnoreCase("PAID");

                        lblCustomerName.setText(name.isEmpty() ? "Khách hàng" : name);
                        lblCustomerPhone.setText("SĐT: " + phone);
                        lblPickupAddress.setText("<html><span style='color:#4CAF50; font-size:16px;'>●</span> <b>Đón:</b> " + pickup + "</html>");
                        lblDropoffAddress.setText("<html><span style='color:#F44336; font-size:16px;'>●</span> <b>Đến:</b> " + dropoff + "</html>");
                        
                        if (isPaid) {
                            lblPrice.setText(String.format("%,.0f VNĐ", price));
                            lblPaymentStatus.setText("✔ Đã thanh toán (KHÔNG THU TIỀN)");
                            lblPaymentStatus.setForeground(new Color(76, 175, 80)); // Màu xanh lá
                        } else {
                            lblPrice.setText(String.format("%,.0f VNĐ", price));
                            lblPaymentStatus.setText("⚠ THU TIỀN MẶT");
                            lblPaymentStatus.setForeground(new Color(244, 67, 54)); // Màu đỏ
                        }

                        try {
                            mapLabel.setText(""); 
                            mapLabel.setIcon(new MapUtil(pickup, dropoff).getIcon());
                        } catch (Exception mapEx) {
                            mapLabel.setText("Không thể tải bản đồ");
                        }

                        this.revalidate();
                        this.repaint();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void processNextState() {
        Color startColor = btnAction.getBackground();
        btnAction.setEnabled(false);
        btnAction.setText("Đang xử lý...");
        animateColorTransition(btnAction, startColor, COLOR_PROCESSING, 200);

        new Thread(() -> {
            try {
                DriverChuyenDiBSL bsl = new DriverChuyenDiBSL();
                String nextStatusStr = "";

                switch (currentState) {
                    case ACCEPTED: nextStatusStr = "ARRIVED"; break;
                    case ARRIVED: nextStatusStr = "IN_PROGRESS"; break;
                    case IN_PROGRESS: nextStatusStr = "COMPLETED"; break;
                    case COMPLETED: return;
                }

                Map<String, Object> response = bsl.capNhatTrangThaiChuyen(bookingId, nextStatusStr);
                Number status = (Number) response.get("status");

                if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            updateUIForNextState();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> showErrorState(startColor));
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> showErrorState(startColor));
            }
        }).start();
    }

    private void showErrorState(Color originalColor) {
        JOptionPane.showMessageDialog(this, "Lỗi mạng hoặc hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        btnAction.setEnabled(true);
        btnAction.setText("THỬ LẠI");
        animateColorTransition(btnAction, btnAction.getBackground(), originalColor, 300);
    }

    private void updateUIForNextState() {
        btnAction.setEnabled(true);
        Color targetColor = COLOR_ACCEPTED; 

        switch (currentState) {
            case ACCEPTED:
                currentState = TripState.ARRIVED;
                targetColor = COLOR_ARRIVED;
                lblHeaderTitle.setText("KHÁCH ĐANG LÊN XE");
                btnAction.setText("BẮT ĐẦU CHUYẾN");
                break;
                
            case ARRIVED:
                currentState = TripState.IN_PROGRESS;
                targetColor = COLOR_IN_PROGRESS;
                lblHeaderTitle.setText("ĐANG DI CHUYỂN");
                btnAction.setText("HOÀN THÀNH CHUYẾN ĐI"); // Sửa lại text để hợp lý khi đã thanh toán hoặc chưa
                break;
                
            case IN_PROGRESS:
                currentState = TripState.COMPLETED;
                JOptionPane.showMessageDialog(this, "Chuyến đi hoàn tất! Chúc mừng bạn.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                this.dispose(); 
                new TrangChuDriverView(tk); 
                return; 
        }

        animateColorTransition(headerPanel, headerPanel.getBackground(), targetColor, 400);
        animateColorTransition(btnAction, btnAction.getBackground(), targetColor, 400);
    }


    private void animateColorTransition(JComponent component, Color fromColor, Color toColor, int duration) {
        int delay = 15; 
        int totalFrames = duration / delay;

        Timer timer = new Timer(delay, new ActionListener() {
            int frame = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                frame++;
                float ratio = (float) frame / totalFrames;
                
                int r = (int) (fromColor.getRed() + ratio * (toColor.getRed() - fromColor.getRed()));
                int g = (int) (fromColor.getGreen() + ratio * (toColor.getGreen() - fromColor.getGreen()));
                int b = (int) (fromColor.getBlue() + ratio * (toColor.getBlue() - fromColor.getBlue()));
                
                component.setBackground(new Color(r, g, b));
                
                if (frame >= totalFrames) {
                    ((Timer) e.getSource()).stop();
                    component.setBackground(toColor); 
                }
            }
        });
        timer.start();
    }
}