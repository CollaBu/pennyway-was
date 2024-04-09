package kr.co.pennyway.infra.client.aws.sms;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Objects;

// FIXME: Naver Cloud Platform Snes 기준 DTO. AWS SNS 요청, 응답 포맷에 맞게 수정 필요
public class SmsDto {
    public record To(
            String phone
    ) {
        /**
         * @param phone String : SMS 인증 요청을 할 전화번호 (ex. 010-1234-5678)
         */
        public static To of(String phone) {
            return new To(phone);
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
