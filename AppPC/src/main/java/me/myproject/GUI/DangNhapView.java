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
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import me.myproject.BUSINESSLOGIC.DangNhapBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.MODEL.TaiXe;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.FontMain;

public class DangNhapView extends FrameMain implements ActionListener {
    private final DangNhapBSL bussiness;
    public JTextField tfdPhone;
    public JPasswordField tfdPass;
    public JComboBox<String> cbRole;
    public JButton btnLogin, btnForgetPass;
    private JCheckBox rememberPasswordCheckBox;

    private JLabel phoneErrorLabel, passErrorLabel;

    public DangNhapView() {
        super("Đăng nhập");
        bussiness = new DangNhapBSL();
        init();
    }

    private void init() {
        this.setLayout(new BorderLayout());

        Dimension frameDimension = this.getSize();
        int frameWidth = frameDimension.width;
        int frameHeight = frameDimension.height;

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

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

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 255, 255, 245), getWidth(), getHeight(), new Color(240, 248, 255, 245));
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2d.dispose();
            }
        };

        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        int panelWidth = (int)(frameWidth * 0.5);
        int panelHeight = (int)(frameHeight * 0.7);
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

        JLabel titleLabel = new JLabel("ĐĂNG NHẬP");
        titleLabel.setFont(FontMain.TITLE_FONT);
        titleLabel.setForeground(ColorMain.colorPrimary);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        JPanel underline = new JPanel();
        underline.setBackground(ColorMain.colorPrimary);
        underline.setPreferredSize(new Dimension(150, 3));
        JPanel underlineWrapper = new JPanel(new BorderLayout());
        underlineWrapper.setOpaque(false);
        underlineWrapper.add(underline, BorderLayout.CENTER);
        titlePanel.add(underlineWrapper, BorderLayout.SOUTH);

        panel.add(titlePanel, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel phoneLabel = new JLabel("Số điện thoại");
        phoneLabel.setFont(FontMain.LABEL_FONT);
        phoneLabel.setForeground(ColorMain.colorPrimary);
        phoneLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        tfdPhone = createStyledTextField();
        panel.add(tfdPhone, gbc);

        phoneErrorLabel = createErrorLabel("");
        gbc.gridy = 2;
        panel.add(phoneErrorLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel passLabel = new JLabel("Mật khẩu");
        passLabel.setFont(FontMain.LABEL_FONT);
        passLabel.setForeground(ColorMain.colorPrimary);
        passLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(passLabel, gbc);
        gbc.gridx = 1;
        tfdPass = createStyledPasswordField();
        panel.add(tfdPass, gbc);

        passErrorLabel = createErrorLabel("");
        gbc.gridy = 4;
        panel.add(passErrorLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel roleLabel = new JLabel("Vai trò");
        roleLabel.setFont(FontMain.LABEL_FONT);
        roleLabel.setForeground(ColorMain.colorPrimary);
        roleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        panel.add(roleLabel, gbc);
        gbc.gridx = 1;
        String[] roles = {"CUSTOMER", "DRIVER", "ADMIN"};
        cbRole = new JComboBox<>(roles);
        cbRole.setFont(FontMain.TEXT_FONT);
        cbRole.setBackground(Color.WHITE);
        cbRole.setSelectedIndex(0);
        cbRole.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(1, 2, 0, 2)));
        panel.add(cbRole, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        rememberPasswordCheckBox = new JCheckBox("Nhớ mật khẩu");
        rememberPasswordCheckBox.setFont(new Font("Arial", Font.PLAIN, 13));
        rememberPasswordCheckBox.setForeground(new Color(70, 70, 70));
        rememberPasswordCheckBox.setOpaque(false);
        panel.add(rememberPasswordCheckBox, gbc);

        gbc.gridx = 1;
        btnForgetPass = new JButton("Quên mật khẩu?");
        btnForgetPass.setFont(new Font("Arial", Font.BOLD, 13));
        btnForgetPass.setBorderPainted(false);
        btnForgetPass.setContentAreaFilled(false);
        btnForgetPass.setForeground(ColorMain.colorSecondary);
        btnForgetPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgetPass.setFocusPainted(false);
        btnForgetPass.addActionListener(this);
        panel.add(btnForgetPass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        btnLogin = createStyledButton("Đăng nhập");
        btnLogin.addActionListener(this);
        panel.add(btnLogin, gbc);

        gbc.gridy = 8;
        JLabel orLabel = new JLabel("HOẶC");
        orLabel.setFont(new Font("Arial", Font.BOLD, 12));
        orLabel.setForeground(new Color(150, 150, 150));
        orLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(orLabel, gbc);

        gbc.gridy = 9;
        JLabel signUpLabel = new JLabel("Bạn chưa có tài khoản? Đăng ký ngay");
        signUpLabel.setFont(new Font("Arial", Font.BOLD, 13));
        signUpLabel.setForeground(ColorMain.colorSecondary);
        signUpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        signUpLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new DangKyView();
                dispose();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                signUpLabel.setText("<html><u>Bạn chưa có tài khoản? Đăng ký ngay</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                signUpLabel.setText("Bạn chưa có tài khoản? Đăng ký ngay");
            }
        });
        panel.add(signUpLabel, gbc);

        layeredPane.add(panel, JLayeredPane.PALETTE_LAYER);
        this.add(layeredPane, BorderLayout.CENTER);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Dimension size = getSize();
                layeredPane.setSize(size);
                backgroundLabel.setBounds(0, 0, size.width, size.height);
                int newPanelWidth = (int)(size.width * 0.5);
                int newPanelHeight = (int)(size.height * 0.7);
                panel.setBounds((size.width - newPanelWidth) / 2, (size.height - newPanelHeight) / 2, newPanelWidth, newPanelHeight);
            }
        });

        this.setVisible(true);
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(15);
        textField.setFont(FontMain.TEXT_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return textField;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(FontMain.TEXT_FONT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return passwordField;
    }

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

                GradientPaint gradient;
                if (hover)
                    gradient = new GradientPaint(0, 0, ColorMain.colorSecondary, getWidth(), getHeight(), new Color(0, 119, 204));
                else
                    gradient = new GradientPaint(0, 0, ColorMain.colorPrimary, getWidth(), getHeight(), ColorMain.colorSecondary);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

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

    private JLabel createErrorLabel(String errorMessage) {
        JLabel label = new JLabel(errorMessage);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(Color.RED);
        return label;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("Đăng nhập")) {
            String phone = tfdPhone.getText().trim();
            String pass = String.valueOf(tfdPass.getPassword()).trim();
            String role = (String) cbRole.getSelectedItem();

            // Kiểm tra dữ liệu nhập vào
            if (phone.isEmpty() || pass.isEmpty()) {
                if (phone.isEmpty()) phoneErrorLabel.setText("Số điện thoại không được để trống");
                else phoneErrorLabel.setText("");
                if (pass.isEmpty()) passErrorLabel.setText("Mật khẩu không được để trống");
                else passErrorLabel.setText("");
                return;
            }

            // Kiểm tra định dạng số điện thoại (ví dụ: kiểm tra chỉ chứa số và đủ 10 chữ số)
            if (!phone.matches("\\d{10}")) {
                phoneErrorLabel.setText("Số điện thoại không hợp lệ.");
                return;
            } else {
                phoneErrorLabel.setText(""); 
            }

            // Kiểm tra mật khẩu có đủ độ dài (ví dụ: ít nhất 6 ký tự)
            if (pass.length() < 3) {
                passErrorLabel.setText("Mật khẩu phải ít nhất 6 ký tự.");
                return;
            } else 
                passErrorLabel.setText(""); 

            boolean rememberToken = rememberPasswordCheckBox.isSelected();
            Map<String, Object> result = bussiness.xuLyDangNhap(phone, pass, role, rememberToken);
            String message = (String) result.get("message");
            TaiKhoan taiKhoan = (TaiKhoan) result.get("taiKhoan");
            ImageIcon icon = new ImageIcon(getClass().getResource("/me/myproject/IMAGE/logo.png"));
            JLabel label = new JLabel(message, icon, JLabel.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            JOptionPane.showMessageDialog(this, label, "Thông báo", taiKhoan != null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
  
            if (taiKhoan != null) {
                switch (taiKhoan.getID_VaiTro().trim()) {
                    case "CUSTOMER":
                        new TrangChuUserView(taiKhoan);
                        break;
                    case "DRIVER":
                            new TrangChuDriverView(taiKhoan);
                        break;
                    case "ADMIN":
                        new TrangChuAdminView(taiKhoan);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Vai trò không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                }
                dispose();
            }

        } else if (action.equals("Quên mật khẩu?")) {
            String phone = tfdPhone.getText().trim();
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số điện thoại để khôi phục mật khẩu", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String message = bussiness.xuLyQuenMatKhau(phone);
            JOptionPane.showMessageDialog(this, message, "Thông báo", message.contains("Mã OTP đã được gửi") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (!message.contains("Mã OTP đã được gửi")) return;

            JTextField otpField = new JTextField(10);
            otpField.setFont(new Font("Arial", Font.BOLD, 14));
            otpField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(8, 10, 8, 10)));

            JPanel otpPanel = new JPanel(new BorderLayout(10, 10));
            otpPanel.add(new JLabel("Nhập mã OTP đã gửi:"), BorderLayout.NORTH);
            otpPanel.add(otpField, BorderLayout.CENTER);

            int otpResult = JOptionPane.showConfirmDialog(this, otpPanel, "Xác nhận OTP", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (otpResult != JOptionPane.OK_OPTION) return;

            String otpInput = otpField.getText().trim();
            String otpValidMessage = bussiness.xacThucOTP(phone, otpInput);

            JOptionPane.showMessageDialog(this, otpValidMessage, "Thông báo", otpValidMessage.contains("thành công") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

            if (!otpValidMessage.contains("thành công")) return;

            JPasswordField newPassField = new JPasswordField();
            JPasswordField confirmPassField = new JPasswordField();
            newPassField.setFont(FontMain.TEXT_FONT);
            confirmPassField.setFont(FontMain.TEXT_FONT);
            newPassField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            confirmPassField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(8, 10, 8, 10)));

            JPanel passPanel = new JPanel(new GridBagLayout());
            GridBagConstraints passGbc = new GridBagConstraints();
            passGbc.fill = GridBagConstraints.HORIZONTAL;
            passGbc.insets = new Insets(5, 5, 5, 5);
            passGbc.gridx = 0;
            passGbc.gridy = 0;
            passPanel.add(new JLabel("Mật khẩu mới:"), passGbc);
            passGbc.gridx = 1;
            passGbc.weightx = 1.0;
            passPanel.add(newPassField, passGbc);
            passGbc.gridx = 0;
            passGbc.gridy = 1;
            passGbc.weightx = 0.0;
            passPanel.add(new JLabel("Xác nhận mật khẩu:"), passGbc);
            passGbc.gridx = 1;
            passGbc.weightx = 1.0;
            passPanel.add(confirmPassField, passGbc);

            int option = JOptionPane.showConfirmDialog(this, passPanel, "Đổi mật khẩu",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                String newPass = new String(newPassField.getPassword()).trim();
                String confirmPass = new String(confirmPassField.getPassword()).trim();
                String updateMessage = bussiness.capNhatMatKhau(phone, newPass, confirmPass);
                ImageIcon icon = new ImageIcon(getClass().getResource("/me/myproject/IMAGE/logo.png"));
                JLabel label = new JLabel(updateMessage, icon, JLabel.CENTER);
                label.setFont(new Font("Arial", Font.PLAIN, 14));
                JOptionPane.showMessageDialog(this, label, "Thông báo",updateMessage.contains("thành công") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
