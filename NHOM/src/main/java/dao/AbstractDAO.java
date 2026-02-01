package dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.DBConnection;

@FunctionalInterface
interface ResultSetMapper {
    Map<String, Object> map(ResultSet rs) throws SQLException;
}

public abstract class AbstractDAO<T> {
    
    protected T executeQuery(String sql, Object[] params, RowMapper<T> rowMapper) {
        T result = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                 result = rowMapper.mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    protected List<T> executeQueryList(String sql, Object[] params, RowMapper<T> rowMapper) {
        List<T> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(rowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    protected int executeUpdate(String sql, Object[] params) {
        int rowsAffected = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            
            rowsAffected = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowsAffected;
    }

    private void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
    protected static Object callFunction(String functionName, Object[] params, int returnType) {
        // Kiểm tra tên hàm hợp lệ
        if (!functionName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid function name");
        }

        // Tạo câu lệnh SQL
        StringBuilder sql = new StringBuilder("SELECT dbo." + functionName + "(");

        // Kiểm tra và thêm tham số nếu có
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                sql.append("?");
                if (i < params.length - 1) {
                    sql.append(", ");
                }
            }
        }

        sql.append(") AS result");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Thiết lập tham số nếu có
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }

            ResultSet rs = stmt.executeQuery();
            if (rs != null) {
                if (rs.next()) {
                    switch (returnType) {
                        case Types.VARCHAR:
                            return rs.getString("result");
                        case Types.INTEGER:
                            return rs.getInt("result");
                        case Types.DOUBLE:
                            return rs.getDouble("result");
                        default:
                            return rs.getObject("result");
                    }
                } else {
                    System.out.println("⚠ ResultSet is empty or no rows returned");
                    return null;
                }
            } else {
                System.out.println("⚠ ResultSet is null");
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static ResultSet callProcedureResultSet(String procedureName, Object[] params) throws SQLException {
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // Sinh câu lệnh SQL chuẩn với đúng số dấu ?
            StringBuilder sqlBuilder = new StringBuilder("{CALL dbo.");
            sqlBuilder.append(procedureName);

            if (params != null && params.length > 0) {
                sqlBuilder.append("(");
                for (int i = 0; i < params.length; i++) {
                    sqlBuilder.append("?");
                    if (i < params.length - 1) {
                        sqlBuilder.append(", ");
                    }
                }
                sqlBuilder.append(")");
            }

            sqlBuilder.append("}");
            String sql = sqlBuilder.toString();

            stmt = conn.prepareCall(sql);

            // Thiết lập tham số
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }

            // Thực thi
            rs = stmt.executeQuery();

            return rs;

        } catch (SQLException e) {
            System.err.println("Error calling procedure " + procedureName + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static List<Map<String, Object>> executeProcedure(
            String procedureName, 
            Object[] params, 
            ResultSetMapper mapper
    ) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection(); // Giả định DBConnection tồn tại
            String placeholders = params.length > 0 ? "(" + "?, ".repeat(params.length).replaceAll(", $", "") + ")" : "";
            String sql = "{CALL dbo." + procedureName + placeholders + "}";

            stmt = conn.prepareCall(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(mapper.map(rs));
            }

            return result;
        } catch (SQLException e) {
            System.err.println("Error executing procedure " + procedureName + ": " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    // Interface RowMapper giúp ánh xạ ResultSet thành đối tượng T
    public interface RowMapper<T> {
        T mapRow(ResultSet rs) throws SQLException;
    }
}
