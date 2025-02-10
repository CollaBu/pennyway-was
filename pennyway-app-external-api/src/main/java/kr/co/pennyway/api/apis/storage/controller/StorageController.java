package kr.co.pennyway.api.apis.storage.controller;

import kr.co.pennyway.api.apis.storage.api.StorageApi;
import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.api.apis.storage.usecase.StorageUseCase;
import kr.co.pennyway.api.common.exception.CustomValidationException;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.api.common.validator.PresignedUrlDtoReqValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/storage")
public class StorageController implements StorageApi {
    private final StorageUseCase storageUseCase;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(new PresignedUrlDtoReqValidator());
    }

    @Override
    @GetMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPresignedUrl(@Validated PresignedUrlDto.Req request, BindingResult bindingResult, @AuthenticationPrincipal SecurityUserDetails user) {
        if (bindingResult.hasErrors()) {
            throw new CustomValidationException(bindingResult);
        }

        return ResponseEntity.ok(storageUseCase.getPresignedUrl(user.getUserId(), request));
    }
}
