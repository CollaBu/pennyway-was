package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Helper
@RequiredArgsConstructor
public class UserSignInHelper {
    private final UserService userService;

    private final PasswordEncoder bCryptPasswordEncoder;

    /**
     * 로그인 시 유저가 존재하고 비밀번호가 일치하는지 확인
     */
    @Transactional(readOnly = true)
    public User readUserIfValid(String username, String password) {
        User user;

        try {
            user = userService.readUserByUsername(username);

            if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
                throw new UserErrorException(UserErrorCode.NOT_MATCHED_PASSWORD);
            }
        } catch (UserErrorException e) {
            log.warn("request not valid : {} : {}", username, e.getExplainError());
            throw new UserErrorException(UserErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        return user;
    }
}
