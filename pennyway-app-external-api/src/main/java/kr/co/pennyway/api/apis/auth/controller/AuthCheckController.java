package kr.co.pennyway.api.apis.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.pennyway.api.apis.auth.usecase.AuthCheckUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "[계정 검사 API]")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/duplicate")
public class AuthCheckController {
    private final AuthCheckUseCase authCheckUseCase;

    @Operation(summary = "닉네임 중복 검사")
    @GetMapping("/username")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<?> checkUsername(@RequestParam @Validated String username) {
        return ResponseEntity.ok(SuccessResponse.from("isDuplicate", authCheckUseCase.checkUsernameDuplicate(username)));
    }
}
