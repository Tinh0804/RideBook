package me.myproject.Utilities;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class ImagePanel extends JPanel {
    private Image image;
    private int width;
    private int height;

    public ImagePanel(String imagePath, int width, int height) {
        this.width = width;
        this.height = height;

        // Load image from path
        URL imageUrl = ImageAccess.class.getResource(imagePath);
        if (imageUrl != null) {
            this.image = new ImageIcon(imageUrl).getImage();
        } else {
            System.err.println("Không tìm thấy ảnh: " + imagePath);
        }

        setPreferredSize(new Dimension(width, height));
    }

    public static void loadIconFromNetworkAsync(String imageUrl, JLabel label, int width, int height) {
        // 1. Hiển thị trạng thái chờ tạm thời
        label.setIcon(null);
        label.setText("Đang tải...");

        // 2. Khởi tạo một luồng chạy ngầm (Background Thread)
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                // Tải ảnh dưới nền
                URL url = new URL(imageUrl);
                Image img = ImageIO.read(url);
                if (img != null) {
                    Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Lấy kết quả từ doInBackground() khi nó chạy xong
                    ImageIcon icon = get(); 
                    if (icon != null) {
                        label.setText(""); // Xóa chữ "Đang tải..."
                        label.setIcon(icon); // Cập nhật ảnh lên UI
                    } else {
                        label.setText("Ảnh lỗi");
                    }
                } catch (Exception e) {
                    label.setText("Lỗi mạng");
                    System.err.println("Không thể tải ảnh: " + imageUrl);
                }
            }
        };
        
        // 3. Kích hoạt luồng chạy ngầm
        worker.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, width, height, this);
        }
    }
}