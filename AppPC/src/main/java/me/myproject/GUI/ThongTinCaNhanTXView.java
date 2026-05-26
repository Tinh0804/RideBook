package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.MODEL.TaiXe;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.ImagePanel;

public class ThongTinCaNhanTXView extends FrameMain {
    private TaiKhoan tk;
    private TaiXe taiXe;

    // Thành phần phần Avatar & Tên
    private JLabel lblAvatar;
    private JLabel lblDriverNameTitle;

    // Các trường nhập liệu (Đã bỏ txtDriverName)
    private JTextField txtBirthDate, txtGender, txtPhone, txtEmail;
    private JTextField txtAddress, txtArea;
    private JTextField txtCitizenId, txtDrivingLicense;
    private JTextField txtVehicleName, txtLicensePlate;
    
    private JButton btnUpdate, btnBack;

    public ThongTinCaNhanTXView(TaiXe tx) {
        super("Hồ Sơ Tài Xế");
        this.taiXe = tx;
        
        initUI();
        loadDriverData();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(new Color(245, 245, 245));
        this.setSize(500, 750); // Tăng chiều cao lên một chút để chứa Avatar
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String fontName = "Segoe UI";

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.blueHeader);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 60));
        headerPanel.setBorder(new EmptyBorder(0, 15, 0, 15));

        JLabel lblTitle = new JLabel("HỒ SƠ CÁ NHÂN", JLabel.CENTER);
        lblTitle.setFont(new Font(fontName, Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        btnBack = new JButton("Đóng");
        btnBack.setFont(new Font(fontName, Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(ColorMain.blueHeader);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> this.dispose());
        headerPanel.add(btnBack, BorderLayout.WEST);

        // 2. CENTER - FORM THÔNG TIN (Cuộn được)
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(new Color(245, 245, 245));
        formContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // -- Card 0: AVATAR & TÊN TÀI XẾ
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setBackground(new Color(245, 245, 245));
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblAvatar = new JLabel();
        lblAvatar.setPreferredSize(new Dimension(100, 100));
        lblAvatar.setMaximumSize(new Dimension(100, 100));
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatar.setHorizontalAlignment(JLabel.CENTER);
        lblAvatar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2)); // Viền ảnh
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(Color.WHITE);
        lblAvatar.setText("Ảnh"); // Chữ hiển thị mặc định nếu chưa có ảnh

        lblDriverNameTitle = new JLabel("Đang tải tên...");
        lblDriverNameTitle.setFont(new Font(fontName, Font.BOLD, 22));
        lblDriverNameTitle.setForeground(new Color(33, 33, 33));
        lblDriverNameTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        avatarPanel.add(lblAvatar);
        avatarPanel.add(Box.createVerticalStrut(10));
        avatarPanel.add(lblDriverNameTitle);
        avatarPanel.add(Box.createVerticalStrut(25)); // Khoảng cách tới các card dưới
        setDefaultAvatar();

        // -- Card 1: Thông tin cơ bản
        JPanel pnlBasic = createCardPanel("Thông Tin Cơ Bản");
        pnlBasic.setLayout(new GridLayout(0, 2, 15, 10)); // 2 cột
        
        txtPhone = createTextField();
        txtBirthDate = createTextField();
        txtGender = createTextField();
        txtEmail = createTextField();
        txtArea = createTextField();

        addFormField(pnlBasic, "Số điện thoại:", txtPhone);
        addFormField(pnlBasic, "Ngày sinh:", txtBirthDate);
        addFormField(pnlBasic, "Giới tính:", txtGender);
        addFormField(pnlBasic, "Email:", txtEmail);
        addFormField(pnlBasic, "Khu vực HĐ:", txtArea);
        
        // Địa chỉ chiếm 2 cột để có chỗ hiển thị dài
        txtAddress = createTextField();
        JPanel pnlAddress = new JPanel(new BorderLayout());
        pnlAddress.setBackground(Color.WHITE);
        pnlAddress.add(new JLabel("Địa chỉ:"), BorderLayout.NORTH);
        pnlAddress.add(txtAddress, BorderLayout.CENTER);
        
        // -- Card 2: Giấy tờ & Pháp lý
        JPanel pnlLegal = createCardPanel("Giấy Tờ Định Danh");
        pnlLegal.setLayout(new GridLayout(0, 2, 15, 10));
        
        txtCitizenId = createTextField();
        txtDrivingLicense = createTextField();
        
        addFormField(pnlLegal, "Số CCCD:", txtCitizenId);
        addFormField(pnlLegal, "Giấy phép Lái xe:", txtDrivingLicense);

        // -- Card 3: Phương tiện
        JPanel pnlVehicle = createCardPanel("Phương Tiện Hoạt Động");
        pnlVehicle.setLayout(new GridLayout(0, 2, 15, 10));
        
        txtVehicleName = createTextField();
        txtLicensePlate = createTextField();

        addFormField(pnlVehicle, "Tên xe:", txtVehicleName);
        addFormField(pnlVehicle, "Biển số xe:", txtLicensePlate);

        // Thêm tất cả vào Container chính
        formContainer.add(avatarPanel);
        formContainer.add(pnlBasic);
        formContainer.add(Box.createVerticalStrut(10));
        formContainer.add(pnlAddress); 
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(pnlLegal);
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(pnlVehicle);

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 3. BOTTOM - NÚT CẬP NHẬT
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        bottomPanel.setPreferredSize(new Dimension(this.getWidth(), 70));

        btnUpdate = new JButton("CẬP NHẬT THÔNG TIN");
        btnUpdate.setFont(new Font(fontName, Font.BOLD, 15));
        btnUpdate.setBackground(new Color(0, 150, 136));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setPreferredSize(new Dimension(250, 45));
        btnUpdate.setFocusPainted(false);
        btnUpdate.setBorderPainted(false);
        btnUpdate.setOpaque(true);
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bottomPanel.add(btnUpdate);

        // Lắp ráp toàn bộ layout
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        this.setVisible(true);
    }

    // --- CÁC HÀM TIỆN ÍCH TẠO UI ---

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createTitledBorder(
                new EmptyBorder(5, 5, 5, 5), 
                title, 
                TitledBorder.LEFT, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14), 
                new Color(0, 150, 136)
            )
        ));
        return panel;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 8, 5, 8)
        ));
        txt.setEditable(true); 
        return txt;
    }

    private void addFormField(JPanel parent, String labelText, JTextField textField) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(new Color(100, 100, 100));
        
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(textField, BorderLayout.CENTER);
        
        parent.add(pnl);
    }

    // --- LOGIC XỬ LÝ DỮ LIỆU ---

    private void loadDriverData() {
        bindDataToForm(this.taiXe);
    }

    private void bindDataToForm(TaiXe tx) {
        if (tx == null) return;
        
        // Gán Tên hiển thị to ở trên cùng
        lblDriverNameTitle.setText(tx.getDriverName() != null ? tx.getDriverName() : "Chưa cập nhật tên");
        

        txtBirthDate.setText(tx.getBirthDate());
        txtGender.setText(tx.getGender());
        txtPhone.setText(tx.getPhone());
        txtEmail.setText(tx.getEmail());
        txtAddress.setText(tx.getAddress());
        txtArea.setText(tx.getArea());
        
        txtCitizenId.setText(tx.getCitizenId());
        txtDrivingLicense.setText(tx.getDrivingLicense());
        
        txtVehicleName.setText(tx.getVehicleName());
        txtLicensePlate.setText(tx.getLicensePlate());

       if(tx.getAvatar() != null && !tx.getAvatar().trim().isEmpty()) {
           ImagePanel.loadIconFromNetworkAsync(tx.getAvatar(), lblAvatar, 150, 150);
        }
    }

    private void setDefaultAvatar() {
        try {
            URL avatarUrl = getClass().getResource("/me/myproject/IMAGE/default_avatar.png");
            if (avatarUrl != null) {
                ImageIcon icon = new ImageIcon(avatarUrl);
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblAvatar.setIcon(new ImageIcon(img));
            } else {
                lblAvatar.setText("Không có ảnh");
                lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}