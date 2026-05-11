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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;

public class WelcomeView extends FrameMain implements ActionListener {
    private JButton btnLogin, btnRegister;
    
    public WelcomeView() {
        super("Chào mừng đến với ứng dụng đặt xe");
        init();
    }
    
    private void init() {
        this.setLayout(new BorderLayout());
        
        // Lấy kích thước của FrameMain
        Dimension frameDimension = this.getSize();
        int frameWidth = frameDimension.width;
        int frameHeight = frameDimension.height;
        
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));
        
        // Hình nền - phủ toàn bộ frame với hình nền hấp dẫn hơn
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
        
        // Panel chính với hiệu ứng gradient và bo tròn
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ background với gradient đẹp hơn
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 255, 255, 245), getWidth(), getHeight(), new Color(230, 246, 255, 245));
                g2d.setPaint(gradient);
                
                // Vẽ hình chữ nhật bo tròn với độ cong lớn hơn
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
                
                // Thêm shadow nhẹ
                g2d.setColor(new Color(0, 0, 0, 20));
                for(int i = 0; i < 5; i++) 
                    g2d.draw(new RoundRectangle2D.Double(i, i, getWidth() - i*2, getHeight() - i*2, 30, 30));
                g2d.dispose();
            }
        };
        
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        int panelWidth = (int)(frameWidth * 0.7);
        int panelHeight = (int)(frameHeight * 0.85);
        mainPanel.setBounds((frameWidth - panelWidth) / 2, (frameHeight - panelHeight) / 2, panelWidth, panelHeight);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Logo panel với hiệu ứng đẹp hơn
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JPanel logoPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);       
                g2d.dispose();
            }
        };
        logoPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel();
        URL logoUrl = getClass().getResource("/me/myproject/IMAGE/logo.png");
        if (logoUrl != null) {
            ImageIcon logoIcon = new ImageIcon(logoUrl);
            Image logoImage = logoIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(logoImage));
        } else {
            System.err.println("Không tìm thấy logo: /me/myproject/IMAGE/logo.png");
        }
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoPanel.add(logoLabel, BorderLayout.CENTER);
        mainPanel.add(logoPanel, gbc);
        
        // Title Panel với animation effect
        gbc.gridy = 1;
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        // App Name với font đẹp hơn
        JLabel appNameLabel = new JLabel("BOOKING APP");
        Font titleFont = new Font("Arial", Font.BOLD, 38);
        appNameLabel.setFont(titleFont);
        appNameLabel.setForeground(ColorMain.colorBlueBlack1); 
        appNameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        titlePanel.add(appNameLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Line separator nâng cao
        JPanel linePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient line
                GradientPaint gradient = new GradientPaint(0, 0, new Color(41, 128, 185, 50), getWidth(), 0, new Color(41, 128, 185, 255));
                g2d.setPaint(gradient);
                g2d.fillRect(getWidth()/4, 0, getWidth()/2, 3);
                g2d.dispose();
            }
        };
        linePanel.setPreferredSize(new Dimension(300, 3));
        linePanel.setMaximumSize(new Dimension(300, 3));
        linePanel.setOpaque(false);
        linePanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        titlePanel.add(linePanel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Slogan đẹp hơn
        JLabel sloganLabel = new JLabel("Di chuyển nhanh chóng - An toàn - Tiện lợi");
        sloganLabel.setFont(new Font("Arial", Font.ITALIC, 20));
        sloganLabel.setForeground(ColorMain.colorBlueBlack2); 
        sloganLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        titlePanel.add(sloganLabel);
        
        mainPanel.add(titlePanel, gbc);
        
        // Description Panel với style hiện đại hơn
        gbc.gridy = 2;
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setOpaque(false);
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200, 100), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            )
        ));
        
        JLabel descLabel = new JLabel("<html><div style='text-align: center;'>Ứng dụng đặt xe trực tuyến giúp bạn di chuyển dễ dàng. "
                + "Với mạng lưới tài xế rộng khắp, chúng tôi cam kết mang đến trải nghiệm di chuyển an toàn, "
                + "nhanh chóng và tiện lợi. Đặt xe ngay để trải nghiệm dịch vụ đẳng cấp!</div></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        descLabel.setForeground(new Color(40, 40, 40));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descPanel.add(descLabel, BorderLayout.CENTER);
        
        mainPanel.add(descPanel, gbc);
        
        // Features Panel với icon và hiệu ứng
        gbc.gridy = 3;
        JPanel featuresPanel = new JPanel(new GridBagLayout());
        featuresPanel.setOpaque(false);
        featuresPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        
        String[] featureIcons = {"⚡", "🔍", "🚗", "💳"};
        String[] features = {
                "Đặt xe nhanh chóng, chỉ với vài thao tác đơn giản",
                "Theo dõi vị trí tài xế trực tiếp trên bản đồ",
                "Nhiều loại phương tiện để lựa chọn",
                "Thanh toán đa dạng và an toàn"
        };
        
        GridBagConstraints featureConstraints = new GridBagConstraints();
        featureConstraints.insets = new Insets(8, 10, 8, 10);
        featureConstraints.fill = GridBagConstraints.HORIZONTAL;
        featureConstraints.anchor = GridBagConstraints.WEST;
        
        for (int i = 0; i < features.length; i++) {
            featureConstraints.gridx = 0;
            featureConstraints.gridy = i;
            
            JPanel featureItemPanel = new JPanel(new BorderLayout(10, 0));
            featureItemPanel.setOpaque(false);
            
            // Feature icon với circle background
            JLabel iconLabel = new JLabel(featureIcons[i]) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Circle background
                    g2d.setColor(new Color(52, 152, 219, 40));
                    g2d.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                    
                    g2d.setColor(new Color(52, 152, 219, 80));
                    g2d.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                    
                    g2d.dispose();
                    super.paintComponent(g);
                }
            };
            iconLabel.setFont(new Font("Dialog", Font.PLAIN, 20));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setPreferredSize(new Dimension(40, 40));
            
            JLabel featureLabel = new JLabel(features[i]);
            featureLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            featureLabel.setForeground(new Color(40, 40, 40));
            
            featureItemPanel.add(iconLabel, BorderLayout.WEST);
            featureItemPanel.add(featureLabel, BorderLayout.CENTER);
            featuresPanel.add(featureItemPanel, featureConstraints);
        }
        
        mainPanel.add(featuresPanel, gbc);
        
        // Buttons Panel với căn giữa tốt hơn
        gbc.gridy = 4;
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        
        // Tạo khoảng trống bên trái để căn giữa
        buttonsPanel.add(Box.createHorizontalGlue());
        
        btnLogin = createStyledButton("Đăng nhập", new Color(41, 128, 185), ColorMain.colorBlueBlack2);
        btnLogin.setActionCommand("login");
        btnLogin.addActionListener(this);
        
        buttonsPanel.add(btnLogin);
        buttonsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        
        btnRegister = createStyledButton("Đăng ký", ColorMain.colorBlueBlack2, new Color(41, 128, 185));
        btnRegister.setActionCommand("register");
        btnRegister.addActionListener(this);
        
        buttonsPanel.add(btnRegister);
        
        // Tạo khoảng trống bên phải để căn giữa
        buttonsPanel.add(Box.createHorizontalGlue());
        
        mainPanel.add(buttonsPanel, gbc);
        
        // Footer với style hiện đại hơn
        gbc.gridy = 5;
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JLabel footerLabel = new JLabel("© 2025 TAXI BOOKING APP - Mọi quyền được bảo lưu");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(120, 120, 120));
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(footerLabel, BorderLayout.CENTER);
        
        mainPanel.add(footerPanel, gbc);
        
        layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);
        this.add(layeredPane, BorderLayout.CENTER);
        
        // Đảm bảo các thành phần được cập nhật đúng kích thước khi resize
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Dimension newSize = getSize();
                layeredPane.setSize(newSize);
                backgroundLabel.setBounds(0, 0, newSize.width, newSize.height);
                
                int newPanelWidth = (int)(newSize.width * 0.7);
                int newPanelHeight = (int)(newSize.height * 0.85);
                mainPanel.setBounds((newSize.width - newPanelWidth) / 2, (newSize.height - newPanelHeight) / 2, newPanelWidth, newPanelHeight);
            }
        });
        
        this.setVisible(true);
    }
    
    // Tạo nút có hiệu ứng gradient, hover và shadow
    private JButton createStyledButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            private boolean hover = false;
            private boolean pressed = false; 
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
                    
                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                        repaint();
                    }
                    
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient và hover effect
                GradientPaint gradient;
                if (pressed) {
                    gradient = new GradientPaint(0, 0, darken(startColor), getWidth(), getHeight(), darken(endColor));
                } else if (hover) {
                    gradient = new GradientPaint(0, 0, lighten(endColor), getWidth(), getHeight(), lighten(startColor));
                } else {
                    gradient = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
                }
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                
                // Shadow effect when not pressed
                if (!pressed) {
                    g2d.setColor(new Color(0, 0, 0, 30));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                    g2d.setColor(new Color(0, 0, 0, 20));
                    g2d.drawRoundRect(0, 1, getWidth() - 1, getHeight() - 2, 20, 20);
                    g2d.setColor(new Color(0, 0, 0, 10));
                    g2d.drawRoundRect(0, 2, getWidth() - 1, getHeight() - 3, 20, 20);
                }
                
                // Vẽ text với hiệu ứng shadow text nhẹ
                if (!pressed) {
                    g2d.setColor(new Color(0, 0, 0, 40));
                    g2d.setFont(new Font("Arial", Font.BOLD, 18));
                    FontMetrics metrics = g2d.getFontMetrics();
                    int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                    int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                    g2d.drawString(getText(), x + 1, y + 1);
                }
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
                
                // Text position slightly adjusted when pressed to give "clicked" feel
                if (pressed) {
                    g2d.drawString(getText(), x + 1, y + 1);
                } else {
                    g2d.drawString(getText(), x, y);
                }
                
                g2d.dispose();
            }
        };
        
        button.setPreferredSize(new Dimension(180, 50));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    // Utility method to lighten a color
    private Color lighten(Color color) {
        int r = Math.min(255, (int)(color.getRed() * 1.2));
        int g = Math.min(255, (int)(color.getGreen() * 1.2));
        int b = Math.min(255, (int)(color.getBlue() * 1.2));
        return new Color(r, g, b);
    }
    
    // Utility method to darken a color
    private Color darken(Color color) {
        int r = (int)(color.getRed() * 0.8);
        int g = (int)(color.getGreen() * 0.8);
        int b = (int)(color.getBlue() * 0.8);
        return new Color(r, g, b);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if ("login".equals(action)) {
            new DangNhapView();
            dispose();
        } else if ("register".equals(action)) {
            new DangKyView();
            dispose();
        }
    }
    
    public static void main(String[] args) {
        new WelcomeView();
    }
}
