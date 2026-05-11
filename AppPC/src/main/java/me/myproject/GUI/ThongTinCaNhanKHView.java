package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import me.myproject.MODEL.KhachHang;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.ImagePanel;

public class ThongTinCaNhanKHView extends JFrame {

    private KhachHang khachHang; // Object chứa dữ liệu từ API
    private TaiKhoan taiKhoan; // Object chứa thông tin tài khoản (nếu cần)

    // Các thành phần giao diện
    private JLabel lblAvatar;
    private JTextField txtHoTen, txtSoDienThoai, txtEmail, txtDiaChi, txtGioiTinh, txtNgaySinh;
    private JTextField txtTenTaiKhoan, txtVaiTro, txtNgayTao, txtTrangThai;
    private JButton btnCapNhat, btnDoiMatKhau, btnDong;

    public ThongTinCaNhanKHView(KhachHang kh) {
        this.khachHang = kh;
        initUI();
        if (kh != null) {
            loadDataToForm();
        }
    }
    // public ThongTinCaNhanKHView(TaiKhoan tk) {
    //     this.taiKhoan = tk;
    //     initUI();
    //       loadDataToForm();

    // }

    private void initUI() {
        setTitle("Thông Tin Cá Nhân");
        // Đảm bảo setSize trước khi lấy Dimension
        setSize(800, 600); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // 1. Lấy kích thước động của FrameMain
        Dimension frameDimension = this.getSize();
        int frameWidth = frameDimension.width;
        int frameHeight = frameDimension.height;

        // 2. Thiết lập JLayeredPane theo kích thước động
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

        // 3. Hình nền - Phủ toàn bộ frame (Layer DEFAULT)
        JLabel backgroundLabel = new JLabel();
        URL backgroundUrl = getClass().getResource("/me/myproject/IMAGE/bgrmenu.png");
        if (backgroundUrl != null) {
            ImageIcon backgroundIcon = new ImageIcon(backgroundUrl);
            // Scale ảnh theo kích thước động của frame
            Image backgroundImage = backgroundIcon.getImage().getScaledInstance(frameWidth, frameHeight, Image.SCALE_SMOOTH);
            backgroundLabel.setIcon(new ImageIcon(backgroundImage));
        }
        backgroundLabel.setBounds(0, 0, frameWidth, frameHeight);
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        // 4. Panel bọc nội dung chính (Layer PALETTE)
        JPanel mainWrapperPanel = new JPanel(new BorderLayout());
        mainWrapperPanel.setOpaque(false); // Nhìn xuyên qua nền
        mainWrapperPanel.setBounds(0, 0, frameWidth, frameHeight);

        // --- 4.1 HEADER (Chiều cao cố định 70px nhưng chiều rộng động) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 118, 210, 200)); 
        headerPanel.setPreferredSize(new Dimension(frameWidth, 70));
        
        JLabel lblTitle = new JLabel("HỒ SƠ CỦA TÔI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.CENTER);

        // --- 4.2 CONTENT AREA ---
        JPanel contentArea = new JPanel(new BorderLayout(20, 20));
        contentArea.setOpaque(false);
        contentArea.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Panel trắng mờ (Glass Effect) để thông tin dễ đọc trên nền ảnh
        JPanel glassPanel = new JPanel(new BorderLayout(20, 20));
        glassPanel.setBackground(new Color(255, 255, 255, 230)); 
        glassPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Avatar bên trái
        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));
        avatarPanel.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(180, 0));

        lblAvatar = new JLabel();
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        setDefaultAvatar(); 
        
        JLabel lblAvatarHint = new JLabel("Ảnh đại diện", SwingConstants.CENTER);
        lblAvatarHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblAvatarHint.setFont(new Font("Arial", Font.ITALIC, 12));

        avatarPanel.add(lblAvatar);
        avatarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        avatarPanel.add(lblAvatarHint);

        // Form thông tin bên phải
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(25, 118, 210)), 
                "Chi tiết hồ sơ", TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Arial", Font.BOLD, 14), new Color(25, 118, 210)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        txtHoTen = addFormField(formPanel, "Họ và tên:", 0, row++, gbc);
        txtSoDienThoai = addFormField(formPanel, "Số điện thoại:", 0, row++, gbc);
        txtEmail = addFormField(formPanel, "Email:", 0, row++, gbc);
        txtDiaChi = addFormField(formPanel, "Địa chỉ:", 0, row++, gbc);

        row = 0; // Reset dòng cho cột bên cạnh
        txtNgaySinh = addFormField(formPanel, "Ngày sinh:", 2, row++, gbc);
        txtGioiTinh = addFormField(formPanel, "Giới tính:", 2, row++, gbc);
        txtTenTaiKhoan = addFormField(formPanel, "Tên tài khoản:", 2, row++, gbc);
        txtNgayTao = addFormField(formPanel, "Ngày tham gia:", 2, row++, gbc);

        glassPanel.add(avatarPanel, BorderLayout.WEST);
        glassPanel.add(formPanel, BorderLayout.CENTER);
        contentArea.add(glassPanel, BorderLayout.CENTER);

        // --- 4.3 BOTTOM BUTTONS ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        bottomPanel.setOpaque(false);

        btnDoiMatKhau = createButton("Đổi Mật Khẩu",  Color.BLUE, Color.BLACK);
        btnCapNhat = createButton("Cập Nhật",  Color.YELLOW, Color.BLACK);
        btnDong = createButton("Đóng", Color.RED, Color.WHITE);
        
        btnDong.addActionListener(e -> this.dispose());
        bottomPanel.add(btnDoiMatKhau);
        bottomPanel.add(btnCapNhat);
        bottomPanel.add(btnDong);
        bottomPanel.setBorder(new EmptyBorder(0, 0, 20, 10)); // Padding cho panel nút

        // Ráp các panel vào mainWrapperPanel
        mainWrapperPanel.add(headerPanel, BorderLayout.NORTH);
        mainWrapperPanel.add(contentArea, BorderLayout.CENTER);
        mainWrapperPanel.add(bottomPanel, BorderLayout.SOUTH);

        layeredPane.add(mainWrapperPanel, JLayeredPane.PALETTE_LAYER);

        // 5. Đồng bộ co dãn (Responsive)
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Dimension size = getSize();
                layeredPane.setSize(size);
                backgroundLabel.setBounds(0, 0, size.width, size.height);
                mainWrapperPanel.setBounds(0, 0, size.width, size.height);
                
                // Re-scale lại ảnh nền khi kéo dãn cửa sổ
                if (backgroundUrl != null) {
                    ImageIcon icon = new ImageIcon(backgroundUrl);
                    Image img = icon.getImage().getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH);
                    backgroundLabel.setIcon(new ImageIcon(img));
                }
            }
        });

        this.setContentPane(layeredPane);
        this.setVisible(true);
    }

    // Hàm tiện ích tạo dòng nhập liệu
    private JTextField addFormField(JPanel panel, String labelText, int col, int row, GridBagConstraints gbc) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 0.1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Color.DARK_GRAY);
        panel.add(label, gbc);

        gbc.gridx = col + 1;
        gbc.weightx = 0.9;
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Arial", Font.PLAIN, 13));
        textField.setEditable(false); // Chỉ đọc (Read-only)
        textField.setBackground(new Color(245, 245, 245));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panel.add(textField, gbc);
        return textField;
    }

    // Hàm tiện ích tạo nút bấm
   private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(fg);
        
        // Thêm 2 dòng quan trọng này:
        btn.setContentAreaFilled(true); // Cho phép vẽ vùng nội dung
        btn.setOpaque(true);            // Ép thành phần phải đục (để hiện màu nền)
        
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 35));
        
        return btn;
    }

    private void setDefaultAvatar() {
        try {
            URL avatarUrl = getClass().getResource("/me/myproject/IMAGE/default_avatar.png");
            if (avatarUrl != null) {
                ImageIcon icon = new ImageIcon(avatarUrl);
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblAvatar.setIcon(new ImageIcon(img));
            } else {
                lblAvatar.setText("NO AVATAR");
                lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Đổ dữ liệu từ Object vào giao diện
    private void loadDataToForm() {
        txtHoTen.setText(khachHang.getCustomerName() != null ? khachHang.getCustomerName() : "N/A");
        txtSoDienThoai.setText(khachHang.getPhone() != null ? khachHang.getPhone() : "N/A");
        txtEmail.setText(khachHang.getEmail() != null ? khachHang.getEmail() : "Chưa cập nhật");
        txtDiaChi.setText(khachHang.getAddress() != null ? khachHang.getAddress() : "Chưa cập nhật");
        txtGioiTinh.setText( "Nam");
        txtNgaySinh.setText(khachHang.getBirthDate() != null ? khachHang.getBirthDate() : "N/A");

        // Format ngày tháng từ chuỗi ISO 8601 (2026-04-10T03:08:05.810Z)
        if (khachHang.getAccount() != null) {
            txtTenTaiKhoan.setText(khachHang.getAccount().getUserName());
            
            try {
                String rawDate = khachHang.getAccount().getCreatedAt().toString();
                ZonedDateTime zdt = ZonedDateTime.parse(rawDate);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                txtNgayTao.setText(zdt.format(formatter));
            } catch (Exception e) {
                txtNgayTao.setText(khachHang.getAccount().getCreatedAt().toString()); // Fallback nếu lỗi parse
            }
        }
        
        // Cập nhật Avatar nếu link API avatar có tồn tại (Logic tải ảnh từ URL mạng)
        if(khachHang.getAvatar() != null && !khachHang.getAvatar().isEmpty() && !khachHang.getAvatar().equals("string")) {
            // Bạn có thể dùng một hàm chạy ngầm (SwingWorker) để tải ảnh từ URL về lblAvatar.
            ImagePanel.loadIconFromNetworkAsync(khachHang.getAvatar(), lblAvatar, 150, 150);
        }
    }
}