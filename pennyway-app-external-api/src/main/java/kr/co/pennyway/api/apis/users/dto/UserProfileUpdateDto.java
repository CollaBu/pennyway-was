package kr.co.pennyway.api.apis.users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;

public class UserProfileUpdateDto {
    @Schema(title = "사용자 알림 설정 응답 DTO")
    public record NotifySettingUpdateReq(
            @Schema(description = "계좌 알림 설정", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean accountBookNotify,
            @Schema(description = "피드 알림 설정", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean feedNotify,
            @Schema(description = "채팅 알림 설정", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            Boolean chatNotify
    ) {
        public static NotifySettingUpdateReq of(NotifySetting.NotifyType type, Boolean flag) {
            return switch (type) {
                case ACCOUNT_BOOK -> new NotifySettingUpdateReq(flag, null, null);
                case FEED -> new NotifySettingUpdateReq(null, flag, null);
                case CHAT -> new NotifySettingUpdateReq(null, null, flag);
            };
        }
    }
}
