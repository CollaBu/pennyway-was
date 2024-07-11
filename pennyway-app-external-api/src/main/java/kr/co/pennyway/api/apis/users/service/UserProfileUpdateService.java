package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeKeyType;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeService;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.client.aws.s3.AwsS3Provider;
import kr.co.pennyway.infra.client.aws.s3.ObjectKeyType;
import kr.co.pennyway.infra.common.exception.StorageErrorCode;
import kr.co.pennyway.infra.common.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {
    private final UserService userService;
    private final AwsS3Provider awsS3Provider;

    private final PhoneVerificationService phoneVerificationService;
    private final PhoneCodeService phoneCodeService;

    @Transactional
    public void updateName(Long userId, String newName) {
        User user = readUserOrThrow(userId);

        user.updateName(newName);
    }

    @Transactional
    public void updateUsername(Long userId, String newUsername) {
        User user = readUserOrThrow(userId);

        user.updateUsername(newUsername);
    }

    @Transactional
    public void updateProfileImage(Long userId, String profileImageUrl) {
        User user = readUserOrThrow(userId);

        // Profile Image 존재 여부 확인
        if (!awsS3Provider.isObjectExist(profileImageUrl)) {
            log.info("프로필 이미지 URL이 유효하지 않습니다.");
            throw new StorageException(StorageErrorCode.NOT_FOUND);
        }

        // Profile Image 원본 저장
        awsS3Provider.copyObject(ObjectKeyType.PROFILE, profileImageUrl);

        // Profile Image URL 업데이트
        String originKey = ObjectKeyType.PROFILE.convertDeleteKeyToOriginKey(profileImageUrl);
        user.updateProfileImageUrl(awsS3Provider.getObjectPrefix() + originKey);
    }

    @Transactional
    public void updatePhone(Long userId, String phone, String code) {
        User user = readUserOrThrow(userId);

        phoneVerificationService.isValidCode(PhoneVerificationDto.VerifyCodeReq.of(phone, code), PhoneCodeKeyType.PHONE);
        phoneCodeService.delete(phone, PhoneCodeKeyType.PHONE);

        if (userService.isExistPhone(phone)) {
            throw new UserErrorException(UserErrorCode.ALREADY_EXIST_PHONE);
        }

        user.updatePhone(phone);
    }

    @Transactional
    public void updateNotifySetting(Long userId, NotifySetting.NotifyType type, Boolean flag) {
        User user = readUserOrThrow(userId);

        user.getNotifySetting().updateNotifySetting(type, flag);
    }

    private User readUserOrThrow(Long userId) {
        return userService.readUser(userId).orElseThrow(
                () -> {
                    log.info("사용자를 찾을 수 없습니다.");
                    return new UserErrorException(UserErrorCode.NOT_FOUND);
                }
        );
    }
}
