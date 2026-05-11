package me.myproject.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import me.myproject.BUSINESSLOGIC.ChatBSL;
import me.myproject.Utilities.AppConfig;
import me.myproject.Utilities.StompWebSocketClient;

public class ChatDialog extends JDialog {
    private String bookingId;
    private String senderId;
    private String receiverId;
    private boolean isDriver; // Thêm cờ để xác định app đang mở là của Tài xế hay Khách hàng
    
    private ChatBSL chatBSL;
    private StompWebSocketClient webSocketClient;

    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField txtMessage;
    private JButton btnSend;

    // Cập nhật Constructor thêm tham số boolean isDriver
    public ChatDialog(Frame parent, String bookingId, String senderId, String receiverId, boolean isDriver) {
        super(parent, "Trò chuyện chuyến xe", false);
        this.bookingId = bookingId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.isDriver = isDriver; 
        this.chatBSL = new ChatBSL();
        
        initUI();
        loadHistory();
        initWebSocket();
    }

    private void initUI() {
        setSize(420, 550);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 245));

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);
        chatPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(220, 220, 220)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Panel Nhập tin nhắn
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 15, 15, 15));
        inputPanel.setBackground(new Color(245, 245, 245));

        txtMessage = new JTextField();
        txtMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        txtMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        btnSend = new JButton("Gửi");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.setBackground(new Color(0, 150, 136));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setBorderPainted(false);
        btnSend.setOpaque(true);
        btnSend.addActionListener(e -> sendMessage());

        inputPanel.add(txtMessage, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);
    }

    private void loadHistory() {
        new Thread(() -> {
            Map<String, Object> res = chatBSL.layLichSuTinNhan(bookingId);
            Number status = (Number) res.get("status");
            if (status != null && status.intValue() == 200) {
                List<Map<String, Object>> messages = (List<Map<String, Object>>) res.get("result");
                SwingUtilities.invokeLater(() -> {
                    for (Map<String, Object> msg : messages) {
                        String sId = (String) msg.get("senderId");
                        String content = (String) msg.get("content");
                        appendMessage(sId, content);
                    }
                    scrollToBottom();
                });
            }
        }).start();
    }

    private void initWebSocket() {
        String destination = "/topic/chat/" + bookingId;
        webSocketClient = new StompWebSocketClient(destination, this::handleWebSocketMessage);
        webSocketClient.connect(AppConfig.WS_URL);
    }

    private void handleWebSocketMessage(String payload) {
        try {
            String sId = extractJsonValue(payload, "senderId");
            String content = extractJsonValue(payload, "content");
            
            SwingUtilities.invokeLater(() -> {
                appendMessage(sId, content);
                scrollToBottom();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractJsonValue(String json, String key) {
        int index = json.indexOf("\"" + key + "\"");
        if (index < 0) return "";
        int colonIndex = json.indexOf(":", index);
        int startQuote = json.indexOf("\"", colonIndex);
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (startQuote > 0 && endQuote > startQuote) {
            return json.substring(startQuote + 1, endQuote);
        }
        return "";
    }

    private void sendMessage() {
        String text = txtMessage.getText().trim();
        if (text.isEmpty()) return;

        txtMessage.setText("");

        new Thread(() -> {
            Map<String, Object> request = new HashMap<>();
            request.put("bookingId", bookingId);
            request.put("senderId", senderId);
            request.put("receiverId", receiverId);
            request.put("content", text);
            chatBSL.guiTinNhan(request);
        }).start();
    }

    private void appendMessage(String sId, String content) {
        boolean isMe = (sId != null && sId.equals(senderId));
        
        // Xác định tên hiển thị dựa vào role
        String senderName;
        if (isMe) {
            senderName = isDriver ? "Bạn (Tài xế)" : "Bạn";
        } else {
            senderName = isDriver ? "Khách hàng" : "Tài xế";
        }

        // Panel bọc toàn bộ 1 dòng tin nhắn
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        // Panel chứa Tên và Nội dung bong bóng
        JPanel contentGroup = new JPanel();
        contentGroup.setLayout(new BoxLayout(contentGroup, BoxLayout.Y_AXIS));
        contentGroup.setBackground(Color.WHITE);

        // Label Tên người gửi
        JLabel lblName = new JLabel(senderName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblName.setForeground(new Color(130, 130, 130));

        // Label Bong bóng chat
        // Giới hạn max-width bằng HTML để không bị tràn màn hình với tin nhắn dài
        String htmlContent = "<html><div style='max-width: 200px; padding: 6px 10px; font-family: Segoe UI;'>" + content.replace("\n", "<br>") + "</div></html>";
        JLabel lblBubble = new JLabel(htmlContent);
        lblBubble.setOpaque(true);
        lblBubble.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        if (isMe) {
            lblName.setAlignmentX(Component.RIGHT_ALIGNMENT);
            lblBubble.setAlignmentX(Component.RIGHT_ALIGNMENT);
            lblBubble.setBackground(new Color(220, 248, 198)); // Xanh nhạt kiểu WhatsApp
            
            contentGroup.add(lblName);
            contentGroup.add(Box.createVerticalStrut(3));
            contentGroup.add(lblBubble);
            
            rowPanel.add(contentGroup, BorderLayout.EAST);
        } else {
            lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblBubble.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblBubble.setBackground(new Color(235, 235, 235)); // Xám nhạt
            
            contentGroup.add(lblName);
            contentGroup.add(Box.createVerticalStrut(3));
            contentGroup.add(lblBubble);
            
            rowPanel.add(contentGroup, BorderLayout.WEST);
        }

        chatPanel.add(rowPanel);
        chatPanel.revalidate();
        chatPanel.repaint();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    @Override
    public void dispose() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        super.dispose();
    }
}