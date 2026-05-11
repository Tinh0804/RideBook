package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import me.myproject.BUSINESSLOGIC.DatXeBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.AuthManager;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.GoogleMapsClient;
import me.myproject.Utilities.MapUtil;
import me.myproject.Utilities.StompWebSocketClient;

public class DatXeView extends FrameMain implements ActionListener {
    // Fields for input and display
    private JTextField tfdPickup, tfdDestination;
    private JComboBox<PromotionItem> cboDiscountCode;
    private JLabel lblDistance, lblPrice, lblDiscountStatus;
    private JButton btnBooking;
    private JComboBox<String> cboPaymentMethod;
    private double currentDistanceKm = 0.0;
    
    private java.util.Map<String, JRadioButton> radioButtonsMap = new java.util.HashMap<>();
    private java.util.Map<String, JPanel> xePanelsMap = new java.util.HashMap<>();
    private ButtonGroup vehicleGroup = new ButtonGroup();
    private String selectedVehicleId = "";
    private java.util.List<me.myproject.MODEL.LoaiXe> loaiXeList;
    private java.util.List<PromotionItem> activePromotions = new java.util.ArrayList<>();

    // Lớp Model nội bộ để chứa thông tin Khuyến Mãi
    class PromotionItem {
        String id;
        String code;
        String name;
        double discountLimit;

        public PromotionItem(String id, String code, String name, double discountLimit) {
            this.id = id;
            this.code = code;
            this.name = name;
            this.discountLimit = discountLimit;
        }

        @Override
        public String toString() {
            if (id == null) return "Không áp dụng";
            return "[" + code + "] " + name + " (Giảm " + String.format("%,.0fđ", discountLimit).replace(",", ".") + ")";
        }
    }    private final TaiKhoan taiKhoan;
    private final DatXeBSL datXeBSL;
    private final GoogleMapsClient mapsClient;
    private StompWebSocketClient webSocketClient;
    private final String pickupAddress;
    private final String destinationAddress;

    // Constants for pricing replaced by dynamic API


    // Colors
    private final Color TEAL_COLOR = new Color(0, 160, 160);
    private final Color LIGHT_GRAY_BG = new Color(240, 240, 240);
    private final Color SELECTED_VEHICLE_BG = new Color(230, 250, 250);
    private final Color SELECTED_VEHICLE_BORDER = new Color(0, 180, 180);

    // Current discount percentage (0-100)
    private int discountPercentage = 0;

    // Vehicle panels maps handled dynamically

    public DatXeView(TaiKhoan taiKhoan, String pickupAddress, String destinationAddress) {
        super("Đặt Xe");
        this.taiKhoan = taiKhoan;
        this.datXeBSL = new DatXeBSL();
        this.mapsClient = new GoogleMapsClient(AppConfig.GOOGLE_API_KEY);
        this.pickupAddress = pickupAddress;
        this.destinationAddress = destinationAddress;
        init();
        initWebSocket();
        updateDistanceAndPrice();
    }

    public DatXeView(TaiKhoan taiKhoan) {
        this(taiKhoan, "", "");
    }

    public DatXeView() {
        this(null, "", "");
    }

    private void initData() {
        try {
            Map<String, Object> response = datXeBSL.getTatCaLoaiXe();
            Number status = (Number) response.get("status");
            if (status != null && status.intValue() == 200) {
                java.util.List<Map<String, Object>> result = (java.util.List<Map<String, Object>>) response.get("result");
                loaiXeList = new java.util.ArrayList<>();
                if (result != null) {
                    for (Map<String, Object> item : result) {
                        me.myproject.MODEL.LoaiXe lx = new me.myproject.MODEL.LoaiXe(
                            (String) item.get("vehicleTypeId"),
                            (String) item.get("vehicleTypeName"),
                            item.get("pricePerKm") instanceof Number ? ((Number) item.get("pricePerKm")).doubleValue() : 0.0
                        );
                        loaiXeList.add(lx);
                    }
                }
            } else {
                loaiXeList = new java.util.ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loaiXeList = new java.util.ArrayList<>();
        }

        // Fetch active promotions
        try {
            Map<String, Object> responsePromo = datXeBSL.getActivePromotions();
            Number statusPromo = (Number) responsePromo.get("status");
            if (statusPromo != null && statusPromo.intValue() == 200) {
                java.util.List<Map<String, Object>> resultPromo = (java.util.List<Map<String, Object>>) responsePromo.get("result");
                if (resultPromo != null) {
                    for (Map<String, Object> item : resultPromo) {
                        PromotionItem pi = new PromotionItem(
                            (String) item.get("promotionId"),
                            (String) item.get("promotionCode"),
                            (String) item.get("promotionName"),
                            item.get("discountLimit") instanceof Number ? ((Number) item.get("discountLimit")).doubleValue() : 0.0
                        );
                        activePromotions.add(pi);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        initData();
        // Set up the frame with JLayeredPane as the content pane
        JLayeredPane layeredPane = new JLayeredPane();
        this.setContentPane(layeredPane);
        Dimension frameDimension = this.getSize();
        int frameWidth = frameDimension.width;
        int frameHeight = frameDimension.height;


        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(TEAL_COLOR);
        headerPanel.setPreferredSize(new Dimension(frameWidth, 80));
        headerPanel.setBounds(0, 0, frameWidth, 80);
        layeredPane.add(headerPanel, JLayeredPane.PALETTE_LAYER);

        JButton btnBack = new JButton("← Quay lại");
        btnBack.setFocusPainted(false);
        btnBack.setBackground(TEAL_COLOR);
        btnBack.setForeground(Color.WHITE);
        btnBack.setBorderPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new ChonDiemView(taiKhoan);
            dispose();
        });
        btnBack.setFocusable(false); // <--- QUAN TRỌNG: Nút này không được phép nhận focus từ bàn phím
        btnBack.setDefaultCapable(false);
        headerPanel.add(btnBack, BorderLayout.WEST);

        // Title in header (centered)
        JLabel titleLabel = new JLabel("ĐẶT XE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Main content panel with BorderLayout
        JPanel contentPanel = new JPanel(new GridLayout(1, 2));
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPanel.setOpaque(false); // Make content panel transparent to show background
        contentPanel.setBounds(0, 80, frameWidth, frameHeight - 80);
        layeredPane.add(contentPanel, JLayeredPane.PALETTE_LAYER);

        // Left panel - booking form
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // Right panel - map
        JPanel mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(LIGHT_GRAY_BG);
        JLabel mapLabel = new MapUtil(pickupAddress, destinationAddress);
        mapLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mapLabel.setForeground(Color.DARK_GRAY);
        mapPanel.add(mapLabel, BorderLayout.CENTER);

        // Rest of the left panel setup (unchanged)
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        gbc.gridy = 0;
        JLabel questionLabel = new JLabel("Bạn muốn đi đâu?");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 30)); // Bold and larger font size
        questionLabel.setForeground(new Color(0, 128, 128)); // Stylish color (teal)
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text horizontally
        questionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Margin at the bottom for spacing
        leftPanel.add(questionLabel, gbc);

        // Pickup point
        gbc.gridy = 1;
        JLabel pickupLabel = new JLabel("Điểm đón");
        pickupLabel.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(pickupLabel, gbc);

        gbc.gridy = 2;
        tfdPickup = new JTextField(20);
        tfdPickup.setPreferredSize(new Dimension(tfdPickup.getPreferredSize().width, 30));
        tfdPickup.setText(pickupAddress);
        tfdPickup.setEditable(false);
        leftPanel.add(tfdPickup, gbc);

        // Destination
        gbc.gridy = 3;
        JLabel destinationLabel = new JLabel("Điểm đến");
        destinationLabel.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(destinationLabel, gbc);

        gbc.gridy = 4;
        tfdDestination = new JTextField(20);
        tfdDestination.setPreferredSize(new Dimension(tfdDestination.getPreferredSize().width, 30));
        tfdDestination.setText(destinationAddress);
        tfdDestination.setEditable(false);
        leftPanel.add(tfdDestination, gbc);

        

        // Distance
        gbc.gridy = 5;
        JPanel distancePanel = new JPanel(new BorderLayout());
        distancePanel.setOpaque(false);
        JLabel distanceLabel = new JLabel("Quãng đường:");
        distanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        lblDistance = new JLabel("5 km");
        lblDistance.setFont(new Font("Arial", Font.PLAIN, 14));
        lblDistance.setHorizontalAlignment(SwingConstants.RIGHT);
        distancePanel.add(distanceLabel, BorderLayout.WEST);
        distancePanel.add(lblDistance, BorderLayout.EAST);
        leftPanel.add(distancePanel, gbc);

        // Price
        gbc.gridy = 6;
        JPanel pricePanel = new JPanel(new BorderLayout());
        pricePanel.setOpaque(false);
        JLabel pricePromptLabel = new JLabel("Giá tiền:");
        pricePromptLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPrice = new JLabel("25,000 VND (ước tính)");
        lblPrice.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPrice.setForeground(TEAL_COLOR);
        lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);
        pricePanel.add(pricePromptLabel, BorderLayout.WEST);
        pricePanel.add(lblPrice, BorderLayout.EAST);
        leftPanel.add(pricePanel, gbc);

        // Discount Code Section with ComboBox
        gbc.gridy = 7;
        JPanel discountPanel = new JPanel(new BorderLayout(10, 0));
        discountPanel.setOpaque(false);
        JLabel discountLabel = new JLabel("Mã giảm giá:");
        discountLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        cboDiscountCode = new JComboBox<>();
        cboDiscountCode.addItem(new PromotionItem(null, null, "Không áp dụng", 0));
        for (PromotionItem pi : activePromotions) {
            cboDiscountCode.addItem(pi);
        }
        
        cboDiscountCode.setPreferredSize(new Dimension(150, 30));
        cboDiscountCode.addActionListener(e -> applySelectedDiscount());
        discountPanel.add(discountLabel, BorderLayout.WEST);
        discountPanel.add(cboDiscountCode, BorderLayout.CENTER);
        leftPanel.add(discountPanel, gbc);

        // Discount Status
        gbc.gridy = 8;
        lblDiscountStatus = new JLabel("");
        lblDiscountStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        leftPanel.add(lblDiscountStatus, gbc);

        // Payment method
        gbc.gridy = 9;
        JPanel paymentPanel = new JPanel(new BorderLayout(10, 0));
        paymentPanel.setOpaque(false);
        JLabel paymentLabel = new JLabel("Thanh toán:");
        paymentLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] paymentOptions = { "VNPay", "MoMo", "Tiền mặt" };
        cboPaymentMethod = new JComboBox<>(paymentOptions);
        cboPaymentMethod.setPreferredSize(new Dimension(cboPaymentMethod.getPreferredSize().width, 30));
        paymentPanel.add(paymentLabel, BorderLayout.WEST);
        paymentPanel.add(cboPaymentMethod, BorderLayout.CENTER);
        leftPanel.add(paymentPanel, gbc);

        // Separator
        gbc.gridy = 10;
        JPanel separatorPanel = new JPanel();
        separatorPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        separatorPanel.setPreferredSize(new Dimension(leftPanel.getWidth(), 1));
        leftPanel.add(separatorPanel, gbc);

        // Vehicle type header
        gbc.gridy = 11;
        JLabel vehicleTypeLabel = new JLabel("Chọn loại xe");
        vehicleTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        leftPanel.add(vehicleTypeLabel, gbc);

        // Vehicle selection panel
        gbc.gridy = 12;
        JPanel vehiclesPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        vehiclesPanel.setOpaque(false);

        // Create vehicle panels dynamically
        if (loaiXeList != null && !loaiXeList.isEmpty()) {
            boolean first = true;
            for (me.myproject.MODEL.LoaiXe lx : loaiXeList) {
                JPanel vPanel = createImprovedVehiclePanel(lx.getID_LoaiXe(), lx.getTenLoaiXe(), lx.getGia1KM());
                JRadioButton rdo = (JRadioButton) vPanel.getComponent(0);
                if (first) {
                    rdo.setSelected(true);
                    selectedVehicleId = lx.getID_LoaiXe();
                    first = false;
                }
                vehicleGroup.add(rdo);
                vehiclesPanel.add(vPanel);
                
                radioButtonsMap.put(lx.getID_LoaiXe(), rdo);
                xePanelsMap.put(lx.getID_LoaiXe(), vPanel);
                
                rdo.addActionListener(e -> {
                    selectedVehicleId = lx.getID_LoaiXe();
                    updateVehicleSelection();
                    updatePrice();
                });
            }
        }

        // Add the vehicle selection panel to the left panel
        leftPanel.add(vehiclesPanel, gbc);

        // Booking button
        gbc.gridy = 13;
        gbc.insets = new Insets(20, 0, 5, 0);
        btnBooking = new JButton("ĐẶT XE");
        btnBooking.setPreferredSize(new Dimension(leftPanel.getWidth(), 40));
        btnBooking.setBackground(TEAL_COLOR);
        btnBooking.setForeground(Color.BLACK);
        btnBooking.setFocusPainted(false);
        btnBooking.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBooking.setFont(new Font("Arial", Font.BOLD, 16));
        btnBooking.addActionListener(this);
        leftPanel.add(btnBooking, gbc);

        // Add panels to content panel
        contentPanel.add(leftPanel);
        contentPanel.add(mapPanel);

        // Initial vehicle selection highlight
        updateVehicleSelection();

        // Initial price update
        updatePrice();

        this.setVisible(true);
    }

    private JPanel createImprovedVehiclePanel(String id, String name, double rate) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        panel.setBackground(new Color(250, 250, 250));

        // Create radio button with name
        JRadioButton radioButton = new JRadioButton(name);
        radioButton.setFont(new Font("Arial", Font.BOLD, 13));
        radioButton.setHorizontalAlignment(SwingConstants.CENTER);
        radioButton.setBackground(new Color(250, 250, 250));
        radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Create panel for icon
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        imagePanel.setBackground(Color.WHITE);

        // Format price with thousand separator
        String formattedRate = String.format("%,.0f", rate).replace(",", ".");
        JLabel priceLabel = new JLabel(formattedRate + " VND/km");
        priceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        priceLabel.setForeground(TEAL_COLOR);
        priceLabel.setFont(new Font("Arial", Font.BOLD, 12));
        priceLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // Add components to panel
        panel.add(radioButton, BorderLayout.NORTH);
        panel.add(imagePanel, BorderLayout.CENTER);
        panel.add(priceLabel, BorderLayout.SOUTH);

        // Make entire panel clickable
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                radioButton.setSelected(true);
                selectedVehicleId = id;
                updateVehicleSelection();
                updatePrice();
            }
        });

        return panel;
    }

    private void updateVehicleSelection() {
        // Reset all panels
        for (String id : xePanelsMap.keySet()) {
            JPanel p = xePanelsMap.get(id);
            p.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
            p.setBackground(new Color(250, 250, 250));
            radioButtonsMap.get(id).setBackground(new Color(250, 250, 250));
        }

        // Highlight selected panel
        if (!selectedVehicleId.isEmpty() && xePanelsMap.containsKey(selectedVehicleId)) {
            JPanel selectedP = xePanelsMap.get(selectedVehicleId);
            JRadioButton selectedRdo = radioButtonsMap.get(selectedVehicleId);
            selectedP.setBorder(new LineBorder(SELECTED_VEHICLE_BORDER, 2, true));
            selectedP.setBackground(SELECTED_VEHICLE_BG);
            selectedRdo.setBackground(SELECTED_VEHICLE_BG);
        }
    }

    private void applySelectedDiscount() {
        PromotionItem selectedDiscount = (PromotionItem) cboDiscountCode.getSelectedItem();

        if (selectedDiscount == null || selectedDiscount.id == null) {
            lblDiscountStatus.setText("");
        } else {
            lblDiscountStatus.setText("Đang áp dụng: " + selectedDiscount.name);
            lblDiscountStatus.setForeground(new Color(0, 128, 0)); // Dark green
        }

        // Update price with the discount fetched from backend
        updatePrice();
    }

   

    private void updatePrice() {
        double distance = currentDistanceKm > 0 ? currentDistanceKm : 5.0;
        String vId = mapVehicleTypeId();
        if (vId == null || vId.isEmpty()) return;

        PromotionItem selectedPromo = (PromotionItem) cboDiscountCode.getSelectedItem();
        String pCode = (selectedPromo != null && selectedPromo.code != null) ? selectedPromo.code : null;

        new Thread(() -> {
            try {
                Map<String, Object> response = datXeBSL.estimatePrice(vId, distance, pCode);
                Number status = (Number) response.get("status");
                if (status != null && status.intValue() == 200) {
                    Map<String, Object> result = (Map<String, Object>) response.get("result");
                    Number totalPrice = (Number) result.get("totalPrice");
                    Number discountNum = (Number) result.get("discount");
                    
                    double finalPrice = totalPrice.doubleValue();
                    double totalDiscount = discountNum != null ? discountNum.doubleValue() : 0.0;
                    
                    String formattedPrice = String.format("%,.0f", finalPrice).replace(",", ".");
                    String discountText = "";
                    if (totalDiscount > 0) {
                        discountText = " (Giảm: -" + String.format("%,.0f", totalDiscount).replace(",", ".") + "đ)";
                    }
                    
                    final String displayStr = formattedPrice + " VND " + discountText;
                    SwingUtilities.invokeLater(() -> lblPrice.setText(displayStr));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBooking) {
            if (tfdPickup.getText().trim().isEmpty() || tfdDestination.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập điểm đón và điểm đến",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String customerId = resolveCustomerId();
            
            if (customerId == null || customerId.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để đặt xe",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String pickup = tfdPickup.getText().trim();
                String dropoff = tfdDestination.getText().trim();
                double distance = currentDistanceKm > 0 ? currentDistanceKm : parseDistance(lblDistance.getText());
                String vehicleTypeId = mapVehicleTypeId();

                String paymentMethod = (String) cboPaymentMethod.getSelectedItem();
                String paymentMethodCode = mapPaymentMethod(paymentMethod);

                PromotionItem selectedPromo = (PromotionItem) cboDiscountCode.getSelectedItem();
                String promotionId = (selectedPromo != null && selectedPromo.id != null) ? selectedPromo.id : null;

                Map<String, Object> response = datXeBSL.taoDatXe(customerId, pickup, dropoff, distance, vehicleTypeId, paymentMethodCode, promotionId);
                Number status = (Number) response.get("status");
                if (status == null || status.intValue() < 200 || status.intValue() >= 300) {
                    String message = (String) response.getOrDefault("message", "Đặt xe thất bại");
                    JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> booking = (Map<String, Object>) response.get("result");
                String bookingId = booking != null ? (String) booking.get("bookingId") : null;
                Number totalPrice = booking != null ? (Number) booking.get("totalPrice") : null;

                if (bookingId == null) {
                    JOptionPane.showMessageDialog(this, "Không nhận được mã đặt xe từ hệ thống", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if ("Tiền mặt".equalsIgnoreCase(paymentMethod)) {
                    JOptionPane.showMessageDialog(this, "Đặt xe thành công! Vui lòng trả tiền mặt khi kết thúc chuyến.",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    this.setVisible(false);
                    new ChoTaiXeView(taiKhoan,this);
                    return;
                }

                long amount = totalPrice != null ? totalPrice.longValue() : parsePrice(lblPrice.getText());
                Map<String, Object> paymentResponse;
                String orderInfo = "Thanh toán chuyến xe " + bookingId;

                if ("MoMo".equalsIgnoreCase(paymentMethod)) {
                    paymentResponse = datXeBSL.taoThanhToanMoMo(bookingId, amount, orderInfo);
                } else {
                    paymentResponse = datXeBSL.taoThanhToanVNPay(bookingId, amount, orderInfo);
                }

                showPaymentResult(bookingId, paymentResponse);
                new PaymentWaitingView(taiKhoan, bookingId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đặt xe: " + ex.getMessage(),
                        "Thông báo", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getSelectedVehicle() {
        if (selectedVehicleId.isEmpty()) return "";
        for (me.myproject.MODEL.LoaiXe lx : loaiXeList) {
            if (lx.getID_LoaiXe().equals(selectedVehicleId)) {
                return lx.getTenLoaiXe();
            }
        }
        return "";
    }

    private String mapVehicleTypeId() {
        return selectedVehicleId;
    }

    private String mapPaymentMethod(String paymentMethod) {
        if (paymentMethod == null) {
            return "ONLINE";
        }
        if ("Tiền mặt".equalsIgnoreCase(paymentMethod)) {
            return "CASH";
        }
        return "ONLINE";
    }

    private double parseDistance(String text) {
        if (text == null) {
            return 1.0;
        }
        String normalized = text.replace("km", "").trim().replace(",", ".");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return 1.0;
        }
    }

    private long parsePrice(String text) {
        if (text == null) {
            return 0L;
        }
        String numeric = text.replaceAll("[^0-9]", "");
        if (numeric.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(numeric);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private void showPaymentResult(String bookingId, Map<String, Object> paymentResponse) {
        Number status = paymentResponse != null ? (Number) paymentResponse.get("status") : null;
        if (status == null || status.intValue() < 200 || status.intValue() >= 300) {
            String message = paymentResponse != null ? (String) paymentResponse.getOrDefault("message", "Tạo thanh toán thất bại")
                    : "Tạo thanh toán thất bại";
            JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) paymentResponse.get("result");
        String paymentUrl = result != null ? (String) result.get("paymentUrl") : null;
        String message = "Đặt xe thành công!\nMã chuyến: " + bookingId;
        if (paymentUrl != null && !paymentUrl.isBlank()) {
            message += "\nLink thanh toán: " + paymentUrl;
            openPaymentUrl(paymentUrl);
        }
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openPaymentUrl(String paymentUrl) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(paymentUrl));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initWebSocket() {
        String customerId = resolveCustomerId();
        System.out.println("CustomerID:"+customerId);
        if (customerId == null || customerId.isBlank()) {
            return;
        }
        String destination = "/topic/customer/" + customerId;
        webSocketClient = new StompWebSocketClient(destination, this::handleWebSocketMessage);
        webSocketClient.connect(AppConfig.WS_URL);
    }

    private String resolveCustomerId() {
        if (taiKhoan != null && taiKhoan.getID_Ref() != null && !taiKhoan.getID_Ref().isBlank()) {
            return taiKhoan.getID_Ref();
        }
        TaiKhoan restored = AuthManager.tryRestoreSession();
        if (restored != null && restored.getID_Ref() != null && !restored.getID_Ref().isBlank()) {
            return restored.getID_Ref();
        }
        return null;
    }

    private void updateDistanceAndPrice() {
        String pickup = tfdPickup.getText().trim();
        String destination = tfdDestination.getText().trim();
        if (pickup.isBlank() || destination.isBlank()) return;

        // Chạy ngầm để không treo UI
        new Thread(() -> {
            try {
                double distance = mapsClient.getDistanceKm(pickup, destination);
                if (distance > 0) {
                    // Chỉ gán dữ liệu trong UI Thread
                    SwingUtilities.invokeLater(() -> {
                        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                        currentDistanceKm = distance;
                        lblDistance.setText(String.format("%.1f km", distance));
                        updatePrice(); // Hàm này chỉ cập nhật Label, không nên revalidate toàn bộ Frame

                        if (owner != null) {
                            owner.requestFocusInWindow();
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void handleWebSocketMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("NEW_RIDE") || message.startsWith("NO_DRIVER_FOUND")) {
                JOptionPane.showMessageDialog(this, "Cập nhật chuyến: " + message,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    // public static void main(String[] args) {
    //     try {
    //         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

    //     SwingUtilities.invokeLater(() -> new DatXeView(null, "", ""));
    // }
}