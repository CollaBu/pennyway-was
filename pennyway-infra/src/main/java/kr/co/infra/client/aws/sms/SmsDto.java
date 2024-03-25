package kr.co.infra.client.aws.sms;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

// FIXME: Naver Cloud Platform Snes 기준 DTO. AWS SNS 요청, 응답 포맷에 맞게 수정 필요
public class SmsDto {
    public record To(
            String phone
    ) {
        /**
         * @param phone String : SMS 인증 요청을 할 전화번호
         */
        public static To of(String phone) {
            return new To(phone);
        }
    }

    @Builder
    public record Request(
            String type,
            String contentType,
            String countryCode,
            String from,
            String content,
            List<To> messages
    ) {
        /**
         * AWS SNS API 요청 객체 생성
         *
         * @param type        String : SMS | LMS | MMS
         * @param contentType String : COMM | AD
         * @param countryCode String : 국가번호
         * @param from        String : 발신번호
         * @param content     String : 메시지 내용
         * @param messages    List<{@link To}> : 메시지 정보 (to: 수신번호, subject: 개별 메시지 제목, content: 개별 메시지 내용)
         */
        public static Request of(String type, String contentType, String countryCode, String from, String content, List<To> messages) {
            return Request.builder()
                    .type(type)
                    .contentType(contentType)
                    .countryCode(countryCode)
                    .from(from)
                    .content(content)
                    .messages(messages)
                    .build();
        }
    }

    /**
     * AWS SNS API 요청에 대한 응답 객체
     *
     * @param requestId  String : 요청 ID
     * @param requestAt  LocalDateTime : 요청 시간
     * @param statusCode String : 응답 코드
     * @param statusName String : 응답 상태
     */
    @Builder
    public record Response(
            String requestId,
            LocalDateTime requestAt,
            String statusCode,
            String statusName
    ) {
    }

    /**
     * 인증번호 전송 정보를 확인할 수 있는 DTO
     *
     * @param requestId  String : 요청 ID (NCP SMS API 요청 시 발급된 요청 ID)
     * @param code       String : 발급된 인증번호 정수 6자리 문자열
     * @param requestAt  LocalDateTime : 요청 시간
     * @param statusCode String : 응답 코드
     * @param statusName String : 응답 상태
     */
    @Builder
    public record Info(
            String requestId,
            String code,
            LocalDateTime requestAt,
            String statusCode,
            String statusName
    ) {
        /**
         * @param request {@link Response}
         * @param code    String : 인증 코드 정수 6자리 문자열
         */
        public static Info from(Response request, String code) {
            return Info.builder()
                    .requestId(request.requestId())
                    .code(code)
                    .requestAt(request.requestAt())
                    .statusCode(request.statusCode())
                    .statusName(request.statusName())
                    .build();
        }
    }
}
