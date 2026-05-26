package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.DimensionFrame;

public class KetThucChuyenView extends JFrame implements ActionListener {
    
    public JButton btnBack;
    private static final Color PRIMARY_COLOR = ColorMain.colorPrimary;
    private static final Color ACCENT_COLOR = ColorMain.btnHeader;
    private static final Color TEXT_COLOR = ColorMain.colorBlueBlack1;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Border SHADOW_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    );

    public KetThucChuyenView() {
        super("Kết Thúc Chuyến");
        init();
    }

    public KetThucChuyenView(TaiKhoan tk) {
        //TODO Auto-generated constructor stub
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DimensionFrame.widthFrame, DimensionFrame.heightFrame);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(33, 150, 243)); // Blue header
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        btnBack = createStyledButton("Quay lại", new Color(255, 255, 255, 50), Color.WHITE);
        btnBack.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        btnBack.addActionListener(this);
        headerPanel.add(btnBack, BorderLayout.WEST);

        JLabel headerLabel = new JLabel("Kết Thúc Chuyến", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Content Panel with ScrollPane
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Trip Summary Panel
        contentPanel.add(createTripSummaryPanel());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. Trip Info and Driver Info Panel (side by side)
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        middlePanel.setBackground(Color.WHITE);
        middlePanel.add(createTripInfoPanel());
        middlePanel.add(createDriverInfoPanel());
        contentPanel.add(middlePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 3. Payment Panel
        contentPanel.add(createPaymentPanel());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 4. Rating Panel
        contentPanel.add(createRatingPanel());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 5. Finish Button
        contentPanel.add(createFinishButtonPanel());

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createTripSummaryPanel() {
        JPanel panel = createTitledPanel("Tóm tắt chuyến đi");
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // First row - Trip details
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(createStyledLabel("Mã chuyến:"), gbc);
        gbc.gridx = 1;
        contentPanel.add(createBoldLabel("RW12345678"), gbc);

        gbc.gridx = 2;
        contentPanel.add(createStyledLabel("Thời gian chạy:"), gbc);
        gbc.gridx = 3;
        contentPanel.add(createBoldLabel("23 phút"), gbc);
        gbc.gridx = 4;
        contentPanel.add(createStyledLabel("Quãng đường:"), gbc);
        gbc.gridx = 5;
        contentPanel.add(createBoldLabel("3.8 km"), gbc);

        // Second row - Pickup location
        gbc.gridx = 0; gbc.gridy = 1;
        JCheckBox pickupCheck = createStyledCheckBox(true);
        contentPanel.add(pickupCheck, gbc);
        gbc.gridx = 1; gbc.gridwidth = 5;
        contentPanel.add(createStyledLabel("227 Hà Huy Tập, Phường Chính Gián, Quận Thanh Khê, Đà Nẵng"), gbc);

        // Third row - Dropoff location
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JCheckBox dropoffCheck = createStyledCheckBox(true);
        contentPanel.add(dropoffCheck, gbc);
        gbc.gridx = 1; gbc.gridwidth = 5;
        contentPanel.add(createStyledLabel("45 Trần Cao Vân, Phường Chính Gián, Quận Thanh Khê, Đà Nẵng"), gbc);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTripInfoPanel() {
        JPanel panel = createTitledPanel("Thông tin chuyến đi");
        ImageIcon icon = new ImageIcon("icon.png");
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Vehicle info
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(createStyledCheckBox(true), gbc);
        gbc.gridx = 1;
        contentPanel.add(createBoldLabel("Honda Wave Alpha"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        contentPanel.add(createStyledLabel("51F-12345"), gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(createStyledLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        JLabel statusLabel = createStyledLabel("Đang chạy");
        statusLabel.setForeground(new Color(76, 175, 80)); // Green color
        contentPanel.add(statusLabel, gbc);
        // Completion note
        gbc.gridx = 0; gbc.gridy = 3;
        contentPanel.add(createStyledCheckBox(true), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        JLabel completionLabel = createStyledLabel("Chuyến đi đã hoàn tất. Vui lòng hoàn thành và đánh giá");
        completionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        completionLabel.setForeground(Color.GRAY);
        contentPanel.add(completionLabel, gbc);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDriverInfoPanel() {
        JPanel panel = createTitledPanel("Nguyễn Thị Vy Hậu");
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Phone
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(createStyledCheckBox(true), gbc);
        gbc.gridx = 1;
        contentPanel.add(createStyledLabel("0901234567"), gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(createStyledCheckBox(true), gbc);
        gbc.gridx = 1;
        contentPanel.add(createStyledLabel("vyhaucute@gmail.com"), gbc);

        // Call button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton callButton = createStyledButton("Gọi khách hàng", new Color(0, 150, 136), Color.WHITE);
        callButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        contentPanel.add(callButton, gbc);

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = createTitledPanel("Chi tiết thanh toán");
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Payment method and income
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4; gbc.anchor = GridBagConstraints.WEST;
        contentPanel.add(createBoldLabel("Tiền mặt (đã nhận)"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.anchor = GridBagConstraints.EAST;
        contentPanel.add(createStyledLabel("Thu nhập của bạn:"), gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2; gbc.anchor = GridBagConstraints.EAST;
        JLabel incomeLabel = createBoldLabel("44,100 VND");
        incomeLabel.setForeground(new Color(76, 175, 80)); // Green
        contentPanel.add(incomeLabel, gbc);

        // Fare details
        String[] labels = {"Giá cước", "Giảm giá", "Phí nền tảng", "Tổng cộng"};
        String[] values = {"54,000", "-5,000", "-4,900", "49,000"};
        Color[] colors = {Color.BLACK, Color.RED, Color.RED, Color.BLACK};
        boolean[] isBold = {false, false, false, true};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0.8; gbc.anchor = GridBagConstraints.WEST;
            JLabel label = isBold[i] ? createBoldLabel(labels[i]) : createStyledLabel(labels[i]);
            contentPanel.add(label, gbc);
            
            gbc.gridx = 2; gbc.weightx = 0.2; gbc.anchor = GridBagConstraints.EAST;
            JLabel valueLabel = isBold[i] ? createBoldLabel(values[i]) : createStyledLabel(values[i]);
            valueLabel.setForeground(colors[i]);
            contentPanel.add(valueLabel, gbc);
        }

        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRatingPanel() {
        JPanel panel = createTitledPanel("Đánh giá chuyến đi");
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Rating prompt
        JLabel promptLabel = createStyledLabel("Hãy đánh giá trải nghiệm của bạn với khách hàng");
        promptLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        promptLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        contentPanel.add(promptLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Stars panel
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        starsPanel.setBackground(Color.WHITE);
        starsPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        for (int i = 0; i < 5; i++) {
            JLabel star = new JLabel("★");
            star.setFont(new Font("Arial", Font.PLAIN, 30));
            star.setForeground(Color.YELLOW);
            star.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            starsPanel.add(star);
        }
        contentPanel.add(starsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Comment section
        JPanel commentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        commentPanel.setBackground(Color.WHITE);
        commentPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
        JButton excellentBtn = createStyledButton("Tuyệt vời", Color.WHITE, new Color(33, 150, 243));
        excellentBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(33, 150, 243), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        commentPanel.add(excellentBtn);
        
        JLabel commentLabel = createStyledLabel("Để lại bình luận về khách hàng (không bắt buộc)");
        commentLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        commentLabel.setForeground(Color.GRAY);
        commentPanel.add(commentLabel);
        
        contentPanel.add(commentPanel);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFinishButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.WHITE);
        
        JButton finishBtn = createStyledButton("Hoàn Thành Chuyến", new Color(0, 150, 136), Color.WHITE);
        finishBtn.setFont(new Font("Arial", Font.BOLD, 16));
        finishBtn.setPreferredSize(new Dimension(200, 45));
        finishBtn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        panel.add(finishBtn);
        
        return panel;
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title,
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            TITLE_FONT,
            TEXT_COLOR
        ));
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(LABEL_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createBoldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JCheckBox createStyledCheckBox(boolean selected) {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(selected);
        checkBox.setEnabled(false);
        checkBox.setBackground(Color.WHITE);
        return checkBox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("Hệ thống không hỗ trợ giao diện đồ họa (headless environment).");
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> new KetThucChuyenView());
    }
}