package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceTokenDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.api.apis.users.mapper.DeviceTokenMapper;
import kr.co.pennyway.api.apis.users.mapper.UserProfileMapper;
import kr.co.pennyway.api.apis.users.service.*;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAccountUseCase {
    private final DeviceTokenRegisterService deviceTokenRegisterService;
    private final DeviceTokenUnregisterService deviceTokenUnregisterService;

    private final UserProfileSearchService userProfileSearchService;
    private final UserProfileUpdateService userProfileUpdateService;
    private final UserDeleteService userDeleteService;

    private final PasswordUpdateService passwordUpdateService;

    @Transactional
    public DeviceTokenDto.RegisterRes registerDeviceToken(Long userId, DeviceTokenDto.RegisterReq request) {
        DeviceToken deviceToken = deviceTokenRegisterService.execute(userId, request.token());
        return DeviceTokenMapper.toRegisterRes(deviceToken);
    }

    public void unregisterDeviceToken(Long userId, String token) {
        deviceTokenUnregisterService.execute(userId, token);
    }

    public UserProfileDto getMyAccount(Long userId) {
        return userProfileSearchService.readMyAccount(userId);
    }

    public void updateName(Long userId, String newName) {
        userProfileUpdateService.updateName(userId, newName);
    }

    public void updateUsername(Long userId, String newUsername) {
        userProfileUpdateService.updateUsername(userId, newUsername);
    }

    public void verifyPassword(Long userId, String expectedPassword) {
        passwordUpdateService.verify(userId, expectedPassword);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        passwordUpdateService.execute(userId, oldPassword, newPassword);
    }

    public UserProfileUpdateDto.NotifySettingUpdateRes activateNotification(Long userId, NotifySetting.NotifyType type) {
        userProfileUpdateService.updateNotifySetting(userId, type, Boolean.TRUE);
        return UserProfileMapper.toNotifySettingUpdateRes(type, Boolean.TRUE);
    }

    public UserProfileUpdateDto.NotifySettingUpdateRes deactivateNotification(Long userId, NotifySetting.NotifyType type) {
        userProfileUpdateService.updateNotifySetting(userId, type, Boolean.FALSE);
        return UserProfileMapper.toNotifySettingUpdateRes(type, Boolean.FALSE);
    }

    public void deleteAccount(Long userId) {
        userDeleteService.execute(userId);
    }
}
