package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import me.myproject.BUSINESSLOGIC.DangKyBSL;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.FontMain;
import me.myproject.Utilities.DIMENSION.FrameMain;

public class DangKyView extends FrameMain implements ActionListener {
    private final DangKyBSL business;
    public JTextField tfdFullName, tfdBLXCode, tfdVehicleType, tfdLicensePlate, tfdPhone, tfAddress;
    public JPasswordField tfdPass, tdfConfirmPass;
    public JButton btnRegister;
    private JCheckBox termsCheckBox;

    public DangKyView() {
        super("Đăng ký");
        business = new DangKyBSL();
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());

        // Lấy kích thước của FrameMain bằng getSize()
        Dimension frameDimension = this.getSize();
        int frameWidth = frameDimension.width;
        int frameHeight = frameDimension.height;

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

        // Hình nền - phủ toàn bộ frame
        JLabel backgroundLabel = new JLabel();
        URL backgroundUrl = getClass().getResource("/me/myproject/IMAGE/bgrmenu.png");
        if (backgroundUrl != null) {
            ImageIcon backgroundIcon = new ImageIcon(backgroundUrl);
            Image backgroundImage = backgroundIcon.getImage().getScaledInstance(frameWidth, frameHeight, Image.SCALE_SMOOTH);
            backgroundLabel.setIcon(new ImageIcon(backgroundImage));
        } else {
            System.err.println("Không tìm thấy ảnh nền: /me/myproject/IMAGE/bgrmenu.png");
        }
        backgroundLabel.setBounds(0, 0, frameWidth, frameHeight);
        layeredPane.add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);

        // Panel chính với viền bo tròn và hiệu ứng shadow
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với gradient
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 255, 255, 245), getWidth(), getHeight(), new Color(240, 248, 255, 245));
                g2d.setPaint(gradient);
                
                // Vẽ hình chữ nhật bo tròn
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                
                g2d.dispose();
            }
        };
        
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        int panelWidth = (int)(frameWidth * 0.5);
        int panelHeight = (int)(frameHeight * 0.75);
        panel.setBounds((frameWidth - panelWidth) / 2, (frameHeight - panelHeight) / 2, panelWidth, panelHeight);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("ĐĂNG KÝ");
        titleLabel.setFont(FontMain.TITLE_FONT);
        titleLabel.setForeground(ColorMain.colorPrimary);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // Underline
        JPanel underline = new JPanel();
        underline.setBackground(ColorMain.colorPrimary);
        underline.setPreferredSize(new Dimension(150, 3));
        JPanel underlineWrapper = new JPanel(new BorderLayout());
        underlineWrapper.setOpaque(false);
        underlineWrapper.add(underline, BorderLayout.CENTER);
        titlePanel.add(underlineWrapper, BorderLayout.SOUTH);
        
        panel.add(titlePanel, gbc);

        // Custom text fields
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel fullNameLabel = new JLabel("Họ tên");
        fullNameLabel.setFont(FontMain.LABEL_FONT);
        fullNameLabel.setForeground(ColorMain.colorPrimary);
        panel.add(fullNameLabel, gbc);
        
        gbc.gridx = 1;
        tfdFullName = createStyledTextField();
        panel.add(tfdFullName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel phoneLabel = new JLabel("Số điện thoại");
        phoneLabel.setFont(FontMain.LABEL_FONT);
        phoneLabel.setForeground(ColorMain.colorPrimary);
        panel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        tfdPhone = createStyledTextField();
        panel.add(tfdPhone, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        JLabel addressLabel = new JLabel("Địa chỉ");
        addressLabel.setFont(FontMain.LABEL_FONT);
        addressLabel.setForeground(ColorMain.colorPrimary);
        panel.add(addressLabel, gbc);
        
        gbc.gridx = 1;
        tfAddress = createStyledTextField();
        panel.add(tfAddress, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        JLabel passLabel = new JLabel("Mật khẩu");
        passLabel.setFont(FontMain.LABEL_FONT);
        passLabel.setForeground(ColorMain.colorPrimary);
        panel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        tfdPass = createStyledPasswordField();
        panel.add(tfdPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        JLabel confirmPassWordLabel = new JLabel("Xác nhận mật khẩu:");
        confirmPassWordLabel.setFont(FontMain.LABEL_FONT);
        confirmPassWordLabel.setForeground(ColorMain.colorPrimary);
        panel.add(confirmPassWordLabel, gbc);
        
        gbc.gridx = 1;
        tdfConfirmPass = createStyledPasswordField();
        panel.add(tdfConfirmPass, gbc);

        // Checkbox với style mới
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        termsCheckBox = new JCheckBox("Tôi đồng ý với điều khoản dịch vụ");
        termsCheckBox.setFont(new Font("Arial", Font.PLAIN, 13));
        termsCheckBox.setForeground(new Color(70, 70, 70));
        termsCheckBox.setOpaque(false);
        termsCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(termsCheckBox, gbc);

        // Nút đăng ký đẹp hơn
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        btnRegister = createStyledButton("Đăng ký");
        btnRegister.addActionListener(this);
        panel.add(btnRegister, gbc);

        // Thêm hoặc cải thiện các phần khác
        gbc.gridy = 11;
        JLabel orLabel = new JLabel("HOẶC");
        orLabel.setFont(new Font("Arial", Font.BOLD, 12));
        orLabel.setForeground(new Color(150, 150, 150));
        orLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(orLabel, gbc);

        gbc.gridy = 12;
        JLabel loginLabel = new JLabel("Bạn đã có tài khoản? Đăng nhập ngay");
        loginLabel.setFont(new Font("Arial", Font.BOLD, 13));
        loginLabel.setForeground(ColorMain.colorSecondary);
        loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new DangNhapView();
                dispose();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                loginLabel.setText("<html><u>Bạn đã có tài khoản? Đăng nhập ngay</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                loginLabel.setText("Bạn đã có tài khoản? Đăng nhập ngay");
            }
        });
        panel.add(loginLabel, gbc);

        layeredPane.add(panel, JLayeredPane.PALETTE_LAYER);
        this.add(layeredPane, BorderLayout.CENTER);

        // Đảm bảo các thành phần được cập nhật đúng kích thước
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Dimension size = getSize();
                layeredPane.setSize(size);
                backgroundLabel.setBounds(0, 0, size.width, size.height);
                int newPanelWidth = (int)(size.width * 0.5);
                int newPanelHeight = (int)(size.height * 0.75);
                panel.setBounds((size.width - newPanelWidth) / 2, (size.height - newPanelHeight) / 2, newPanelWidth, newPanelHeight);
            }
        });

        // Hiển thị frame
        this.setVisible(true);
    }
    
    // Tạo text field đẹp
    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(15);
        textField.setFont(FontMain.TEXT_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return textField;
    }
    
    // Tạo password field đẹp
    private JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(FontMain.TEXT_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)),BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return passwordField;
    }
    
    // Tạo nút có hiệu ứng gradient và hover
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient từ PRIMARY đến SECONDARY color
                GradientPaint gradient;
                if (hover) 
                    gradient = new GradientPaint(0, 0, ColorMain.colorSecondary, getWidth(), getHeight(), new Color(0, 119, 204));
                else 
                    gradient = new GradientPaint(0, 0, ColorMain.colorPrimary, getWidth(), getHeight(), ColorMain.colorSecondary);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Vẽ text
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.setColor(Color.WHITE);
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(200, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("Đăng ký")) {
            if (!termsCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(this, "Vui lòng đồng ý với điều khoản dịch vụ", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String fullName = tfdFullName.getText().trim();
            String phone = tfdPhone.getText().trim();
            String address = tfAddress.getText().trim();
            String pass = String.valueOf(tfdPass.getPassword()).trim();
            String confirmPass = String.valueOf(tdfConfirmPass.getPassword()).trim();
            // Kiểm tra dữ liệu đầu vào cơ bản
            if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (confirmPass.isEmpty()){
                JOptionPane.showMessageDialog(this, "Vui lòng xác nhận lại mật khẩu","Thông báo",JOptionPane.WARNING_MESSAGE);
                return;
            }
            String message = business.xuLyDangKy(fullName, phone, address, pass, confirmPass);
            // Hiển thị thông báo đẹp hơn
            ImageIcon icon = new ImageIcon(getClass().getResource("/me/myproject/IMAGE/logo.png"));
            JLabel label = new JLabel(message, icon, JLabel.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            JOptionPane.showMessageDialog(this, label, "Thông báo", message.contains("thành công") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new DangKyView();
    }
}
