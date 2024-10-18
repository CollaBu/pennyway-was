package kr.co.pennyway.api.apis.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.net.URI;

public class PresignedUrlDto {
    @Schema(title = "S3 이미지 저장을 위한 Presigned URL 발급 요청 DTO", description = "S3에 이미지를 저장하기 위한 Presigned URL을 발급 요청을 위한 DTO")
    public record Req(
            @Schema(description = "이미지 종류", example = "PROFILE/FEED/CHATROOM_PROFILE/CHAT/CHAT_PROFILE")
            @NotNull(message = "이미지 종류는 필수입니다.")
            ObjectKeyType type,
            @Schema(description = "파일 확장자", example = "jpg/png/jpeg")
            @NotBlank(message = "파일 확장자는 필수입니다.")
            @Pattern(regexp = "^(jpg|png|jpeg)$", message = "파일 확장자는 jpg, png, jpeg 중 하나여야 합니다.")
            String ext,
            @Schema(description = "채팅방 ID", example = "123456789")
            Long chatroomId,
            @Schema(description = "채팅 ID", example = "123456789")
            Long chatId,
            @Schema(description = "피드 ID", example = "123456789")
            Long feedId
    ) {
    }

    @Schema(title = "S3 이미지 저장을 위한 Presigned URL 발급 응답 DTO")
    public record Res(
            @Schema(description = "Presigned URL")
            URI presignedUrl
    ) {
        /**
         * Presigned URL 발급 응답 객체 생성
         *
         * @param presignedUrl String : Presigned URL
         */
        public static Res of(URI presignedUrl) {
            return new Res(presignedUrl);
        }
    }
}
