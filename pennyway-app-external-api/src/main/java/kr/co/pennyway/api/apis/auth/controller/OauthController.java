package kr.co.pennyway.api.apis.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // [1] 소셜 로그인
    // 등록된 provier -> login
    // 등록되지 않은 provider -> 전화번호 인증
    @Operation(summary = "소셜 로그인")
    @Parameter(name = "provider", description = "소셜 제공자", examples = {
            @ExampleObject(name = "카카오", value = "kakao"), @ExampleObject(name = "애플", value = "apple"), @ExampleObject(name = "구글", value = "google")
    }, required = true, in = ParameterIn.QUERY)
    @RequestMapping("/sign-in")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signIn(@RequestParam Provider provider, @RequestBody @Validated SignInReq.Oauth request) {
        Pair<Long, Jwts> userInfo = oauthUseCase.signIn(provider, request);

        if (userInfo.getLeft().equals(-1L)) {
            return ResponseEntity.ok(SuccessResponse.from("message", "회원가입 진행"));
        }
        return createAuthenticatedResponse(userInfo);
    }

    // [2] 전화번호 인증
    // 전화번호 인증 -> 계정 존재하면 연동 -> 로그인
    // 전화번호 인증 -> 계정 없으면 회원가입

    // [3] 소셜 회원가입
    // 회원 정보 입력(이름, 아이디) -> 회원가입 -> 로그인


    private ResponseEntity<?> createAuthenticatedResponse(Pair<Long, Jwts> userInfo) {
        ResponseCookie cookie = cookieUtil.createCookie("refreshToken", userInfo.getValue().refreshToken(), Duration.ofDays(7).toSeconds());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.AUTHORIZATION, userInfo.getValue().accessToken())
                .body(SuccessResponse.from("user", Map.of("id", userInfo.getKey())))
                ;
    }
}
