package utils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class DistanceCalculator {

    private static final String GOOGLE_API_KEY = "AIzaSyA2lvATUTysfX6qUU_kA_FdZuHtvGarEuk";
    public static double calculateDistanceByAddress(String address1, String address2) throws IOException {
        if (address1 == null || address2 == null || address1.trim().isEmpty() || address2.trim().isEmpty()) {
            throw new IllegalArgumentException("Addresses cannot be null or empty");
        }

        // Convert addresses to coordinates
        Coordinates coords1 = geocodeAddress(address1);
        Coordinates coords2 = geocodeAddress(address2);

        // Calculate distance using Haversine formula
        return calculateHaversineDistance(
            coords1.getLatitude(), coords1.getLongitude(),
            coords2.getLatitude(), coords2.getLongitude()
        );
    }

    // Geocode address using Google Maps Geocoding API
    private static Coordinates geocodeAddress(String address) throws IOException {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
        String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedAddress +
                          "&key=" + GOOGLE_API_KEY;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
            StringBuilder response = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                response.append((char) c);
            }

            JSONObject json = new JSONObject(response.toString());
            if (!json.getString("status").equals("OK")) {
                throw new IOException("Geocoding failed for address '" + address + "': " + json.getString("status"));
            }

            JSONObject location = json.getJSONArray("results")
                                     .getJSONObject(0)
                                     .getJSONObject("geometry")
                                     .getJSONObject("location");
            double latitude = location.getDouble("lat");
            double longitude = location.getDouble("lng");
            return new Coordinates(latitude, longitude);
        } finally {
            conn.disconnect();
        }
    }

    // Haversine formula to calculate distance between two points (in kilometers)
    private static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Coordinates class to store latitude and longitude
    private static class Coordinates {
        private final double latitude;
        private final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }

    // Example usage
    public static void main(String[] args) {
        try {
            double distance = calculateDistanceByAddress(
                "123 Nguyen Van Cu, HCMC, Vietnam",
                "456 Le Loi, HCMC, Vietnam"
            );
            System.out.printf("Distance: %.2f km%n", distance);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}