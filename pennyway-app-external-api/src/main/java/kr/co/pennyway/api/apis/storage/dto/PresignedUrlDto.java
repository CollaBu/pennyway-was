package kr.co.pennyway.api.apis.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class PresignedUrlDto {
	@Schema(title = "S3 이미지 저장을 위한 Presigned URL 발급 요청 DTO", description = "S3에 이미지를 저장하기 위한 Presigned URL을 발급 요청을 위한 DTO")
	public record PresignedUrlReq(
			@Schema(description = "이미지 종류", example = "PROFILE/FEED/CHATROOM_PROFILE/CHAT/CHAT_PROFILE")
			@NotBlank(message = "이미지 종류는 필수입니다.")
			String type,
			@Schema(description = "파일 확장자", example = "jpg/png/jpeg")
			@NotBlank(message = "파일 확장자는 필수입니다.")
			String ext,
			@Schema(description = "사용자 ID", example = "1")
			String userId,
			@Schema(description = "피드 ID", example = "12345678-1234-5678-1234-567812345678")
			String chatroomId,
			@Schema(description = "채팅 ID", example = "12345678-1234-5678-1234-567812345678")
			String chatId
	) {
	}

	@Schema(title = "S3 이미지 저장을 위한 Presigned URL 발급 응답 DTO", description = "S3에 이미지를 저장하기 위한 Presigned URL을 발급 응답을 위한 DTO")
	public record PresignedUrlRes(
			@Schema(description = "Presigned URL")
			String presignedUrl
	) {
		/**
		 * Presigned URL 발급 응답 객체 생성
		 *
		 * @param presignedUrl String : Presigned URL
		 */
		public static PresignedUrlRes of(String presignedUrl) {
			return new PresignedUrlRes(presignedUrl);
		}
	}
}
