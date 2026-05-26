package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.DimensionFrame;
import me.myproject.Utilities.DIMENSION.FrameMain;

public class TaiKhoanNganHangView extends FrameMain {

    private static final Color PRIMARY_COLOR = ColorMain.colorPrimary;
    private static final Color ACCENT_COLOR = ColorMain.btnHeader;
    private static final Color TEXT_COLOR = ColorMain.colorBlueBlack1;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Border SHADOW_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)
    );
    private TaiKhoan taiKhoan;

    public TaiKhoanNganHangView() {
        super("Quản Lý Tài Khoản Ngân Hàng");
        init();
    }

    public TaiKhoanNganHangView(TaiKhoan tk) {
        super("Quản Lý Tài Khoản Ngân Hàng");
        init();
        this.taiKhoan = tk;
        //TODO Auto-generated constructor stub
    }

    private void init() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DimensionFrame.widthFrame, DimensionFrame.heightFrame);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Panel with Shadow
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ColorMain.inforPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.inforPanel);
        
        JLabel headerLabel = new JLabel("Quản Lý Tài Khoản Ngân Hàng", SwingConstants.LEFT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(ColorMain.colorBlueBlack1);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // User info panel (top right)
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(ColorMain.inforPanel);
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel userNameLabel = new JLabel("Nguyễn Văn An");
        userNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        userNameLabel.setForeground(ColorMain.colorBlueBlack1);
        userNameLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        
        JLabel userIdLabel = new JLabel("Mã KH: KH001234");
        userIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userIdLabel.setForeground(Color.GRAY);
        userIdLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        
        userInfoPanel.add(userNameLabel);
        userInfoPanel.add(userIdLabel);
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content Panel - Main 2-column layout
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(ColorMain.inforPanel);
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.insets = new Insets(5, 5, 5, 5);

        // Left Column
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setBackground(ColorMain.inforPanel);

        // Saved Bank Accounts Panel (top left)
        JPanel savedAccountsPanel = createTitledPanel("Danh Sách Tài Khoản Đã Lưu");
        savedAccountsPanel.setPreferredSize(new Dimension(450, 150));
        savedAccountsPanel.setLayout(new BorderLayout());

        // Sample saved accounts
        JPanel accountsListPanel = new JPanel();
        accountsListPanel.setLayout(new BoxLayout(accountsListPanel, BoxLayout.Y_AXIS));
        accountsListPanel.setBackground(ColorMain.inforPanel);
        accountsListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Account 1
        JPanel account1Panel = new JPanel(new BorderLayout());
        account1Panel.setBackground(new Color(245, 245, 245));
        account1Panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JLabel account1Label = new JLabel("Vietcombank - 1234567890");
        account1Label.setFont(SUBTITLE_FONT);
        account1Label.setForeground(ColorMain.colorBlueBlack1);
        
        JLabel account1Holder = new JLabel("Nguyễn Văn An");
        account1Holder.setFont(new Font("Arial", Font.PLAIN, 12));
        account1Holder.setForeground(Color.GRAY);
        
        JLabel defaultLabel = new JLabel("Mặc định");
        defaultLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        defaultLabel.setForeground(Color.WHITE);
        defaultLabel.setOpaque(true);
        defaultLabel.setBackground(new Color(40, 167, 69));
        defaultLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        
        JPanel account1Left = new JPanel();
        account1Left.setLayout(new BoxLayout(account1Left, BoxLayout.Y_AXIS));
        account1Left.setBackground(new Color(245, 245, 245));
        account1Left.add(account1Label);
        account1Left.add(account1Holder);
        
        account1Panel.add(account1Left, BorderLayout.WEST);
        account1Panel.add(defaultLabel, BorderLayout.EAST);
        
        accountsListPanel.add(account1Panel);
        savedAccountsPanel.add(accountsListPanel, BorderLayout.CENTER);

        // Add Bank Account Panel (bottom left)
        JPanel addAccountPanel = createTitledPanel("Thêm Tài Khoản Ngân Hàng Mới");
        addAccountPanel.setLayout(new BorderLayout());
        JPanel addAccountContent = new JPanel(new GridBagLayout());
        addAccountContent.setBackground(ColorMain.inforPanel);
        addAccountContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Bank name
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel bankLabel = createStyledLabel("Chọn Ngân Hàng (*)");
        bankLabel.setForeground(Color.RED);
        addAccountContent.add(bankLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JComboBox<String> bankComboBox = new JComboBox<>(new String[]{
            "Chọn ngân hàng...", 
            "Vietcombank", 
            "Techcombank", 
            "BIDV", 
            "VietinBank", 
            "ACB", 
            "MB Bank", 
            "TPBank",
            "Sacombank",
            "VPBank",
            "SCB"
        });
        bankComboBox.setFont(LABEL_FONT);
        bankComboBox.setBackground(Color.WHITE);
        addAccountContent.add(bankComboBox, gbc);

        // Account fields in 2 columns
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.5;
        JLabel accountNumberLabel = createStyledLabel("Số Tài Khoản (*)");
        accountNumberLabel.setForeground(Color.RED);
        addAccountContent.add(accountNumberLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JLabel accountHolderLabel = createStyledLabel("Tên Chủ Tài Khoản (*)");
        accountHolderLabel.setForeground(Color.RED);
        addAccountContent.add(accountHolderLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        JTextField accountNumberField = new JTextField();
        accountNumberField.setFont(LABEL_FONT);
        addAccountContent.add(accountNumberField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        JTextField accountHolderField = new JTextField();
        accountHolderField.setFont(LABEL_FONT);
        addAccountContent.add(accountHolderField, gbc);

        // Branch field
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        addAccountContent.add(createStyledLabel("Chi Nhánh (tùy chọn)"), gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JTextField branchField = new JTextField();
        branchField.setFont(LABEL_FONT);
        addAccountContent.add(branchField, gbc);

        // Checkbox and button
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JCheckBox defaultCheckBox = new JCheckBox("Đặt làm tài khoản mặc định");
        defaultCheckBox.setFont(LABEL_FONT);
        defaultCheckBox.setBackground(ColorMain.inforPanel);
        addAccountContent.add(defaultCheckBox, gbc);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JButton addButton = createStyledButton("Thêm Tài Khoản", new Color(0, 123, 255), Color.WHITE);
        addButton.setFont(SUBTITLE_FONT);
        addButton.setPreferredSize(new Dimension(150, 35));
        addAccountContent.add(addButton, gbc);

        addAccountPanel.add(addAccountContent, BorderLayout.CENTER);

        leftColumn.add(savedAccountsPanel);
        leftColumn.add(Box.createRigidArea(new Dimension(0, 10)));
        leftColumn.add(addAccountPanel);

        // Right Column
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setBackground(ColorMain.inforPanel);

        // Account Statistics Panel (top right)
        JPanel statsPanel = createTitledPanel("Thống Kê Tài Khoản");
        statsPanel.setPreferredSize(new Dimension(300, 150));
        JPanel statsContent = new JPanel(new GridBagLayout());
        statsContent.setBackground(ColorMain.inforPanel);
        statsContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 5, 8, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        statsContent.add(createStyledLabel("Tổng Số Tài Khoản"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 1.0;
        JLabel totalAccounts = createStyledLabel("1 tài khoản");
        totalAccounts.setFont(SUBTITLE_FONT);
        totalAccounts.setForeground(new Color(0, 150, 255));
        statsContent.add(totalAccounts, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        statsContent.add(createStyledLabel("Tài Khoản Mặc Định"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 1.0;
        JLabel defaultAccount = createStyledLabel("Vietcombank");
        defaultAccount.setFont(SUBTITLE_FONT);
        defaultAccount.setForeground(new Color(40, 167, 69));
        statsContent.add(defaultAccount, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        statsContent.add(createStyledLabel("Trạng Thái"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 1.0;
        JLabel accountStatus = createStyledLabel("Hoạt động");
        accountStatus.setFont(SUBTITLE_FONT);
        accountStatus.setForeground(new Color(40, 167, 69));
        statsContent.add(accountStatus, gbc);

        statsPanel.add(statsContent, BorderLayout.CENTER);

        // Recent Transactions Panel (bottom right)
        JPanel transactionsPanel = createTitledPanel("Giao Dịch Gần Đây");
        JPanel transactionsContent = new JPanel(new GridBagLayout());
        transactionsContent.setBackground(ColorMain.inforPanel);
        transactionsContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Transaction 1
        gbc.gridx = 0; gbc.gridy = 0;
        transactionsContent.add(createStyledLabel("15/12/2024"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 1.0;
        JLabel transaction1Amount = createStyledLabel("+ 2,500,000 VND");
        transaction1Amount.setFont(SUBTITLE_FONT);
        transaction1Amount.setForeground(new Color(40, 167, 69));
        transactionsContent.add(transaction1Amount, gbc);
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0;
        JButton status1 = createStyledButton("Thành công", new Color(40, 167, 69), Color.WHITE);
        status1.setFont(new Font("Arial", Font.PLAIN, 12));
        status1.setPreferredSize(new Dimension(100, 25));
        transactionsContent.add(status1, gbc);

        // Transaction 2
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        transactionsContent.add(createStyledLabel("12/12/2024"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 1.0;
        JLabel transaction2Amount = createStyledLabel("- 850,000 VND");
        transaction2Amount.setFont(SUBTITLE_FONT);
        transaction2Amount.setForeground(new Color(220, 53, 69));
        transactionsContent.add(transaction2Amount, gbc);
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0;
        JButton status2 = createStyledButton("Thành công", new Color(40, 167, 69), Color.WHITE);
        status2.setFont(new Font("Arial", Font.PLAIN, 12));
        status2.setPreferredSize(new Dimension(100, 25));
        transactionsContent.add(status2, gbc);

        // Transaction 3
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.weightx = 0;
        transactionsContent.add(createStyledLabel("10/12/2024"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 1.0;
        JLabel transaction3Amount = createStyledLabel("+ 1,200,000 VND");
        transaction3Amount.setFont(SUBTITLE_FONT);
        transaction3Amount.setForeground(new Color(40, 167, 69));
        transactionsContent.add(transaction3Amount, gbc);
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 0;
        JButton status3 = createStyledButton("Đang xử lý", new Color(255, 193, 7), Color.BLACK);
        status3.setFont(new Font("Arial", Font.PLAIN, 12));
        status3.setPreferredSize(new Dimension(100, 25));
        transactionsContent.add(status3, gbc);

        transactionsPanel.add(transactionsContent, BorderLayout.CENTER);

        rightColumn.add(statsPanel);
        rightColumn.add(Box.createRigidArea(new Dimension(0, 10)));
        rightColumn.add(transactionsPanel);

        // Add columns to main content
        mainGbc.gridx = 0; mainGbc.gridy = 0; mainGbc.weightx = 0.6; mainGbc.weighty = 1.0;
        contentPanel.add(leftColumn, mainGbc);
        mainGbc.gridx = 1; mainGbc.weightx = 0.4;
        contentPanel.add(rightColumn, mainGbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ColorMain.inforPanel);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title,
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            TITLE_FONT,
            ColorMain.colorBlueBlack1
        ));
        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(LABEL_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
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
        label.setForeground(ColorMain.colorBlueBlack1);
        return label;
    }

    public static void main(String[] args) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("Hệ thống không hỗ trợ giao diện đồ họa.");
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> new TaiKhoanNganHangView());
    }
}