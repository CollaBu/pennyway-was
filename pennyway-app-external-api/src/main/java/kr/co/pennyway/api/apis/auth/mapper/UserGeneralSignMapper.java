package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 일반 회원가입, 로그인 시나리오 도우미 클래스
 *
 * @author YANG JAESEO
 */
@Slf4j
@Mapper
@RequiredArgsConstructor
public class UserGeneralSignMapper {
    private final UserService userService;

    private final PasswordEncoder bCryptPasswordEncoder;

    /**
     * 일반 회원가입이라면 새롭게 유저를 생성하고, 기존 Oauth 유저라면 비밀번호를 업데이트한다.
     *
     * @param request {@link SignUpReq.Info}
     */
    @Transactional
    public User saveUserWithEncryptedPassword(SignUpReq.Info request, Pair<Boolean, String> isOauthUser) {
        User user;

        if (isOauthUser.getLeft().equals(Boolean.TRUE)) {
            user = userService.readUserByUsername(isOauthUser.getRight())
                    .orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
            user.updatePassword(request.password(bCryptPasswordEncoder));
        } else {
            user = userService.createUser(request.toEntity(bCryptPasswordEncoder));
        }

        return user;
    }

    /**
     * 로그인 시 유저가 존재하고 비밀번호가 일치하는지 확인
     *
     * @throws UserErrorException : 유저가 존재하지 않거나 비밀번호가 일치하지 않는 경우
     */
    @Transactional(readOnly = true)
    public User readUserIfValid(String username, String password) {
        Optional<User> user = userService.readUserByUsername(username);

        if (user.isEmpty()) {
            log.warn("해당 유저가 존재하지 않습니다. username: {}", username);
            throw new UserErrorException(UserErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        if (!bCryptPasswordEncoder.matches(password, user.get().getPassword())) {
            log.warn("비밀번호가 일치하지 않습니다. username: {}", username);
            throw new UserErrorException(UserErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        return user.get();
    }
}
