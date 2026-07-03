package com.project.BookCarOnline.Configuration;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {
    @Value("${firebase.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void initializeFirebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase/firebase-adminsdk.json");
            
            if (serviceAccount != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
    }

    @Bean
    public Storage storage() throws IOException {
        InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("firebase/firebase-adminsdk.json");

        return StorageOptions.newBuilder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
                .getService();
    }
}