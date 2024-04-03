package kr.co.pennyway.api.apis.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.usecase.OauthUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.api.common.util.CookieUtil;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
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
@Tag(name = "[소셜 인증 API]")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth/oauth")
public class OauthController {
    private final OauthUseCase oauthUseCase;
    private final CookieUtil cookieUtil;

    @Operation(summary = "[1] 소셜 로그인", description = "기존에 Provider로 가입한 사용자는 로그인, 가입하지 않은 사용자는 전화번호 인증으로 이동")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @PostMapping("/sign-in")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signIn(@RequestParam Provider provider, @RequestBody @Validated SignInReq.Oauth request) {
        Pair<Long, Jwts> userInfo = oauthUseCase.signIn(provider, request);

        if (userInfo.getLeft().equals(-1L)) {
            return ResponseEntity.ok(SuccessResponse.from("message", "전화번호 인증 진행")); // TODO: 응답 수정
        }
        return createAuthenticatedResponse(userInfo);
    }

    @Operation(summary = "[2] 인증번호 발송", description = "전화번호 입력 후 인증번호 발송")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @PostMapping("/phone")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> sendCode(@RequestParam Provider provider, @RequestBody @Validated PhoneVerificationDto.PushCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("sms", oauthUseCase.sendCode(provider, request)));
    }

    @Operation(summary = "[3] 전화번호 인증", description = "전화번호 인증 후 이미 계정이 존재하면 연동, 없으면 회원가입")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @PostMapping("/phone/verification")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> verifyCode(@RequestParam Provider provider, @RequestBody @Validated PhoneVerificationDto.VerifyCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("sms", oauthUseCase.verifyCode(provider, request)));
    }

    @Operation(summary = "[4-1] 계정 연동", description = "일반 혹은 소셜 계정이 존재하는 경우 연동")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @PostMapping("/link-auth")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> linkAuth(@RequestParam Provider provider, @RequestBody @Validated SignUpReq.SyncWithAuth request) {
        return createAuthenticatedResponse(oauthUseCase.signUp(provider, request.toOauthInfo()));
    }

    @Operation(summary = "[4-2] 소셜 회원가입", description = "회원 정보 입력 후 회원가입")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @PostMapping("/sign-up")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signUp(@RequestParam Provider provider, @RequestBody @Validated SignUpReq.Oauth request) {
        return createAuthenticatedResponse(oauthUseCase.signUp(provider, request.toOauthInfo()));
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
