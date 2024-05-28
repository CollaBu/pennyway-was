package kr.co.pennyway.infra.client.aws.s3;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;
import kr.co.pennyway.infra.config.AwsS3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Provider {
	private static final Set<String> extensionSet = Set.of("jpg", "png", "jpeg");

	private final AwsS3Config awsS3Config;
	private final S3Presigner s3Presigner;

	/**
	 * type에 해당하는 확장자를 가진 파일을 S3에 저장하기 위한 Presigned URL을 생성한다.
	 * @param type : ObjectKeyType (PROFILE, FEED, CHATROOM_PROFILE, CHAT, CHAT_PROFILE)
	 * @param ext : 파일 확장자 (jpg, png, jpeg)
	 * @param userId : 사용자 ID (PK) - PROFILE, CHAT_PROFILE
	 * @param chatroomId : 채팅방 ID (PK) - CHATROOM_PROFILE, CHAT, CHAT_PROFILE
	 * @return Presigned URL
	 * @throws Exception
	 */
	public URI generatedPresignedUrl(String type, String ext, String userId, String chatroomId) {
		try {
			if (!extensionSet.contains(ext)) {
				throw new StorageException(StorageErrorCode.INVALID_EXTENSION);
			}

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(awsS3Config.getBucketName())
					.key(generateObjectKey(type, ext, userId, chatroomId))
					.build();

			PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r.putObjectRequest(putObjectRequest)
					.signatureDuration(Duration.ofMinutes(10)));

			return presignedRequest.url().toURI();
		} catch (Exception e) {
			log.error("Presigned URL 생성 중 오류 발생", e);
			throw new StorageException(StorageErrorCode.MISSING_REQUIRED_PARAMETER);
		}
	}

	/**
	 * type에 해당하는 ObjectKeyTemplate을 적용하여 ObjectKey(S3에 저장하기 위한 정적 파일의 경로 및 이름)를 생성한다.
	 * @param type : ObjectKeyType (PROFILE, FEED, CHATROOM_PROFILE, CHAT, CHAT_PROFILE)
	 * @param ext : 파일 확장자 (jpg, png, jpeg)
	 * @param userId : 사용자 ID (PK) - PROFILE, CHAT_PROFILE
	 * @param chatroomId : 채팅방 ID (PK) - CHATROOM_PROFILE, CHAT, CHAT_PROFILE
	 * @return ObjectKey
	 */
	private String generateObjectKey(String type, String ext, String userId, String chatroomId) {
		ObjectKeyTemplate objectKeyTemplate = new ObjectKeyTemplate(ObjectKeyType.valueOf(type).getTemplate());
		Map<String, String> variables = generateObjectKeyVariables(type, ext, userId, chatroomId);
		String objectKey = objectKeyTemplate.apply(variables);
		return objectKey;

	}

	/**
	 * ObjectKey에 사용될 변수들을 Template에 적용하기 위한 Map에 담아 반환한다.
	 * @param type : ObjectKeyType (PROFILE, FEED, CHATROOM_PROFILE, CHAT, CHAT_PROFILE)
	 * @param ext : 파일 확장자 (jpg, png, jpeg)
	 * @param userId : 사용자 ID (PK) - PROFILE, CHAT_PROFILE
	 * @param chatroomId : 채팅방 ID (PK) - CHATROOM_PROFILE, CHAT, CHAT_PROFILE
	 * @return
	 */
	private Map<String, String> generateObjectKeyVariables(String type, String ext, String userId, String chatroomId) {
		ObjectKeyType objectType;
		try {
			objectType = ObjectKeyType.valueOf(type);
		} catch (IllegalArgumentException e) {
			throw new StorageException(StorageErrorCode.INVALID_TYPE);
		}

		UrlGenerator urlGenerator = UrlGeneratorFactory.getUrlGenerator(objectType);
		return urlGenerator.generate(type, ext, userId, chatroomId);
	}
}
