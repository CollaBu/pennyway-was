package kr.co.pennyway.api.apis.auth.controller;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.mapper.PhoneVerificationMapper;
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
public class SmsController {
    private final PhoneVerificationMapper phoneVerificationMapper;

    @Override
    @PostMapping("/phone")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> sendCode(@RequestParam VerificationType type, @RequestParam(required = false) Provider provider, @RequestBody @Validated PhoneVerificationDto.PushCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("sms", phoneVerificationMapper.sendCode(request, type.toPhoneVerificationType(provider))));
    }
}
