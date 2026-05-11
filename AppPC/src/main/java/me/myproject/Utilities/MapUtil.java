package me.myproject.Utilities;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import me.myproject.Utilities.AppConfig;

public class MapUtil extends JLabel {

    public MapUtil(String address) {
        this(address, null);
    }
    // Khởi tạo bằng tọa độ Lat/Lng (Double)
    public MapUtil(double lat, double lng) {

        try {
            String apiKey = AppConfig.GOOGLE_API_KEY;
            String coords = lat + "," + lng;
            String mapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                    "center=" + coords +
                    "&zoom=15" +
                    "&size=600x1000" +
                    "&markers=color:red|" + coords +
                    "&key=" + apiKey;
            ImageIcon imageIcon = new ImageIcon(new URL(mapUrl));
            this.setIcon(imageIcon);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public MapUtil(String origin, String destination) {
        String apiKey = AppConfig.GOOGLE_API_KEY;
        
        try {
            String mapUrl;
            if (origin != null && destination != null && !origin.isBlank() && !destination.isBlank()) {
                String encodedOrigin = URLEncoder.encode(origin, StandardCharsets.UTF_8);
                String encodedDestination = URLEncoder.encode(destination, StandardCharsets.UTF_8);
                mapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                        "size=600x1000" +
                        "&markers=color:green|" + encodedOrigin +
                        "&markers=color:red|" + encodedDestination +
                        "&path=color:0x0077FF|weight:5|" + encodedOrigin + "|" + encodedDestination +
                        "&key=" + apiKey;
            } else {
                String target = origin != null ? origin : "";
                String encodedAddress = URLEncoder.encode(target, StandardCharsets.UTF_8);
                mapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                        "center=" + encodedAddress +
                        "&zoom=15" +
                        "&size=600x1000" +
                        "&markers=color:red|" + encodedAddress +
                        "&key=" + apiKey;
            }
            ImageIcon imageIcon = new ImageIcon(new URL(mapUrl));
            this.setIcon(imageIcon);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

   
}
