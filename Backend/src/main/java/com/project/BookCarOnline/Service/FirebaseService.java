package com.project.BookCarOnline.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class FirebaseService {

    private Storage storage;

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Value("${firebase.config-path}")
    private String credentialsPath;

    @PostConstruct
    private void init() throws IOException {
        InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(credentialsPath);
        if (serviceAccount != null) {
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();
        }
    }

    // 1. FIREBASE CLOUD MESSAGING (PUSH NOTIFICATIONS)

    public void sendNotificationToToken(String fcmToken, String title, String body) {
        if (fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
        } catch (Exception e) {
            log.error("Error sending FCM message to token: {}", fcmToken, e);
        }
    }

    // 2. FIREBASE STORAGE (FILE UPLOAD/DOWNLOAD)

    public String uploadFile(MultipartFile file, String folderPath, String fileName) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : "";
            fileName = UUID.randomUUID().toString() + extension;
        }

        // Tạo đường dẫn đầy đủ (Folder + FileName)
        String fullPath = (folderPath == null || folderPath.isEmpty())
                ? fileName
                : folderPath.replaceAll("/$", "") + "/" + fileName;

        BlobId blobId = BlobId.of(bucketName, fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        log.info("Uploaded file to Firebase: {}", fullPath);
        return fullPath;
    }

    public byte[] getFile(String filePath) {
        try {
            Blob blob = storage.get(BlobId.of(bucketName, filePath));
            return blob.getContent();
        } catch (StorageException e) {
            log.error("File not found: {}", filePath);
            return null;
        }
    }

    public void deleteFile(String filePath) {
        storage.delete(BlobId.of(bucketName, filePath));
        log.info("Deleted file from Firebase: {}", filePath);
    }

    public String getPublicUrl(String filePath) {
        // Thay thế "/" bằng "%2F" để URL hoạt động đúng
        String escapedPath = filePath.replace("/", "%2F");
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName, escapedPath);
    }

    public String getFilePathFromUrl(String url) {
        if (url == null || !url.contains("/o/")) return null;
        try {
            String pathWithEscapedChars = url.split("/o/")[1].split("\\?")[0];
            return java.net.URLDecoder.decode(pathWithEscapedChars, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Lỗi khi trích xuất Path từ URL: {}", e.getMessage());
            return null;
        }
    }
}
