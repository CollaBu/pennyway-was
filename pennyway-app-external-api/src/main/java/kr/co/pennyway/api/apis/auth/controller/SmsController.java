package kr.co.pennyway.api.apis.auth.controller;

import kr.co.pennyway.api.apis.auth.api.SmsApi;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.usecase.SmsUseCase;
import kr.co.pennyway.api.common.exception.PhoneVerificationErrorCode;
import kr.co.pennyway.api.common.exception.PhoneVerificationException;
import kr.co.pennyway.api.common.query.VerificationType;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/phone")
public class SmsController implements SmsApi {
    private final SmsUseCase smsUseCase;

    @Override
    @PostMapping("")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> sendCode(@RequestParam(value = "type") VerificationType type, @RequestParam(name = "provider", required = false) Provider provider, @RequestBody @Validated PhoneVerificationDto.PushCodeReq request) {
        if (type.equals(VerificationType.OAUTH) && provider == null) {
            throw new PhoneVerificationException(PhoneVerificationErrorCode.PROVIDER_IS_REQUIRED);
        }
        return ResponseEntity.ok(SuccessResponse.from("sms", smsUseCase.sendCode(request, type, provider)));
    }
}
