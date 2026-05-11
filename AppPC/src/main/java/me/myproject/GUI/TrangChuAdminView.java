package me.myproject.GUI;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.TokenStore;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TrangChuAdminView – Dashboard màn hình trang chủ của Admin.
 * Sidebar gồm: Quản lý chuyến đi, Khuyến mãi, Tài xế (future), Đăng xuất.
 * Content panel hiển thị các thẻ thống kê nhanh.
 */
public class TrangChuAdminView extends FrameMain implements ActionListener {

    // ─── Palette ──────────────────────────────────────────────────────
    private static final Color TEAL       = new Color(0, 172, 172);
    private static final Color TEAL_DARK  = new Color(0, 115, 115);
    private static final Color SIDEBAR_BG = new Color(20, 30, 50);
    private static final Color WHITE      = Color.WHITE;
    private static final Color BG         = new Color(245, 248, 252);

    // ─── State ────────────────────────────────────────────────────────
    private final TaiKhoan tk;

    // ─── Panels ───────────────────────────────────────────────────────
    private JPanel headerPanel, menuPanel, contentPanel;

    // ─── Menu buttons ─────────────────────────────────────────────────
    private JButton btnTrangChu, btnQuanLyKhuyenMai, btnQuanLyChuyenDi,
                    btnDangXuat;

    // ─── Stat labels ──────────────────────────────────────────────────
    private JLabel lblTotalBookings, lblActivePromos, lblTotalRevenue;

    // ─────────────────────────────────────────────────────────────────
    public TrangChuAdminView(TaiKhoan taiKhoan) {
        super("Trang Chủ Admin – BookCar");
        this.tk = taiKhoan;
        buildUI();
        loadStats();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
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

        // ── Header (top bar) ─────────────────────────────────────────
        headerPanel = buildHeader(W);
        headerPanel.setBounds(0, 0, W, 65);
        layered.add(headerPanel, JLayeredPane.PALETTE_LAYER);

        // ── Sidebar (left) ───────────────────────────────────────────
        menuPanel = buildSidebar(H);
        menuPanel.setBounds(0, 65, 210, H - 65);
        layered.add(menuPanel, JLayeredPane.PALETTE_LAYER);

        // ── Content (right) ──────────────────────────────────────────
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        contentPanel.setBounds(210, 65, W - 210, H - 65);
        layered.add(contentPanel, JLayeredPane.PALETTE_LAYER);

        // Resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension s = getSize();
                layered.setSize(s);
                headerPanel.setBounds(0, 0, s.width, 65);
                menuPanel.setBounds(0, 65, 210, s.height - 65);
                contentPanel.setBounds(210, 65, s.width - 210, s.height - 65);
            }
        });

        buildContent();
    }

    // ── Header ───────────────────────────────────────────────────────
    private JPanel buildHeader(int W) {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(TEAL_DARK);
        h.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setBackground(TEAL_DARK);

        JLabel logo = new JLabel("🚖");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel title = new JLabel("CRAB  Admin Panel");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(WHITE);

        left.add(logo); left.add(title);
        h.add(left, BorderLayout.WEST);

        // Time + greet
        JLabel greet = new JLabel("Xin chào, " + (tk != null ? tk.getUserName() : "Admin") + " 👋");
        greet.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        greet.setForeground(new Color(200, 240, 240));
        h.add(greet, BorderLayout.EAST);

        return h;
    }

    // ── Sidebar ──────────────────────────────────────────────────────
    private JPanel buildSidebar(int H) {
        JPanel side = new JPanel(null); // absolute layout
        side.setBackground(SIDEBAR_BG);
        side.setPreferredSize(new Dimension(210, H - 65));

        // Section label
        JLabel lblSec = new JLabel("  MENU QUẢN TRỊ");
        lblSec.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSec.setForeground(new Color(130, 150, 180));
        lblSec.setBounds(0, 18, 210, 24);
        side.add(lblSec);

        int gap = 44, top = 50, bW = 200, bH = 40, lm = 5;
        btnTrangChu        = sideBtn("⬛ Trang Chủ",        "TEAL");
        btnQuanLyChuyenDi  = sideBtn("📋 Quản Lý Chuyến",  null);
        btnQuanLyKhuyenMai = sideBtn("🎟 Khuyến Mãi",      null);

        btnTrangChu       .setBounds(lm, top,            bW, bH);
        btnQuanLyChuyenDi .setBounds(lm, top + gap,      bW, bH);
        btnQuanLyKhuyenMai.setBounds(lm, top + gap * 2,  bW, bH);

        // Logout at bottom
        btnDangXuat = sideBtn("⏏ Đăng Xuất", "RED");
        btnDangXuat.setBounds(lm, H - 65 - 60, bW, bH);

        side.add(btnTrangChu);
        side.add(btnQuanLyChuyenDi);
        side.add(btnQuanLyKhuyenMai);
        side.add(btnDangXuat);

        // Active highlight for home
        setActive(btnTrangChu);

        // Resize update for logout position
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                btnDangXuat.setBounds(lm, getHeight() - 65 - 60, bW, bH);
            }
        });

        return side;
    }

    private JButton sideBtn(String text, String accent) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setForeground("RED".equals(accent) ? new Color(250, 100, 100) : WHITE);
        b.setBackground(SIDEBAR_BG);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 8));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(this);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!b.getForeground().equals(TEAL)) {
                    b.setForeground(TEAL);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!isActive(b)) {
                    b.setForeground("RED".equals(accent) ? new Color(250, 100, 100) : WHITE);
                }
            }
        });
        return b;
    }

    // ── Content ──────────────────────────────────────────────────────
    private void buildContent() {
        contentPanel.removeAll();

        // Welcome banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(0, 145, 145));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        banner.setAlignmentX(0);

        JLabel bannerTitle = new JLabel("Chào mừng đến trang quản trị CRAB 👋");
        bannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        bannerTitle.setForeground(WHITE);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        JLabel bannerDate = new JLabel(sdf.format(new Date()));
        bannerDate.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bannerDate.setForeground(new Color(200, 240, 240));

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);
        bannerText.add(bannerTitle);
        bannerText.add(Box.createRigidArea(new Dimension(0, 4)));
        bannerText.add(bannerDate);
        banner.add(bannerText, BorderLayout.CENTER);

        contentPanel.add(banner);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Stat cards row
        JLabel statTitle = new JLabel("  📊 Tổng Quan Hệ Thống");
        statTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        statTitle.setForeground(new Color(50, 60, 80));
        statTitle.setAlignmentX(0);
        contentPanel.add(statTitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setBackground(BG);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cards.setAlignmentX(0);

        lblTotalBookings = new JLabel("...");
        lblActivePromos  = new JLabel("...");
        lblTotalRevenue  = new JLabel("...");

        cards.add(statCard("📦 Tổng Chuyến Đi", lblTotalBookings, new Color(99, 102, 241)));
        cards.add(statCard("🎟 Khuyến Mãi Đang Chạy", lblActivePromos, new Color(16, 185, 129)));
        cards.add(statCard("💰 Doanh Thu Tổng", lblTotalRevenue, new Color(245, 158, 11)));

        contentPanel.add(cards);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 28)));

        // Quick actions
        JLabel actionTitle = new JLabel("  ⚡ Thao Tác Nhanh");
        actionTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        actionTitle.setForeground(new Color(50, 60, 80));
        actionTitle.setAlignmentX(0);
        contentPanel.add(actionTitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setBackground(BG);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        actions.setAlignmentX(0);

        actions.add(quickBtn("📋 Xem Chuyến Đi",   new Color(99, 102, 241), e -> openChuyenDi()));
        actions.add(quickBtn("🎟 Tạo Khuyến Mãi",  new Color(16, 185, 129), e -> openKhuyenMai()));

        contentPanel.add(actions);
        contentPanel.add(Box.createVerticalGlue());

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 228, 240), 1, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(120, 130, 145));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accent);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(lbl);
        left.add(Box.createRigidArea(new Dimension(0, 6)));
        left.add(valueLabel);

        card.add(left, BorderLayout.CENTER);

        // Accent bar on left
        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(5, 0));
        card.add(bar, BorderLayout.WEST);

        return card;
    }

    private JButton quickBtn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(bg);
        b.setForeground(WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(bg.darker()); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    // ══════════════════════════════════════════════════════════════════
    //  LOAD STATS
    // ══════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void loadStats() {
        new Thread(() -> {
            try {
                // Bookings count
                Map<String, Object> bResp = APIHelper.getForMap(AppConfig.BASE_URL + "/bookings/admin/all?page=0&size=1");
                // Active promotions count
                Map<String, Object> pResp = APIHelper.getForMap(AppConfig.BASE_URL + "/promotions/active");
                // Revenue (reuse QuanLyChuyenDi BSL summary if available)
                Map<String, Object> rResp = APIHelper.getForMap(AppConfig.BASE_URL + "/bookings/admin/summary");

                SwingUtilities.invokeLater(() -> {
                    // Booking total
                    try {
                        Map<String, Object> bResult = (Map<String, Object>) bResp.get("result");
                        if (bResult != null) {
                            Object total = bResult.get("totalElements");
                            lblTotalBookings.setText(total != null ? total.toString() : "N/A");
                        }
                    } catch (Exception ignored) { lblTotalBookings.setText("N/A"); }

                    // Promos active count
                    try {
                        java.util.List<?> promos = (java.util.List<?>) pResp.get("result");
                        lblActivePromos.setText(promos != null ? String.valueOf(promos.size()) : "0");
                    } catch (Exception ignored) { lblActivePromos.setText("N/A"); }

                    // Revenue
                    try {
                        Map<String, Object> rResult = (Map<String, Object>) rResp.get("result");
                        if (rResult != null) {
                            Object rev = rResult.get("totalRevenue");
                            if (rev instanceof Number) {
                                lblTotalRevenue.setText(String.format("%,.0f đ", ((Number) rev).doubleValue()).replace(",", "."));
                            }
                        }
                    } catch (Exception ignored) { lblTotalRevenue.setText("N/A"); }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblTotalBookings.setText("N/A");
                    lblActivePromos .setText("N/A");
                    lblTotalRevenue .setText("N/A");
                });
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════════════
    //  ACTIONS
    // ══════════════════════════════════════════════════════════════════

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        resetSidebarColors();

        if (src == btnTrangChu) {
            setActive(btnTrangChu);
            buildContent();
            loadStats();

        } else if (src == btnQuanLyChuyenDi) {
            setActive(btnQuanLyChuyenDi);
            openChuyenDi();

        } else if (src == btnQuanLyKhuyenMai) {
            setActive(btnQuanLyKhuyenMai);
            openKhuyenMai();

        } else if (src == btnDangXuat) {
            doLogout();
        }
    }

    private void openChuyenDi() {
        try {
            new QuanLyChuyenDiView();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi mở màn hình: " + ex.getMessage());
        }
    }

    private void openKhuyenMai() {
        new QuanLyKhuyenMaiView(tk);
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            if (tk != null && tk.getRefreshToken() != null) {
                String rt = URLEncoder.encode(tk.getRefreshToken(), StandardCharsets.UTF_8);
                APIHelper.postForMap(AppConfig.BASE_URL + "/auth/logout?refreshToken=" + rt, Map.of());
            }
        } catch (Exception ignored) {}

        try { TokenStore.clearTokens(); } catch (IOException ignored) {}
        APIHelper.clearAuthToken();
        dispose();
        new DangNhapView();
    }

    // ── Sidebar highlight helpers ─────────────────────────────────────
    private void setActive(JButton b) {
        b.setForeground(TEAL);
    }

    private boolean isActive(JButton b) {
        return b.getForeground().equals(TEAL);
    }

    private void resetSidebarColors() {
        for (JButton b : new JButton[]{btnTrangChu, btnQuanLyChuyenDi, btnQuanLyKhuyenMai}) {
            b.setForeground(WHITE);
        }
        btnDangXuat.setForeground(new Color(250, 100, 100));
    }
}
