package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;

public class DanhGiaTaiXeView extends FrameMain implements ActionListener {
    private TaiKhoan taiKhoan;
    private JPanel mainPanel, contentPanel, headerPanel;
    private JPanel yourRatingPanel, ratingDistPanel, strengthPanel, recentReviewsPanel;
    private JLabel lblTitle;
    private JButton btnBack;
    private JProgressBar[] ratingBars;
    private JLabel[] ratingLabels;
    private JLabel[] ratingPercents;
    private JTable reviewsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> orderIdCombo;
    private JTextField ratingTextField;
    private ImageIcon starIcon, largeStarIcon;
    
    private String[] strengthLabels = {"Lái xe an toàn", "Lịch sự", "Xe sạch sẽ", "Thân thiện"};
    
    public DanhGiaTaiXeView() {
        super("Đánh Giá Cá Nhân - Tài xế");
        this.taiKhoan = null;
        loadImages();
        init();
    }
    
    public DanhGiaTaiXeView(TaiKhoan taiKhoan) {
        super("Đánh Giá Cá Nhân - Tài xế");
        this.taiKhoan = taiKhoan;
        loadImages();
        init();
    }
    
    private void loadImages() {
        // Load các hình ảnh cần thiết
        try {
            // Load hình ảnh ngôi sao
            URL starUrl = getClass().getResource("/me/myproject/IMAGE/star.png");
            if (starUrl != null) {
                ImageIcon originalStarIcon = new ImageIcon(starUrl);
                Image starImg = originalStarIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                starIcon = new ImageIcon(starImg);
                Image largeStarImg = originalStarIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                largeStarIcon = new ImageIcon(largeStarImg);
                if (starIcon.getIconWidth() <= 0) {
                    System.out.println("Không tìm thấy hình ảnh ngôi sao, sẽ sử dụng kí tự thay thế");
                }
            } else {
                System.err.println("Không tìm thấy ảnh sao: /me/myproject/IMAGE/star.png");
                starIcon = null;
                largeStarIcon = null;
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi tải hình ảnh: " + e.getMessage());
            // Tạo một icon mặc định nếu không tìm thấy hình ảnh
            starIcon = null;
        }
    }

    
    private void init() {
        // Thiết lập layout chính
        this.setLayout(new BorderLayout());
        
        // Tạo các panel chính
        createMainPanels();
        
        // Tạo header
        createHeader();
        
        // Tạo nội dung chính
        createContent();
        
        // Hiển thị frame
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    
    private void createMainPanels() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel nội dung chính
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Panel header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.blueHeader);
        headerPanel.setPreferredSize(new Dimension(this.getWidth(), 70));
        
        // Thêm các panel vào panel chính
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(mainPanel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    private void createHeader() {
        // Panel bên trái để chứa nút Back
        JPanel leftHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        leftHeaderPanel.setBackground(ColorMain.blueHeader);
        
        // Tạo nút back với hình ảnh
        btnBack = new JButton();
        try {
            URL backUrl = getClass().getResource("/me/myproject/IMAGE/back.png");
            if (backUrl != null) {
                ImageIcon originalBackIcon = new ImageIcon(backUrl);
                Image backImg = originalBackIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                btnBack.setIcon(new ImageIcon(backImg));
            } else {
                System.err.println("Không tìm thấy icon back: /me/myproject/IMAGE/back.png");
            }
        } catch (Exception e) {
            btnBack.setText("←");
        }
        
        btnBack.setPreferredSize(new Dimension(40, 40));
        btnBack.setBackground(ColorMain.blueHeader);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(this);
        
        leftHeaderPanel.add(btnBack);
        
        // Tiêu đề chính ở giữa header với căn giữa cả X và Y
        JPanel centerHeaderPanel = new JPanel();
        centerHeaderPanel.setLayout(new BoxLayout(centerHeaderPanel, BoxLayout.X_AXIS));
        centerHeaderPanel.setBackground(ColorMain.blueHeader);
        
        lblTitle = new JLabel("Đánh Giá Cá Nhân");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Thêm khoảng trống để căn giữa theo chiều dọc (Y)
        centerHeaderPanel.add(Box.createVerticalGlue());
        centerHeaderPanel.add(lblTitle);
        centerHeaderPanel.add(Box.createVerticalGlue());
        
        // Panel bên phải của header với thông tin người dùng
        JPanel rightHeaderPanel = new JPanel();
        rightHeaderPanel.setLayout(new BoxLayout(rightHeaderPanel, BoxLayout.X_AXIS));
        rightHeaderPanel.setBackground(ColorMain.blueHeader);
        rightHeaderPanel.setBorder(new EmptyBorder(5, 5, 5, 10));
        
        // Thông tin người dùng
        JLabel lblUserName = new JLabel("Duong Thanh Long");
        lblUserName.setFont(new Font("Arial", Font.BOLD, 14));
        lblUserName.setForeground(Color.WHITE);
        
        JLabel lblOnline = new JLabel("Online");
        lblOnline.setFont(new Font("Arial", Font.PLAIN, 12));
        lblOnline.setForeground(Color.WHITE);
        
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setBackground(ColorMain.blueHeader);
        userInfoPanel.add(lblUserName);
        userInfoPanel.add(lblOnline);
        
        // Placeholder cho ảnh đại diện
        JPanel avatarPanel = new JPanel();
        avatarPanel.setBackground(Color.WHITE);
        avatarPanel.setPreferredSize(new Dimension(50, 50));
        avatarPanel.setMaximumSize(new Dimension(50, 50));
        avatarPanel.setMinimumSize(new Dimension(50, 50));
        
        rightHeaderPanel.add(userInfoPanel);
        rightHeaderPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        rightHeaderPanel.add(avatarPanel);
        
        // Thêm các panel vào header
        headerPanel.add(leftHeaderPanel, BorderLayout.WEST);
        headerPanel.add(centerHeaderPanel, BorderLayout.CENTER);
        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);
    }
    
    private void createContent() {
        // Tạo layout 3 panels chính theo hàng ngang như trong hình
        JPanel topRowPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        topRowPanel.setBackground(Color.WHITE);
        
        // Tạo các panels nội dung
        createYourRatingPanel();
        createRatingDistPanel();
        createStrengthPanel();
        createRecentReviewsPanel();
        
        // Thêm vào hàng đầu tiên
        topRowPanel.add(yourRatingPanel);
        topRowPanel.add(ratingDistPanel);
        
        // Thêm tất cả vào panel chính
        contentPanel.add(topRowPanel);
        contentPanel.add(strengthPanel);
        contentPanel.add(recentReviewsPanel);
        
        // Đặt khoảng cách giữa các panel
        topRowPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        strengthPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
    }
    
    private void createYourRatingPanel() {
        yourRatingPanel = new JPanel();
        yourRatingPanel.setLayout(new BoxLayout(yourRatingPanel, BoxLayout.Y_AXIS));
        yourRatingPanel.setBackground(Color.WHITE);
        yourRatingPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        // Tiêu đề panel
        JLabel lblYourRating = new JLabel("Điểm Đánh Giá Của Bạn");
        lblYourRating.setFont(new Font("Arial", Font.BOLD, 14));
        lblYourRating.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblYourRating.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        // Hiển thị điểm số và sao
        JPanel scorePanel = new JPanel();
        scorePanel.setBackground(Color.WHITE);
        scorePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblScore = new JLabel("4.85");//Tính tb cộng số điểm đánh giá
        lblScore.setFont(new Font("Arial", Font.BOLD, 40));
        lblScore.setForeground(ColorMain.blueHeader);
        
        JLabel lblStar = new JLabel();
        if (starIcon != null) {
            lblStar.setIcon(largeStarIcon);
        } else {
            lblStar.setText("⭐");
            lblStar.setFont(new Font("Arial", Font.PLAIN, 30));  
        }
        
        scorePanel.add(lblScore);
        scorePanel.add(lblStar);
        
        // Hiển thị số lượng đánh giá
        JPanel ratingCountPanel = new JPanel();
        ratingCountPanel.setBackground(Color.WHITE);
        ratingCountPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblRatingCount = new JLabel("dựa trên 126 đánh giá");
        lblRatingCount.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRatingCount.setForeground(Color.GRAY);
        ratingCountPanel.add(lblRatingCount);
        
        // Thêm vào panel với khoảng cách đều nhau
        yourRatingPanel.add(Box.createVerticalGlue());
        yourRatingPanel.add(lblYourRating);
        yourRatingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        yourRatingPanel.add(scorePanel);
        yourRatingPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        yourRatingPanel.add(ratingCountPanel);
        yourRatingPanel.add(Box.createVerticalGlue());
    }
    
    private void createRatingDistPanel() {
        ratingDistPanel = new JPanel();
        ratingDistPanel.setLayout(new BoxLayout(ratingDistPanel, BoxLayout.Y_AXIS));
        ratingDistPanel.setBackground(Color.WHITE);
        ratingDistPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        // Tiêu đề panel
        JLabel lblDistTitle = new JLabel("Phân Bố Đánh Giá");
        lblDistTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblDistTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDistTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Panel chứa các thanh đánh giá
        JPanel barsPanel = new JPanel();
        barsPanel.setLayout(new GridLayout(5, 3, 5, 5));
        barsPanel.setBackground(Color.WHITE);
        barsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        // Tạo mảng các thành phần
        ratingBars = new JProgressBar[5];
        ratingLabels = new JLabel[5];
        ratingPercents = new JLabel[5];
                
        // Tạo các thanh đánh giá
        for (int i = 0; i < 5; i++) {
            // Label số sao - thay bằng hình ảnh
            ratingLabels[i] = new JLabel();
            JPanel starPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
            starPanel.setBackground(Color.WHITE);
            
            // Tạo panel chứa số + ảnh sao
            JLabel numberLabel = new JLabel((5 - i) + " ");
            numberLabel.setFont(new Font("Arial", Font.BOLD, 12));
            starPanel.add(numberLabel);
            
            // Thêm hình ảnh sao
            JLabel starLabel = new JLabel();
            if (starIcon != null) {
                starLabel.setIcon(starIcon);
            } else {
                starLabel.setText("★");
                starLabel.setFont(new Font("Arial", Font.BOLD, 12));
            }
            starPanel.add(starLabel);
            
            ratingLabels[i].add(starPanel);
            
            // Thanh progress
            ratingBars[i] = new JProgressBar(0, 100);
            ratingBars[i].setValue(0);
            ratingBars[i].setForeground(new Color(0, 186, 0)); // Màu xanh lá
            ratingBars[i].setBackground(new Color(230, 230, 230));
            ratingBars[i].setBorderPainted(false);
            
            // Label phần trăm
            ratingPercents[i] = new JLabel(0 + "%");
            ratingPercents[i].setFont(new Font("Arial", Font.PLAIN, 12));
            
            // Thêm vào panel
            barsPanel.add(starPanel); // Thay thế ratingLabels[i] bằng starPanel
            barsPanel.add(ratingBars[i]);
            barsPanel.add(ratingPercents[i]);
        }
        
        // Thêm vào panel phân bố
        ratingDistPanel.add(lblDistTitle);
        ratingDistPanel.add(barsPanel);
    }
    
    private void createStrengthPanel() {
        strengthPanel = new JPanel();
        strengthPanel.setLayout(new BoxLayout(strengthPanel, BoxLayout.Y_AXIS));
        strengthPanel.setBackground(Color.WHITE);
        strengthPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        // Tiêu đề panel
        JLabel lblStrengthTitle = new JLabel("Điểm Mạnh Của Bạn");
        lblStrengthTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblStrengthTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStrengthTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Panel chứa các điểm mạnh
        JPanel strengthsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        strengthsPanel.setBackground(Color.WHITE);
        strengthsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        // Tạo các ô điểm mạnh
        for (String label : strengthLabels) {
            JPanel strengthBox = new JPanel();
            strengthBox.setLayout(new BorderLayout());
            strengthBox.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
            strengthBox.setBackground(Color.WHITE);
            
            JLabel lblStrength = new JLabel(label);
            lblStrength.setFont(new Font("Arial", Font.PLAIN, 14));
            lblStrength.setHorizontalAlignment(SwingConstants.CENTER);
            lblStrength.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            strengthBox.add(lblStrength, BorderLayout.CENTER);
            strengthsPanel.add(strengthBox);
        }
        
        // Thêm vào panel điểm mạnh
        strengthPanel.add(lblStrengthTitle);
        strengthPanel.add(strengthsPanel);
    }
    
    private void createRecentReviewsPanel() {
        recentReviewsPanel = new JPanel();
        recentReviewsPanel.setLayout(new BoxLayout(recentReviewsPanel, BoxLayout.Y_AXIS));
        recentReviewsPanel.setBackground(Color.WHITE);
        recentReviewsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        
        // Tiêu đề panel
        JLabel lblRecentReviewsTitle = new JLabel("Đánh Giá Gần Đây");
        lblRecentReviewsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblRecentReviewsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRecentReviewsTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Panel lọc với combobox
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        // ComboBox và TextField
        JLabel lblOrderId = new JLabel("Mã đặt xe");
        orderIdCombo = new JComboBox<>();
        orderIdCombo.setPreferredSize(new Dimension(120, 25));
        
        JLabel lblRating = new JLabel("Đánh giá");
        ratingTextField = new JTextField(8);
        ratingTextField.setPreferredSize(new Dimension(90, 25));
        ratingTextField.setEditable(false);
        
        // Thêm vào panel lọc
        filterPanel.add(lblOrderId);
        filterPanel.add(orderIdCombo);
        filterPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        filterPanel.add(lblRating);
        filterPanel.add(ratingTextField);
        
        // Tạo bảng đánh giá
        String[] columnNames = {"Mã đặt xe", "Tên khách hàng", "Đánh giá", "Nội dung đánh giá"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        // Tạo JTable và ScrollPane
        reviewsTable = new JTable(tableModel);
        reviewsTable.setRowHeight(30);
        
        // Căn giữa nội dung các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        for (int i = 0; i < reviewsTable.getColumnCount() - 1; i++) {
            reviewsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Độ rộng của các cột
        reviewsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        reviewsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        reviewsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        reviewsTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(reviewsTable);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        
        // Thêm tất cả vào panel đánh giá gần đây
        recentReviewsPanel.add(lblRecentReviewsTitle);
        recentReviewsPanel.add(filterPanel);
        recentReviewsPanel.add(scrollPane);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnBack) {
            this.dispose(); // đóng cửa sổ hiện tại
                new TrangChuDriverView(taiKhoan);
        }
    }
    public static void main(String[] args) {
        new DanhGiaTaiXeView();
    }
}