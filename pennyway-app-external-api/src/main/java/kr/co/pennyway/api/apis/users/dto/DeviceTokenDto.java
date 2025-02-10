package kr.co.pennyway.api.apis.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.user.domain.User;

public class DeviceTokenDto {
    @Schema(title = "디바이스 등록 요청")
    public record RegisterReq(
            @Schema(description = "디바이스 FCM 토큰", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "token은 필수입니다.")
            String token,
            @Schema(description = "디바이스 ID", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "deviceId는 필수입니다.")
            String deviceId,
            @Schema(description = "디바이스 이름", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank(message = "deviceName은 필수입니다.")
            String deviceName
    ) {
        public DeviceToken toEntity(User user) {
            return DeviceToken.of(token, deviceId, deviceName, user);
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
