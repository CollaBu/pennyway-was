package kr.co.pennyway.api.apis.storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;

import java.net.URI;

public class PresignedUrlDto {
    public record Req(
            @NotNull(message = "이미지 종류는 필수입니다.")
            ObjectKeyType type,
            @NotBlank(message = "파일 확장자는 필수입니다.")
            @Pattern(regexp = "^(jpg|png|jpeg)$", message = "파일 확장자는 jpg, png, jpeg 중 하나여야 합니다.")
            String ext,
            Long chatroomId,
            Long chatId,
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
