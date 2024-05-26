package kr.co.pennyway.api.apis.storage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.pennyway.api.apis.storage.api.StorageApi;
import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.api.apis.storage.usecase.StorageUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/storage")
public class StorageController implements StorageApi {
	private final StorageUseCase storageUseCase;

	@Override
	@GetMapping("/presigned-url")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> getPresignedUrl(@Validated PresignedUrlDto.PresignedUrlReq request) {
		return ResponseEntity.ok(SuccessResponse.from(storageUseCase.getPresignedUrl(request)));
	}
}
