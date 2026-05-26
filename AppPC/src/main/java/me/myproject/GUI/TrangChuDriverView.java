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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import me.myproject.BUSINESSLOGIC.TaiXeBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.MODEL.TaiXe;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.AuthManager;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.MapUtil;
import me.myproject.Utilities.StompWebSocketClient;

public class TrangChuDriverView extends FrameMain implements ActionListener {
    private TaiKhoan tk;
    private TaiXe taiXe;
    private TaiXeBSL taiXeBSL;
    private JPanel mainPanel, headerPanel, menuPanel, contentPanel;
    private JLabel logoLabel, timeLabel, driverNameLabel, phuongTienLbl, bienSoXeLbl;
    private JLabel thuNhapNowLbl, chaoTaiXeLbl, onlineLabel, inforJLabel;
    private JLabel tripCountValueLabel, ratingValueLabel;
    private JButton btnBatDauChuyen, btnNhanChuyen, btnDoanhThu, btnTrangThai;
    private JButton btnTKNganHang, btnLichSuChuyen, btnThongTinCaNhan, btnDangXuat, btnTrangThaiHoatDong;
    private ImageIcon inforDriver;
    private Timer timeTimer;
    private DateTimeFormatter timeFormatter;
    private StompWebSocketClient webSocketClient;

    public TrangChuDriverView() throws IOException {
        super("Trang Chủ - Tài xế");
        this.tk = null;
        this.taiXe = null;
        this.taiXeBSL = new TaiXeBSL();
        init();
    }

    public TrangChuDriverView(TaiKhoan taiKhoan) {
        super("Trang Chủ - Tài xế");
        this.tk = taiKhoan;
        this.taiXe = null;
        this.taiXeBSL = new TaiXeBSL();
        init();
    }

    public void init() {
        this.setLayout(new BorderLayout());
        Dimension frameDimension = this.getSize();
        int frameWidth = frameDimension.width;
        int frameHeight = frameDimension.height;

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(frameWidth, frameHeight));

        // 1. Tải dữ liệu cơ bản trước để vẽ UI mượt hơn
        preloadTaiXeData();

        // 2. Khởi tạo toàn bộ giao diện
        createMainPanels();
        createHeader();
        createLeftMenu();
        createContent();

        // 3. Đổ dữ liệu Dashboard (Doanh thu, số chuyến...)
        try {
            initData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ráp panel
        mainPanel.setBounds(0, 70, frameWidth, frameHeight - 70);
        layeredPane.add(mainPanel, JLayeredPane.PALETTE_LAYER);

        headerPanel.setBounds(0, 0, frameWidth, 70);
        layeredPane.add(headerPanel, JLayeredPane.PALETTE_LAYER);

        this.add(layeredPane, BorderLayout.CENTER);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                Dimension size = getSize();
                layeredPane.setSize(size);
                headerPanel.setBounds(0, 0, size.width, 70);
                mainPanel.setBounds(0, 70, size.width, size.height - 70);
            }
        });

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        initWebSocket();
    }

    // Hàm lấy trước dữ liệu Tài xế để vẽ bản đồ và trạng thái
    private void preloadTaiXeData() {
        if (tk == null)
            return;
        try {
            Map<String, Object> responseMap = taiXeBSL.layThongTinTaiXe();
            if (responseMap != null) {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.toJsonTree(responseMap).getAsJsonObject();
                if (jsonObject.has("result") && !jsonObject.get("result").isJsonNull()) {
                    this.taiXe = gson.fromJson(jsonObject.get("result"), TaiXe.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Chưa thể tải dữ liệu tài xế ban đầu: " + e.getMessage());
        }
    }

    private void createHeader() {
        headerPanel.setBackground(ColorMain.blueHeader);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 70));

        JPanel headerContentPanel = new JPanel(new BorderLayout());
        headerContentPanel.setBackground(ColorMain.blueHeader);

        // -- LOGO --
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        logoPanel.setBackground(ColorMain.blueHeader);
        logoPanel.setBorder(new EmptyBorder(5, 10, 5, 0));
        logoPanel.setPreferredSize(new Dimension(150, 70));

        URL logoUrl = getClass().getResource("/me/myproject/IMAGE/logo1.png");
        ImageIcon logoApp = null;
        if (logoUrl != null) {
            logoApp = new ImageIcon(new ImageIcon(logoUrl).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH));
        }
        logoPanel.add(new JLabel(logoApp));

        logoLabel = new JLabel("CRAB");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);
        logoPanel.add(logoLabel);

        // -- STATUS & VEHICLE --
        JPanel statusPanel = new JPanel(new GridBagLayout());
        statusPanel.setBackground(ColorMain.blueHeader);
        statusPanel.setBorder(new EmptyBorder(0, 20, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);

        JLabel statusLabel = new JLabel("Hệ Thống Driver");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
        statusPanel.add(statusLabel, gbc);

        JPanel vehicleInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        vehicleInfoPanel.setBackground(ColorMain.blueHeader);

        phuongTienLbl = new JLabel(taiXe != null ? taiXe.getVehicleName() : "Đang tải...");
        bienSoXeLbl = new JLabel(taiXe != null ? taiXe.getLicensePlate() : "...");

        for (JLabel l : new JLabel[] { new JLabel("Tài Xế"), new JLabel("|"), phuongTienLbl, new JLabel("|"),
                bienSoXeLbl }) {
            l.setFont(new Font("Arial", Font.PLAIN, 12));
            l.setForeground(Color.WHITE);
            vehicleInfoPanel.add(l);
        }

        gbc.gridy = 1;
        statusPanel.add(vehicleInfoPanel, gbc);

        // -- CENTER (DASHBOARD METRICS) --
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(ColorMain.blueHeader);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        infoPanel.setBackground(ColorMain.blueHeader);

        JLabel incomeLabel = new JLabel("Thu nhập hôm nay");
        incomeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        incomeLabel.setForeground(Color.WHITE);

        thuNhapNowLbl = new JLabel("0 VND");
        thuNhapNowLbl.setFont(new Font("Arial", Font.BOLD, 14));
        thuNhapNowLbl.setForeground(Color.WHITE);

        JLabel separator = new JLabel(" | ");
        separator.setFont(new Font("Arial", Font.PLAIN, 14));
        separator.setForeground(Color.WHITE);

        timeLabel = new JLabel("Thời gian");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(Color.WHITE);

        timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        JLabel timeValueLabel = new JLabel();
        timeValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timeValueLabel.setForeground(Color.WHITE);

        timeTimer = new Timer(1000, e -> timeValueLabel.setText(timeFormatter.format(LocalTime.now())));
        timeTimer.start();

        infoPanel.add(incomeLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        infoPanel.add(thuNhapNowLbl);
        infoPanel.add(separator);
        infoPanel.add(timeLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        infoPanel.add(timeValueLabel);

        centerPanel.add(infoPanel);

        // -- DRIVER PROFILE RIGHT --
        JPanel driverPanel = new JPanel(new GridBagLayout());
        driverPanel.setBackground(ColorMain.blueHeader);
        driverPanel.setBorder(new EmptyBorder(0, 0, 0, 15));

        GridBagConstraints driverGbc = new GridBagConstraints();
        driverGbc.gridx = 0;
        driverGbc.gridy = 0;
        driverGbc.anchor = GridBagConstraints.EAST;
        driverGbc.insets = new Insets(0, 0, 5, 15);

        driverNameLabel = new JLabel(taiXe != null ? taiXe.getDriverName() : "Đang tải...");
        driverNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        driverNameLabel.setForeground(Color.WHITE);
        driverPanel.add(driverNameLabel, driverGbc);

        driverGbc.gridy = 1;
        onlineLabel = new JLabel("Ngoại tuyến");
        onlineLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        onlineLabel.setForeground(Color.WHITE);
        driverPanel.add(onlineLabel, driverGbc);

        JPanel leftCombinedPanel = new JPanel(new BorderLayout());
        leftCombinedPanel.setBackground(ColorMain.blueHeader);
        leftCombinedPanel.add(logoPanel, BorderLayout.WEST);
        leftCombinedPanel.add(statusPanel, BorderLayout.CENTER);

        headerContentPanel.add(leftCombinedPanel, BorderLayout.WEST);
        headerContentPanel.add(centerPanel, BorderLayout.CENTER);
        headerContentPanel.add(driverPanel, BorderLayout.EAST);

        headerPanel.removeAll();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(headerContentPanel, BorderLayout.CENTER);
    }

    private void createMainPanels() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(255, 255, 255, 200));
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.blueHeader);
        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(ColorMain.blueMenuLeft);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(menuPanel, BorderLayout.WEST);
    }

    private void createLeftMenu() {
        menuPanel.removeAll();
        menuPanel.setLayout(null);
        menuPanel.setPreferredSize(new Dimension(200, this.getHeight()));

        btnNhanChuyen = createMenuButton("Trang chủ");
        btnDoanhThu = createMenuButton("Doanh Thu");
        btnTrangThai = createMenuButton("Trạng Thái");
        btnTKNganHang = createMenuButton("Ví Tiền");
        btnLichSuChuyen = createMenuButton("Lịch Sử Chuyến Đi");
        btnThongTinCaNhan = createMenuButton("Thông tin cá nhân");
        btnDangXuat = createMenuButton("Đăng Xuất");

        btnDangXuat.setForeground(new Color(255, 100, 100));

        int btnHeight = 40, btnWidth = 180, leftMargin = 10;

        btnNhanChuyen.setBounds(leftMargin, 30, btnWidth, btnHeight);
        btnDoanhThu.setBounds(leftMargin, 30 + btnHeight, btnWidth, btnHeight);
        btnTrangThai.setBounds(leftMargin, 30 + btnHeight * 2, btnWidth, btnHeight);
        btnTKNganHang.setBounds(leftMargin, 30 + btnHeight * 3, btnWidth, btnHeight);
        btnLichSuChuyen.setBounds(leftMargin, 30 + btnHeight * 4, btnWidth, btnHeight);
        btnThongTinCaNhan.setBounds(leftMargin, 30 + btnHeight * 5, btnWidth, btnHeight);

        int menuPanelHeight = this.getHeight() - 70;
        btnDangXuat.setBounds(leftMargin, menuPanelHeight - btnHeight - 50, btnWidth, btnHeight);

        menuPanel.add(btnNhanChuyen);
        menuPanel.add(btnDoanhThu);
        menuPanel.add(btnTrangThai);
        menuPanel.add(btnTKNganHang);
        menuPanel.add(btnLichSuChuyen);
        menuPanel.add(btnThongTinCaNhan);
        menuPanel.add(btnDangXuat);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                btnDangXuat.setBounds(leftMargin, getHeight() - 70 - btnHeight - 50, btnWidth, btnHeight);
            }
        });
    }

    private void createContent() {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(255, 255, 255, 240));

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(new EmptyBorder(5, 5, 10, 10));

        // -- PANEL AVATAR & RATING --
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        JPanel welcomeInnerPanel = new JPanel(new BorderLayout());
        welcomeInnerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        welcomeInnerPanel.setBackground(Color.WHITE);
        welcomeInnerPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), 150));
        welcomePanel.add(welcomeInnerPanel, BorderLayout.CENTER);

        JPanel profileImagePanel = new JPanel(new BorderLayout());
        profileImagePanel.setBackground(Color.WHITE);
        profileImagePanel.setPreferredSize(new Dimension(150, 150));
        profileImagePanel.setBorder(new EmptyBorder(5, 5, 5, 10));

        URL driverUrl = getClass().getResource("/me/myproject/IMAGE/Dn.png");
        if (driverUrl != null) {
            inforDriver = new ImageIcon(
                    new ImageIcon(driverUrl).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        }
        inforJLabel = new JLabel(inforDriver);
        profileImagePanel.add(inforJLabel, BorderLayout.CENTER);

        JPanel welcomeTextPanel = new JPanel();
        welcomeTextPanel.setLayout(new BoxLayout(welcomeTextPanel, BoxLayout.Y_AXIS));
        welcomeTextPanel.setBackground(Color.WHITE);
        welcomeTextPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        chaoTaiXeLbl = new JLabel("Chào " + (taiXe != null ? taiXe.getDriverName() : "") + "!");
        chaoTaiXeLbl.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel readyMessage = new JLabel("Sẵn sàng cho ngày làm việc");
        readyMessage.setFont(new Font("Arial", Font.PLAIN, 16));

        btnBatDauChuyen = new JButton("Bắt Đầu Nhận");
        btnBatDauChuyen.setFont(new Font("Arial", Font.BOLD, 14));
        btnBatDauChuyen.setBackground(ColorMain.btnHeader);
        btnBatDauChuyen.setForeground(Color.WHITE);
        btnBatDauChuyen.setFocusPainted(false);
        btnBatDauChuyen.setCursor(new Cursor(Cursor.HAND_CURSOR));

        welcomeTextPanel.add(chaoTaiXeLbl);
        welcomeTextPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        welcomeTextPanel.add(readyMessage);
        welcomeTextPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomeTextPanel.add(btnBatDauChuyen);

        JPanel ratingPanel = new JPanel();
        ratingPanel.setLayout(new BoxLayout(ratingPanel, BoxLayout.Y_AXIS));
        ratingPanel.setBackground(Color.WHITE);
        ratingPanel.setPreferredSize(new Dimension(180, 150));

        JLabel ratingTitleLabel = new JLabel("Điểm Đánh Giá");
        ratingTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        ratingTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel ratingValuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ratingValuePanel.setBackground(Color.WHITE);

        ratingValueLabel = new JLabel("0.0");
        ratingValueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        ratingValueLabel.setForeground(new Color(255, 150, 0));

        JLabel starLabel = new JLabel("★");
        starLabel.setFont(new Font("Arial", Font.BOLD, 32));
        starLabel.setForeground(new Color(255, 150, 0));

        ratingValuePanel.add(ratingValueLabel);
        ratingValuePanel.add(starLabel);

        ratingPanel.add(Box.createVerticalGlue());
        ratingPanel.add(ratingTitleLabel);
        ratingPanel.add(ratingValuePanel);
        ratingPanel.add(Box.createVerticalGlue());

        welcomeInnerPanel.add(profileImagePanel, BorderLayout.WEST);
        welcomeInnerPanel.add(welcomeTextPanel, BorderLayout.CENTER);
        welcomeInnerPanel.add(ratingPanel, BorderLayout.EAST);

        // -- TRIP COUNT PANEL --
        JPanel tripCountPanel = new JPanel(new BorderLayout());
        tripCountPanel.setBackground(Color.WHITE);
        tripCountPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel tripCountInnerPanel = new JPanel(new BorderLayout());
        tripCountInnerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        tripCountInnerPanel.setBackground(Color.WHITE);
        tripCountInnerPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), 70));
        tripCountPanel.add(tripCountInnerPanel, BorderLayout.CENTER);

        JPanel tripCountLeftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        tripCountLeftPanel.setBackground(Color.WHITE);

        JLabel tripCountTitleLabel = new JLabel("Số Chuyến Hôm Nay:");
        tripCountTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tripCountValueLabel = new JLabel("0");
        tripCountValueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        tripCountValueLabel.setForeground(new Color(20, 180, 180));

        tripCountLeftPanel.add(tripCountTitleLabel);
        tripCountLeftPanel.add(tripCountValueLabel);

        JPanel tripCountRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        tripCountRightPanel.setBackground(Color.WHITE);

        boolean isOnline = taiXe != null && taiXe.getActivityStatus();
        btnTrangThaiHoatDong = new JButton();
        btnTrangThaiHoatDong.setFont(new Font("Arial", Font.BOLD, 12));
        btnTrangThaiHoatDong.setFocusPainted(false);
        btnTrangThaiHoatDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        updateToggleButtonStyle(btnTrangThaiHoatDong, isOnline);

        btnTrangThaiHoatDong.addActionListener(e -> {
            try {
                handleToggleStatus();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        tripCountRightPanel.add(btnTrangThaiHoatDong);

        tripCountInnerPanel.add(tripCountLeftPanel, BorderLayout.WEST);
        tripCountInnerPanel.add(tripCountRightPanel, BorderLayout.EAST);

        // -- MAP PANEL --
        JPanel mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 10));

        JPanel mapInnerPanel = new JPanel(new BorderLayout());
        mapInnerPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        mapInnerPanel.setBackground(Color.WHITE);
        mapPanel.add(mapInnerPanel, BorderLayout.CENTER);

        JLabel mapTitleLabel = new JLabel("Bản Đồ Vị Trí Hiện Tại");
        mapTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mapTitleLabel.setBorder(new EmptyBorder(5, 20, 5, 10));

        JPanel mapContentPanel = new JPanel(new BorderLayout());
        mapContentPanel.setBackground(new Color(245, 245, 245));
        mapContentPanel.setPreferredSize(new Dimension(contentPanel.getWidth(), 250));

        try {
            double lat = (taiXe != null) ? taiXe.getCurrentLat() : 16.0669;
            double lng = (taiXe != null) ? taiXe.getCurrentLng() : 108.203;
            mapContentPanel.add(new MapUtil(lat, lng), BorderLayout.CENTER);
        } catch (Exception e) {
            mapContentPanel.add(new JLabel("Không thể tải bản đồ", JLabel.CENTER), BorderLayout.CENTER);
        }

        mapInnerPanel.add(mapTitleLabel, BorderLayout.NORTH);
        mapInnerPanel.add(mapContentPanel, BorderLayout.CENTER);

        mainContentPanel.add(welcomePanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContentPanel.add(tripCountPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainContentPanel.add(mapPanel);

        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(25, 37, 60));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(8, 15, 8, 10));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        button.setPressedIcon(button.getIcon());
        button.setRolloverEnabled(false);
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        resetButtonColors();
        Object source = e.getSource();

        if (source == btnNhanChuyen || source == btnBatDauChuyen) {
            btnNhanChuyen.setForeground(ColorMain.blueHeader);
            // BỎ LỆNH ĐỂ TRÁNH MỞ TRÙNG: new TrangChuDriverView(tk);
        } else if (source == btnDoanhThu) {
            btnDoanhThu.setForeground(ColorMain.blueHeader);
            try {
                new QuanLyDoanhThuView(taiXe);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (source == btnTrangThai) {
            btnTrangThai.setForeground(ColorMain.blueHeader);
            new TaiKhoanNganHangView(tk);
        } else if (source == btnTKNganHang) {
            btnTKNganHang.setForeground(ColorMain.blueHeader);
            new ViTienTaiXeView(tk);
        } else if (source == btnLichSuChuyen) {
            btnLichSuChuyen.setForeground(ColorMain.blueHeader);
            try {
                new LichSuChuyenDiView(tk);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (source == btnThongTinCaNhan) {
            btnThongTinCaNhan.setForeground(ColorMain.blueHeader);
            new ThongTinCaNhanTXView(taiXe);
        } else if (source == btnDangXuat) {
            this.dispose();
            new DangNhapView();
        }
    }

    private void resetButtonColors() {
        btnNhanChuyen.setForeground(Color.WHITE);
        btnDoanhThu.setForeground(Color.WHITE);
        btnTrangThai.setForeground(Color.WHITE);
        btnTKNganHang.setForeground(Color.WHITE);
        btnLichSuChuyen.setForeground(Color.WHITE);
        btnThongTinCaNhan.setForeground(Color.WHITE);
        btnDangXuat.setForeground(new Color(255, 100, 100));
    }

    private void initData() throws IOException {
        if (tk == null)
            return;
        Gson gson = new Gson();

        // 1. Cập nhật thông tin chi tiết Tài xế nếu chưa lấy được ở đầu
        if (this.taiXe == null) {
            Map<String, Object> responseMap = taiXeBSL.layThongTinTaiXe();
            if (responseMap != null) {
                JsonObject jsonObject = gson.toJsonTree(responseMap).getAsJsonObject();
                if (jsonObject.has("result") && !jsonObject.get("result").isJsonNull()) {
                    this.taiXe = gson.fromJson(jsonObject.get("result"), TaiXe.class);
                }
            }
        }

        // Đổ thông tin tài xế lên giao diện
        if (this.taiXe != null) {
            if (phuongTienLbl != null)
                phuongTienLbl.setText(this.taiXe.getVehicleName());
            if (bienSoXeLbl != null)
                bienSoXeLbl.setText(this.taiXe.getLicensePlate());
            if (driverNameLabel != null)
                driverNameLabel.setText(this.taiXe.getDriverName());
            if (chaoTaiXeLbl != null)
                chaoTaiXeLbl.setText("Chào " + this.taiXe.getDriverName() + "!");
            updateToggleButtonStyle(btnTrangThaiHoatDong, this.taiXe.getActivityStatus());

            // TẢI ẢNH AVATAR NGẦM TRÁNH ĐƠ UI
            if (taiXe.getAvatar() != null && !taiXe.getAvatar().isBlank()) {
                new Thread(() -> {
                    try {
                        ImageIcon icon = new ImageIcon(new URL(taiXe.getAvatar()));
                        Image scaledImg = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                        ImageIcon finalIcon = new ImageIcon(scaledImg);
                        SwingUtilities.invokeLater(() -> {
                            if (inforJLabel != null)
                                inforJLabel.setIcon(finalIcon);
                        });
                    } catch (Exception ignored) {
                    }
                }).start();
            }
        }

        // 2. Fetch số liệu thống kê (Dashboard metrics)
        Map<String, Object> dashResponse = taiXeBSL.layThongKeTaiXe();
        if (dashResponse != null) {
            JsonObject dashJson = gson.toJsonTree(dashResponse).getAsJsonObject();
            if (dashJson.has("result") && !dashJson.get("result").isJsonNull()) {
                JsonObject result = dashJson.getAsJsonObject("result");
                // CỰC KỲ QUAN TRỌNG: Chỉ dùng .setText(), tuyệt đối không dùng "= new JLabel()"
                if (result.has("todayIncome") && thuNhapNowLbl != null) {
                    thuNhapNowLbl.setText(String.format("%,.0f VND", result.get("todayIncome").getAsDouble()));
                }
                if (result.has("totalRides") && tripCountValueLabel != null) {
                    tripCountValueLabel.setText(result.get("totalRides").getAsString());
                }
                if (result.has("averageRating") && ratingValueLabel != null) {
                    ratingValueLabel.setText(result.get("averageRating").getAsString());
                }
            }
        }
    }

    private void handleToggleStatus() throws IOException {
        if (taiXe == null)
            return;
        boolean newStatus = !taiXe.getActivityStatus();

        Map<String, Object> response = taiXeBSL.doiTrangThaiHoatDong();
        boolean success = response != null && response.get("status").toString().contains("200");

        if (success) {
            taiXe.setActivityStatus(newStatus);
            updateToggleButtonStyle(btnTrangThaiHoatDong, newStatus);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái!");
        }
    }

    private void updateToggleButtonStyle(JButton btn, boolean status) {
        if (btn == null || onlineLabel == null)
            return;
        if (status) {
            onlineLabel.setText("Trực tuyến");
            btn.setBackground(new Color(0, 188, 212));
            btn.setText("Đang Hoạt Động");
        } else {
            onlineLabel.setText("Ngoại tuyến");
            btn.setBackground(new Color(158, 158, 158));
            btn.setText("Đang Tắt");
        }
        btn.setForeground(Color.BLACK);
    }

    private void initWebSocket() {
        String driverId = resolveDriverId();
        if (driverId == null || driverId.isBlank())
            return;

        String destination = "/topic/driver/" + driverId;
        webSocketClient = new StompWebSocketClient(destination, this::handleMessage);
        webSocketClient.connect(AppConfig.WS_URL);
    }

    private void handleMessage(String message) {
        if (message != null && message.contains("NEW_RIDE:")) {
            SwingUtilities.invokeLater(() -> {
                if (webSocketClient != null)
                    webSocketClient.disconnect();
                TrangChuDriverView.this.dispose();
                NhanChuyenView view = new NhanChuyenView(tk);
                view.handleMessage(message);
            });
        }
    }

    private String resolveDriverId() {
        if (tk != null && tk.getID_Ref() != null && !tk.getID_Ref().isBlank())
            return tk.getID_Ref();
        TaiKhoan restored = AuthManager.tryRestoreSession();
        if (restored != null && restored.getID_Ref() != null && !restored.getID_Ref().isBlank())
            return restored.getID_Ref();
        return null;
    }
}