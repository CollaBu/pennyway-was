package kr.co.pennyway.api.apis.auth.controller;

import kr.co.pennyway.api.apis.auth.api.AuthCheckApi;
import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.usecase.AuthCheckUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class AuthCheckController implements AuthCheckApi {
    private final AuthCheckUseCase authCheckUseCase;

    @GetMapping("/duplicate/username")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> checkUsername(@RequestParam @Validated String username) {
        return ResponseEntity.ok(
                SuccessResponse.from("isDuplicate",
                        authCheckUseCase.checkUsernameDuplicate(username)));
    }

    @GetMapping("/find/username")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> findUsername(@Validated PhoneVerificationDto.VerifyCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("user", authCheckUseCase.findUsername(request)));
    }

    @PostMapping("/find/password/verification")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> verifyCodeForPassword(@RequestBody @Validated PhoneVerificationDto.VerifyCodeReq request) {
        authCheckUseCase.verifyCode(request);

        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    @PatchMapping("/find/password")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> findPassword(@RequestBody @Validated AuthFindDto.UpdatePasswordReq request) {
        PhoneVerificationDto.VerifyCodeReq codeReq = new PhoneVerificationDto.VerifyCodeReq(request.phone(), request.code());

        authCheckUseCase.findPassword(codeReq, request.newPassword());
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
