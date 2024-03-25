package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.common.exception.PhoneVerificationErrorCode;
import kr.co.pennyway.api.common.exception.PhoneVerificationException;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationCode;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.infra.client.aws.sms.SmsDto;
import kr.co.pennyway.infra.client.aws.sms.SmsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Mapper
@RequiredArgsConstructor
public class PhoneVerificationMapper {
    private final PhoneVerificationService phoneVerificationService;
    private final SmsProvider smsProvider;

    public PhoneVerificationDto.PushCodeRes sendCode(PhoneVerificationDto.PushCodeReq request, PhoneVerificationCode codeType) {
        SmsDto.Info info = smsProvider.sendCode(SmsDto.To.of(request.phone()));
        LocalDateTime expiresAt = phoneVerificationService.create(request.phone(), info.code(), codeType);
        return PhoneVerificationDto.PushCodeRes.of(request.phone(), info.requestAt(), expiresAt);
    }

    public Boolean isValidCode(PhoneVerificationDto.VerifyCodeReq request, PhoneVerificationCode codeType) {
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
}
