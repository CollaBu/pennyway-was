package kr.co.pennyway.infra.client.aws.sms;

import kr.co.pennyway.infra.common.event.PhoneVerificationEvent;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;

public class SmsDto {
    public record Request(
            String phone,
            String code
    ) {
        public static Request from(PhoneVerificationEvent e) {
            return new Request(e.phone(), e.code());
        }

        public String parsePhone() {
            return "+82" + phone.replaceAll("-", "");
        }
    }

    /**
     * AWS SNS API 요청에 대한 응답 객체
     *
     * @param requestId String : 요청 ID
     * @param requestAt LocalDateTime : 요청 시간
     */
    @Builder
    public record Info(
            String requestId,
            String code,
            LocalDateTime requestAt
    ) {
        public Info {
            Objects.requireNonNull(requestId);
            Objects.requireNonNull(code);
            Objects.requireNonNull(requestAt);
        }

        public static Info of(String requestId, String code, LocalDateTime requestAt) {
            return new Info(requestId, code, requestAt);
        }
    }
}
