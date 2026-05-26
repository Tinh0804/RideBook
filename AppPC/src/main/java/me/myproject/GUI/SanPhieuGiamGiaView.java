package me.myproject.GUI;

import me.myproject.BUSINESSLOGIC.DatXeBSL;
import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.DIMENSION.FrameMain;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Màn hình "Săn Phiếu Giảm Giá" – hiển thị toàn bộ mã khuyến mãi đang active
 * dưới dạng thẻ voucher đẹp mắt. Khách hàng có thể sao chép mã bằng 1 click.
 */
public class SanPhieuGiamGiaView extends FrameMain {

    // ──────────────────────────── Palette ────────────────────────────
    private static final Color TEAL        = new Color(0, 172, 172);
    private static final Color TEAL_DARK   = new Color(0, 130, 130);
    private static final Color TEAL_LIGHT  = new Color(220, 249, 249);
    private static final Color BG          = new Color(245, 248, 252);
    private static final Color SIDEBAR     = new Color(25, 37, 60);
    private static final Color WHITE       = Color.WHITE;
    private static final Color TEXT_TITLE  = new Color(30, 40, 60);
    private static final Color TEXT_MUTED  = new Color(120, 130, 145);
    private static final Color TAG_GREEN   = new Color(34, 197, 94);
    private static final Color TAG_RED     = new Color(239, 68, 68);
    private static final Color GOLD        = new Color(245, 158, 11);

    // ──────────────────────────── State ──────────────────────────────
    private final TaiKhoan taiKhoan;
    private final DatXeBSL datXeBSL = new DatXeBSL();

    private JPanel cardArea;
    private JLabel lblStatus;

    // ─────────────────────────────────────────────────────────────────
    public SanPhieuGiamGiaView(TaiKhoan taiKhoan) {
        super("Săn Phiếu Giảm Giá");
        this.taiKhoan = taiKhoan;
        buildUI();
        loadPromotions();
        setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════
    //  UI BUILDER
    // ══════════════════════════════════════════════════════════════════

    private void buildUI() {
        Dimension dim = getSize();
        int W = dim.width, H = dim.height;

        JLayeredPane layered = new JLayeredPane();
        layered.setPreferredSize(dim);
        setContentPane(layered);

        // ── Header ──────────────────────────────────────────────────
        JPanel header = buildHeader(W);
        header.setBounds(0, 0, W, 70);
        layered.add(header, JLayeredPane.PALETTE_LAYER);

        // ── Body ────────────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG);
        body.setBounds(0, 70, W, H - 70);
        layered.add(body, JLayeredPane.PALETTE_LAYER);

        // ── Search bar + title ──────────────────────────────────────
        JPanel topBar = buildTopBar();
        body.add(topBar, BorderLayout.NORTH);

        // ── Card scroll area ────────────────────────────────────────
        cardArea = new JPanel();
        cardArea.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
        cardArea.setBackground(BG);
        cardArea.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(cardArea);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        body.add(scroll, BorderLayout.CENTER);

        // ── Status label (loading / empty) ──────────────────────────
        lblStatus = new JLabel("Đang tải dữ liệu...", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblStatus.setForeground(TEXT_MUTED);
        cardArea.add(lblStatus);
    }

    private JPanel buildHeader(int width) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(TEAL);
        header.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        // Back button
        JButton btnBack = new JButton("← Quay lại");
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(WHITE); btnBack.setBackground(TEAL);
        btnBack.setBorderPainted(false); btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new TrangChuUserView(taiKhoan);
            dispose();
        });
        header.add(btnBack, BorderLayout.WEST);

        // Title
        JLabel title = new JLabel("🎫  Săn Phiếu Giảm Giá", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(WHITE);
        header.add(title, BorderLayout.CENTER);

        // Refresh button
        JButton btnRefresh = new JButton("⟳ Làm mới");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnRefresh.setForeground(WHITE); btnRefresh.setBackground(TEAL);
        btnRefresh.setBorderPainted(false); btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadPromotions());
        header.add(btnRefresh, BorderLayout.EAST);

        return header;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(BG);
        bar.setBorder(BorderFactory.createEmptyBorder(18, 24, 4, 24));

        // Big title
        JLabel lbl = new JLabel("Tất Cả Ưu Đãi Dành Cho Bạn 🎉");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(TEXT_TITLE);
        bar.add(lbl, BorderLayout.WEST);

        // Hint
        JLabel hint = new JLabel("Click vào mã để sao chép  📋");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        hint.setForeground(TEXT_MUTED);
        bar.add(hint, BorderLayout.EAST);

        return bar;
    }

    // ══════════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ══════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void loadPromotions() {
        lblStatus.setVisible(true);
        lblStatus.setText("Đang tải dữ liệu...");
        // Remove all previous cards (keep status label)
        for (Component c : cardArea.getComponents()) {
            if (c != lblStatus) cardArea.remove(c);
        }
        cardArea.revalidate();
        cardArea.repaint();

        new Thread(() -> {
            try {
                Map<String, Object> resp = datXeBSL.getActivePromotions();
                Number status = (Number) resp.get("status");

                SwingUtilities.invokeLater(() -> {
                    if (status == null || status.intValue() != 200) {
                        lblStatus.setText("Không thể tải dữ liệu. Vui lòng thử lại.");
                        return;
                    }

                    List<Map<String, Object>> items =
                            (List<Map<String, Object>>) resp.get("result");

                    if (items == null || items.isEmpty()) {
                        lblStatus.setText("Hiện chưa có phiếu giảm giá nào. Hãy quay lại sau! 😊");
                        return;
                    }

                    lblStatus.setVisible(false);
                    for (Map<String, Object> item : items) {
                        cardArea.add(buildVoucherCard(item));
                    }
                    cardArea.revalidate();
                    cardArea.repaint();
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    lblStatus.setText("Lỗi kết nối: " + ex.getMessage()));
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  VOUCHER CARD
    // ══════════════════════════════════════════════════════════════════

    private JPanel buildVoucherCard(Map<String, Object> data) {
        String id         = str(data, "promotionId");
        String code       = str(data, "promotionCode");
        String name       = str(data, "promotionName");
        double limit      = numDouble(data, "discountLimit");
        int    qty        = numInt(data, "quantity");
        String condition  = str(data, "applicationCondition");
        String endStr     = formatTimestamp(data.get("endTime"));
        boolean nearEnd   = isNearEnd(data.get("endTime"), 3); // <= 3 ngày là "sắp hết hạn"

        // ── Outer card ──────────────────────────────────────────────
        JPanel card = new JPanel(null); // absolute layout for ribbon effect
        card.setPreferredSize(new Dimension(340, 190));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 228, 240), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        // ── Left accent bar ─────────────────────────────────────────
        JPanel accentBar = new JPanel();
        accentBar.setBackground(TEAL);
        accentBar.setBounds(0, 0, 8, 190);
        card.add(accentBar);

        // ── Dashed separator (voucher style) ────────────────────────
        JLabel dashed = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(200, 210, 220));
                float[] dash = {4f, 4f};
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 1, dash, 0));
                g2.drawLine(0, 0, 0, getHeight());
            }
        };
        dashed.setBounds(245, 10, 2, 170);
        card.add(dashed);

        // ── Discount value (right section) ──────────────────────────
        JPanel rightSec = new JPanel();
        rightSec.setLayout(new BoxLayout(rightSec, BoxLayout.Y_AXIS));
        rightSec.setBackground(TEAL_LIGHT);
        rightSec.setBounds(247, 0, 93, 190);
        rightSec.setBorder(BorderFactory.createEmptyBorder(20, 6, 20, 6));

        JLabel lblGiam = new JLabel("GIẢM", SwingConstants.CENTER);
        lblGiam.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblGiam.setForeground(TEAL_DARK);
        lblGiam.setAlignmentX(0.5f);

        String limitStr = String.format("%,.0f đ", limit).replace(",", ".");
        JLabel lblAmount = new JLabel("<html><center>" + limitStr + "</center></html>", SwingConstants.CENTER);
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblAmount.setForeground(TEAL_DARK);
        lblAmount.setAlignmentX(0.5f);

        // Qty badge
        JLabel lblQty = new JLabel("Còn " + qty, SwingConstants.CENTER);
        lblQty.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblQty.setForeground(qty <= 5 ? TAG_RED : TAG_GREEN);
        lblQty.setAlignmentX(0.5f);

        rightSec.add(Box.createVerticalGlue());
        rightSec.add(lblGiam);
        rightSec.add(Box.createRigidArea(new Dimension(0, 4)));
        rightSec.add(lblAmount);
        rightSec.add(Box.createRigidArea(new Dimension(0, 8)));
        rightSec.add(lblQty);
        rightSec.add(Box.createVerticalGlue());
        card.add(rightSec);

        // ── Left section (main info) ─────────────────────────────────
        JPanel leftSec = new JPanel();
        leftSec.setLayout(new BoxLayout(leftSec, BoxLayout.Y_AXIS));
        leftSec.setBackground(WHITE);
        leftSec.setBounds(16, 12, 222, 168);

        // Name
        JLabel lblName = new JLabel("<html><b>" + name + "</b></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(TEXT_TITLE);
        leftSec.add(lblName);

        leftSec.add(Box.createRigidArea(new Dimension(0, 6)));

        // Condition
        if (condition != null && !condition.isBlank()) {
            JLabel lblCond = new JLabel("<html><font color='#788090'>Điều kiện: " + condition + "</font></html>");
            lblCond.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            leftSec.add(lblCond);
            leftSec.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        // Expire info
        String expLabel = nearEnd ? "⚠ Hết hạn: " : "Hạn dùng: ";
        Color expColor  = nearEnd ? GOLD : TEXT_MUTED;
        JLabel lblExp = new JLabel(expLabel + endStr);
        lblExp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExp.setForeground(expColor);
        leftSec.add(lblExp);

        leftSec.add(Box.createRigidArea(new Dimension(0, 10)));

        // Code chip (the clickable badge)
        JPanel codeChip = buildCodeChip(code);
        leftSec.add(codeChip);

        card.add(leftSec);

        // ── "Sắp hết hạn" ribbon ────────────────────────────────────
        if (nearEnd) {
            JLabel ribbon = new JLabel(" Sắp h.hạn ");
            ribbon.setFont(new Font("Segoe UI", Font.BOLD, 10));
            ribbon.setForeground(WHITE);
            ribbon.setBackground(GOLD);
            ribbon.setOpaque(true);
            ribbon.setBorder(new EmptyBorder(2, 6, 2, 6));
            ribbon.setBounds(248, 155, 92, 22);
            card.add(ribbon);
        }

        // ── Hover shadow effect ──────────────────────────────────────
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    new LineBorder(TEAL, 2, true),
                    new EmptyBorder(0, 0, 0, 0)
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(new CompoundBorder(
                    new LineBorder(new Color(220, 228, 240), 1, true),
                    new EmptyBorder(0, 0, 0, 0)
                ));
            }
        });

        return card;
    }

    /**
     * Chip hiển thị mã code – click để copy vào clipboard.
     */
    private JPanel buildCodeChip(String code) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chip.setMaximumSize(new Dimension(220, 34));
        chip.setBackground(WHITE);

        JLabel prefix = new JLabel("Mã:");
        prefix.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        prefix.setForeground(TEXT_MUTED);

        JButton btnCode = new JButton(code);
        btnCode.setFont(new Font("Consolas", Font.BOLD, 13));
        btnCode.setForeground(TEAL_DARK);
        btnCode.setBackground(TEAL_LIGHT);
        btnCode.setBorder(new CompoundBorder(
            new LineBorder(TEAL, 1, true),
            new EmptyBorder(3, 10, 3, 10)
        ));
        btnCode.setFocusPainted(false);
        btnCode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCode.setToolTipText("Click để sao chép mã");

        btnCode.addActionListener(e -> {
            StringSelection ss = new StringSelection(code);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            // Pulse feedback
            btnCode.setText("✓ Đã sao chép!");
            btnCode.setForeground(TAG_GREEN);
            javax.swing.Timer t = new javax.swing.Timer(1500, ev -> {
                btnCode.setText(code);
                btnCode.setForeground(TEAL_DARK);
            });
            t.setRepeats(false);
            t.start();
        });

        JLabel copyIcon = new JLabel("📋");
        copyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        copyIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        chip.add(prefix);
        chip.add(btnCode);
        chip.add(copyIcon);
        return chip;
    }

    // ══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════

    private String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private double numDouble(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number ? ((Number) v).doubleValue() : 0.0;
    }

    private int numInt(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v instanceof Number ? ((Number) v).intValue() : 0;
    }

    private String formatTimestamp(Object raw) {
        if (raw == null) return "N/A";
        try {
            // API trả về dạng ISO string hoặc số milliseconds
            long ms;
            if (raw instanceof Number) {
                ms = ((Number) raw).longValue();
            } else {
                // Thử parse dưới dạng Timestamp map { date: ..., time: ... }
                if (raw instanceof Map) {
                    Object t = ((Map<?, ?>) raw).get("time");
                    ms = t instanceof Number ? ((Number) t).longValue() : 0;
                } else {
                    return raw.toString();
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(new Date(ms));
        } catch (Exception e) {
            return "N/A";
        }
    }

    private boolean isNearEnd(Object raw, int days) {
        if (raw == null) return false;
        try {
            long ms;
            if (raw instanceof Number) {
                ms = ((Number) raw).longValue();
            } else if (raw instanceof Map) {
                Object t = ((Map<?, ?>) raw).get("time");
                ms = t instanceof Number ? ((Number) t).longValue() : 0;
            } else return false;

            long now   = Instant.now().toEpochMilli();
            long diff  = ms - now;
            long dayMs = 24L * 60 * 60 * 1000;
            return diff > 0 && diff <= days * dayMs;
        } catch (Exception e) { return false; }
    }

    // ══════════════════════════════════════════════════════════════════
    //  WrapLayout – layout tự xuống dòng như CSS flex-wrap
    // ══════════════════════════════════════════════════════════════════

    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;

                for (int i = 0; i < target.getComponentCount(); i++) {
                    Component m = target.getComponent(i);
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        dim.width = Math.max(dim.width, rowWidth);
                        dim.height += rowHeight + vgap;
                        rowWidth = 0; rowHeight = 0;
                    }
                    rowWidth += d.width + hgap;
                    rowHeight = Math.max(rowHeight, d.height);
                }
                dim.width = Math.max(dim.width, rowWidth);
                dim.height += rowHeight + insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }
}
