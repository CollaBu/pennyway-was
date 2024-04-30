package kr.co.pennyway.api.apis.auth.controller;

import kr.co.pennyway.api.apis.auth.api.UserAuthApi;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.usecase.UserAuthUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.common.util.CookieUtil;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class UserAuthController implements UserAuthApi {
    private final UserAuthUseCase userAuthUseCase;
    private final CookieUtil cookieUtil;

    @Override
    @GetMapping("/auth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAuthState(@RequestHeader(value = "Authorization") String authHeader) {
        return ResponseEntity.ok(SuccessResponse.from("user", userAuthUseCase.isSignIn(authHeader)));
    }

    @Override
    @GetMapping("/sign-out")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> signOut(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        String accessToken = authHeader.split(" ")[1];
        userAuthUseCase.signOut(user.getUserId(), accessToken, refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.deleteCookie("refreshToken").toString())
                .body(SuccessResponse.noContent());
    }

    @Override
    @PutMapping("/link-oauth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> linkOauth(@RequestParam Provider provider, @RequestBody @Validated SignInReq.Oauth request, @AuthenticationPrincipal SecurityUserDetails user) {
        userAuthUseCase.linkOauth(provider, request, user.getUserId());
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
