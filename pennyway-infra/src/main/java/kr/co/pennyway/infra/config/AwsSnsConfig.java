package kr.co.pennyway.infra.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Getter
@Configuration
public class AwsSnsConfig {
    private final String accessKey;
    private final String secretKey;
    private final String region;

    public AwsSnsConfig(
            @Value("${spring.cloud.aws.sns.credentials.access-key}") String accessKey,
            @Value("${spring.cloud.aws.sns.credentials.secret-key}") String secretKey,
            @Value("${spring.cloud.aws.sns.region.static}") String region
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
    }

    @Bean
    public AwsCredentials awsSnsCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public SnsClient awsSnsClient() {
        return SnsClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsSnsCredentials()))
                .region(Region.of(region))
                .build();
    }
}
