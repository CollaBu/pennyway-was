package kr.co.pennyway.api.apis.auth.controller;

import jakarta.validation.Valid;
import kr.co.pennyway.api.apis.auth.api.AuthApi;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.usecase.AuthUseCase;
import kr.co.pennyway.api.common.aop.RequestHeaderLog;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.api.common.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController implements AuthApi {
    private final AuthUseCase authUseCase;
    private final CookieUtil cookieUtil;

    @PostMapping("/phone/verification")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> verifyCode(@RequestBody @Validated PhoneVerificationDto.VerifyCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("sms", authUseCase.verifyCode(request)));
    }

    @PostMapping("/sign-up")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signUp(@RequestBody @Validated SignUpReq.General request) {
        return createAuthenticatedResponse(authUseCase.signUp(request.toInfo()));
    }

    @PostMapping("/link-oauth")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> linkOauth(@RequestBody @Validated SignUpReq.SyncWithOauth request) {
        return createAuthenticatedResponse(authUseCase.signUp(request.toInfo()));
    }

    @PostMapping("/sign-in")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signIn(@RequestBody @Validated SignInReq.General request) {
        return createAuthenticatedResponse(authUseCase.signIn(request));
    }

    @GetMapping("/refresh")
    @RequestHeaderLog(hasCookie = true)
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> refresh(@CookieValue("refreshToken") @Valid String refreshToken) {
        return createAuthenticatedResponse(authUseCase.refresh(refreshToken));
    }

    private ResponseEntity<?> createAuthenticatedResponse(Pair<Long, Jwts> userInfo) {
        ResponseCookie cookie = cookieUtil.createCookie("refreshToken", userInfo.getValue().refreshToken(), Duration.ofDays(7).toSeconds());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.AUTHORIZATION, userInfo.getValue().accessToken())
                .body(SuccessResponse.from("user", Map.of("id", userInfo.getKey())))
                ;
    }
}
