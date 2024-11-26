package kr.co.pennyway.api.apis.users.service;

import kr.co.pennyway.api.apis.users.helper.PasswordEncoderHelper;
import kr.co.pennyway.domain.context.account.service.UserService;
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
public class PasswordUpdateService {
    private final UserService userService;
    private final PasswordEncoderHelper passwordEncoderHelper;

    @Transactional(readOnly = true)
    public void verify(Long userId, String expectedPassword) {
        User user = readUserOrThrow(userId);

        validateGeneralSignedUpUser(user);
        validatePasswordMatch(expectedPassword, user.getPassword());
    }

    @Transactional
    public void execute(Long userId, String oldPassword, String newPassword) {
        User user = readUserOrThrow(userId);

        validateGeneralSignedUpUser(user);
        validatePasswordMatch(oldPassword, user.getPassword());

        updatePassword(user, newPassword);
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

    private void updatePassword(User user, String newPassword) {
        if (passwordEncoderHelper.isSamePassword(user.getPassword(), newPassword)) {
            log.info("기존과 동일한 비밀번호로는 변경할 수 없습니다.");
            throw new UserErrorException(UserErrorCode.PASSWORD_NOT_CHANGED);
        }

        user.updatePassword(passwordEncoderHelper.encodePassword(newPassword));
    }
}
