import java.sql.*;

public class GetCustomer {
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookcar", "root", "12345678");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM booking");
        if(rs.next()) {
            System.out.println("Total bookings: " + rs.getInt(1));
        }
        
        ResultSet rs2 = stmt.executeQuery("SELECT booking_id, customer_id, booking_time FROM booking LIMIT 5");
        while(rs2.next()) {
            System.out.println("Booking: " + rs2.getString(1) + ", Customer: " + rs2.getString(2) + ", Time: " + rs2.getTimestamp(3));
        }
        conn.close();
    }
}
