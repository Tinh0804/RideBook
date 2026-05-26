package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import me.myproject.BUSINESSLOGIC.ChuyenDiBSL;
import me.myproject.MODEL.DatXe;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.ColorMain;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.Enum.BookingStatus;

public class HuyChuyenDiView extends FrameMain {
    private final ChuyenDiBSL business;
    private JTable currentTripsTable;
    private DefaultTableModel model;
    private JTextField reasonField;
    private JButton cancelButton, backButton;
    private JPanel mainPanel;
    private JPanel centerPanel;
    private JScrollPane scrollPane;
    private TaiKhoan tk;

    // Khai báo các label ở cấp class để dễ dàng cập nhật khi click vào bảng
    private JLabel routeLabel, driverNameLabel, driverPhoneLabel, licensePlateLabel, carTypeLabel;

    public HuyChuyenDiView(TaiKhoan taiKhoan) throws Exception {
        super("Hủy chuyến đi");
        this.tk = taiKhoan;
        this.business = new ChuyenDiBSL();
        initComponents();
        loadTripData(business.getChuyenDiDangChoTheoKH(tk.getID_Ref()));
        this.setVisible(true);
    }

    private void initComponents() {
        Dimension frameDimension = this.getSize();
        int frameWidth = frameDimension.width;

        // Main panel
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(ColorMain.colorBlue1);

        // --- HEADER PANEL (CÓ NÚT BACK) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorMain.colorSecondary);
        headerPanel.setPreferredSize(new Dimension(frameWidth, 60));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Nút Back
        backButton = new JButton("❮ Trở về");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setForeground(Color.WHITE);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            this.dispose(); // Đóng cửa sổ hiện tại
            // Có thể mở lại Trang Chủ tại đây: new TrangChuKhachHangView(tk);
        });

        JLabel headerLabel = new JLabel("HỦY CHUYẾN ĐI", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);

        // Panel trống để cân bằng label ở giữa theo BorderLayout
        JPanel dummyPanel = new JPanel();
        dummyPanel.setPreferredSize(new Dimension(100, 30));
        dummyPanel.setOpaque(false);

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(dummyPanel, BorderLayout.EAST);

        // --- CONTENT PANEL ---
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(ColorMain.colorBlue1);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Table Panel
        JPanel tripsPanel = new JPanel(new BorderLayout(0, 10));
        tripsPanel.setBackground(ColorMain.colorBlue1);

        JLabel currentTripsLabel = new JLabel("Chuyến đi hiện tại của bạn:");
        currentTripsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        tripsPanel.add(currentTripsLabel, BorderLayout.NORTH);

        String[] columnNames = { "Mã chuyến", "Mã tài xế", "Điểm đón", "Điểm đến", "Loại xe" };
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Khóa edit trực tiếp trên bảng
        };

        currentTripsTable = new JTable(model);
        currentTripsTable.setRowHeight(30);
        currentTripsTable.setFont(new Font("Arial", Font.PLAIN, 13));
        currentTripsTable.setShowGrid(true);
        currentTripsTable.setSelectionBackground(new Color(226, 242, 238));
        currentTripsTable.setSelectionForeground(Color.BLACK);
        currentTripsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION); // Chỉ chọn 1 dòng

        JTableHeader header = currentTripsTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(new Color(220, 240, 235));
        
        scrollPane = new JScrollPane(currentTripsTable);
        tripsPanel.add(scrollPane, BorderLayout.CENTER);

        // Sự kiện khi click vào 1 dòng trong bảng -> Cập nhật thông tin chi tiết
        currentTripsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && currentTripsTable.getSelectedRow() != -1) {
                int row = currentTripsTable.getSelectedRow();
                String diemDon = model.getValueAt(row, 2).toString();
                String diemTra = model.getValueAt(row, 3).toString();
                
                routeLabel.setText(" " + diemDon + " — " + diemTra);
                // TODO: Gọi API lấy chi tiết tài xế bằng ID_TX ở model.getValueAt(row, 1) nếu cần
                driverNameLabel.setText("     Đang lấy thông tin...");
            }
        });

        // 2. Info Panel
        JPanel infoPanel = new JPanel(new BorderLayout(0, 10));
        infoPanel.setBackground(ColorMain.colorBlue1);

        JLabel tripInfoLabel = new JLabel("Thông tin chuyến đi:");
        tripInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(tripInfoLabel, BorderLayout.NORTH);

        JPanel tripDetailsPanel = new JPanel();
        tripDetailsPanel.setLayout(new BoxLayout(tripDetailsPanel, BoxLayout.Y_AXIS));
        tripDetailsPanel.setBackground(ColorMain.inforPanel);
        tripDetailsPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 20, 15, 20)));

        JPanel routePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        routePanel.setBackground(ColorMain.inforPanel);
        routeLabel = new JLabel(" Vui lòng chọn chuyến đi từ bảng phía trên");
        routeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        routeLabel.setForeground(new Color(20, 120, 180));
        routePanel.add(new JLabel("📍"));
        routePanel.add(routeLabel);

        JPanel driverPanel = createInfoRow(" Thông tin tài xế:", ColorMain.inforPanel, true);
        JPanel namePanel = createInfoRow("", ColorMain.inforPanel, false);
        driverNameLabel = (JLabel) namePanel.getComponent(0);
        
        tripDetailsPanel.add(routePanel);
        tripDetailsPanel.add(Box.createVerticalStrut(15));
        tripDetailsPanel.add(driverPanel);
        tripDetailsPanel.add(Box.createVerticalStrut(5));
        tripDetailsPanel.add(namePanel);

        infoPanel.add(tripDetailsPanel, BorderLayout.CENTER);

        // 3. Reason Panel
        JPanel reasonPanel = new JPanel(new BorderLayout(0, 10));
        reasonPanel.setBackground(ColorMain.colorBlue1);

        JLabel reasonLabel = new JLabel("Lý do hủy (Bắt buộc):");
        reasonLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        reasonField = new JTextField();
        reasonField.setFont(new Font("Arial", Font.PLAIN, 14));
        reasonField.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 10, 10, 10)));

        reasonPanel.add(reasonLabel, BorderLayout.NORTH);
        reasonPanel.add(reasonField, BorderLayout.CENTER);

        // Gom Layout
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(ColorMain.colorBlue1);

        centerPanel.add(tripsPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(infoPanel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(reasonPanel);

        // 4. Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(ColorMain.colorBlue1);

        cancelButton = new JButton("XÁC NHẬN HỦY");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(Color.RED); // Đỏ chuẩn Bootstrap
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setBorder(new EmptyBorder(10, 25, 10, 25));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        cancelButton.addActionListener(e -> processCancelTrip());

        buttonPanel.add(cancelButton);

        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateComponentSizes();
            }
        });

        this.setContentPane(mainPanel);
    }

    private void processCancelTrip() {
        int selectedRow = currentTripsTable.getSelectedRow();
        
        // Validate chọn bảng
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một chuyến đi từ danh sách để hủy.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate lý do
        String reason = reasonField.getText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải nhập lý do hủy chuyến!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            reasonField.requestFocus();
            return;
        }

        String maChuyen = (String) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn hủy chuyến đi: " + maChuyen + "?\nHành động này không thể hoàn tác.", 
            "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Đã sửa truyền reasonField.getText() vào API
                business.HuyChuyenDi(maChuyen, reason); 
                
                model.removeRow(selectedRow); 
                reasonField.setText("");
                routeLabel.setText(" Vui lòng chọn chuyến đi từ bảng phía trên");
                driverNameLabel.setText("");
                
                JOptionPane.showMessageDialog(this, "Đã hủy chuyến " + maChuyen + " thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi hủy: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private JPanel createInfoRow(String text, Color bgColor, boolean isBold) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(bgColor);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", isBold ? Font.BOLD : Font.PLAIN, 14));
        panel.add(label);
        return panel;
    }

    private void updateComponentSizes() {
        Dimension size = getSize();
        int tableHeight = Math.max(150, (int) (size.height * 0.25));
        scrollPane.setPreferredSize(new Dimension(size.width - 80, tableHeight));
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void loadTripData(List<DatXe> chuyenDiList) {
        try {
            model.setRowCount(0); // Xóa sạch dữ liệu cũ
            if (chuyenDiList == null || chuyenDiList.isEmpty()) return;

            for (DatXe chuyenDi : chuyenDiList) {
                if (!BookingStatus.CANCELLED.equals(chuyenDi.getBookingStatus())) {
                    model.addRow(new Object[] { 
                        chuyenDi.getBookingId(), 
                        chuyenDi.getDriverId(), 
                        chuyenDi.getPickupLocation(),
                        chuyenDi.getDropoffLocation(), 
                        chuyenDi.getTotalPrice() // Cập nhật tạm field loại xe
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}