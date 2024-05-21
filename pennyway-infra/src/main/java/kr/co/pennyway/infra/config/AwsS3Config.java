package kr.co.pennyway.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Getter
@Configuration
public class AwsS3Config {
	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
				.region(Region.AP_NORTHEAST_2)
				.credentialsProvider(ProfileCredentialsProvider.create())
				.build();
	}
}
