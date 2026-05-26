package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.GoogleMapsClient;

public class ChonDiemView extends FrameMain implements ActionListener {
    private final TaiKhoan taiKhoan;
    private final GoogleMapsClient mapsClient;

    private JTextField tfdPickup;
    private JTextField tfdDestination;
    private DefaultListModel<String> pickupModel;
    private DefaultListModel<String> destinationModel;
    private JPopupMenu pickupPopup;
    private JPopupMenu destinationPopup;
    private JList<String> pickupList;
    private JList<String> destinationList;
    private JButton btnNext;

    public ChonDiemView(TaiKhoan taiKhoan) {
        super("Chọn điểm đi/đến");
        this.taiKhoan = taiKhoan;
        this.mapsClient = new GoogleMapsClient(AppConfig.GOOGLE_API_KEY);
        init();
    }

    private void init() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Chọn điểm đi và điểm đến", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 128, 128));
        panel.add(title, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel pickupLabel = new JLabel("Điểm đón");
        pickupLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(pickupLabel, gbc);

        gbc.gridy++;
        tfdPickup = new JTextField(25);
        tfdPickup.setPreferredSize(new Dimension(250, 30));
        tfdPickup.setFocusTraversalKeysEnabled(false);
        panel.add(tfdPickup, gbc);
        initAutocompleteForPickup();

        gbc.gridy++;
        JLabel destinationLabel = new JLabel("Điểm đến");
        destinationLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(destinationLabel, gbc);

        gbc.gridy++;
        tfdDestination = new JTextField(25);
        tfdDestination.setPreferredSize(new Dimension(250, 30));
        tfdDestination.setFocusTraversalKeysEnabled(false);
        panel.add(tfdDestination, gbc);
        initAutocompleteForDestination();

        gbc.gridy++;
        gbc.gridwidth = 2;
        btnNext = new JButton("TIẾP TỤC");
        btnNext.setPreferredSize(new Dimension(220, 40));
        btnNext.setBackground(new Color(0, 160, 160));
        btnNext.setForeground(Color.BLACK);
        btnNext.setFont(new Font("Arial", Font.BOLD, 16));
        btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNext.addActionListener(this);
        panel.add(btnNext, gbc);

        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnNext) {
            String pickup = tfdPickup.getText().trim();
            String destination = tfdDestination.getText().trim();
            if (pickup.isBlank() || destination.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập điểm đón và điểm đến", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new DatXeView(taiKhoan, pickup, destination);
            dispose();
        }
    }

    private void initAutocompleteForPickup() {
        pickupModel = new DefaultListModel<>();
        pickupList = new JList<>(pickupModel);
        pickupPopup = createPopupWithList(pickupList, tfdPickup);
        bindAutocomplete(tfdPickup, pickupModel, pickupPopup);
    }

    private void initAutocompleteForDestination() {
        destinationModel = new DefaultListModel<>();
        destinationList = new JList<>(destinationModel);
        destinationPopup = createPopupWithList(destinationList, tfdDestination);
        bindAutocomplete(tfdDestination, destinationModel, destinationPopup);
    }

    private JPopupMenu createPopupWithList(JList<String> list, JTextField owner) {
        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false); // <--- QUAN TRỌNG: Không cho popup cướp focus của TextField

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        popup.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
        popup.add(scroll);

        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                String selected = list.getSelectedValue();
                if (selected != null) {
                    owner.setText(selected);
                    popup.setVisible(false);
                    // Sau khi chọn xong, đưa focus xuống ô Điểm Đến nếu đang ở ô Điểm Đón
                    if (owner == tfdPickup) {
                        tfdDestination.requestFocusInWindow();
                    }
                }
            }
        });
        return popup;
    }

    private void bindAutocomplete(JTextField field, DefaultListModel<String> model, JPopupMenu popup) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int keyCode = e.getKeyCode();
                // Bỏ qua các phím điều hướng, Enter, Shift... để không gọi API vô ích
                if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN 
                    || keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SHIFT) {
                    return;
                }

                String text = field.getText().trim();
                if (text.length() < 3 || AppConfig.GOOGLE_API_KEY.isBlank()) {
                    popup.setVisible(false);
                    return;
                }

                // QUAN TRỌNG: Chạy luồng ngầm (Background Thread) để gọi API, không làm treo UI
                new Thread(() -> {
                    try {
                        List<String> results = mapsClient.autocomplete(text);
                        
                        // Chỉ dùng invokeLater khi cần cập nhật lại Giao diện
                        SwingUtilities.invokeLater(() -> {
                            // Tránh tình trạng gõ quá nhanh, kết quả cũ về đè lên kết quả mới
                            if (!field.getText().trim().equals(text)) return;

                            model.clear();
                            results.stream().limit(6).forEach(model::addElement);
                            
                            if (!results.isEmpty()) {
                                if (!popup.isVisible()) {
                                    popup.show(field, 0, field.getHeight());
                                }
                                // Ép giữ con trỏ chuột ở lại TextField
                                field.requestFocusInWindow(); 
                            } else {
                                popup.setVisible(false);
                            }
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> popup.setVisible(false));
                    }
                }).start();
            }
        });

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Khi mất focus, chờ 1 chút xíu (100ms) để sự kiện MouseClick ở List kịp nhận diện
                // Nếu tắt ngay lập tức, click vào list sẽ không có tác dụng
                new Thread(() -> {
                    try { Thread.sleep(150); } catch (InterruptedException ex) {}
                    SwingUtilities.invokeLater(() -> popup.setVisible(false));
                }).start();
            }
        });
    }
}
