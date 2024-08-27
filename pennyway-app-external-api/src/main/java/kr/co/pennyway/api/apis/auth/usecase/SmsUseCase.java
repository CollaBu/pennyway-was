package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.api.common.query.VerificationType;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SmsUseCase {
    private final PhoneVerificationService phoneVerificationService;

    public PhoneVerificationDto.PushCodeRes sendCode(PhoneVerificationDto.PushCodeReq request, VerificationType type, Provider provider) {
        return phoneVerificationService.sendCode(request, type.toPhoneVerificationType(provider));
    }
}
