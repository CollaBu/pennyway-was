package kr.co.pennyway.infra.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import jakarta.annotation.PostConstruct;
import kr.co.pennyway.infra.common.importer.PennywayInfraConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
public class FcmConfig implements PennywayInfraConfig {
    private final ClassPathResource firebaseResource;
    private final String projectId;

    public FcmConfig(@Value("${app.firebase.config.file}") String firebaseConfigPath,
                     @Value("${app.firebase.project.id}") String projectId) {
        this.firebaseResource = new ClassPathResource(firebaseConfigPath);
        this.projectId = projectId;
    }

    @PostConstruct
    public void init() throws IOException {
        FirebaseOptions option = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(firebaseResource.getInputStream()))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(option);
            log.info("FirebaseApp is initialized");
        }
    }
    
    @Bean
    FirebaseMessaging firebaseMessaging() {
        return FirebaseMessaging.getInstance(FirebaseApp.getInstance());
    }
}
