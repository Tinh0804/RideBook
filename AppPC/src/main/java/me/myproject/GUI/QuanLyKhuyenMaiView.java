package me.myproject.GUI;

import me.myproject.BUSINESSLOGIC.KhuyenMaiBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.DIMENSION.FrameMain;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * QuanLyKhuyenMaiView – Màn hình quản lý Khuyến Mãi dành cho Admin.
 * Chức năng: Xem danh sách, Tạo mới, Sửa, Bật/Tắt, Xóa.
 */
public class QuanLyKhuyenMaiView extends FrameMain {

    // ─── Palette ──────────────────────────────────────────────────────
    private static final Color TEAL       = new Color(0, 172, 172);
    private static final Color TEAL_DARK  = new Color(0, 120, 120);
    private static final Color BG         = new Color(245, 248, 252);
    private static final Color WHITE      = Color.WHITE;
    private static final Color GREEN      = new Color(34, 197, 94);
    private static final Color RED        = new Color(239, 68, 68);
    private static final Color AMBER      = new Color(245, 158, 11);
    private static final Color BLUE       = new Color(59, 130, 246);
    private static final Color TEXT_DIM   = new Color(100, 110, 125);

    // ─── State ────────────────────────────────────────────────────────
    private final TaiKhoan taiKhoan;
    private final KhuyenMaiBSL bsl = new KhuyenMaiBSL();

    // ─── Table ────────────────────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;
    private final String[] COL_NAMES = { "#", "Mã", "Tên", "Giảm (đ)", "SL", "Hết hạn", "Trạng thái" };
    // Lưu ID của từng hàng để thao tác chính xác
    private final List<String> rowIds = new ArrayList<>();

    // ─── Buttons ──────────────────────────────────────────────────────
    private JButton btnReload, btnCreate, btnEdit, btnToggle, btnDelete;

    // ─────────────────────────────────────────────────────────────────
    public QuanLyKhuyenMaiView(TaiKhoan taiKhoan) {
        super("Quản Lý Khuyến Mãi");
        this.taiKhoan = taiKhoan;
        buildUI();
        loadData();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ══════════════════════════════════════════════════════════════════

    private void buildUI() {
        Dimension dim = getSize();
        int W = dim.width, H = dim.height;

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(dim);
        setContentPane(layered);

        // Header
        JPanel header = buildHeader(W);
        header.setBounds(0, 0, W, 65);
        layered.add(header, JLayeredPane.PALETTE_LAYER);

        // Body
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setBackground(BG);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        body.setBounds(0, 65, W, H - 65);
        layered.add(body, JLayeredPane.PALETTE_LAYER);

        // Toolbar
        body.add(buildToolbar(), BorderLayout.NORTH);

        // Table
        body.add(buildTable(), BorderLayout.CENTER);
    }

    private JPanel buildHeader(int W) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(TEAL);
        h.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel title = new JLabel("🎟  Quản Lý Khuyến Mãi", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setForeground(WHITE);
        h.add(title, BorderLayout.CENTER);

        return h;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        btnReload = mkBtn("⟳ Làm mới",  TEAL);
        btnCreate = mkBtn("＋ Tạo mới",  BLUE);
        btnEdit   = mkBtn("✎ Chỉnh sửa", AMBER);
        btnToggle = mkBtn("⏯ Bật/Tắt",  new Color(139, 92, 246));
        btnDelete = mkBtn("✕ Xóa",       RED);

        btnReload.addActionListener(e -> loadData());
        btnCreate.addActionListener(e -> showFormDialog(null));
        btnEdit  .addActionListener(e -> editSelected());
        btnToggle.addActionListener(e -> toggleSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        bar.add(btnReload); bar.add(btnCreate); bar.add(btnEdit);
        bar.add(btnToggle); bar.add(btnDelete);
        return bar;
    }

    private JScrollPane buildTable() {
        tableModel = new DefaultTableModel(COL_NAMES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 234, 240));
        table.setShowGrid(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 245, 252));
        table.getTableHeader().setReorderingAllowed(false);

        // Center renderer for most columns
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < COL_NAMES.length; i++) {
            if (i != 2) table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        // Status column color renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        // Column widths
        int[] widths = {35, 120, 200, 110, 60, 110, 95};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(new Color(210, 218, 230)));
        sp.getViewport().setBackground(WHITE);
        return sp;
    }

    // ══════════════════════════════════════════════════════════════════
    //  DATA
    // ══════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void loadData() {
        tableModel.setRowCount(0);
        rowIds.clear();

        new Thread(() -> {
            try {
                Map<String, Object> resp = bsl.layTatCaKhuyenMai();
                Number status = (Number) resp.get("status");
                List<Map<String, Object>> list = (List<Map<String, Object>>) resp.get("result");

                SwingUtilities.invokeLater(() -> {
                    if (status == null || status.intValue() != 200 || list == null) {
                        JOptionPane.showMessageDialog(this,
                            "Không tải được dữ liệu. Kiểm tra kết nối!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    int idx = 1;
                    for (Map<String, Object> item : list) {
                        String id      = str(item, "promotionId");
                        String code    = str(item, "promotionCode");
                        String name    = str(item, "promotionName");
                        double limit   = numDouble(item, "discountLimit");
                        int    qty     = numInt(item, "quantity");
                        String expire  = formatTs(item.get("endTime"), sdf);
                        boolean active = Boolean.TRUE.equals(item.get("isActive"));

                        rowIds.add(id);
                        tableModel.addRow(new Object[]{
                            idx++, code, name,
                            String.format("%,.0f", limit).replace(",", "."),
                            qty, expire,
                            active ? "✅ Hoạt động" : "⛔ Tắt"
                        });
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════════════════════

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { toast("Vui lòng chọn một khuyến mãi!"); return; }
        String id   = rowIds.get(row);
        String code = (String) tableModel.getValueAt(row, 1);
        String name = (String) tableModel.getValueAt(row, 2);
        showFormDialog(Map.of("promotionId", id, "promotionCode", code, "promotionName", name));
    }

    private void toggleSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { toast("Vui lòng chọn một khuyến mãi!"); return; }
        String id = rowIds.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bật/Tắt khuyến mãi đã chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            try {
                Map<String, Object> resp = bsl.toggleKhuyenMai(id);
                SwingUtilities.invokeLater(() -> {
                    Number st = (Number) resp.get("status");
                    if (st != null && st.intValue() == 200) {
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Thao tác thất bại: " + resp.getOrDefault("message", "Lỗi"), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()));
            }
        }).start();
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { toast("Vui lòng chọn một khuyến mãi!"); return; }
        String id   = rowIds.get(row);
        String code = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Xóa vĩnh viễn khuyến mãi [" + code + "]?\nHành động này không thể hoàn tác!",
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            try {
                Map<String, Object> resp = bsl.xoaKhuyenMai(id);
                SwingUtilities.invokeLater(() -> {
                    Number st = (Number) resp.get("status");
                    if (st == null || st.intValue() == 200) { // 200 hoặc empty = OK
                        JOptionPane.showMessageDialog(this, "Đã xóa khuyến mãi " + code);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Xóa thất bại: " + resp.getOrDefault("message", "Lỗi"), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()));
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  FORM DIALOG (Tạo mới / Sửa)
    // ══════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void showFormDialog(Map<String, Object> existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(this, isEdit ? "Chỉnh Sửa Khuyến Mãi" : "Tạo Khuyến Mãi Mới", true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // ── Form panel ────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        // Fields
        JTextField tfCode     = formField(isEdit ? str(existing, "promotionCode") : "");
        JTextField tfName     = formField(isEdit ? str(existing, "promotionName") : "");
        JTextField tfDiscount = formField("0");
        JTextField tfQty      = formField("100");
        JTextField tfCond     = formField("");

        // Date spinners – default today → 30 days later
        Calendar now   = Calendar.getInstance();
        Calendar later = Calendar.getInstance(); later.add(Calendar.DAY_OF_YEAR, 30);
        JSpinner spStart = dateSpin(now.getTime());
        JSpinner spEnd   = dateSpin(later.getTime());

        // Header in dialog
        JLabel hdr = new JLabel(isEdit ? "✎ Chỉnh sửa khuyến mãi" : "＋ Tạo khuyến mãi mới");
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hdr.setForeground(TEAL_DARK);

        int r = 0;
        addRow(form, gbc, r++, "Tiêu đề:", hdr);
        addRow(form, gbc, r++, "Mã khuyến mãi *:", tfCode);
        addRow(form, gbc, r++, "Tên hiển thị *:", tfName);
        addRow(form, gbc, r++, "Giảm giá (đ) *:", tfDiscount);
        addRow(form, gbc, r++, "Số lượng *:", tfQty);
        addRow(form, gbc, r++, "Điều kiện:", tfCond);
        addRow(form, gbc, r++, "Ngày bắt đầu:", spStart);
        addRow(form, gbc, r++, "Ngày hết hạn:", spEnd);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);

        // ── Button bar ────────────────────────────────────────────────
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnBar.setBackground(new Color(248, 250, 252));
        btnBar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 228, 240)));

        JButton btnSave   = mkBtn(isEdit ? "💾 Cập nhật" : "✅ Tạo mới", TEAL);
        JButton btnCancel = mkBtn("Hủy", new Color(160, 170, 185));
        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(ev -> {
            // Validate
            String code = tfCode.getText().trim();
            String name = tfName.getText().trim();
            if (code.isBlank() || name.isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ các trường bắt buộc (*)", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double discount;
            int qty;
            try { discount = Double.parseDouble(tfDiscount.getText().trim().replace(".", "").replace(",", "")); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dialog, "Giảm giá phải là số!"); return; }
            try { qty = Integer.parseInt(tfQty.getText().trim()); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dialog, "Số lượng phải là số nguyên!"); return; }

            Date startDate = ((SpinnerDateModel) spStart.getModel()).getDate();
            Date endDate   = ((SpinnerDateModel) spEnd.getModel()).getDate();
            if (!endDate.after(startDate)) {
                JOptionPane.showMessageDialog(dialog, "Ngày hết hạn phải sau ngày bắt đầu!"); return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("promotionCode",        code);
            payload.put("promotionName",        name);
            payload.put("discountLimit",        discount);
            payload.put("quantity",             qty);
            payload.put("applicationCondition", tfCond.getText().trim());
            payload.put("startTime",            startDate.getTime());
            payload.put("endTime",              endDate.getTime());
            payload.put("isActive",             true);

            btnSave.setEnabled(false);
            new Thread(() -> {
                try {
                    Map<String, Object> resp;
                    if (isEdit) {
                        String pid = str(existing, "promotionId");
                        resp = bsl.capNhatKhuyenMai(pid, payload);
                    } else {
                        resp = bsl.taoKhuyenMai(payload);
                    }
                    Number st = (Number) resp.get("status");
                    SwingUtilities.invokeLater(() -> {
                        btnSave.setEnabled(true);
                        if (st != null && (st.intValue() == 200 || st.intValue() == 201)) {
                            JOptionPane.showMessageDialog(dialog, isEdit ? "Cập nhật thành công!" : "Tạo khuyến mãi thành công!");
                            dialog.dispose();
                            loadData();
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                "Thất bại: " + resp.getOrDefault("message", "Lỗi không xác định"), "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> { btnSave.setEnabled(true); JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage()); });
                }
            }).start();
        });

        btnBar.add(btnCancel);
        btnBar.add(btnSave);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════

    private JButton mkBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private JTextField formField(String val) {
        JTextField tf = new JTextField(val);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(210, 218, 230), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tf.setPreferredSize(new Dimension(250, 32));
        return tf;
    }

    private JSpinner dateSpin(Date date) {
        SpinnerDateModel model = new SpinnerDateModel(date, null, null, Calendar.DAY_OF_YEAR);
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "dd/MM/yyyy"));
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sp.setPreferredSize(new Dimension(140, 32));
        return sp;
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.weightx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(TEXT_DIM);
        form.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void toast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v != null ? v.toString() : "";
    }
    private double numDouble(Map<String, Object> m, String k) {
        Object v = m.get(k); return v instanceof Number ? ((Number) v).doubleValue() : 0;
    }
    private int numInt(Map<String, Object> m, String k) {
        Object v = m.get(k); return v instanceof Number ? ((Number) v).intValue() : 0;
    }
    private String formatTs(Object raw, SimpleDateFormat sdf) {
        if (raw == null) return "N/A";
        try {
            long ms;
            if (raw instanceof Number) ms = ((Number) raw).longValue();
            else if (raw instanceof Map) {
                Object t = ((Map<?,?>) raw).get("time");
                ms = t instanceof Number ? ((Number) t).longValue() : 0;
            } else return raw.toString();
            return sdf.format(new Date(ms));
        } catch (Exception e) { return "N/A"; }
    }

    // ── Status cell renderer ──────────────────────────────────────────
    static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, val, sel, foc, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String s = val == null ? "" : val.toString();
            lbl.setForeground(s.contains("✅") ? new Color(21, 128, 61) : new Color(185, 28, 28));
            lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
            return lbl;
        }
    }
}
