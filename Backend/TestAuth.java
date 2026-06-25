import java.io.*;
import java.net.*;

public class TestAuth {
    public static void main(String[] args) throws Exception {
        // Login as admin
        String loginJson = "{\"userName\":\"admin\",\"passWord\":\"12345\",\"roleName\":\"ADMIN\"}";
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8080/RideBook/auth/login").openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(loginJson.getBytes());
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) content.append(inputLine);
        in.close();
        
        String response = content.toString();
        String token = response.split("\"token\":\"")[1].split("\"")[0];
        
        // Fetch trips for customer ac6554cc-ff25-4104-8575-9ba7ff2d3031
        String customerId = "ac6554cc-ff25-4104-8575-9ba7ff2d3031";
        HttpURLConnection tripConn = (HttpURLConnection) new URL("http://localhost:8080/RideBook/bookings/customer/" + customerId).openConnection();
        tripConn.setRequestMethod("GET");
        tripConn.setRequestProperty("Authorization", "Bearer " + token);
        
        BufferedReader tripIn = new BufferedReader(new InputStreamReader(tripConn.getInputStream()));
        StringBuilder tripContent = new StringBuilder();
        while ((inputLine = tripIn.readLine()) != null) tripContent.append(inputLine);
        tripIn.close();
        
        System.out.println("Trips Response: " + tripContent.toString());
    }
}
