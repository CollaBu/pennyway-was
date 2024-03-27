package kr.co.pennyway.api.apis.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.usecase.AuthUseCase;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Tag(name = "[인증 API]")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {
    private final AuthUseCase authUseCase;
    private final CookieUtil cookieUtil;

    @Operation(summary = "인증번호 전송")
    @PostMapping("/phone")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> sendCode(@RequestBody @Validated PhoneVerificationDto.PushCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("sms", authUseCase.sendCode(request)));
    }

    @Operation(summary = "인증번호 검증")
    @PostMapping("/phone/verification")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> verifyCode(@RequestBody @Validated PhoneVerificationDto.VerifyCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("sms", authUseCase.verifyCode(request)));
    }

    @Operation(summary = "일반 회원가입")
    @PostMapping("/sign-up")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signUp(@RequestBody @Validated SignUpReq.General request) {
        Pair<Long, Jwts> jwts = authUseCase.signUp(request.toInfo());
        ResponseCookie cookie = cookieUtil.createCookie("refreshToken", jwts.getValue().refreshToken(), Duration.ofDays(7).toSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.AUTHORIZATION, jwts.getValue().accessToken())
                .body(SuccessResponse.from("user", Map.of("id", jwts.getKey())))
                ;
    }

    @Operation(summary = "일반 로그인")
    @PostMapping("/sign-in")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signIn(@RequestBody @Validated SignInReq.General request) {
        Pair<Long, Jwts> jwts = authUseCase.signIn(request);
        ResponseCookie cookie = cookieUtil.createCookie("refreshToken", jwts.getValue().refreshToken(), Duration.ofDays(7).toSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.AUTHORIZATION, jwts.getValue().accessToken())
                .body(SuccessResponse.from("user", Map.of("id", jwts.getKey())))
                ;
    }
}
