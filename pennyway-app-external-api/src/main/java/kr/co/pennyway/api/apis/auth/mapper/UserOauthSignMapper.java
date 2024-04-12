package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.domains.user.type.ProfileVisibility;
import kr.co.pennyway.domain.domains.user.type.Role;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Mapper
@RequiredArgsConstructor
public class UserOauthSignMapper {
    private final UserService userService;
    private final OauthService oauthService;

    @Transactional(readOnly = true)
    public User readUser(String oauthId, Provider provider) {
        Optional<Oauth> oauth = oauthService.readOauthByOauthIdAndProvider(oauthId, provider);

        return oauth.map(Oauth::getUser).orElse(null);
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
            user = User.builder()
                    .username(request.username())
                    .name(request.name())
                    .phone(request.phone())
                    .role(Role.USER)
                    .profileVisibility(ProfileVisibility.PUBLIC).build();
            userService.createUser(user);
        }

        Oauth oauth = Oauth.of(provider, oauthId, user);
        oauthService.createOauth(oauth);

        return user;
    }
}
