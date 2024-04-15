package kr.co.pennyway.api.apis.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[계정 검사 API]")
public interface AuthCheckApi {
    @Operation(summary = "닉네임 중복 검사")
    ResponseEntity<?> checkUsername(@RequestParam @Validated String username);
}
