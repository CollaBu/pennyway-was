package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOauthSignService {
    private final UserService userService;
    private final OauthService oauthService;

    @Transactional(readOnly = true)
    public User readUser(String oauthId, Provider provider) {
        Optional<Oauth> oauth = oauthService.readOauthByOauthIdAndProvider(oauthId, provider);

        return oauth.map(Oauth::getUser).orElse(null);
    }

    /**
     * Oauth 회원가입 시나리오를 결정한다.
     *
     * @return Pair<Boolean, String> : 이미 가입된 회원인지 여부 (TRUE: 계정 연동, FALSE: 소셜 회원가입)
     * 단, 이미 동일한 Provider로 가입된 회원이 있는 경우에는 해당 회원의 ID를 반환한다.
     */
    @Transactional(readOnly = true)
    public Pair<Boolean, String> isSignUpAllowed(Provider provider, String phone) {
        Optional<User> user = userService.readUserByPhone(phone);

        if (user.isEmpty()) {
            log.info("회원가입 이력이 없는 사용자입니다. phone: {}", phone);
            return Pair.of(Boolean.FALSE, null);
        }

        if (oauthService.isExistOauthAccount(user.get().getId(), provider)) {
            log.info("이미 동일한 Provider로 가입된 사용자입니다. phone: {}, provider: {}", phone, provider);
            return null;
        }

        return Pair.of(Boolean.TRUE, user.get().getUsername());
    }

    /**
     * 기존 계정이 존재하면 Oauth 계정을 생성하여 연동하고, 존재하지 않으면 새로운 계정을 생성한다.
     *
     * @param request {@link SignUpReq.OauthInfo}
     */
    @Transactional
    public User saveUser(SignUpReq.OauthInfo request, Pair<Boolean, String> isSignUpUser, Provider provider, String oauthId) {
        User user;

        if (isSignUpUser.getLeft().equals(Boolean.TRUE)) {
            user = userService.readUserByUsername(isSignUpUser.getRight())
                    .orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        } else {
            user = request.toUser();
            userService.createUser(user);
        }

        Oauth oauth = Oauth.of(provider, oauthId, user);
        oauthService.createOauth(oauth);

        return user;
    }
}
