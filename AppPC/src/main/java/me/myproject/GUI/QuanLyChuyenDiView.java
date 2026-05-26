package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.toedter.calendar.JDateChooser;

import me.myproject.BUSINESSLOGIC.QuanLyChuyenDiBSL;
import me.myproject.MODEL.DatXe;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.Enum.BookingStatus;

public class QuanLyChuyenDiView extends FrameMain implements ActionListener {
	private QuanLyChuyenDiBSL business;
    // Các panel thống kê
    private JPanel pnlStats, pnlRideId;
    private JLabel lblTotalRides, lblCompletedRides, lblCanceledRides, lblTotalRevenue;
    
    private DecimalFormat dinhDangTien = new DecimalFormat("###,###,### VNĐ");
    // Các thành phần tìm kiếm
    private JTextField txtSearch;
    private JDateChooser dateFrom, dateTo;
    private JComboBox<String> cboFilter1, cboFilter2;
    private JButton btnSearch, btnClear, btnFilter, btnReset;
    
    // Bảng danh sách chuyến đi
    private JTable tblRides;
    private DefaultTableModel modelRides;
    
    // Các panel chi tiết
    private JPanel pnlRideDetails;
    private JLabel lblRideId, lblRideDate, lblRideTime;
    private JLabel lblPickupLocation, lblDropLocation, lblDistance, lblDuration;
    private JLabel lblPrice, lblStatus,lblRideIdLabel, lblRideIdValue, lblDate, lblTime, lblPickup, lblDropoff;
    
    // Thông tin khách hàng và tài xế
    private JPanel pnlCustomerInfo, pnlDriverInfo, pnlNotes;
    
    // Các nút hành động
    private JButton btnViewDetails, btnDeleteRide, btnExportExcel, btnPrintReport, btnExit;
    
    // Khai báo font ở cấp độ lớp
    private final Font fontTitle = new Font("Arial", Font.BOLD, 16);
    private final Font fontBold = new Font("Arial", Font.BOLD, 14);
    private final Font fontNormal = new Font("Arial", Font.PLAIN, 14);
    
    public QuanLyChuyenDiView() throws Exception {
        super("RideWave - Quản Lý Chuyến Đi");
        business = new QuanLyChuyenDiBSL();
        init();
    }
    
    public QuanLyChuyenDiView(TaiKhoan tk) {
         super("RideWave - Quản Lý Chuyến Đi");
    }

    private void init() throws Exception {
        // Ẩn các panel màu xanh ở bên trái và trên cùng
        pnL.setVisible(false);
        pnT.setVisible(false);
        
        // Bố cục chính
        pnC.setLayout(new BorderLayout(0, 0));
        
        // Panel tiêu đề
        JPanel headerPanel = createHeaderPanel();
        pnC.add(headerPanel, BorderLayout.NORTH);
        
        // Panel nội dung chính
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        
        // Panel thống kê
        pnlStats = createStatsPanel();
        contentPanel.add(pnlStats, BorderLayout.NORTH);
        
        // Tạo panel trung tâm với danh sách chuyến đi và chi tiết
        JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
        
        // Panel tìm kiếm
        JPanel searchPanel = createSearchPanel();
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Panel chia cho bảng chuyến đi và chi tiết
        JPanel splitPanel = new JPanel(new BorderLayout(10, 0));
        
        // Bảng chuyến đi
        JPanel ridesPanel = createRidesPanel();
        splitPanel.add(ridesPanel, BorderLayout.CENTER);
        
        // Panel chi tiết chuyến đi (khởi tạo ban đầu)
        try {
            business.fetchChuyenDi();
            List<DatXe> chuyenDiList = business.getChuyenDiList();
            if (!chuyenDiList.isEmpty()) {
                pnlRideDetails = createRideDetailsPanel(chuyenDiList.get(0)); // Khởi tạo với chuyến xe đầu tiên
                tblRides.setRowSelectionInterval(0, 0); // Chọn dòng đầu tiên
            } else {
                pnlRideDetails = createRideDetailsPanel(null); // Khởi tạo với panel rỗng nếu không có dữ liệu
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            pnlRideDetails = createRideDetailsPanel(null); // Đảm bảo khởi tạo mặc định
        }
        splitPanel.add(pnlRideDetails, BorderLayout.EAST);
        
        centerPanel.add(splitPanel, BorderLayout.CENTER);
        
        // Panel nút
        JPanel buttonPanel = createButtonPanel();
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        pnC.add(contentPanel, BorderLayout.CENTER);
        
        this.add(pnC, BorderLayout.CENTER);
        
        this.setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0, 178, 192)); // Màu xanh đậm
        panel.setPreferredSize(new Dimension(getWidth(), 50));
        
        JLabel lblTitle = new JLabel("Danh Sách Chuyến Đi");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0));
        panel.add(lblTitle, BorderLayout.WEST);  
        return panel;
    }
    
    private JPanel createStatsPanel() throws Exception {
        business.fetchChuyenDi();
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        // Thống kê tổng chuyến đi
        JPanel pnlTotalRides = createStatBox("Tổng Chuyến Đi",String.valueOf(business.calculateTotalTrips()), Color.BLACK);
        panel.add(pnlTotalRides);
        
        // Thống kê chuyến đi hoàn thành
        JPanel pnlCompletedRides = createStatBox("Hoàn Thành", String.valueOf(business.calculateCompletedTrips()), new Color(40, 167, 69));
        panel.add(pnlCompletedRides);
        
        // Thống kê chuyến đi đã hủy
        JPanel pnlCanceledRides = createStatBox("Đã Hủy", String.valueOf(business.calculateCanceledTrips()), Color.RED);
        panel.add(pnlCanceledRides);
        
        // Thống kê tổng doanh thu
        JPanel pnlTotalRevenue = createStatBox("Tổng Doanh Thu", String.valueOf(dinhDangTien.format(business.calculateTotalRevenue())), Color.BLUE);
        panel.add(pnlTotalRevenue);
        
        return panel;
    }
    
    private JPanel createStatBox(String title, String value, Color valueColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setBackground(Color.WHITE);
        
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(Color.WHITE);
        innerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Nhãn tiêu đề
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Nhãn giá trị
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.BOLD, 16));
        lblValue.setForeground(valueColor);
        lblValue.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        innerPanel.add(lblTitle);
        innerPanel.add(Box.createVerticalStrut(5));
        innerPanel.add(lblValue);
        
        panel.add(innerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.setBackground(Color.WHITE);
        
        // Panel điều khiển tìm kiếm
        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchControls.setBackground(Color.WHITE);
        searchControls.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        txtSearch = new JTextField("Tìm kiếm theo ID, tên khách hàng, địa điểm...");
        txtSearch.setPreferredSize(new Dimension(300, 30));
        txtSearch.setForeground(Color.GRAY);
        
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Tìm kiếm theo ID, tên khách hàng, địa điểm...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm kiếm theo ID, tên khách hàng, địa điểm...");
                    txtSearch.setForeground(Color.GRAY);
                }
            }
        });
        
        btnSearch = createButton("Tìm Kiếm", new Color(0, 123, 255), Color.WHITE);
        btnClear = createButton("Xóa", Color.LIGHT_GRAY, Color.BLACK);
        btnFilter = createButton("Áp Dụng Lọc", new Color(40, 167, 69), Color.WHITE);
        btnReset = createButton("Đặt Lại", Color.LIGHT_GRAY, Color.BLACK);
        
        searchControls.add(txtSearch);
        searchControls.add(btnSearch);
        searchControls.add(btnClear);
        searchControls.add(btnFilter);
        searchControls.add(btnReset);
        
        panel.add(searchControls);
        
        // Panel khoảng thời gian và bộ lọc
        JPanel dateRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        dateRangePanel.setBackground(Color.WHITE);
        dateRangePanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        JLabel lblDateRange = new JLabel("Khoảng thời gian:");
        
        dateFrom = new JDateChooser();
        dateFrom.setPreferredSize(new Dimension(120, 30));
        dateFrom.setDate(new Date());
        
        JLabel lblTo = new JLabel("đến");
        
        dateTo = new JDateChooser();
        dateTo.setPreferredSize(new Dimension(120, 30));
        dateTo.setDate(new Date());
        
        JLabel lblFilter = new JLabel("Bộ lọc:");
        
        // Khởi tạo combobox lọc
        cboFilter1 = new JComboBox<>(new String[]{"Tất cả trạng thái", "Hoàn thành", "Đã huỷ", "Đang xử lý", "Đang thực hiện", "Chờ tài xế nhận"});
        cboFilter1.setPreferredSize(new Dimension(150, 30));
        
        cboFilter2 = new JComboBox<>(new String[]{"Tất cả phương tiện", "Xe máy", "Xe ga", "Ô tô 4 chỗ", "Ô tô 7 chỗ"});
        cboFilter2.setPreferredSize(new Dimension(150, 30));
        
        dateRangePanel.add(lblDateRange);
        dateRangePanel.add(dateFrom);
        dateRangePanel.add(lblTo);
        dateRangePanel.add(dateTo);
        dateRangePanel.add(Box.createHorizontalStrut(20));
        dateRangePanel.add(lblFilter);
        dateRangePanel.add(cboFilter1);
        
        panel.add(dateRangePanel);
        
        return panel;
    }
    
    private JPanel createRidesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Bảng với các cột được chỉ định
        String[] columns = {"ID", "Ngày", "Khách hàng", "Tài xế", "Trạng thái"};
        modelRides = new DefaultTableModel(columns, 0);
        tblRides = new JTable(modelRides);
        loadTripData(business.getChuyenDiList());
        // Tùy chỉnh giao diện của bảng
        tblRides.setFont(fontNormal);
        tblRides.setRowHeight(25);
        tblRides.getTableHeader().setFont(fontBold);
        tblRides.getTableHeader().setBackground(new Color(240, 240, 240));
        tblRides.setSelectionBackground(new Color(232, 240, 254));
        tblRides.setGridColor(new Color(230, 230, 230));
        
        JScrollPane scrollPane = new JScrollPane(tblRides);
        // Áp dụng giao diện thanh cuộn tùy chỉnh
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadTripData(List<DatXe> chuyenDiList) {
        try {
            modelRides.setRowCount(0); 
            if (chuyenDiList == null || chuyenDiList.isEmpty()) 
                return;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (DatXe chuyenDi : chuyenDiList) { // Iterate through the list
                String formattedDate = dateFormat.format(chuyenDi.getBookingTime()); // Assuming getNgayDat() returns a Date
                modelRides.addRow(new Object[] {chuyenDi.getBookingId(), formattedDate, chuyenDi.getCustomerId(), chuyenDi.getDriverId(),chuyenDi.getBookingStatus().toString()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử chuyến đi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createRideDetailsPanel(DatXe x) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Chi Tiết Chuyến Đi", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 14)));
        panel.setPreferredSize(new Dimension(400, 600));
        // Panel thông tin chuyến đi
        JPanel pnlRideInfo = new JPanel();
        pnlRideInfo.setLayout(new BoxLayout(pnlRideInfo, BoxLayout.Y_AXIS));
        pnlRideInfo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        pnlRideInfo.setBackground(Color.WHITE);
        pnlRideInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Mã chuyến đi
        JPanel pnlRideId = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlRideId.setBackground(Color.WHITE);
        JLabel lblRideIdLabel = new JLabel("Mã chuyến:");
        lblRideIdLabel.setFont(fontBold);
        JLabel lblRideIdValue = new JLabel(x != null ? x.getBookingId() : "Không có dữ liệu");
        lblRideIdValue.setFont(fontBold);
        lblRideIdValue.setForeground(new Color(0, 102, 204));
        pnlRideId.add(lblRideIdLabel);
        pnlRideId.add(Box.createHorizontalStrut(5));
        pnlRideId.add(lblRideIdValue);
        pnlRideInfo.add(pnlRideId);
        // Ngày và thời gian
        JPanel pnlDateTime = new JPanel(new BorderLayout());
        pnlDateTime.setBackground(Color.WHITE);
        // Panel ngày bên trái
        JPanel pnlDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlDate.setBackground(Color.WHITE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        JLabel lblDate = new JLabel(x != null ? "Ngày: " + dateFormat.format(x.getBookingTime()) : "Ngày: Không có dữ liệu");
        lblDate.setFont(fontNormal);
        pnlDate.add(lblDate);
        // Panel thời gian bên phải
        JPanel pnlTime = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlTime.setBackground(Color.WHITE);
        JLabel lblTime = new JLabel(x != null ? "Thời gian: " + timeFormat.format(x.getBookingTime()) : "Thời gian: Không có dữ liệu");
        lblTime.setFont(fontNormal);
        pnlTime.add(lblTime);
        
        pnlDateTime.add(pnlDate, BorderLayout.WEST);
        pnlDateTime.add(pnlTime, BorderLayout.EAST);
        pnlRideInfo.add(pnlDateTime);
        // Điểm đón
        JPanel pnlPickup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlPickup.setBackground(Color.WHITE);
        JLabel lblPickup = new JLabel(x != null && x.getPickupLocation() != null ? "Điểm đón: " + x.getPickupLocation() : "Điểm đón: Không có dữ liệu");
        lblPickup.setFont(fontNormal);
        pnlPickup.add(lblPickup);
        pnlRideInfo.add(pnlPickup);
        // Điểm đến
        JPanel pnlDropoff = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlDropoff.setBackground(Color.WHITE);
        JLabel lblDropoff = new JLabel(x != null && x.getDropoffLocation() != null ? "Điểm đến: " + x.getDropoffLocation(): "Điểm đến: Không có dữ liệu");
        lblDropoff.setFont(fontNormal);
        pnlDropoff.add(lblDropoff);
        pnlRideInfo.add(pnlDropoff);
        
        // Khoảng cách và thời gian
        JPanel pnlDistTime = new JPanel(new BorderLayout());
        pnlDistTime.setBackground(Color.WHITE);
        
        JPanel pnlDistance = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlDistance.setBackground(Color.WHITE);
        JLabel lblDistance = new JLabel(x != null && x.getDistance() != null && x.getDistance() != 0 ? "Khoảng cách: " + x.getDistance() + " km" : "Khoảng cách: Không có dữ liệu");
        lblDistance.setFont(fontNormal);
        pnlDistance.add(lblDistance);
        
        JPanel pnlDuration = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlDuration.setBackground(Color.WHITE);
        String durationText;
        if (x != null && x.getBookingTime() != null && x.getArrivalTime() != null) {
            long diffInMillis = x.getArrivalTime().getTime() - x.getBookingTime().getTime();
            long diffInMinutes = diffInMillis / (1000 * 60); // Chuyển đổi sang phút
            if (diffInMinutes >= 60) {
                long hours = diffInMinutes / 60;
                long minutes = diffInMinutes % 60;
                durationText = "Thời gian: " + hours + " giờ " + minutes + " phút";
            } else 
                durationText = "Thời gian: " + diffInMinutes + " phút";
        } else 
            durationText = "Thời gian: Không có dữ liệu";
        JLabel lblDuration = new JLabel(durationText);
        lblDuration.setFont(fontNormal);
        pnlDuration.add(lblDuration);
        
        pnlDistTime.add(pnlDistance, BorderLayout.WEST);
        pnlDistTime.add(pnlDuration, BorderLayout.EAST);
        pnlRideInfo.add(pnlDistTime);
        
        // Giá tiền và Trạng thái
        JPanel pnlPriceStatus = new JPanel(new BorderLayout());
        pnlPriceStatus.setBackground(Color.WHITE);
        
        JPanel pnlPrice = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlPrice.setBackground(Color.WHITE);
        JLabel lblPrice = new JLabel(x != null ? "Giá tiền: " + String.format("%,.0f VND", x.getTotalPrice()) : "Giá tiền: Không có dữ liệu");
        lblPrice.setFont(fontBold);
        lblPrice.setForeground(new Color(25, 25, 112));
        pnlPrice.add(lblPrice);
        
        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlStatus.setBackground(Color.WHITE);
        JLabel lblStatus = new JLabel(x != null ? x.getBookingStatus().toString() : "Không có dữ liệu");
        lblStatus.setFont(fontBold);
        lblStatus.setForeground(x != null && x.getBookingStatus() == BookingStatus.COMPLETED ? new Color(40, 167, 69) : Color.RED);
        pnlStatus.add(lblStatus);
        
        pnlPriceStatus.add(pnlPrice, BorderLayout.WEST);
        pnlPriceStatus.add(pnlStatus, BorderLayout.EAST);
        pnlRideInfo.add(pnlPriceStatus);
        
        panel.add(pnlRideInfo);
        panel.add(Box.createVerticalStrut(15));
        
        // Panel kết hợp cuộn cho thông tin bổ sung
        JPanel combinedInfoPanel = new JPanel();
        combinedInfoPanel.setLayout(new BoxLayout(combinedInfoPanel, BoxLayout.Y_AXIS));
        combinedInfoPanel.setBackground(Color.WHITE);
        
        // Thêm panel thông tin khách hàng
        JPanel customerPanel = createInfoPanel("Thông tin khách", x != null ? x.getCustomerId() : "Không có dữ liệu", "SĐT: Chưa có thông tin", 4); // Cần lấy thông tin từ ID_KH
        customerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        combinedInfoPanel.add(customerPanel);
        combinedInfoPanel.add(Box.createVerticalStrut(15));
        
        // Thêm panel thông tin tài xế
        JPanel driverPanel = createInfoPanel("Thông tin tài xế",  x != null ? x.getDriverId() : "Không có dữ liệu", "Phương tiện: Chưa có thông tin", 5); // Cần lấy thông tin từ ID_TaiXe
        driverPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        combinedInfoPanel.add(driverPanel);
        combinedInfoPanel.add(Box.createVerticalStrut(15));
        
        // Thêm panel ghi chú
        JPanel notesPanel = createNotesPanel("Không có ghi chú");
        notesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        combinedInfoPanel.add(notesPanel);
        
        // Tạo một thanh cuộn cho tất cả các panel thông tin
        JScrollPane combinedScrollPane = new JScrollPane(combinedInfoPanel);
        combinedScrollPane.setBorder(null);
        combinedScrollPane.setPreferredSize(new Dimension(380, 320));
        combinedScrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        combinedScrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        panel.add(combinedScrollPane);
        
        return panel;
    }
    
    // Sửa createNotesPanel để nhận tham số ghi chú
    private JPanel createNotesPanel(String ghiChu) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true), 
                "Ghi chú",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                fontBold
        ));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblNotes = new JLabel(ghiChu);
        lblNotes.setFont(fontNormal);
        lblNotes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(lblNotes);
        
        return panel;
    }
    
    private JPanel createInfoPanel(String title, String name, String info, int rating) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true), 
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                fontBold
        ));
        panel.setBackground(Color.WHITE);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Phần ảnh đại diện
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = Math.min(getWidth(), getHeight()) - 4;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Vẽ nền hình tròn
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillOval(x, y, size, size);
                
                // Vẽ hình bóng người
                g2d.setColor(new Color(200, 200, 200));
                // Đầu
                int headSize = size / 3;
                g2d.fillOval(x + size/2 - headSize/2, y + size/4, headSize, headSize);
                
                // Thân
                g2d.fillOval(x + size/4, y + size/2, size/2, size/2);
                
                g2d.dispose();
            }
        };
        avatarPanel.setPreferredSize(new Dimension(50, 50));
        avatarPanel.setBackground(Color.WHITE);
        
        // Thông tin người dùng
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        // Nhãn tên với kiểu dáng tùy chỉnh
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(fontBold);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Nhãn thông tin
        JLabel infoLabel = new JLabel(info);
        infoLabel.setFont(fontNormal);
        infoLabel.setForeground(new Color(100, 100, 100));
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Sao đánh giá - Vẽ ngôi sao thay vì sử dụng ký tự Unicode
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        ratingPanel.setBackground(Color.WHITE);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        for (int i = 1; i <= 5; i++) {
            final boolean filled = i <= rating;
            JPanel star = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int size = 16;
                    int[] xPoints = new int[10];
                    int[] yPoints = new int[10];
                    
                    // Tính toán các điểm của ngôi sao
                    for (int i = 0; i < 10; i++) {
                        double angle = Math.PI / 2 + i * 2 * Math.PI / 10;
                        int radius = (i % 2 == 0) ? size / 2 : size / 4;
                        xPoints[i] = (int) (size / 2 + radius * Math.cos(angle));
                        yPoints[i] = (int) (size / 2 - radius * Math.sin(angle));
                    }
                    
                    // Tô màu cho ngôi sao dựa trên đánh giá
                    if (filled) {
                        g2d.setColor(new Color(255, 215, 0)); // Màu vàng cho sao được đánh giá
                    } else {
                        g2d.setColor(new Color(220, 220, 220)); // Màu xám nhạt cho sao trống
                    }
                    g2d.fillPolygon(xPoints, yPoints, 10);
                    
                    // Viền ngôi sao
                    g2d.setColor(filled ? new Color(218, 165, 32) : new Color(200, 200, 200));
                    g2d.drawPolygon(xPoints, yPoints, 10);
                    
                    g2d.dispose();
                }
            };
            star.setPreferredSize(new Dimension(18, 18));
            star.setOpaque(false);
            ratingPanel.add(star);
        }
        
        // Thêm các thành phần vào panel thông tin
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(infoLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(ratingPanel);
        
        // Lắp ráp panel nội dung
        contentPanel.add(avatarPanel, BorderLayout.WEST);
        contentPanel.add(infoPanel, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNotesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true), 
                "Ghi chú",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                fontBold
        ));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblNotes = new JLabel("Khách hàng yêu cầu đợi 5 phút tại điểm đón.");
        lblNotes.setFont(fontNormal);
        lblNotes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(lblNotes);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btnViewDetails = createHoverButton("Xem Chi Tiết", new Color(0, 123, 255), Color.WHITE);
        btnDeleteRide = createHoverButton("Cập nhật chuyến", new Color(220, 53, 69), Color.WHITE);
        btnExportExcel = createHoverButton("Xuất Excel", new Color(40, 167, 69), Color.WHITE);
        btnPrintReport = createHoverButton("In Báo Cáo", new Color(108, 117, 125), Color.WHITE);
        
        // Thêm nút thoát
        btnExit = createHoverButton("Thoát", new Color(220, 53, 69), Color.WHITE);
        
        panel.add(btnViewDetails);
        panel.add(btnDeleteRide);
        panel.add(btnExportExcel);
        panel.add(btnPrintReport);
        panel.add(btnExit);
        
        return panel;
    }
    
    private JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setFont(fontBold);
        button.addActionListener(this);
        
        // Thêm hiệu ứng 3D với viền kép
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        
        return button;
    }
    
    // Phương thức tạo nút với hiệu ứng hover
    private JButton createHoverButton(String text, Color bgColor, Color fgColor) {
        JButton button = createButton(text, bgColor, fgColor);
        
        // Lưu trữ màu sắc ban đầu
        final Color originalBg = bgColor;
        final Color darkerBg = darkenColor(bgColor, 0.9f); // Tối hơn 10% khi hover
        
        // Thêm hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(darkerBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });
        
        return button;
    }
    
    // Phương thức hỗ trợ để làm tối màu sắc cho hiệu ứng hover
    private Color darkenColor(Color color, float factor) {
        return new Color(
            Math.max((int)(color.getRed() * factor), 0),
            Math.max((int)(color.getGreen() * factor), 0),
            Math.max((int)(color.getBlue() * factor), 0)
        );
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        
        if (source == btnSearch) {
            String searchText = txtSearch.getText().trim().toLowerCase();
            if (searchText.equals("tìm kiếm theo id, tên khách hàng, địa điểm...")) 
                loadTripData(business.getChuyenDiList());
            else
                loadTripData(business.TimKiemChuyen(searchText));
        } else if (source == btnClear) {
            txtSearch.setText("Tìm kiếm theo ID, tên khách hàng, địa điểm...");
            txtSearch.setForeground(Color.GRAY);
        } else if (source == btnFilter) {
            String trangThai = (String) cboFilter1.getSelectedItem();
            Date tuNgay = dateFrom.getDate();
            Date denNgay = dateTo.getDate();
            if (tuNgay.after(denNgay)) {
                JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                System.out.println(business.LocChuyenDi(trangThai,new Timestamp(tuNgay.getTime()), new Timestamp(denNgay.getTime())));
                loadTripData(business.LocChuyenDi(trangThai,new Timestamp(tuNgay.getTime()), new Timestamp(denNgay.getTime())));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (source == btnReset) {
            System.out.println("Đặt lại bộ lọc");
            
        } else if (source == btnViewDetails) {
            System.out.println("Xem chi tiết chuyến đi");
            int selectedRow = tblRides.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một chuyến đi để xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Lấy ID chuyến đi từ dòng được chọn
            String tripId = (String) modelRides.getValueAt(selectedRow, 0); // Cột "ID"
            DatXe selectedTrip = business.getChuyenDi(tripId);
            if (selectedTrip == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin chuyến đi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Cập nhật panel chi tiết với chuyến đi được chọn
            JPanel newRideDetailsPanel = createRideDetailsPanel(selectedTrip);
            updateRideDetailsPanel(newRideDetailsPanel);
        } else if (source == btnDeleteRide) {
    
        } else if (source == btnExportExcel) {
        try {
                // Create a new workbook and sheet
                XSSFWorkbook workbook = new XSSFWorkbook();
                XSSFSheet sheet = workbook.createSheet("Trip Data");

                // Create header row
                XSSFRow headerRow = sheet.createRow(0);
                String[] headers = {"ID Đặt Xe", "Ngày Đặt", "ID Khách Hàng", "ID Tài Xế", "Trạng Thái"};
                for (int i = 0; i < headers.length; i++) {
                    XSSFCell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // Populate data from modelRides
                for (int i = 0; i < modelRides.getRowCount(); i++) {
                    XSSFRow row = sheet.createRow(i + 1); // Start from row 1 (after header)
                    for (int j = 0; j < modelRides.getColumnCount(); j++) {
                        XSSFCell cell = row.createCell(j);
                        Object value = modelRides.getValueAt(i, j);
                        if (value != null) {
                            cell.setCellValue(value.toString()); // Convert to string for simplicity
                        } else {
                            cell.setCellValue(""); // Handle null values
                        }
                    }
                }

                // Auto-size columns for better readability
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Open a file chooser to let the user select the save location
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Lưu tệp Excel");
                fileChooser.setSelectedFile(new File("TripData.xlsx")); // Default file name
                fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));

                int userSelection = fileChooser.showSaveDialog(this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    String filePath = fileToSave.getAbsolutePath();
                    // Ensure the file has .xlsx extension
                    if (!filePath.toLowerCase().endsWith(".xlsx")) {
                        filePath += ".xlsx";
                    }

                    // Save the workbook to the selected file
                    try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                        workbook.write(fileOut);
                        JOptionPane.showMessageDialog(this, "Xuất tệp Excel thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                // Close the workbook to free resources
                workbook.close();

            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + e1.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }  
        } else if (source == btnPrintReport) {
            System.out.println("In báo cáo");
            
        } else if (source == btnExit) {
            System.out.println("Thoát ứng dụng");
            dispose(); // Đóng cửa sổ
        }
    }
    private void updateRideDetailsPanel(JPanel newRideDetailsPanel) {
        // Tìm panel cha chứa pnlRideDetails (splitPanel)
        Container parent = pnlRideDetails.getParent();
        if (parent != null) {
            parent.remove(pnlRideDetails);
            pnlRideDetails = newRideDetailsPanel;
            parent.add(pnlRideDetails, BorderLayout.EAST);
            parent.revalidate();
            parent.repaint();
        }
    }
    // Lớp UI thanh cuộn tùy chỉnh để cải thiện trải nghiệm cuộn
    class CustomScrollBarUI extends BasicScrollBarUI {
        private final Color trackColor = new Color(240, 240, 240);
        private final Color thumbColor = new Color(180, 180, 180);
        private final Color thumbHoverColor = new Color(160, 160, 160);
        
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
        
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
        
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? thumbHoverColor : thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }
    }
    
    public static void main(String[] args) throws Exception {
        new QuanLyChuyenDiView();
    }
}
