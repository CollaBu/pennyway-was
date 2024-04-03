package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.apis.auth.mapper.UserOauthSignMapper;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.exception.OauthException;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.infra.common.oidc.OidcDecodePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class OauthUseCase {
    private final OauthOidcHelper oauthOidcHelper;
    private final JwtAuthHelper jwtAuthHelper;
    private UserOauthSignMapper userOauthSignMapper;

    public Pair<Long, Jwts> signIn(Provider provider, SignInReq.Oauth request) {
        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken());
        log.info("payload : {}", payload);

        if (!request.oauthId().equals(payload.sub()))
            throw new OauthException(OauthErrorCode.NOT_MATCHED_OAUTH_ID);
        User user = userOauthSignMapper.readUser(request.oauthId(), provider);

        return (user != null) ? Pair.of(user.getId(), jwtAuthHelper.createToken(user)) : Pair.of(-1L, null);
    }
}
