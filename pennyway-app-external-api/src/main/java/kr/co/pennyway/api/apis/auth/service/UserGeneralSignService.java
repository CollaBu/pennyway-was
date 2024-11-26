package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.dto.UserSyncDto;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @return {@link UserSyncDto}
     */
    @Transactional(readOnly = true)
    public UserSyncDto isSignUpAllowed(String phone) {
        Optional<User> user = userService.readUserByPhone(phone);

        if (!isExistUser(user)) {
            log.info("회원가입 이력이 없는 사용자입니다. phone: {}", phone);
            return UserSyncDto.signUpAllowed();
        }

        if (isGeneralSignUpUser(user.get())) {
            log.warn("이미 회원가입된 사용자입니다. user: {}", user.get());
            return UserSyncDto.abort(user.get().getId(), user.get().getUsername());
        }

        log.info("소셜 회원가입 사용자입니다. user: {}", user.get());
        return UserSyncDto.of(true, true, user.get().getId(), user.get().getUsername());
    }

    /**
     * 일반 회원가입이라면 새롭게 유저를 생성하고, 기존 Oauth 유저라면 비밀번호를 업데이트한다.
     *
     * @param request {@link SignUpReq.Info}
     */
    @Transactional
    public User saveUserWithEncryptedPassword(SignUpReq.Info request, UserSyncDto userSync) {
        User user;

        if (userSync.isExistAccount()) {
            log.info("기존 Oauth 회원입니다. username: {}", userSync.username());
            user = userService.readUser(userSync.userId())
                    .orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
            user.updatePassword(request.password(bCryptPasswordEncoder));
        } else {
            log.info("새로운 회원입니다. username: {}", request.username());
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
