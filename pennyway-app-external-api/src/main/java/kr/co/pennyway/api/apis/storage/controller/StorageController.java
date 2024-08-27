package kr.co.pennyway.api.apis.storage.controller;

import kr.co.pennyway.api.apis.storage.api.StorageApi;
import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.api.apis.storage.usecase.StorageUseCase;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/storage")
public class StorageController implements StorageApi {
    private final StorageUseCase storageUseCase;

    @Override
    @GetMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPresignedUrl(@Validated PresignedUrlDto.Req request, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(storageUseCase.getPresignedUrl(user.getUserId(), request));
    }
}
