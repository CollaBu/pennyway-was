package kr.co.pennyway.api.apis.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.usecase.AuthUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@Tag(name = "[인증 API]")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthUseCase authUseCase;

    @Operation(summary = "일반 회원가입")
    @PostMapping("/sign-up")
    // TODO: Spring Security 설정 후 @PreAuthorize("isAnonymous()") 추가
    public ResponseEntity<?> signUp(@RequestBody @Validated SignUpReq.General request) {
        Pair<Long, Jwts> jwts = authUseCase.signUp(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwts.getValue().refreshToken())
                .header(HttpHeaders.AUTHORIZATION, jwts.getValue().accessToken())
                .body(SuccessResponse.from("user", Map.of("id", jwts.getKey())))
                ;
    }
}
