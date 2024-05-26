package kr.co.pennyway.infra.client.aws.s3;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import kr.co.pennyway.common.util.UUIDUtil;
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

	public URI generatedPresignedUrl(String type, String ext, String userId, String chatId, String chatroomId) {
		try {
			if (!extensionSet.contains(ext)) {
				throw new IllegalArgumentException("지원하지 않는 확장자입니다.");
			}

			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(awsS3Config.getBucketName())
					.key(generateObjectKey(type, ext, userId, chatId, chatroomId))
					.build();

			PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r.putObjectRequest(putObjectRequest)
					.signatureDuration(Duration.ofMinutes(10)));

			return presignedRequest.url().toURI();
		} catch (Exception e) {
			log.error("S3 PreSigned URL 생성 실패: {}", e.getMessage());
			throw new RuntimeException("S3 PreSigned URL 생성에 실패했습니다.");
		}
	}

	/**
	 * ObjectKey를 생성한다.
	 * @param type
	 * @param ext
	 * @param userId
	 * @param chatId
	 * @param chatroomId
	 * @return
	 */
	private String generateObjectKey(String type, String ext, String userId, String chatId, String chatroomId) {
		ObjectKeyTemplate objectKeyTemplate = new ObjectKeyTemplate(ObjectKeyType.valueOf(type).getTemplate());
		Map<String, String> variables = generateObjectKeyVariables(type, ext, userId, chatId, chatroomId);
		String objectKey = objectKeyTemplate.apply(variables);
		return objectKey;

	}

	/**
	 * ObjectKey에 사용될 변수들을 생성한다.
	 * @param type
	 * @param ext
	 * @param userId
	 * @param chatId
	 * @param chatroomId
	 * @return
	 */
	private Map<String, String> generateObjectKeyVariables(String type, String ext, String userId, String chatId, String chatroomId) {
		Map<String, String> variablesMap = new HashMap<>();
		variablesMap.put("uuid", UUIDUtil.generateUUID());
		variablesMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
		variablesMap.put("ext", ext);
		switch (ObjectKeyType.valueOf(type)) {
			case PROFILE:
				if (userId == null) {
					throw new IllegalArgumentException("userId는 필수입니다.");
				}
				variablesMap.put("userId", userId);
				variablesMap.put("uuid", UUIDUtil.generateUUID());
				break;
			case FEED:
				variablesMap.put("feed_id", UUIDUtil.generateUUID());
				break;
			case CHATROOM_PROFILE:
				if (chatroomId == null) {
					chatroomId = UUIDUtil.generateUUID();
				}
				variablesMap.put("chatroom_id", chatroomId);
				break;
			case CHAT:
				variablesMap.put("chatroom_id", chatroomId);
				variablesMap.put("chat_id", UUIDUtil.generateUUID());
				break;
			case CHAT_PROFILE:
				if (userId == null) {
					throw new IllegalArgumentException("userId는 필수입니다.");
				}
				if (chatroomId == null) {
					chatroomId = UUIDUtil.generateUUID();
				}
				variablesMap.put("chatroom_id", chatroomId);
				variablesMap.put("user_id", userId);
				break;
		}
		return variablesMap;
	}
}
