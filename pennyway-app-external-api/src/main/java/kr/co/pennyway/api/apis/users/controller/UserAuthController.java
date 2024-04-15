package kr.co.pennyway.api.apis.users.controller;

import kr.co.pennyway.api.apis.users.usecase.UserAuthUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserAuthController {
    private final UserAuthUseCase userAuthUseCase;

    @GetMapping("/sign-out")
    public ResponseEntity<?> signOut(
            @RequestHeader("Authorization") String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        userAuthUseCase.signOut(user.getUserId(), accessToken, refreshToken);
        return ResponseEntity.ok(SuccessResponse.noContent());
    }
}
