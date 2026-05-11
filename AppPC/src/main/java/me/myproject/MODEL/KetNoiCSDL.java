package me.myproject.MODEL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class KetNoiCSDL {
    private String UserName;
    private String PassWord;
    private String DataBaseName;
    private String ServerName;
    private String DriverClass;
    protected Connection con = null;
    private Statement stm = null;
    private String DriverURL;

    public KetNoiCSDL() {
        this.UserName = "sa";
        this.PassWord = "Ntvh0201@";
        this.DataBaseName = "QLDX";
        this.ServerName = "localhost";
        this.DriverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        this.DriverURL = "jdbc:sqlserver://" + ServerName + ":1433;databaseName=" + DataBaseName + ";user=" + UserName + ";password=" + PassWord + ";encrypt=false;IntegratedSecurity=false";
    }

    public Connection getConnection() {
        try {
            Class.forName(DriverClass);
            con = DriverManager.getConnection(DriverURL);
            if (con != null) {
                System.out.println("Kết nối CSDL thành công");
            }
        } catch (Exception e) {
            System.out.println("Lỗi kết nối CSDL!");
            e.printStackTrace();
        }
        return con; // Trả về kết nối
    }

    public void closeConnection() {
        try {
            if (stm != null) {
                stm.close();
            }
            if (con != null) {
                con.close();
            }
            System.out.println("Đã đóng kết nối CSDL.");
        } catch (SQLException e) {
            System.out.println("Lỗi khi đóng kết nối!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        KetNoiCSDL ketNoi = new KetNoiCSDL();
        ketNoi.getConnection(); 
    }
}
