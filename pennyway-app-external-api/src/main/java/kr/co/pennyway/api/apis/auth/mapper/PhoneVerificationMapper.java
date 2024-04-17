package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.common.exception.PhoneVerificationErrorCode;
import kr.co.pennyway.api.common.exception.PhoneVerificationException;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
import kr.co.pennyway.infra.common.event.PushCodeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Mapper
@RequiredArgsConstructor
public class PhoneVerificationMapper {
    private final PhoneVerificationService phoneVerificationService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 휴대폰 번호로 인증 코드를 발송하고 캐싱한다. (5분간 유효)
     *
     * @param request  {@link PhoneVerificationDto.PushCodeReq}
     * @param codeType {@link PhoneVerificationType}
     * @return {@link PhoneVerificationDto.PushCodeRes}
     */
    public PhoneVerificationDto.PushCodeRes sendCode(PhoneVerificationDto.PushCodeReq request, PhoneVerificationType codeType) {
        String code = issueVerificationCode();
        LocalDateTime expiresAt = phoneVerificationService.create(request.phone(), code, codeType);

        eventPublisher.publishEvent(PushCodeEvent.of(request.phone(), code));

        return PhoneVerificationDto.PushCodeRes.of(request.phone(), LocalDateTime.now(), expiresAt);
    }

    /**
     * 휴대폰 번호로 인증 코드를 확인한다.
     *
     * @param request  {@link PhoneVerificationDto.VerifyCodeReq}
     * @param codeType {@link PhoneVerificationType}
     * @return Boolean : 인증 코드가 유효한지 여부 (TRUE: 유효, 실패하는 경우 예외가 발생하므로 FALSE가 반환되지 않음)
     * @throws PhoneVerificationException : 전화번호가 만료되었거나 유효하지 않은 경우(EXPIRED_OR_INVALID_PHONE), 인증 코드가 유효하지 않은 경우(IS_NOT_VALID_CODE)
     */
    public Boolean isValidCode(PhoneVerificationDto.VerifyCodeReq request, PhoneVerificationType codeType) {
        String expectedCode;
        try {
            expectedCode = phoneVerificationService.readByPhone(request.phone(), codeType);
        } catch (IllegalArgumentException e) {
            throw new PhoneVerificationException(PhoneVerificationErrorCode.EXPIRED_OR_INVALID_PHONE);
        }

        if (!expectedCode.equals(request.code()))
            throw new PhoneVerificationException(PhoneVerificationErrorCode.IS_NOT_VALID_CODE);
        return Boolean.TRUE;
    }

    private String issueVerificationCode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }
}
