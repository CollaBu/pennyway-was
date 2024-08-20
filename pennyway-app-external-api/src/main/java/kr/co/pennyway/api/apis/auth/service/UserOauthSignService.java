package kr.co.pennyway.api.apis.auth.service;

import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.dto.UserSyncDto;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.exception.OauthException;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOauthSignService {
    private final UserService userService;
    private final OauthService oauthService;

    @Transactional(readOnly = true)
    public User readUser(String oauthId, Provider provider) {
        return oauthService.readOauthByOauthIdAndProvider(oauthId, provider)
                .map(Oauth::getUser)
                .orElse(null);
    }

    /**
     * Oauth 회원가입 시나리오를 결정한다.
     *
     * @return {@link UserSyncDto} : 이미 가입된 회원인지 여부를 담은 DTO.
     * 단, 이미 동일한 Provider로 가입된 회원이 있는 경우에는 해당 회원의 ID를 반환한다.
     */
    @Transactional(readOnly = true)
    public UserSyncDto isSignUpAllowed(Provider provider, String phone) {
        Optional<User> user = userService.readUserByPhone(phone);

        if (user.isEmpty()) {
            log.info("회원가입 이력이 없는 사용자입니다. phone: {}", phone);
            return UserSyncDto.signUpAllowed();
        }

        if (oauthService.isExistOauthByUserIdAndProvider(user.get().getId(), provider)) {
            log.info("이미 동일한 Provider로 가입된 사용자입니다. phone: {}, provider: {}", phone, provider);
            return UserSyncDto.abort(user.get().getId(), user.get().getUsername());
        }

        log.info("소셜 회원가입 사용자입니다. user: {}", user.get());
        return UserSyncDto.of(true, true, user.get().getId(), user.get().getUsername());
    }

    /**
     * 인증된 사용자에게 provider로 연동할 수 있는지 여부를 반환한다.
     *
     * @return {@link UserSyncDto}
     */
    @Transactional(readOnly = true)
    public UserSyncDto isLinkAllowed(Long userId, String oauthId, Provider provider) {
        if (oauthService.isExistOauthByUserIdAndProvider(userId, provider)) {
            log.info("이미 해당 Oauth 계정에 연동되어 있습니다. userId: {}, provider: {}", userId, provider);
            throw new OauthException(OauthErrorCode.ALREADY_SIGNUP_OAUTH);
        }

        if (oauthService.isExistOauthByOauthIdAndProvider(oauthId, provider)) {
            log.info("이미 다른 사용자가 사용 중인 Oauth 계정입니다. oauthId: {}, provider: {}", oauthId, provider);
            throw new OauthException(OauthErrorCode.ALREADY_USED_OAUTH);
        }

        User user = userService.readUser(userId).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));

        return UserSyncDto.of(true, true, user.getId(), user.getUsername());
    }

    /**
     * 연동을 해지할 수 있는 Oauth 계정을 조회한다.
     *
     * @throws OauthException : <br/>
     *                        {@link OauthErrorCode#NOT_FOUND_OAUTH} : Provider에 해당하는 Oauth가 없는 경우 <br/>
     *                        {@link OauthErrorCode#CANNOT_UNLINK_OAUTH} : Oauth가 1개이고 일반 회원 이력이 없어 연동 해지가 불가능한 경우 <br/>
     */
    @Transactional(readOnly = true)
    public Oauth readOauthForUnlink(Long userId, Provider provider) {
        Set<Oauth> oauths = oauthService.readOauthsByUserId(userId);

        Oauth oauth = oauths.stream()
                .filter(o -> o.getProvider().equals(provider) && !o.isDeleted())
                .findFirst()
                .orElseThrow(() -> new OauthException(OauthErrorCode.NOT_FOUND_OAUTH));
        long oauthCount = oauths.stream().filter(o -> !o.isDeleted()).count();

        User user = oauth.getUser();

        if (oauthCount == 1 && !user.isGeneralSignedUpUser()) {
            throw new OauthException(OauthErrorCode.CANNOT_UNLINK_OAUTH);
        }

        return oauth;
    }

    /**
     * 기존 계정이 존재하면 Oauth 계정을 생성하여 연동하고, 존재하지 않으면 새로운 계정을 생성한다.
     *
     * @param request {@link SignUpReq.OauthInfo}
     */
    @Transactional
    public User saveUser(SignUpReq.OauthInfo request, UserSyncDto userSync, Provider provider, String oauthId) {
        User user;

        if (userSync.isExistAccount()) {
            log.info("기존 계정에 연동합니다. username: {}", userSync.username());
            user = userService.readUser(userSync.userId()).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
        } else {
            log.info("새로운 계정을 생성합니다. username: {}", request.username());
            user = request.toUser();
            userService.createUser(user);
        }

        Oauth oauth = oauthService.createOauth(Oauth.of(provider, oauthId, user));
        log.info("연동된 Oauth 정보 : {}", oauth);

        return user;
    }
}
