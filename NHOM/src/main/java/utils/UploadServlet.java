package utils;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.io.IOUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
	private final String UPLOAD_DIRECTORY = "uploads";  // Thư mục lưu ảnh trên server

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra xem yêu cầu có phải là multipart (tải tệp) không
        if (ServletFileUpload.isMultipartContent((javax.servlet.http.HttpServletRequest) request)) {
            // Cấu hình các thông số của FileUpload
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            try {
                // Phân tích các phần tử từ request
                List<FileItem> formItems = upload.parseRequest((javax.servlet.http.HttpServletRequest) request);

                // Duyệt qua các phần tử để lấy tệp
                for (FileItem item : formItems) {
                    if (!item.isFormField()) {  // Kiểm tra nếu phần tử là tệp
                        String fileName = new File(item.getName()).getName();
                        String filePath = getServletContext().getRealPath("/") + UPLOAD_DIRECTORY + File.separator + fileName;
                        File storeFile = new File(filePath);

                        // Lưu tệp vào server
                        item.write(storeFile);

                        // Gửi thông báo về ảnh đã tải lên
                        response.getWriter().println("Ảnh đã được tải lên thành công: " + fileName);
                    }
                }
            } catch (Exception ex) {
                response.getWriter().println("Lỗi khi tải tệp lên: " + ex.getMessage());
            }
        }
    }
}