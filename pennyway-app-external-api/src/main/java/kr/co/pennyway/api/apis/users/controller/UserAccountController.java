package kr.co.pennyway.api.apis.users.controller;

import jakarta.validation.constraints.NotBlank;
import kr.co.pennyway.api.apis.users.api.UserAccountApi;
import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.usecase.UserAccountUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/users/me")
public class UserAccountController implements UserAccountApi {
    private final UserAccountUseCase userAccountUseCase;

    @PutMapping("/devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> putDevice(@RequestBody @Validated DeviceDto.RegisterReq request, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("device", userAccountUseCase.registerDevice(user.getUserId(), request)));
    }

    @DeleteMapping("/devices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteDevice(@RequestParam("token") @Validated @NotBlank String token, @AuthenticationPrincipal SecurityUserDetails user) {
        userAccountUseCase.unregisterDevice(user.getUserId(), token);
        return ResponseEntity.ok(SuccessResponse.noContent());
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyAccount(@AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("user", userAccountUseCase.getMyAccount(user.getUserId())));
    }

    @Override
    @PutMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> putNotifySetting(@RequestParam NotifySetting.NotifyType type, @RequestBody @Validated UserProfileDto.NotifySettingReq request, @AuthenticationPrincipal SecurityUserDetails user) {
        return ResponseEntity.ok(SuccessResponse.from("notifySetting", userAccountUseCase.putNotifySetting(user.getUserId(), type, request)));
    }
}
