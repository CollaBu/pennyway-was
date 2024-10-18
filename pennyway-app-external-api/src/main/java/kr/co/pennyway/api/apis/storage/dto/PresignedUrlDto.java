package kr.co.pennyway.api.apis.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.net.URI;
import java.util.Objects;

public class PresignedUrlDto {
    @Schema(title = "S3 이미지 저장을 위한 Presigned URL 발급 요청 DTO", description = "S3에 이미지를 저장하기 위한 Presigned URL을 발급 요청을 위한 DTO")
    public record Req(
            @Schema(description = "이미지 종류", example = "PROFILE/FEED/CHATROOM_PROFILE/CHAT/CHAT_PROFILE")
            @NotNull(message = "이미지 종류는 필수입니다.")
            ObjectKeyType type,
            @Schema(description = "파일 확장자", example = "jpg/png/jpeg")
            @NotBlank(message = "파일 확장자는 필수입니다.")
            String ext,
            @Schema(description = "채팅방 ID", example = "12345678-1234-5678-1234-567812345678")
            String chatroomId,
            @Schema(description = "채팅 ID", example = "12345678-1234-5678-1234-567812345678")
            String chatId,
            @Schema(description = "피드 ID", example = "12345678-1234-5678-1234-567812345678")
            String feedId
    ) {
        public Req {
            if (ObjectKeyType.CHATROOM_PROFILE.equals(type) && Objects.isNull(chatroomId)) {
                throw new IllegalArgumentException("채팅방 이미지를 위해 채팅방 ID는 필수입니다.");
            }
            if (ObjectKeyType.CHAT_PROFILE.equals(type) && Objects.isNull(chatroomId)) {
                throw new IllegalArgumentException("채팅 프로필 이미지를 위해 채팅방 ID는 필수입니다.");
            }
            if (ObjectKeyType.CHAT.equals(type) && (Objects.isNull(chatroomId) || Objects.isNull(chatId))) {
                throw new IllegalArgumentException("채팅 이미지를 위해 채팅방 ID와 채팅 ID는 필수입니다.");
            }
            if (ObjectKeyType.FEED.equals(type) && Objects.isNull(feedId)) {
                throw new IllegalArgumentException("피드 이미지를 위해 피드 ID는 필수입니다.");
            }
        }
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
