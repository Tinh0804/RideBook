package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QLDX;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "@Tinh0804"; 

    public static Connection getConnection() throws SQLException {
    	System.out.println("connect success");
    	try {
    		 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public static void closeConnection(Connection conn) throws SQLException {
    	conn.close();
    }
}
