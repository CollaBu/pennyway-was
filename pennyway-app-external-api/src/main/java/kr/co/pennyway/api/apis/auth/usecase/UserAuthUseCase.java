package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.AuthStateDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.UserSyncDto;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.apis.auth.service.UserOauthSignService;
import kr.co.pennyway.api.common.security.jwt.JwtClaimsParserUtil;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaimKeys;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.oauth.domain.Oauth;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import kr.co.pennyway.infra.common.oidc.OidcDecodePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAuthUseCase {
    private final UserOauthSignService userOauthSignService;
    private final OauthService oauthService;

    private final JwtAuthHelper jwtAuthHelper;
    private final OauthOidcHelper oauthOidcHelper;

    private final JwtProvider accessTokenProvider;

    public AuthStateDto isSignIn(String authHeader) {
        String accessToken = accessTokenProvider.resolveToken(authHeader);
        JwtClaims claims = accessTokenProvider.getJwtClaimsFromToken(accessToken);
        Long userId = JwtClaimsParserUtil.getClaimsValue(claims, AccessTokenClaimKeys.USER_ID.getValue(), Long::parseLong);

        log.info("auth_id {} 사용자는 로그인 중입니다.", userId);

        return AuthStateDto.of(userId);
    }

    public void signOut(Long userId, String authHeader, String refreshToken) {
        jwtAuthHelper.removeAccessTokenAndRefreshToken(userId, authHeader, refreshToken);
    }

    @Transactional
    public void linkOauth(Provider provider, SignInReq.Oauth request, Long userId) {
        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.oauthId(), request.idToken(), request.nonce());

        UserSyncDto userSync = userOauthSignService.isLinkAllowed(userId, provider);
        userOauthSignService.saveUser(null, userSync, provider, payload.sub());
    }

    @Transactional
    public void unlinkOauth(Provider provider, Long userId) {
        Oauth oauth = userOauthSignService.readOauthForUnlink(userId, provider);
        oauthService.deleteOauth(oauth);
    }
}
