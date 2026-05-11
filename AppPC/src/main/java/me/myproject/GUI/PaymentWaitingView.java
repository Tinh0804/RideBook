package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import me.myproject.MODEL.TaiKhoan;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.AuthManager;
import me.myproject.Utilities.DIMENSION.FrameMain;
import me.myproject.Utilities.StompWebSocketClient;

public class PaymentWaitingView extends FrameMain {
    private final TaiKhoan taiKhoan;
    private final String bookingId;
    private StompWebSocketClient webSocketClient;
    private ScheduledExecutorService scheduler;
    private JLabel countdownLabel;
    private long remainingSeconds = 10 * 60;

    public PaymentWaitingView(TaiKhoan taiKhoan, String bookingId) {
        super("Đang chờ thanh toán");
        this.taiKhoan = taiKhoan;
        this.bookingId = bookingId;
        init();
        initWebSocket();
    }

    private void init() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel("Đang chờ thanh toán...", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.CENTER);

        countdownLabel = new JLabel(formatCountdown(), SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        countdownLabel.setForeground(Color.DARK_GRAY);
        panel.add(countdownLabel, BorderLayout.NORTH);

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose, BorderLayout.SOUTH);

        startCountdown();

        this.add(panel);
        this.setVisible(true);
    }

    private void initWebSocket() {
        String customerId = resolveCustomerId();
        if (customerId == null || customerId.isBlank()) {
            return;
        }
        String destination = "/topic/customer/" + customerId;
        webSocketClient = new StompWebSocketClient(destination, this::handleMessage);
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

    private void startCountdown() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if (remainingSeconds <= 0) {
                stopCountdown();
                return;
            }
            remainingSeconds--;
            SwingUtilities.invokeLater(() -> countdownLabel.setText(formatCountdown()));
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void stopCountdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private String formatCountdown() {
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        return String.format("Thời gian còn lại: %02d:%02d", minutes, seconds);
    }

    private void handleMessage(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (message.startsWith("ERROR:")) {
            return;
        }
        if (message.contains("PAYMENT_SUCCESS:" + bookingId)) {
            SwingUtilities.invokeLater(() -> {
                stopCountdown();
                setTitle("Thanh toán thành công");
                new ChoTaiXeView(taiKhoan,this);
                dispose();
            });
        } else if (message.contains("PAYMENT_FAILED:" + bookingId)) {
            SwingUtilities.invokeLater(() -> {
                stopCountdown();
                setTitle("Thanh toán thất bại");
                dispose();
            });
        } else if (message.contains("PAYMENT_TIMEOUT:" + bookingId)) {
            SwingUtilities.invokeLater(() -> {
                stopCountdown();
                setTitle("Hết thời gian thanh toán");
                new DatXeView(taiKhoan);
                dispose();
            });
        }
    }
}
