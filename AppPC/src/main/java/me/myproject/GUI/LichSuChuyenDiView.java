package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.toedter.calendar.JDateChooser;

import me.myproject.BUSINESSLOGIC.ChuyenDiBSL;
import me.myproject.MODEL.DatXe;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.Enum.BookingStatus;
import me.myproject.Utilities.Enum.RoleName;

public class LichSuChuyenDiView extends FrameMain implements ActionListener {
    private final ChuyenDiBSL chuyenDiBSL;
    private JComboBox<String> cboLoaiXe;
    private JDateChooser dateFrom, dateTo;
    private JButton btnLoc, btnXuatFile, btnQuayLai, btnGuiDanhGia;
    private JTable tblLichSu;
    private DefaultTableModel modelTable;
    private JPanel pnlChiTiet;
    private JLabel lblNgayChiTiet, lblLoaiXeChiTiet, lblDiemDonChiTiet, lblDiemDenChiTiet;
    private JLabel lblSoKmChiTiet, lblKhuyenMaiChiTiet;
    private JPanel pnlDanhGia;
    private JButton[] btnSoSao;
    private JTextArea txtFeedback; // Ô nhập nội dung đánh giá
    private TaiKhoan tk;

    // Biến lưu trạng thái để gửi API
    private String currentSelectedMaChuyen = null;
    private int currentSelectedRating = 0;
    private boolean isDriver;

    public LichSuChuyenDiView(TaiKhoan taiKhoan) throws Exception {
        super("Lịch sử di chuyển");
        chuyenDiBSL = new ChuyenDiBSL();
        tk = taiKhoan;
        
        // Xác định Role ngay từ đầu để dùng cho việc render UI
        isDriver = tk.getRole() != null && RoleName.DRIVER.getRoleName().equalsIgnoreCase(tk.getRole().getRoleName());
        
        init();
        System.out.println("Thông tin tài khoản: " + tk.getRole().getRoleName());
        
        if (isDriver) {
            loadTripData(chuyenDiBSL.getLichSuChuyenDiTheoTX(tk.getID_Ref()));
        } else {
            loadTripData(chuyenDiBSL.getLichSuChuyenDiTheoKH(tk.getID_Ref()));
        }
    }

    private void init() throws Exception {
        this.setLayout(new BorderLayout());
        JPanel headerPanel = createHeaderPanel();
        this.add(headerPanel, BorderLayout.NORTH);
        
        // Panel lọc
        JPanel filterPanel = createFilterPanel();
        this.add(filterPanel, BorderLayout.CENTER);

        this.setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0, 178, 192));
        panel.setPreferredSize(new Dimension(getWidth(), 60));
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));

        URL backUrl = getClass().getResource("/me/myproject/IMAGE/back.png");
        ImageIcon backIcon = backUrl != null ? new ImageIcon(backUrl) : null;
        if (backUrl == null) {
            System.err.println("Không tìm thấy icon back: /me/myproject/IMAGE/back.png");
        }
        btnQuayLai = new JButton(backIcon);
        btnQuayLai.setFocusPainted(false);
        btnQuayLai.setPreferredSize(new Dimension(30, 30));
        btnQuayLai.setBackground(new Color(0, 178, 192));
        btnQuayLai.setOpaque(true);
        btnQuayLai.setBorderPainted(false);
        btnQuayLai.setFocusPainted(false);
        btnQuayLai.addActionListener(this);
        panel.add(btnQuayLai);

        JLabel lblTitle = new JLabel("LỊCH SỬ DI CHUYỂN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel tìm kiếm với các điều khiển lọc
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Dropdown loại xe
        JPanel pnlLoaiXe = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlLoaiXe.add(new JLabel("Trạng thái:"));
        cboLoaiXe = new JComboBox<>();
        cboLoaiXe.setPreferredSize(new Dimension(150, 25));
        cboLoaiXe.setBackground(Color.WHITE);
        cboLoaiXe.addItem("Hoàn thành");
        cboLoaiXe.addItem("Đã huỷ");
        cboLoaiXe.addItem("Chờ tài xế nhận");
        cboLoaiXe.addItem("Đang thực hiện");
        pnlLoaiXe.add(cboLoaiXe);
        searchPanel.add(pnlLoaiXe);

        // Chọn khoảng thời gian
        JPanel pnlDate = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlDate.add(new JLabel("Từ ngày:"));
        dateFrom = new JDateChooser();
        dateFrom.setPreferredSize(new Dimension(150, 25));
        dateFrom.setDate(new Date());
        pnlDate.add(dateFrom);

        pnlDate.add(new JLabel("Đến ngày:"));
        dateTo = new JDateChooser();
        dateTo.setPreferredSize(new Dimension(150, 25));
        dateTo.setDate(new Date());
        pnlDate.add(dateTo);
        searchPanel.add(pnlDate);

        // Các nút Lọc và Xuất file
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnLoc = new JButton("Lọc");
        btnLoc.setBackground(new Color(0, 178, 192));
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setOpaque(true);
        btnLoc.setBorderPainted(false);
        btnLoc.setFocusPainted(false);
        btnLoc.addActionListener(this);
        pnlButtons.add(btnLoc);

        btnXuatFile = new JButton("Xuất file");
        btnXuatFile.setBackground(new Color(0, 178, 192));
        btnXuatFile.setForeground(Color.WHITE);
        btnXuatFile.setOpaque(true);
        btnXuatFile.setBorderPainted(false);
        btnXuatFile.setFocusPainted(false);
        btnXuatFile.addActionListener(this);
        pnlButtons.add(btnXuatFile);

        searchPanel.add(pnlButtons);

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout(10, 0));

        // Bảng lịch sử di chuyển
        String[] columnNames = { "Mã chuyến", "Tài xế", "Ngày", "Giá tiền", "Trạng thái" };
        modelTable = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblLichSu = new JTable(modelTable);

        // Căn giữa dữ liệu trong bảng
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tblLichSu.getColumnCount(); i++)
            tblLichSu.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JTableHeader header = tblLichSu.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(new Color(240, 240, 240));
        header.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Thêm sự kiện click vào bảng
        tblLichSu.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tblLichSu.getSelectedRow();
            if (selectedRow >= 0 && !e.getValueIsAdjusting()) {
                String maChuyen = (String) modelTable.getValueAt(selectedRow, 0);
                showTripDetail(maChuyen);
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblLichSu);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel chi tiết bên phải
        pnlChiTiet = createDetailPanel();
        contentPanel.add(pnlChiTiet, BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "CHI TIẾT CHUYẾN ĐI"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 400)); // Nới rộng panel một chút để chứa Textarea đẹp hơn

        // Ngày
        JPanel pnlNgay = new JPanel(new BorderLayout());
        pnlNgay.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        pnlNgay.add(new JLabel("Ngày:"), BorderLayout.NORTH);
        lblNgayChiTiet = new JLabel();
        pnlNgay.add(lblNgayChiTiet, BorderLayout.CENTER);
        panel.add(pnlNgay);

        // Trạng thái
        JPanel pnlLoaiXe = new JPanel(new BorderLayout());
        pnlLoaiXe.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        pnlLoaiXe.add(new JLabel("Trạng thái:"), BorderLayout.NORTH);
        lblLoaiXeChiTiet = new JLabel();
        pnlLoaiXe.add(lblLoaiXeChiTiet, BorderLayout.CENTER);
        panel.add(pnlLoaiXe);

        // Điểm đón
        JPanel pnlDiemDon = new JPanel(new BorderLayout());
        pnlDiemDon.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        pnlDiemDon.add(new JLabel("Điểm đón:"), BorderLayout.NORTH);
        lblDiemDonChiTiet = new JLabel();
        pnlDiemDon.add(lblDiemDonChiTiet, BorderLayout.CENTER);
        panel.add(pnlDiemDon);

        // Điểm đến
        JPanel pnlDiemDen = new JPanel(new BorderLayout());
        pnlDiemDen.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        pnlDiemDen.add(new JLabel("Điểm đến:"), BorderLayout.NORTH);
        lblDiemDenChiTiet = new JLabel();
        pnlDiemDen.add(lblDiemDenChiTiet, BorderLayout.CENTER);
        panel.add(pnlDiemDen);

        // Số km
        JPanel pnlSoKm = new JPanel(new BorderLayout());
        pnlSoKm.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        pnlSoKm.add(new JLabel("Số km:"), BorderLayout.NORTH);
        lblSoKmChiTiet = new JLabel();
        pnlSoKm.add(lblSoKmChiTiet, BorderLayout.CENTER);
        panel.add(pnlSoKm);

        // Khuyến mãi
        JPanel pnlKhuyenMai = new JPanel(new BorderLayout());
        pnlKhuyenMai.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        pnlKhuyenMai.add(new JLabel("Khuyến mãi:"), BorderLayout.NORTH);
        lblKhuyenMaiChiTiet = new JLabel();
        pnlKhuyenMai.add(lblKhuyenMaiChiTiet, BorderLayout.CENTER);
        panel.add(pnlKhuyenMai);

        // --- Khu vực Đánh giá ---
        JPanel pnlDanhGiaContainer = new JPanel(new BorderLayout());
        pnlDanhGiaContainer.add(new JLabel("Đánh giá:"), BorderLayout.NORTH);

        pnlDanhGia = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnSoSao = new JButton[5];
        for (int i = 0; i < 5; i++) {
            btnSoSao[i] = new JButton();
            btnSoSao[i].setFont(new Font("Arial", Font.PLAIN, 12));
            btnSoSao[i].setPreferredSize(new Dimension(45, 40));
            btnSoSao[i].setBackground(Color.WHITE);
            btnSoSao[i].setText("★ " + (i + 1));
            
            // Nếu là Tài xế thì không cho tương tác
            if (isDriver) {
                btnSoSao[i].setEnabled(false);
            } else {
                btnSoSao[i].addActionListener(this);
            }
            pnlDanhGia.add(btnSoSao[i]);
        }
        pnlDanhGiaContainer.add(pnlDanhGia, BorderLayout.CENTER);
        
        // Thêm Form Gửi Đánh Giá (bao gồm Nút và Textarea) nếu là Khách Hàng
        if (!isDriver) {
            JPanel pnlKhachHangInteraction = new JPanel();
            pnlKhachHangInteraction.setLayout(new BoxLayout(pnlKhachHangInteraction, BoxLayout.Y_AXIS));

            // Textarea cho nội dung feedback
            JPanel pnlFeedback = new JPanel(new BorderLayout());
            pnlFeedback.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            pnlFeedback.add(new JLabel("Nội dung đánh giá:"), BorderLayout.NORTH);
            
            txtFeedback = new JTextArea(3, 20); // 3 dòng
            txtFeedback.setLineWrap(true);
            txtFeedback.setWrapStyleWord(true);
            txtFeedback.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            txtFeedback.setEnabled(false); // Mặc định disable
            JScrollPane scrollFeedback = new JScrollPane(txtFeedback);
            pnlFeedback.add(scrollFeedback, BorderLayout.CENTER);
            
            pnlKhachHangInteraction.add(pnlFeedback);

            // Nút gửi đánh giá
            JPanel pnlNutGui = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnGuiDanhGia = new JButton("Gửi đánh giá");
            btnGuiDanhGia.setBackground(new Color(255, 152, 0)); // Màu cam nổi bật
            btnGuiDanhGia.setForeground(Color.WHITE);
            btnGuiDanhGia.setFocusPainted(false);
            btnGuiDanhGia.setOpaque(true);
            btnGuiDanhGia.setBorderPainted(false);
            btnGuiDanhGia.addActionListener(this);
            btnGuiDanhGia.setEnabled(false); // Mặc định disable 
            
            pnlNutGui.add(btnGuiDanhGia);
            pnlKhachHangInteraction.add(pnlNutGui);

            pnlDanhGiaContainer.add(pnlKhachHangInteraction, BorderLayout.SOUTH);
        }

        panel.add(pnlDanhGiaContainer);

        return panel;
    }

    private void loadTripData(List<DatXe> chuyenDiList) {
        try {
            modelTable.setRowCount(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            if (chuyenDiList != null) {
                for (DatXe chuyenDi : chuyenDiList) {
                    String maChuyen = chuyenDi.getBookingId();
                    String tenTaiXe = chuyenDi.getDriverId();
                    java.sql.Timestamp ngayDat = (java.sql.Timestamp) chuyenDi.getBookingTime();
                    Double giaTien = (Double) chuyenDi.getTotalPrice();
                    BookingStatus trangThai = (BookingStatus) chuyenDi.getBookingStatus();
                    modelTable.addRow(new Object[] { maChuyen, tenTaiXe, dateFormat.format(ngayDat), String.format("%,.0f VND", giaTien), trangThai.toString() });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử chuyến đi: " + e.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTripDetail(String maChuyen) {
        currentSelectedMaChuyen = maChuyen; // Lưu lại mã chuyến đang xem
        List<DatXe> chuyenDiList = null;
        try {
            chuyenDiList = chuyenDiBSL.getChuyenDi();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lấy dữ liệu chuyến đi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        DatXe detail = new DatXe();
        if (chuyenDiList != null) {
            for (DatXe item : chuyenDiList) {
                if(item.getBookingId().equals(maChuyen)){
                    detail = item;
                    break;
                }
            }
        }
         
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        lblNgayChiTiet.setText(detail.getBookingTime() != null ? sdf.format((Date) detail.getBookingTime()) : "");
        lblLoaiXeChiTiet.setText(detail.getBookingStatus() != null ? detail.getBookingStatus().toString() : "");
        lblDiemDonChiTiet.setText(detail.getPickupLocation() != null ? detail.getPickupLocation().toString() : "");
        lblDiemDenChiTiet.setText(detail.getDropoffLocation() != null ? detail.getDropoffLocation().toString() : "");
        Double khoangCach = detail.getDistance();
        lblSoKmChiTiet.setText(khoangCach != null ? khoangCach.toString() + " km" : "");
        lblKhuyenMaiChiTiet.setText(detail.getPromotionCode() != null ? detail.getPromotionCode().toString() : "Không có");
        
        int diemSo = detail.getRating() != null ? detail.getRating() : 0;
        setRating(diemSo); 
        
        // Quản lý trạng thái UI Gửi đánh giá cho Khách hàng
        if (!isDriver) {
            // Xóa rỗng nội dung mỗi khi chọn chuyến mới
            txtFeedback.setText("");
            
            // Nếu model DatXe có hàm getFeedback() thì có thể gán vào đây:
            // txtFeedback.setText(detail.getFeedback() != null ? detail.getFeedback() : "");

            // Nếu đã có điểm đánh giá hoặc chuyến đi chưa hoàn thành thì khóa nút và textbox
            if (diemSo > 0) {
                btnGuiDanhGia.setEnabled(false);
                btnGuiDanhGia.setText("Đã đánh giá");
                txtFeedback.setEnabled(false); // Khóa textarea
                for (JButton btn : btnSoSao) btn.setEnabled(false); // Khóa sao
            } else if (detail.getBookingStatus() != BookingStatus.COMPLETED) {
                btnGuiDanhGia.setEnabled(false);
                btnGuiDanhGia.setText("Gửi đánh giá");
                txtFeedback.setEnabled(false); // Chưa hoàn thành thì không cho nhập
                for (JButton btn : btnSoSao) btn.setEnabled(false);
            } else {
                btnGuiDanhGia.setEnabled(true);
                btnGuiDanhGia.setText("Gửi đánh giá");
                txtFeedback.setEnabled(true); // Mở khóa cho nhập nội dung
                for (JButton btn : btnSoSao) btn.setEnabled(true);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == btnLoc) {
            String trangThai = (String) cboLoaiXe.getSelectedItem();
            Date tuNgay = dateFrom.getDate();
            Date denNgay = dateTo.getDate();
            try {
                if (trangThai == null || trangThai.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn trạng thái.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (tuNgay == null || denNgay == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (tuNgay.after(denNgay)) {
                    JOptionPane.showMessageDialog(this, "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            
                loadTripData(chuyenDiBSL.locChuyenDi(trangThai, new Timestamp(tuNgay.getTime()), new Timestamp(denNgay.getTime())));
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(this, "Lỗi khi lọc: " + e1.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if (source == btnXuatFile) {
            System.out.println("Xuất dữ liệu ra file... (Chưa triển khai logic thực tế)");
        } else if (source == btnQuayLai) {
            if (isDriver) {
                new TrangChuDriverView(tk);
            } else {
                new TrangChuUserView(tk);
            }
            this.dispose();
        } else if (source == btnGuiDanhGia) {
            // -- LOGIC GỌI API ĐÁNH GIÁ --
            if (currentSelectedMaChuyen == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn chuyến đi cần đánh giá!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (currentSelectedRating <= 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn số sao để đánh giá!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Lấy nội dung từ TextArea
            String feedbackContent = txtFeedback != null ? txtFeedback.getText().trim() : "";
            
            try {
                // GỌI API THÔNG QUA LỚP BSL kèm Nội dung Feedback
                boolean success = chuyenDiBSL.danhGiaChuyenDi(currentSelectedMaChuyen, currentSelectedRating, feedbackContent);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, "Cảm ơn bạn đã gửi đánh giá!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    btnGuiDanhGia.setEnabled(false);
                    btnGuiDanhGia.setText("Đã đánh giá");
                    txtFeedback.setEnabled(false); // Khóa textarea sau khi gửi
                    for (JButton btn : btnSoSao) btn.setEnabled(false); // Khóa sao
                } else {
                    JOptionPane.showMessageDialog(this, "Gửi đánh giá thất bại, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } else {
            // Xử lý sự kiện click vào nút chọn Sao
            for (int i = 0; i < btnSoSao.length; i++) {
                if (source == btnSoSao[i]) {
                    setRating(i + 1);
                    break;
                }
            }
        }
    }

    private void setRating(int rating) {
        currentSelectedRating = rating; // Lưu lại số sao đang chọn
        for (int i = 0; i < btnSoSao.length; i++) {
            if (i < rating) {
                btnSoSao[i].setBackground(new Color(255, 193, 7));  // Màu vàng nổi bật
                btnSoSao[i].setForeground(Color.BLACK);
                btnSoSao[i].setOpaque(true);  
            } else {
                btnSoSao[i].setBackground(Color.WHITE);  
                btnSoSao[i].setForeground(Color.GRAY);
                btnSoSao[i].setOpaque(true);  
            }
        }
    }
}