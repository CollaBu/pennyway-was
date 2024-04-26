package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {
    private final PasswordEncoderHelper passwordEncoderHelper;

    @Transactional
    public void updateName(User user, String newName) {
        user.updateName(newName);
    }

    @Transactional
    public void updateUsername(User user, String newUsername) {
        user.updateUsername(newUsername);
    }

    @Transactional
    public void updatePassword(User user, String oldPassword, String newPassword) {
        if (passwordEncoderHelper.isSamePassword(user.getPassword(), newPassword)) {
            log.info("기존과 동일한 비밀번호로는 변경할 수 없습니다.");
            throw new UserErrorException(UserErrorCode.PASSWORD_NOT_CHANGED);
        }

        user.updatePassword(passwordEncoderHelper.encodePassword(newPassword));
    }

    @Transactional
    public void updateNotifySetting(User user, NotifySetting.NotifyType type, Boolean flag) {
        user.getNotifySetting().updateNotifySetting(type, flag);
    }
}
