package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.api.apis.users.service.DeviceRegisterService;
import kr.co.pennyway.api.apis.users.service.UserProfileUpdateService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAccountUseCase {
    private final UserService userService;
    private final DeviceService deviceService;

    private final UserProfileUpdateService userProfileUpdateService;
    private final DeviceRegisterService deviceRegisterService;

    @Transactional
    public DeviceDto.RegisterRes registerDevice(Long userId, DeviceDto.RegisterReq request) {
        User user = readUserOrThrow(userId);

        Device device = deviceRegisterService.createOrUpdateDevice(user, request);

        return DeviceDto.RegisterRes.of(device.getId(), request.newToken());
    }

    @Transactional
    public void unregisterDevice(Long userId, String token) {
        User user = readUserOrThrow(userId);

        Device device = deviceService.readDeviceByUserIdAndToken(user.getId(), token).orElseThrow(
                () -> new DeviceErrorException(DeviceErrorCode.NOT_FOUND_DEVICE)
        );

        deviceService.deleteDevice(device);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getMyAccount(Long userId) {
        User user = readUserOrThrow(userId);

        return UserProfileDto.from(user);
    }

    @Transactional
    public void updateName(Long userId, String newName) {
        User user = readUserOrThrow(userId);

        userProfileUpdateService.updateName(user, newName);
    }

    @Transactional
    public void updateUsername(Long userId, String newUsername) {
        User user = readUserOrThrow(userId);

        userProfileUpdateService.updateUsername(user, newUsername);
    }

    @Transactional(readOnly = true)
    public void verifyPassword(Long userId, String expectedPassword) {
        User user = readUserOrThrow(userId);


    }

    @Transactional
    public UserProfileUpdateDto.NotifySettingUpdateReq activateNotification(Long userId, NotifySetting.NotifyType type) {
        User user = readUserOrThrow(userId);

        userProfileUpdateService.updateNotifySetting(user, type, Boolean.TRUE);
        return UserProfileUpdateDto.NotifySettingUpdateReq.of(type, Boolean.TRUE);
    }

    @Transactional
    public UserProfileUpdateDto.NotifySettingUpdateReq deactivateNotification(Long userId, NotifySetting.NotifyType type) {
        User user = readUserOrThrow(userId);

        userProfileUpdateService.updateNotifySetting(user, type, Boolean.FALSE);
        return UserProfileUpdateDto.NotifySettingUpdateReq.of(type, Boolean.FALSE);
    }

    private User readUserOrThrow(Long userId) {
        return userService.readUser(userId).orElseThrow(
                () -> new UserErrorException(UserErrorCode.NOT_FOUND)
        );
    }
}
