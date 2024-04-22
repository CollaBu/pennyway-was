package kr.co.pennyway.api.apis.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.api.apis.auth.api.AuthCheckApi;
import kr.co.pennyway.api.apis.auth.usecase.AuthCheckUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class AuthCheckController implements AuthCheckApi {
	private final AuthCheckUseCase authCheckUseCase;

	@GetMapping("/duplicate/username")
	@PreAuthorize("permitAll()")
	public ResponseEntity<?> checkUsername(@RequestParam @Validated String username) {
		return ResponseEntity.ok(
				SuccessResponse.from("isDuplicate",
						authCheckUseCase.checkUsernameDuplicate(username)));
	}

	@GetMapping("/find/username")
	@PreAuthorize("isAnonymous()")
	public ResponseEntity<?> findUsername(@RequestParam @NotBlank String phone, @RequestParam @NotBlank String code) {
		return ResponseEntity.ok(SuccessResponse.from("user", authCheckUseCase.findUsername(phone, code)));
	}
}
