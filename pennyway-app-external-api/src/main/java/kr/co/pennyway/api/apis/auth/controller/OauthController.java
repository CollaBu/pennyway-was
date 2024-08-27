package kr.co.pennyway.api.apis.auth.controller;

import kr.co.pennyway.api.apis.auth.api.OauthApi;
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
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth/oauth")
public class OauthController implements OauthApi {
    private final OauthUseCase oauthUseCase;
    private final CookieUtil cookieUtil;

    @Override
    @PostMapping("/sign-in")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> signIn(@RequestParam Provider provider, @RequestBody @Validated SignInReq.Oauth request) {
        Pair<Long, Jwts> userInfo = oauthUseCase.signIn(provider, request);

        if (userInfo.getLeft().equals(-1L)) {
            return ResponseEntity.ok(SuccessResponse.from("user", Map.of("id", -1)));
        }
        return createAuthenticatedResponse(userInfo);
    }

    @Override
    @PostMapping("/phone/verification")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> verifyCode(@RequestParam Provider provider, @RequestBody @Validated PhoneVerificationDto.VerifyCodeReq request) {
        return ResponseEntity.ok(SuccessResponse.from("sms", oauthUseCase.verifyCode(provider, request)));
    }

    @Override
    @PostMapping("/link-auth")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> linkAuth(@RequestParam Provider provider, @RequestBody @Validated SignUpReq.SyncWithAuth request) {
        return createAuthenticatedResponse(oauthUseCase.signUp(provider, request.toOauthInfo()));
    }

    @Override
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
