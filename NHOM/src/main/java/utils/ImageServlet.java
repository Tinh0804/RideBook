package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/uploads/*")
public class ImageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String fileName = request.getPathInfo().substring(1); // Bỏ dấu "/"
        String uploadPath = request.getServletContext().getRealPath("/uploads");
        File file = new File(uploadPath, fileName);

        if (file.exists()) {
            String contentType = getServletContext().getMimeType(fileName);
            response.setContentType(contentType != null ? contentType : "application/octet-stream");
            Files.copy(file.toPath(), response.getOutputStream());
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
        }
    }
}