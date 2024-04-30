package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.AuthStateDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.UserSyncDto;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.apis.auth.service.UserOauthSignService;
import kr.co.pennyway.api.common.security.jwt.access.AccessTokenClaimKeys;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.exception.OauthException;
import kr.co.pennyway.domain.domains.oauth.service.OauthService;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.jwt.JwtClaims;
import kr.co.pennyway.infra.common.jwt.JwtProvider;
import kr.co.pennyway.infra.common.oidc.OidcDecodePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserAuthUseCase {
    private final UserService userService;
    private final OauthService oauthService;

    private final UserOauthSignService userOauthSignService;

    private final JwtAuthHelper jwtAuthHelper;
    private final OauthOidcHelper oauthOidcHelper;

    private final JwtProvider accessTokenProvider;

    public AuthStateDto isSignIn(String authHeader) {
        String accessToken = accessTokenProvider.resolveToken(authHeader);
        JwtClaims claims = accessTokenProvider.getJwtClaimsFromToken(accessToken);
        Long userId = jwtAuthHelper.getClaimsValue(claims, AccessTokenClaimKeys.USER_ID.getValue(), Long::parseLong);

        log.info("auth_id {} 사용자는 로그인 중입니다.", userId);

        return AuthStateDto.of(userId);
    }

    public void signOut(Long userId, String authHeader, String refreshToken) {
        jwtAuthHelper.removeAccessTokenAndRefreshToken(userId, authHeader, refreshToken);
    }

    public void linkOauth(Provider provider, SignInReq.Oauth request, Long userId) {
        // 1. 페이로드를 추출한다.
        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken(), request.nonce());

        // 2. 요청을 검증하고 사용자가 연동 가능한 지 validation한다.
        if (!request.oauthId().equals(payload.sub()))
            throw new OauthException(OauthErrorCode.NOT_MATCHED_OAUTH_ID);
        if (oauthService.isExistOauthAccount(userId, provider))
            throw new OauthException(OauthErrorCode.ALREADY_SIGNUP_OAUTH);

        // 3. 사용자를 읽어온다.
        User user = userService.readUser(userId).orElseThrow(
                () -> new UserErrorException(UserErrorCode.NOT_FOUND)
        );

        // 4. 사용자 로그인 정보에 Oauth 정보를 추가한다.
        UserSyncDto userSync = UserSyncDto.of(true, true, user.getId(), user.getUsername());
        userOauthSignService.saveUser(null, userSync, provider, request.oauthId());
    }
}
