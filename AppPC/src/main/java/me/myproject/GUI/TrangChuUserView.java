package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.myproject.BUSINESSLOGIC.KhachHangBSL;
import me.myproject.MODEL.KhachHang;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.TokenStore;

public class TrangChuUserView extends FrameMain implements ActionListener {
    private TaiKhoan tk;
    private KhachHang khachHang;
    private KhachHangBSL khachHangBSL;
    private JPanel mainPanel, menuPanel, contentPanel, headerPanel;
    private JLabel logoLabel, timeLabel, userImageLabel, userTypeLabel;
    private JButton btnDatXe, btnHuyChuyenDi, btnLichSuDiChuyen, btnChatVoiTaiXe, btnDangXuat, btnThongTinCaNhan, btnSanPhieu;
    private DateTimeFormatter timeFormatter;
    private Timer timeTimer;
    
    public TrangChuUserView(TaiKhoan taiKhoan) {
        super("Trang Chủ - Ứng Dụng Đặt Xe");
        tk = taiKhoan;
        this.khachHangBSL = new KhachHangBSL();
        init();
    }
    
    private void init() {
       
        // Thiết lập layout chính
        this.setLayout(new BorderLayout());
        
        // Lấy kích thước của FrameMain
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
        
        // Tạo các panel chính
        createMainPanels();
        
        // Tạo header
        createHeader();
        
        // Tạo menu bên trái
        createLeftMenu();

         try {
            initData();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        // Tạo nội dung chính
        createContent();
        
        // Thêm các panel vào layered pane
        mainPanel.setBounds(0, 70, frameWidth, frameHeight - 70);
        layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);
        
        headerPanel.setBounds(0, 0, frameWidth, 70);
        layeredPane.add(headerPanel, JLayeredPane.PALETTE_LAYER);
        
        this.add(layeredPane, BorderLayout.CENTER);
        
        // Đảm bảo rằng các thành phần được cập nhật đúng kích thước
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Dimension size = getSize();
                layeredPane.setSize(size);
                backgroundLabel.setBounds(0, 0, size.width, size.height);
                headerPanel.setBounds(0, 0, size.width, 70);
                mainPanel.setBounds(0, 70, size.width, size.height - 70);
            }
        });
        
        // Hiển thị frame
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    
   private void initData() throws IOException {
        if (tk != null) {
            Gson gson = new Gson();
            Map<String, Object> responseMap = khachHangBSL.layThongTinKhachHang();
            // Giả sử 'jsonString' là nội dung server trả về

             JsonElement jsonElement = gson.toJsonTree(responseMap);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
           
           
            if (jsonObject.has("result") && !jsonObject.get("result").isJsonNull()) {
                    KhachHang khachHang = gson.fromJson(jsonObject.get("result"), KhachHang.class);
                    if(khachHang!= null){
                        this.khachHang = khachHang;
                        userTypeLabel.setText("Khách hàng: " + khachHang.getCustomerName());

                    }
                    else {
                        System.err.println("Không thể chuyển đổi thông tin khách hàng từ JSON.");
                    }

                   
                    
            }
        }
    }

    private void createMainPanels() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        
        // Panel nội dung chính
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(255, 255, 255, 200)); // Màu trắng với độ trong suốt
        
        // Panel header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(20, 180, 180, 230)); // Màu xanh ngọc với độ trong suốt 
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 70));
        
        // Panel menu bên trái
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(25, 37, 60)); // Màu xanh đậm với độ trong suốt
        menuPanel.setPreferredSize(new Dimension(200, this.getHeight()));

        // Thêm các panel vào panel chính
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(menuPanel, BorderLayout.WEST);
    }
    
    private void createHeader() {
        // Panel bên trái của header
        JPanel leftHeaderPanel = new JPanel();
        leftHeaderPanel.setLayout(new BoxLayout(leftHeaderPanel, BoxLayout.X_AXIS));
        leftHeaderPanel.setBackground(new Color(20, 180, 180, 0)); 
        leftHeaderPanel.setBorder(new EmptyBorder(5, 10, 5, 5)); 
        
        // Thêm hình ảnh logo bên trái chữ CRAB
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(60, 60));
        ImageIcon logoApp = new ImageIcon(getClass().getResource("/me/myproject/IMAGE/logo1.png"));
        Image logoImg = logoApp.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        logoApp = new ImageIcon(logoImg);
        JLabel logoImageLabel = new JLabel(logoApp);
        logoImageLabel.setBorder(new EmptyBorder(0, 0, 0, 10)); // Thêm padding bên phải
        layeredPane.add(logoImageLabel, JLayeredPane.PALETTE_LAYER);

        // Logo và tên ứng dụng
        logoLabel = new JLabel("CRAB");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 23)); 
        logoLabel.setForeground(Color.WHITE);
        
        // Thêm hình ảnh và văn bản vào panel header bên trái
        leftHeaderPanel.add(logoImageLabel);
        leftHeaderPanel.add(logoLabel);
        
        // Đảm bảo logoImageLabel hiển thị
        logoImageLabel.setVisible(true);
        logoImageLabel.setOpaque(false);
        leftHeaderPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        // Panel giữa header
        JPanel centerHeaderPanel = new JPanel(new GridBagLayout()); // canh giữa theo trục Y
        centerHeaderPanel.setBackground(new Color(20, 180, 180, 0)); 
        
        // Tiêu đề và lời chào
        JLabel titleLabel = new JLabel("ỨNG DỤNG ĐẶT XE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 23));
        titleLabel.setForeground(Color.WHITE);
        centerHeaderPanel.add(titleLabel); 
    
        // Panel bên phải với thời gian và thông tin người dùng
        JPanel rightHeaderPanel = new JPanel();
        rightHeaderPanel.setLayout(new BoxLayout(rightHeaderPanel, BoxLayout.X_AXIS));
        rightHeaderPanel.setBackground(new Color(20, 180, 180, 0)); 
        rightHeaderPanel.setBorder(new EmptyBorder(5, 20, 5, 50)); // Giảm padding phải để tránh tràn
        
        // Hiển thị thời gian
        timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timeLabel.setForeground(Color.WHITE);
        updateTime(); // Cập nhật lần đầu
        
        // Tạo timer để cập nhật mỗi giây
        timeTimer = new Timer(1000, e -> updateTime());
        timeTimer.start();
        
        // Thông tin người dùng
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(20, 180, 180, 0)); // Trong suốt
        userInfoPanel.setMaximumSize(new Dimension(100, 50)); // Giới hạn kích thước panel thông tin
        
        // Hình ảnh người dùng
        ImageIcon userIcon = new ImageIcon(getClass().getResource("/me/myproject/IMAGE/user.png")); 
        Image userImg = userIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH); 
        userImageLabel = new JLabel(new ImageIcon(userImg));
        userImageLabel.setPreferredSize(new Dimension(60, 60)); 
        userImageLabel.setMaximumSize(new Dimension(60, 60));
        userImageLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        userImageLabel.setHorizontalAlignment(JLabel.CENTER);
        userImageLabel.setVerticalAlignment(JLabel.CENTER);

        // Đặt layout cho userInfoPanel để có thể kiểm soát khoảng cách tốt hơn
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(new Color(20, 180, 180, 0)); // Trong suốt
        userInfoPanel.setMaximumSize(new Dimension(100, 60)); // Tăng chiều cao tối đa
    
        // Thêm khoảng trống lớn hơn giữa hai nhãn
        userTypeLabel = new JLabel("Khách hàng");
        userTypeLabel.setFont(new Font("Arial", Font.BOLD, 13)); 
        userTypeLabel.setForeground(Color.WHITE);
        userTypeLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT); 
        userTypeLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
    
        userInfoPanel.add(Box.createVerticalGlue()); 
        userInfoPanel.add(userTypeLabel);
        userInfoPanel.add(Box.createVerticalGlue());
        
        // Thêm các thành phần vào rightHeaderPanel với khoảng cách giảm
        rightHeaderPanel.add(timeLabel);
        rightHeaderPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Giảm khoảng cách
        rightHeaderPanel.add(userImageLabel);
        rightHeaderPanel.add(Box.createRigidArea(new Dimension(5, 0))); // Giảm khoảng cách
        rightHeaderPanel.add(userInfoPanel);
    
        // Đảm bảo panel có kích thước hợp lý và không bị tràn
        rightHeaderPanel.setMaximumSize(new Dimension(250, 60));
    
        // Thêm các panel vào header
        headerPanel.add(leftHeaderPanel, BorderLayout.WEST);
        headerPanel.add(centerHeaderPanel, BorderLayout.CENTER);
        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);
    }
    
    // Phương thức cập nhật thời gian
    private void updateTime() {
        LocalTime now = LocalTime.now();
        timeLabel.setText(timeFormatter.format(now));
    }
    
    @Override
    public void dispose() {
        if (timeTimer != null && timeTimer.isRunning()) {
            timeTimer.stop();
        }
        super.dispose();
    }
    
    private void createContent() {
        // Panel chứa nội dung chính
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(new Color(255, 255, 255, 200)); // Màu trắng với độ trong suốt
        welcomePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Thêm khoảng trắng
        welcomePanel.add(Box.createVerticalGlue());
        
        // Logo ứng dụng (placeholder)
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        logoPanel.setMaximumSize(new Dimension(200, 200));
        logoPanel.setPreferredSize(new Dimension(200, 200));
        logoPanel.setAlignmentX(0.5f);
        //logoPanel.add(logoContentLabel, BorderLayout.CENTER);

        ImageIcon logoContentIcon = new ImageIcon(getClass().getResource("/me/myproject/IMAGE/logo.png"));
        Image logoContentImg = logoContentIcon.getImage().getScaledInstance(190, 190, Image.SCALE_SMOOTH);
        JLabel logoContentLabel = new JLabel(new ImageIcon(logoContentImg));
        logoContentLabel.setHorizontalAlignment(JLabel.CENTER);
        logoPanel.add(logoContentLabel, BorderLayout.CENTER);
        welcomePanel.add(logoPanel);
        
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Tiêu đề chào mừng
        JLabel welcomeTitleLabel = new JLabel("Chào Mừng Đến CRAB");
        welcomeTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeTitleLabel.setForeground(new Color(20, 180, 180));
        welcomeTitleLabel.setAlignmentX(0.5f);
        welcomePanel.add(welcomeTitleLabel);
        
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Thông báo
        JLabel instructionLabel = new JLabel("Chọn một dịch vụ từ menu bên trái để bắt đầu. Chúng tôi luôn");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel.setAlignmentX(0.5f);
        welcomePanel.add(instructionLabel);
        
        JLabel instructionLabel2 = new JLabel("sẵn sàng phục vụ bạn 24/7.");
        instructionLabel2.setFont(new Font("Arial", Font.PLAIN, 14));
        instructionLabel2.setAlignmentX(0.5f);
        welcomePanel.add(instructionLabel2);
        
        welcomePanel.add(Box.createVerticalGlue());
        
        contentPanel.add(welcomePanel, BorderLayout.CENTER);
    }
    private void createLeftMenu() {
        // Xóa tất cả các thành phần hiện có khỏi menuPanel
        menuPanel.removeAll();
        
        // Đặt layout tuyệt đối để định vị thành phần chính xác
        menuPanel.setLayout(null);
        
        // Tạo các nút menu
        btnDatXe = createMenuButton("Đặt Xe");
        btnHuyChuyenDi = createMenuButton("Hủy Chuyến Đi");
        btnLichSuDiChuyen = createMenuButton("Lịch Sử Đi Chuyển");
        btnChatVoiTaiXe = createMenuButton("Chat Với Tài Xế");
        btnThongTinCaNhan = createMenuButton("Thông Tin Cá Nhân");
        btnSanPhieu = createMenuButton("Mã Giảm Giá");
        btnDangXuat = createMenuButton("Đăng Xuất");
        
        // Màu đặc biệt cho nút đăng xuất
        btnDangXuat.setForeground(new Color(255, 100, 100)); // Màu đỏ nhạt
        
        // Đặt vị trí và kích thước cho các nút
        int buttonHeight = 40;
        int buttonWidth = 180;
        int leftMargin = 10;
        
        // Định vị các nút menu chính
        btnDatXe.setBounds(leftMargin, 30, buttonWidth, buttonHeight);
        btnHuyChuyenDi.setBounds(leftMargin, 30 + buttonHeight, buttonWidth, buttonHeight);
        btnLichSuDiChuyen.setBounds(leftMargin, 30 + buttonHeight * 2, buttonWidth, buttonHeight);
        btnChatVoiTaiXe.setBounds(leftMargin, 30 + buttonHeight * 3, buttonWidth, buttonHeight);
        btnThongTinCaNhan.setBounds(leftMargin, 30 + buttonHeight * 4, buttonWidth, buttonHeight);
        btnSanPhieu.setBounds(leftMargin, 30 + buttonHeight * 5, buttonWidth, buttonHeight);
        
        // Lấy chiều cao panel trừ chiều cao header để tránh chồng chéo
        int menuPanelHeight = this.getHeight() - 70;
        
        // Định vị nút đăng xuất ở dưới cùng, cách 50px từ cạnh dưới
        btnDangXuat.setBounds(leftMargin, menuPanelHeight - buttonHeight - 50, buttonWidth, buttonHeight);
        
        menuPanel.add(btnDatXe);
        menuPanel.add(btnHuyChuyenDi);
        menuPanel.add(btnLichSuDiChuyen);
        menuPanel.add(btnChatVoiTaiXe);
        menuPanel.add(btnThongTinCaNhan);
        menuPanel.add(btnSanPhieu);
        menuPanel.add(btnDangXuat);
        
        // Đánh dấu nút mặc định được chọn
        btnDatXe.setForeground(new Color(20, 180, 180)); // Màu ngọc lam
        
        // Cập nhật vị trí nút đăng xuất khi kích thước cửa sổ thay đổi
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int updatedMenuPanelHeight = getHeight() - 70;
                btnDangXuat.setBounds(leftMargin, updatedMenuPanelHeight - buttonHeight - 50, buttonWidth, buttonHeight);
            }
        });
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        
        // Thiết lập giao diện nút
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(25, 37, 60));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(8, 15, 8, 10));
        
        // Chỉ thay đổi con trỏ thành hình bàn tay khi di chuột qua, không thay đổi màu chữ
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                // Không thay đổi màu khi di chuột qua như yêu cầu
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                // Không thay đổi màu khi di chuột ra khỏi như yêu cầu
            }
        });
        
        // Ngăn chặn đổi màu nền khi nhấn
        button.setPressedIcon(button.getIcon());
        button.setRolloverEnabled(false);
        
        // Thêm trình lắng nghe sự kiện cho xử lý khi nhấp chuột
        button.addActionListener(this);
        
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Đặt lại màu tất cả các nút
        resetButtonColors();
        Object source = e.getSource();
        if (source == btnDatXe) {
            btnDatXe.setForeground(new Color(20, 180, 180));
            // Hiển thị giao diện chọn điểm
            new ChonDiemView(tk);
        } else if (source == btnHuyChuyenDi) {
            btnHuyChuyenDi.setForeground(new Color(20, 180, 180)); 
            // Hiển thị giao diện hủy chuyến đi
            try {
                new HuyChuyenDiView(tk);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (source == btnLichSuDiChuyen) {
            btnLichSuDiChuyen.setForeground(new Color(20, 180, 180));
            this.dispose();
            try {
                new LichSuChuyenDiView(tk);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (source == btnChatVoiTaiXe) {
            btnChatVoiTaiXe.setForeground(new Color(20, 180, 180));
            // Hiển thị giao diện chat với tài xế
        }
        else if (source == btnSanPhieu) {
            btnSanPhieu.setForeground(new Color(20, 180, 180));
            new SanPhieuGiamGiaView(tk);
        }
        else if (source == btnThongTinCaNhan) {
            btnThongTinCaNhan.setForeground(new Color(20, 180, 180));
            System.out.println("Thông tin khách hàng: " + khachHang);
            // Hiển thị giao diện thông tin cá nhân
            try {
                new ThongTinCaNhanKHView(khachHang);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        else if (source == btnDangXuat) {
            // Xử lý đăng xuất
            try {
                if (tk != null && tk.getRefreshToken() != null && !tk.getRefreshToken().isBlank()) {
                    String refreshToken = URLEncoder.encode(tk.getRefreshToken(), StandardCharsets.UTF_8);
                    APIHelper.postForMap(AppConfig.BASE_URL + "/auth/logout?refreshToken=" + refreshToken, Map.of());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                TokenStore.clearTokens();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            APIHelper.clearAuthToken();
            this.dispose();
            new DangNhapView();
        }
    }

    //Phương thức trợ giúp để đặt lại màu cho tất cả các nút
    private void resetButtonColors() {
        btnDatXe.setForeground(Color.WHITE);
        btnHuyChuyenDi.setForeground(Color.WHITE);
        btnLichSuDiChuyen.setForeground(Color.WHITE);
        btnChatVoiTaiXe.setForeground(Color.WHITE);
        btnThongTinCaNhan.setForeground(Color.WHITE);
        btnSanPhieu.setForeground(Color.WHITE);
        btnDangXuat.setForeground(new Color(255, 100, 100)); // Giữ màu đặc biệt cho nút đăng xuất
    }
}
