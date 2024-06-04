package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {
    private final UserService userService;

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
