package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.api.apis.users.mapper.UserProfileMapper;
import kr.co.pennyway.api.apis.users.service.DeviceRegisterService;
import kr.co.pennyway.api.apis.users.service.UserDeleteService;
import kr.co.pennyway.api.apis.users.service.UserProfileUpdateService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.device.domain.Device;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorCode;
import kr.co.pennyway.domain.domains.device.exception.DeviceErrorException;
import kr.co.pennyway.domain.domains.device.service.DeviceService;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAccountUseCase {
    private final UserService userService;
    private final OauthService oauthService;
    private final DeviceService deviceService;

    private final UserProfileUpdateService userProfileUpdateService;
    private final UserDeleteService userDeleteService;
    private final DeviceRegisterService deviceRegisterService;

    private final PasswordEncoderHelper passwordEncoderHelper;

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
        Set<Oauth> oauths = oauthService.readOauthsByUserId(userId).stream().filter(oauth -> !oauth.isDeleted()).collect(Collectors.toUnmodifiableSet());

        return UserProfileMapper.toUserProfileDto(user, oauths);
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

        validateGeneralSignedUpUser(user);
        validatePasswordMatch(expectedPassword, user.getPassword());
    }

    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = readUserOrThrow(userId);

        validateGeneralSignedUpUser(user);
        validatePasswordMatch(oldPassword, user.getPassword());

        userProfileUpdateService.updatePassword(user, oldPassword, newPassword);
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

    @Transactional
    public void deleteAccount(Long userId) {
        User user = readUserOrThrow(userId);

        // TODO: [2024-05-03] 하나라도 채팅방의 방장으로 참여하는 경우 삭제 불가능 처리

        userDeleteService.deleteUser(user);
    }

    private User readUserOrThrow(Long userId) {
        return userService.readUser(userId).orElseThrow(
                () -> {
                    log.info("사용자를 찾을 수 없습니다.");
                    return new UserErrorException(UserErrorCode.NOT_FOUND);
                }
        );
    }

    private void validateGeneralSignedUpUser(User user) {
        if (!user.isGeneralSignedUpUser()) {
            log.info("일반 회원가입 이력이 없습니다.");
            throw new UserErrorException(UserErrorCode.DO_NOT_GENERAL_SIGNED_UP);
        }
    }

    private void validatePasswordMatch(String password, String storedPassword) {
        if (!passwordEncoderHelper.isSamePassword(password, storedPassword)) {
            log.info("기존 비밀번호와 일치하지 않습니다.");
            throw new UserErrorException(UserErrorCode.NOT_MATCHED_PASSWORD);
        }
    }
}
