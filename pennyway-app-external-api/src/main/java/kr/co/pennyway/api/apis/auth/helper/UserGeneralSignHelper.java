package kr.co.pennyway.api.apis.auth.helper;

import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.common.annotation.Helper;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일반 회원가입, 로그인 시나리오 도우미 클래스
 *
 * @author YANG JAESEO
 */
@Slf4j
@Helper
@RequiredArgsConstructor
public class UserGeneralSignHelper {
    private final UserService userService;

    private final PasswordEncoder bCryptPasswordEncoder;

    /**
     * 일반 회원가입이라면 새롭게 유저를 생성하고, 기존 Oauth 유저라면 비밀번호를 업데이트한다.
     *
     * @param request {@link SignUpReq.Info}
     */
    @Transactional
    public User createUserWithEncryptedPassword(SignUpReq.Info request, Pair<Boolean, String> isOauthUser) {
        User user;

        if (isOauthUser.getLeft().equals(Boolean.TRUE)) {
            user = userService.readUserByUsername(isOauthUser.getRight());
            user.updatePassword(request.password(bCryptPasswordEncoder));
        } else {
            user = userService.createUser(request.toEntity(bCryptPasswordEncoder));
        }

        return user;
    }

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
