package kr.co.pennyway.api.apis.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.user.domain.User;

public class DeviceDto {
    @Schema(title = "디바이스 등록 요청")
    public record RegisterReq(
            @Schema(description = "디바이스 FCM 토큰", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "token은 필수입니다.")
            String token
    ) {
        public Device toEntity(User user) {
            return Device.of(token, user);
        }
    }

    @Schema(title = "디바이스 등록 응답")
    public record RegisterRes(
            @Schema(title = "디바이스 ID")
            Long id,
            @Schema(title = "디바이스 토큰")
            String token
    ) {
        public static RegisterRes of(Long id, String token) {
            return new RegisterRes(id, token);
        }
    }
}
