package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 일반 회원가입, 로그인 시나리오 도우미 클래스
 *
 * @author YANG JAESEO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserGeneralSignService {
    private final UserService userService;

    private final PasswordEncoder bCryptPasswordEncoder;

    /**
     * 일반 회원가입이 가능한 유저인지 확인
     *
     * @return Pair<Boolean, String> : 이미 가입된 회원인지 여부 (TRUE: 가입되지 않은 회원, FALSE: 가입된 회원), 가입된 회원인 경우 회원
     * ID 반환. 단, 이미 일반 회원가입을 한 유저인 경우에는 null을 반환한다.
     */
    @Transactional(readOnly = true)
    public Pair<Boolean, String> isSignUpAllowed(String phone) {
        Optional<User> user = userService.readUserByPhone(phone);

        if (!isExistUser(user)) {
            log.info("회원가입 이력이 없는 사용자입니다. phone: {}", phone);
            return Pair.of(Boolean.FALSE, null);
        }

        if (isGeneralSignUpUser(user.get())) {
            log.warn("이미 회원가입된 사용자입니다. user: {}", user.get());
            return null;
        }

        log.info("소셜 회원가입 사용자입니다. user: {}", user.get());
        return Pair.of(Boolean.TRUE, user.get().getUsername());
    }

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

        if (!isExistUser(user)) {
            log.warn("해당 유저가 존재하지 않습니다. username: {}", username);
            throw new UserErrorException(UserErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        if (!isValidPassword(password, user.get())) {
            log.warn("비밀번호가 일치하지 않습니다. username: {}", username);
            throw new UserErrorException(UserErrorCode.INVALID_USERNAME_OR_PASSWORD);
        }

        return user.get();
    }

    private boolean isExistUser(Optional<User> user) {
        return user.isPresent();
    }

    private boolean isGeneralSignUpUser(User user) {
        return user.getPassword() != null;
    }

    private boolean isValidPassword(String password, User user) {
        return bCryptPasswordEncoder.matches(password, user.getPassword());
    }
}
