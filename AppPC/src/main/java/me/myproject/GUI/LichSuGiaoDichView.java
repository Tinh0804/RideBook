package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import me.myproject.BUSINESSLOGIC.WalletBSL;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;

public class LichSuGiaoDichView extends FrameMain {
    private String walletId;
    private WalletBSL walletBSL;
    
    private JPanel listPanel;
    private JScrollPane scrollPane;

    public LichSuGiaoDichView(String walletId) {
        super("Lịch Sử Giao Dịch");
        this.walletId = walletId;
        this.walletBSL = new WalletBSL();
        
        initUI();
        loadHistoryData();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(new Color(245, 245, 245));
        this.setSize(500, 650); // Thiết lập kích thước phù hợp cho list
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.blueHeader);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 60));
        headerPanel.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel lblTitle = new JLabel("LỊCH SỬ GIAO DỊCH", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        JButton btnBack = new JButton("Đóng");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(ColorMain.blueHeader);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> this.dispose());
        headerPanel.add(btnBack, BorderLayout.WEST);

        // 2. CENTER - DANH SÁCH GIAO DỊCH
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(245, 245, 245));
        listPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Lăn chuột mượt hơn
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(headerPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void loadHistoryData() {
        // Hiển thị trạng thái loading
        listPanel.removeAll();
        JLabel lblLoading = new JLabel("Đang tải dữ liệu...", JLabel.CENTER);
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblLoading.setAlignmentX(Component.CENTER_ALIGNMENT);
        listPanel.add(Box.createVerticalStrut(50));
        listPanel.add(lblLoading);
        listPanel.revalidate();
        listPanel.repaint();

        new Thread(() -> {
            // Giả sử walletBSL.getLichSuGiaoDich(walletId) gọi API và trả về Map
            // Cần cập nhật hàm này trong BSL của bạn cho phù hợp
            Map<String, Object> response = walletBSL.getTransactionHistory(walletId);

            SwingUtilities.invokeLater(() -> {
                listPanel.removeAll();

                try {
                    System.out.println("Response from API: " + response); // Debug log
                    Number status = (Number) response.get("status");
                    if (response != null &&  status != null && status.intValue() >= 200 && status.intValue() < 300) {
                        List<Map<String, Object>> resultList = (List<Map<String, Object>>) response.get("result");

                        if (resultList == null || resultList.isEmpty()) {
                            JLabel lblEmpty = new JLabel("Chưa có giao dịch nào.", JLabel.CENTER);
                            lblEmpty.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
                            listPanel.add(Box.createVerticalStrut(50));
                            listPanel.add(lblEmpty);
                        } else {
                            for (Map<String, Object> item : resultList) {
                                listPanel.add(createTransactionCard(item));
                                listPanel.add(Box.createVerticalStrut(10)); // Khoảng cách giữa các card
                            }
                        }
                    } else {
                        String msg = (response != null && response.containsKey("message")) 
                                ? response.get("message").toString() : "Lỗi hệ thống";
                        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JLabel lblError = new JLabel("Lỗi hiển thị dữ liệu.", JLabel.CENTER);
                    lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
                    listPanel.add(lblError);
                }

                listPanel.revalidate();
                listPanel.repaint();
            });
        }).start();
    }

    private JPanel createTransactionCard(Map<String, Object> data) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90)); // Chống giãn dọc

        // 1. Phân tích dữ liệu từ JSON
        String type = data.getOrDefault("type", "").toString();
        String status = data.getOrDefault("status", "").toString();
        String transId = data.getOrDefault("transactionId", "").toString();
        String rawDate = data.getOrDefault("createdAt", "").toString();
        
        Object amountObj = data.get("amount");
        double amount = (amountObj instanceof Number) ? ((Number) amountObj).doubleValue() : 0.0;

        // 2. Chuyển đổi format ngày tháng (từ ISO-8601 sang chuẩn VN)
        String formattedDate = rawDate;
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(rawDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            formattedDate = zdt.format(formatter);
        } catch (Exception e) {
            System.err.println("Lỗi parse ngày: " + rawDate);
        }

        // 3. Logic màu sắc & Tiêu đề dựa vào Type và Status
        String titleStr = type.equals("DEPOSIT") ? "Nạp tiền vào ví" : "Rút tiền";
        String amountPrefix = type.equals("DEPOSIT") ? "+" : "-";
        
        Color statusColor = Color.GRAY;
        String statusVn = status;

        if (status.equals("COMPLETED")) {
            statusColor = new Color(46, 204, 113); // Xanh lá
            statusVn = "Thành công";
        } else if (status.equals("PENDING")) {
            statusColor = new Color(243, 156, 18); // Cam
            statusVn = "Đang xử lý";
        } else if (status.equals("FAILED")) {
            statusColor = new Color(231, 76, 60); // Đỏ
            statusVn = "Thất bại";
        }

        // 4. Panel TRÁI (Tiêu đề, Thời gian, Mã GD)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(titleStr);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(50, 50, 50));

        JLabel lblTime = new JLabel(formattedDate + "  |  Mã: " + (transId.length() > 8 ? transId.substring(0,8) + "..." : transId));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(new Color(150, 150, 150));

        leftPanel.add(lblTitle);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(lblTime);

        // 5. Panel PHẢI (Số tiền, Trạng thái)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        JLabel lblAmount = new JLabel(amountPrefix + String.format("%,.0f đ", amount));
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblAmount.setForeground(type.equals("DEPOSIT") ? new Color(46, 204, 113) : new Color(231, 76, 60));
        lblAmount.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblStatus = new JLabel(statusVn);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(statusColor);
        lblStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(lblAmount);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(lblStatus);

        // Lắp ráp vào Card
        card.add(leftPanel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }
}