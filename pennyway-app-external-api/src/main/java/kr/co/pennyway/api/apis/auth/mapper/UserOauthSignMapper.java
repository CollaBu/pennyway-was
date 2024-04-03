package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Mapper
@RequiredArgsConstructor
public class UserOauthSignMapper {
    private final OauthService oauthService;

    @Transactional(readOnly = true)
    public User readUser(String oauthId, Provider provider) {
        Optional<Oauth> oauth = oauthService.readOauthByOauthIdAndProvider(oauthId, provider);

        return oauth.map(Oauth::getUser).orElse(null);
    }
}
