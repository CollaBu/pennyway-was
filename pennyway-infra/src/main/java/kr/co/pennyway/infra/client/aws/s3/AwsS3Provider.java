package kr.co.pennyway.infra.client.aws.s3;

import java.net.URI;
import java.time.Duration;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Provider {
	private final S3Presigner s3Presigner;

	public URI generatedPresignedUrl(String BucketName, String objectKey) {
		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(BucketName)
					.key(objectKey)
					.build();

			PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r.putObjectRequest(putObjectRequest)
					.signatureDuration(Duration.ofMinutes(10)));

			return presignedRequest.url().toURI();
		} catch (Exception e) {
			log.error("S3 PreSigned URL 생성 실패: {}", e.getMessage());
			throw new RuntimeException("S3 PreSigned URL 생성에 실패했습니다.");
		}
	}
}
