package com.upsglam.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${app.firebase.service-account-path}")
    private String serviceAccountPath;

    @PostConstruct
    public void initialize() throws IOException {

        InputStream serviceAccount = getClass()
                .getClassLoader()
                .getResourceAsStream(serviceAccountPath.replace("classpath:", ""));

        if (serviceAccount == null) {
            throw new IllegalStateException("NO SE ENCONTRÓ EL ARCHIVO: " + serviceAccountPath);
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
            System.out.println("Firebase inicializado correctamente.");
        }
    }
}
