package kr.co.pennyway.api.apis.users.usecase;

import kr.co.pennyway.api.apis.users.dto.DeviceTokenDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileDto;
import kr.co.pennyway.api.apis.users.dto.UserProfileUpdateDto;
import kr.co.pennyway.api.apis.users.mapper.DeviceTokenMapper;
import kr.co.pennyway.api.apis.users.mapper.UserProfileMapper;
import kr.co.pennyway.api.apis.users.service.*;
import kr.co.pennyway.api.common.storage.AwsS3Adapter;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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

    private final AwsS3Adapter awsS3Adapter;

    @Transactional
    public DeviceTokenDto.RegisterRes registerDeviceToken(Long userId, DeviceTokenDto.RegisterReq request) {
        DeviceToken deviceToken = deviceTokenRegisterService.execute(userId, request.token());
        return DeviceTokenMapper.toRegisterRes(deviceToken);
    }

    public void unregisterDeviceToken(Long userId, String token) {
        deviceTokenUnregisterService.execute(userId, token);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getMyAccount(Long userId) {
        User user = userProfileSearchService.readMyAccount(userId);
        Set<Oauth> oauths = userProfileSearchService.readMyOauths(userId);

        return UserProfileMapper.toUserProfileDto(user, oauths, awsS3Adapter.getObjectPrefix());
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

    public String updateProfileImage(Long userId, UserProfileUpdateDto.ProfileImageReq request) {
        String originImageUrl = awsS3Adapter.saveImage(request.profileImageUrl(), ObjectKeyType.PROFILE);
        String oldImageUrl = userProfileUpdateService.updateProfileImage(userId, originImageUrl);

        if (oldImageUrl != null) {
            awsS3Adapter.deleteImage(oldImageUrl);
        }

        return awsS3Adapter.getObjectPrefix() + originImageUrl;
    }

    public void deleteProfileImage(Long userId) {
        String profileImageUrl = userProfileUpdateService.deleteProfileImage(userId);

        awsS3Adapter.deleteImage(profileImageUrl);
    }

    public void updatePhone(Long userId, UserProfileUpdateDto.PhoneReq request) {
        userProfileUpdateService.updatePhone(userId, request.phone(), request.code());
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
