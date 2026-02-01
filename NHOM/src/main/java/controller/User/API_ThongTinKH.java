package controller.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.KhachHangService;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/user/thongTinUser"})
public class API_ThongTinKH extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final KhachHangService khachHangService = new KhachHangService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String id = req.getParameter("id_Kh");
            if (id == null || id.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                mapper.writeValue(resp.getWriter(), Map.of(
                    "message", "ID khách hàng không hợp lệ",
                    "status", "error"
                ));
                return;
            }

            model.KhachHang user = khachHangService.selectUser(id);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), Map.of(
                    "message", "Không tìm thấy khách hàng với ID: " + id,
                    "status", "error"
                ));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), Map.of(
                "message", "Thành công!",
                "status", "success",
                "data", user
            ));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            mapper.writeValue(resp.getWriter(), Map.of(
                "message", "Lỗi: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}