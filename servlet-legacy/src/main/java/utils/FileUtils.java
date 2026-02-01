package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class FileUtils{
	public static void saveFile(Part filePart, String fileName, HttpServletRequest request) throws IOException {
        if (filePart != null && filePart.getSize() > 0) {
            String uploadPath = request.getServletContext().getRealPath("") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            
            if (!uploadDir.exists()) uploadDir.mkdir();

            String filePath = uploadPath + File.separator + fileName + getFileExtension(filePart);
            filePart.write(filePath);
            System.out.println("Đã lưu file: " + filePath);
        }
    }

    public static String getFileExtension(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String s : contentDisp.split(";")) {
            if (s.trim().startsWith("filename")) {
                String fileName = s.substring(s.indexOf("=") + 1).trim().replace("\"", "");
                return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
            }
        }
        return "";
    }
}