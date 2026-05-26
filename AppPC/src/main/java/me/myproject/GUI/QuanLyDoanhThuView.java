package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.toedter.calendar.JDateChooser;

import me.myproject.BUSINESSLOGIC.TaiXeBSL;
import me.myproject.MODEL.TaiXe;
import me.myproject.Utilities.DIMENSION.DimensionFrame;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.GsonUtil;

public class QuanLyDoanhThuView extends FrameMain implements ActionListener {

    private JPanel panelChinh, panelTieuDe, panelNoiDung, panelLoc, panelThongKe, panelBieuDo, panelChiTiet, panelNutBam;
    private JLabel lblCapNhat;
    
    // Thành phần lọc
    private JComboBox<String> cboThoiGian, cboBieuDo;
    private JDateChooser dateFrom, dateTo;
    private JButton btnApDung, btnLamMoi, btnThoat;

    // Labels Thống Kê
    private JLabel lblValDoanhThu, lblValChuyenDi, lblValTrungBinh, lblValPhiDichVu;

    private JPanel pnlBieuDoContent, panelTabChiTiet;
    private JPanel tabNgay, tabTuan, tabThang, tabNam;
    private JTable tableChiTiet;

    private TaiXeBSL business;
    private TaiXe taiXe;

    // --- BỘ NHỚ CACHE ĐỂ LỌC LỌC LOCAL ---
    private List<DailyRecord> allDataCache = new ArrayList<>();
    private SimpleDateFormat sdfParse = new SimpleDateFormat("dd/MM/yyyy");

    // Định dạng
    private DecimalFormat dinhDangTien = new DecimalFormat("###,###,### VNĐ");
    private Color titleCyan = new Color(0, 188, 212);
    private Color greenColor = new Color(0, 150, 136);
    private Color blueColor = new Color(33, 150, 243);
    private Color orangeColor = new Color(255, 152, 0);
    private Color purpleColor = new Color(156, 39, 176);
    private Color redColor = new Color(244, 67, 54);

    public QuanLyDoanhThuView(TaiXe taiXe) throws Exception {
        super("RideBook - Quản Lý Doanh Thu & Thống Kê");
        this.business = new TaiXeBSL();
        this.taiXe = taiXe;

        khoiTaoThanhPhan();
        thietLapGiaoDien();

        // 1. Gọi API lấy toàn bộ dữ liệu 1 lần
        loadDataFromServer();

        // 2. Tự động set bộ lọc là "Tháng này" làm mặc định và render
        cboThoiGian.setSelectedIndex(2); 

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    // Class nội bộ dùng để lưu trữ cache dữ liệu thô từ Server
    class DailyRecord {
        Date date;
        int tripCount;
        double revenue;

        public DailyRecord(Date date, int tripCount, double revenue) {
            this.date = date;
            this.tripCount = tripCount;
            this.revenue = revenue;
        }
    }

    private void khoiTaoThanhPhan() {
        panelChinh = new JPanel(new BorderLayout());
        panelChinh.setBackground(Color.WHITE);

        panelTieuDe = new JPanel(new BorderLayout());
        panelTieuDe.setBackground(titleCyan);
        panelTieuDe.setPreferredSize(new Dimension(DimensionFrame.widthFrame, 35));

        panelNoiDung = new JPanel();
        panelNoiDung.setLayout(new BoxLayout(panelNoiDung, BoxLayout.Y_AXIS));
        panelNoiDung.setBackground(Color.WHITE);
        panelNoiDung.setBorder(new EmptyBorder(10, 10, 10, 10));

        // -- BỘ LỌC --
        panelLoc = new JPanel();
        panelLoc.setLayout(new BoxLayout(panelLoc, BoxLayout.Y_AXIS));
        panelLoc.setBackground(Color.WHITE);
        panelLoc.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        cboThoiGian = new JComboBox<>(new String[]{"Hôm nay", "Tuần này", "Tháng này", "Tùy chỉnh"});
        dateFrom = new JDateChooser(); 
        dateTo = new JDateChooser(); 

        btnApDung = taoNut("Áp Dụng", greenColor);
        btnLamMoi = taoNut("Làm Mới", redColor);
        btnThoat = taoNut("Thoát", redColor);

        // Bắt sự kiện thay đổi ComboBox Thời Gian
        cboThoiGian.addActionListener(e -> setDateBySelection());

        // -- THỐNG KÊ --
        lblValDoanhThu = new JLabel("0 VNĐ", SwingConstants.CENTER);
        lblValChuyenDi = new JLabel("0", SwingConstants.CENTER);
        lblValTrungBinh = new JLabel("0 VNĐ", SwingConstants.CENTER);
        lblValPhiDichVu = new JLabel("0 VNĐ", SwingConstants.CENTER);

        panelThongKe = new JPanel(new GridLayout(1, 4, 10, 0));
        panelThongKe.setBackground(Color.WHITE);
        panelThongKe.add(taoPanelThongKe("TỔNG DOANH THU", lblValDoanhThu, greenColor));
        panelThongKe.add(taoPanelThongKe("TỔNG CHUYẾN ĐI", lblValChuyenDi, blueColor));
        panelThongKe.add(taoPanelThongKe("TRUNG BÌNH/CHUYẾN", lblValTrungBinh, orangeColor));
        panelThongKe.add(taoPanelThongKe("PHÍ DỊCH VỤ (20%)", lblValPhiDichVu, purpleColor));

        // -- BIỂU ĐỒ & BẢNG --
        panelBieuDo = new JPanel(new BorderLayout());
        panelBieuDo.setBackground(Color.WHITE);
        cboBieuDo = new JComboBox<>(new String[]{"Cột", "Đường", "Tròn"});
        
        panelChiTiet = new JPanel(new BorderLayout());
        panelChiTiet.setBackground(Color.WHITE);
        panelTabChiTiet = new JPanel();
        panelTabChiTiet.setLayout(new BoxLayout(panelTabChiTiet, BoxLayout.X_AXIS));
        
        tabNgay = taoTab("Theo ngày", true);
        tabTuan = taoTab("Theo tuần", false);
        tabThang = taoTab("Theo tháng", false);
        tabNam = taoTab("Theo năm", false); 

        String[] columnNames = {"Thời gian", "Số chuyến đi", "Doanh thu (VNĐ)"};
        tableChiTiet = new JTable(new DefaultTableModel(columnNames, 0));
        tableChiTiet.setRowHeight(25);
        tableChiTiet.getTableHeader().setBackground(new Color(230, 230, 230));

        // -- NÚT CHỨC NĂNG --
        panelNutBam = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    }

    private void thietLapGiaoDien() {
        // Tiêu đề
        JLabel lblTieuDe = new JLabel("Quản Lý Doanh Thu & Thống Kê");
        lblTieuDe.setForeground(Color.WHITE);
        lblTieuDe.setFont(new Font("Arial", Font.BOLD, 16));
        lblTieuDe.setBorder(new EmptyBorder(5, 15, 5, 0));
        lblCapNhat = new JLabel("Cập nhật lần cuối: -");
        lblCapNhat.setForeground(Color.WHITE);
        panelTieuDe.add(lblTieuDe, BorderLayout.WEST);
        panelTieuDe.add(lblCapNhat, BorderLayout.EAST);

        // Lọc
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterRow.setBackground(Color.WHITE);
        filterRow.add(new JLabel("Thời gian:")); filterRow.add(cboThoiGian);
        filterRow.add(new JLabel("Từ ngày:")); filterRow.add(dateFrom);
        filterRow.add(new JLabel("Đến ngày:")); filterRow.add(dateTo);
        filterRow.add(btnApDung); filterRow.add(btnLamMoi);
        panelLoc.add(filterRow);

        // Biểu Đồ
        JPanel pnlBieuDoContainer = new JPanel(new BorderLayout());
        pnlBieuDoContainer.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JPanel headerBD = new JPanel(new BorderLayout());
        headerBD.setBackground(Color.WHITE);
        headerBD.setBorder(new EmptyBorder(5, 5, 5, 5));
        headerBD.add(new JLabel("Biểu đồ doanh thu"), BorderLayout.WEST);
        headerBD.add(cboBieuDo, BorderLayout.EAST);

        pnlBieuDoContent = new JPanel(new BorderLayout());
        pnlBieuDoContent.setBackground(new Color(245, 245, 250));
        pnlBieuDoContainer.add(headerBD, BorderLayout.NORTH);
        pnlBieuDoContainer.add(pnlBieuDoContent, BorderLayout.CENTER);
        panelBieuDo.add(pnlBieuDoContainer, BorderLayout.CENTER);

        // Bảng chi tiết
        panelTabChiTiet.add(tabNgay); panelTabChiTiet.add(tabTuan);
        panelTabChiTiet.add(tabThang); panelTabChiTiet.add(tabNam);
        
        JPanel pnlTableWrap = new JPanel(new BorderLayout());
        pnlTableWrap.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(Color.WHITE);
        tableHeader.setBorder(new EmptyBorder(8, 8, 8, 8));
        tableHeader.add(new JLabel("Chi tiết dữ liệu"), BorderLayout.WEST);
        tableHeader.add(panelTabChiTiet, BorderLayout.CENTER);
        
        JScrollPane scrollTable = new JScrollPane(tableChiTiet);
        scrollTable.setPreferredSize(new Dimension(0, 160));
        pnlTableWrap.add(tableHeader, BorderLayout.NORTH);
        pnlTableWrap.add(scrollTable, BorderLayout.CENTER);
        panelChiTiet.add(pnlTableWrap, BorderLayout.CENTER);

        // Nút bấm
        panelNutBam.add(btnThoat);

        // Ráp panel Nội dung
        panelNoiDung.add(panelLoc); panelNoiDung.add(Box.createVerticalStrut(10));
        panelNoiDung.add(panelThongKe); panelNoiDung.add(Box.createVerticalStrut(10));
        panelNoiDung.add(panelBieuDo); panelNoiDung.add(Box.createVerticalStrut(10));
        panelNoiDung.add(panelChiTiet); panelNoiDung.add(Box.createVerticalStrut(10));
        panelNoiDung.add(panelNutBam);

        panelChinh.add(panelTieuDe, BorderLayout.NORTH);
        panelChinh.add(panelNoiDung, BorderLayout.CENTER);

        this.getContentPane().removeAll();
        this.getContentPane().add(panelChinh, BorderLayout.CENTER);

        // Bắt sự kiện đổi loại Biểu đồ
        cboBieuDo.addActionListener(e -> applyFilterAndRender());

        // Bắt sự kiện Click Tab
        java.awt.event.MouseAdapter tabClickEvent = new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setActiveTab((JPanel) evt.getSource());
                applyFilterAndRender(); // Lọc và vẽ lại ngay lập tức
            }
        };
        tabNgay.addMouseListener(tabClickEvent);
        tabTuan.addMouseListener(tabClickEvent);
        tabThang.addMouseListener(tabClickEvent);
        tabNam.addMouseListener(tabClickEvent);
    }

    // --- 1. GỌI API & LƯU CACHE LẦN ĐẦU ---
    private void loadDataFromServer() {
        try {
            // Lấy dữ liệu dạng chung từ BSL (tránh giả định đó là JsonObject nếu BSL trả về Map)
            Object rawResponse = business.layBaoCaoDoanhThu();
            allDataCache.clear();

            // Ép kiểu an toàn thông qua toJsonTree
            JsonElement rootElement = GsonUtil.getInstance().toJsonTree(rawResponse);
            
            if (rootElement != null && rootElement.isJsonObject()) {
                JsonObject response = rootElement.getAsJsonObject();
                
                if (response.has("result") && !response.get("result").isJsonNull()) {
                    JsonObject result = response.getAsJsonObject("result");
                    
                    if (result.has("details") && result.get("details").isJsonArray()) {
                        JsonArray detailsArray = result.getAsJsonArray("details");
                        
                        for (int i = 0; i < detailsArray.size(); i++) {
                            JsonObject row = detailsArray.get(i).getAsJsonObject();
                            String timeStr = row.has("timeLabel") ? row.get("timeLabel").getAsString() : "";
                            int trips = row.has("tripCount") ? row.get("tripCount").getAsInt() : 0;
                            double rev = row.has("revenue") ? row.get("revenue").getAsDouble() : 0;
                            
                            try {
                                Date date = sdfParse.parse(timeStr);
                                allDataCache.add(new DailyRecord(date, trips, rev));
                            } catch (Exception ex) {
                                System.err.println("Lỗi parse ngày từ API: " + timeStr);
                            }
                        }
                    }
                }
            }
            
            lblCapNhat.setText("Cập nhật lần cuối: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}

    // --- 2. LOGIC LỌC VÀ GỘP NHÓM TỪ CACHE MỖI KHI BẤM ÁP DỤNG/ĐỔI TAB ---
    private void applyFilterAndRender() {
        Date start = dateFrom.getDate();
        Date end = dateTo.getDate();

        if (start == null || end == null || start.after(end)) {
            JOptionPane.showMessageDialog(this, "Ngày tháng không hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Chuẩn hóa giờ phút giây để so sánh bao hàm
        start = resetTime(start, 0, 0, 0);
        end = resetTime(end, 23, 59, 59);

        double sumRevenue = 0;
        int sumTrips = 0;
        
        // Cấu trúc lưu trữ dữ liệu đã được gộp nhóm
        Map<String, DailyRecord> groupedData = new LinkedHashMap<>();
        String activeTab = getActiveTabKey();

        for (DailyRecord record : allDataCache) {
            if (!record.date.before(start) && !record.date.after(end)) {
                // Tính tổng quan
                sumRevenue += record.revenue;
                sumTrips += record.tripCount;

                // Tạo khóa (Key) để Group By
                String groupKey = getGroupKey(record.date, activeTab);

                // Cộng dồn dữ liệu vào Map
                DailyRecord groupedRecord = groupedData.getOrDefault(groupKey, new DailyRecord(record.date, 0, 0));
                groupedRecord.revenue += record.revenue;
                groupedRecord.tripCount += record.tripCount;
                groupedData.put(groupKey, groupedRecord);
            }
        }

        // 3. Cập nhật giao diện Tổng quan
        updateSummaryUI(sumRevenue, sumTrips);

        // 4. Vẽ lại Bảng & Biểu đồ từ groupedData
        renderChartAndTable(groupedData);
    }

    // --- CÁC HÀM TIỆN ÍCH LỌC DỮ LIỆU ---
    private String getGroupKey(Date date, String groupBy) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        switch (groupBy) {
            case "WEEK": return "Tuần " + cal.get(Calendar.WEEK_OF_YEAR) + " - " + cal.get(Calendar.YEAR);
            case "MONTH": return new SimpleDateFormat("MM/yyyy").format(date);
            case "YEAR": return new SimpleDateFormat("yyyy").format(date);
            case "DAY":
            default: return sdfParse.format(date);
        }
    }

    private Date resetTime(Date date, int hour, int min, int sec) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Hàm chọn nhanh thời gian
    private void setDateBySelection() {
        int index = cboThoiGian.getSelectedIndex();
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        
        if (index == 0) { // Hôm nay
            dateFrom.setDate(now);
            dateTo.setDate(now);
        } else if (index == 1) { // Tuần này
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            dateFrom.setDate(cal.getTime());
            cal.add(Calendar.DAY_OF_WEEK, 6);
            dateTo.setDate(cal.getTime());
        } else if (index == 2) { // Tháng này
            cal.set(Calendar.DAY_OF_MONTH, 1);
            dateFrom.setDate(cal.getTime());
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            dateTo.setDate(cal.getTime());
        }
        
        // Nếu không phải "Tùy chỉnh", tự động áp dụng lọc
        if (index != 3) {
            applyFilterAndRender();
        }
    }

    // --- CẬP NHẬT GIAO DIỆN ---
    private void updateSummaryUI(double totalRevenue, int totalTrips) {
        lblValDoanhThu.setText(dinhDangTien.format(totalRevenue));
        lblValChuyenDi.setText(String.valueOf(totalTrips));
        
        double avg = totalTrips > 0 ? (totalRevenue / totalTrips) : 0;
        lblValTrungBinh.setText(dinhDangTien.format(avg));
        
        double fee = totalRevenue * 0.2; 
        lblValPhiDichVu.setText(dinhDangTien.format(fee));
    }

    private void renderChartAndTable(Map<String, DailyRecord> groupedData) {
        DefaultTableModel model = (DefaultTableModel) tableChiTiet.getModel();
        model.setRowCount(0); 
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, DailyRecord> entry : groupedData.entrySet()) {
            String timeLabel = entry.getKey();
            DailyRecord record = entry.getValue();

            model.addRow(new Object[]{ timeLabel, record.tripCount, dinhDangTien.format(record.revenue) });
            dataset.addValue(record.revenue, "Doanh Thu", timeLabel);
        }

        String chartType = (String) cboBieuDo.getSelectedItem();
        JFreeChart chart = createChart(dataset, chartType);
        
        pnlBieuDoContent.removeAll();
        if (chart != null) {
            ChartPanel chartPanel = new ChartPanel(chart);
            pnlBieuDoContent.add(chartPanel, BorderLayout.CENTER);
        }
        pnlBieuDoContent.revalidate();
        pnlBieuDoContent.repaint();
    }

    private JFreeChart createChart(DefaultCategoryDataset dataset, String type) {
        if (dataset.getRowCount() == 0) return null;
        JFreeChart chart;
        String xLabel = "Thời gian", yLabel = "Doanh thu (VNĐ)";

        if ("Đường".equals(type)) {
            chart = ChartFactory.createLineChart("", xLabel, yLabel, dataset, PlotOrientation.VERTICAL, false, true, false);
        } else if ("Tròn".equals(type)) {
            DefaultPieDataset pieDataset = new DefaultPieDataset();
            for (int i = 0; i < dataset.getColumnCount(); i++) pieDataset.setValue(dataset.getColumnKey(i).toString(), dataset.getValue(0, i));
            chart = ChartFactory.createPieChart("", pieDataset, true, true, false);
        } else {
            chart = ChartFactory.createBarChart("", xLabel, yLabel, dataset, PlotOrientation.VERTICAL, false, true, false);
        }

        if (!"Tròn".equals(type) && chart.getCategoryPlot() != null) {
            NumberAxis rangeAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
            rangeAxis.setNumberFormatOverride(dinhDangTien);
        }
        chart.getPlot().setBackgroundPaint(new Color(245, 245, 250));
        return chart;
    }

    // --- UI HELPER ---
    private JPanel taoPanelThongKe(String title, JLabel lblValue, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(Color.LIGHT_GRAY));
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlHeader.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 11));
        pnlHeader.add(lblTitle);
        lblValue.setFont(new Font("Arial", Font.BOLD, 18));
        lblValue.setForeground(color);
        lblValue.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(pnlHeader, BorderLayout.NORTH);
        panel.add(lblValue, BorderLayout.CENTER);
        return panel;
    }

    private JPanel taoTab(String tenTab, boolean active) {
        JPanel tab = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        tab.setBackground(active ? titleCyan : Color.WHITE);
        tab.setBorder(active ? BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY) : BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        JLabel lblTab = new JLabel(tenTab);
        lblTab.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTab.setForeground(active ? Color.WHITE : Color.BLACK);
        tab.add(lblTab);
        return tab;
    }

    private void setActiveTab(JPanel activeTab) {
        JPanel[] tabs = {tabNgay, tabTuan, tabThang, tabNam};
        for (JPanel t : tabs) {
            boolean isActive = (t == activeTab);
            t.setBackground(isActive ? titleCyan : Color.WHITE);
            t.setBorder(isActive ? BorderFactory.createMatteBorder(1, 1, 0, 1, Color.LIGHT_GRAY) : BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            ((JLabel) t.getComponent(0)).setForeground(isActive ? Color.WHITE : Color.BLACK);
        }
    }

    private String getActiveTabKey() {
        if (tabNgay.getBackground().equals(titleCyan)) return "DAY";
        if (tabTuan.getBackground().equals(titleCyan)) return "WEEK";
        if (tabThang.getBackground().equals(titleCyan)) return "MONTH";
        return "YEAR";
    }

    private JButton taoNut(String text, Color bg) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics2D g) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(bg);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(100, 30));
        btn.addActionListener(this);
        return btn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnApDung) {
            applyFilterAndRender();
        } else if (src == btnLamMoi) {
            loadDataFromServer();
            cboThoiGian.setSelectedIndex(2); // Trở về tháng này
        } else if (src == btnThoat) {
            this.dispose();
        }
    }
}