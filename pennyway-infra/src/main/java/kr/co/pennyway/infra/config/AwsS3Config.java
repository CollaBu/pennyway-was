package kr.co.pennyway.infra.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Getter
@Configuration
public class AwsS3Config {
    private final String accessKey;
    private final String secretKey;
    private final String region;
    private final String bucketName;
    private final String objectPrefix;

    public AwsS3Config(
            @Value("${spring.cloud.aws.s3.credentials.access-key}") String accessKey,
            @Value("${spring.cloud.aws.s3.credentials.secret-key}") String secretKey,
            @Value("${spring.cloud.aws.s3.region.static}") String region,
            @Value("${spring.cloud.aws.s3.bucket.name}") String bucketName,
            @Value("${spring.cloud.aws.cloudfront.domain}") String objectPrefix
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.region = region;
        this.bucketName = bucketName;
        this.objectPrefix = objectPrefix;
    }

    public String getBucketName() {
        return bucketName;
    }

    @Bean
    public AwsCredentials awsS3Credentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsS3Credentials()))
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsS3Credentials()))
                .build();
    }
}
