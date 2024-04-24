package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.dto.UserSyncDto;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.api.apis.auth.service.UserOauthSignService;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeKeyType;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeService;
import kr.co.pennyway.domain.domains.oauth.exception.OauthErrorCode;
import kr.co.pennyway.domain.domains.oauth.exception.OauthException;
import kr.co.pennyway.domain.domains.oauth.type.Provider;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.infra.common.oidc.OidcDecodePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class OauthUseCase {
    private final OauthOidcHelper oauthOidcHelper;
    private final PhoneVerificationService phoneVerificationService;
    private final PhoneCodeService phoneCodeService;
    private final JwtAuthHelper jwtAuthHelper;
    private final UserOauthSignService userOauthSignService;

    @Transactional(readOnly = true)
    public Pair<Long, Jwts> signIn(Provider provider, SignInReq.Oauth request) {
        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken(), request.nonce());
        log.debug("payload : {}", payload);

        if (!request.oauthId().equals(payload.sub()))
            throw new OauthException(OauthErrorCode.NOT_MATCHED_OAUTH_ID);
        User user = userOauthSignService.readUser(request.oauthId(), provider);

        return (user != null) ? Pair.of(user.getId(), jwtAuthHelper.createToken(user)) : Pair.of(-1L, null);
    }

    public PhoneVerificationDto.PushCodeRes sendCode(Provider provider, PhoneVerificationDto.PushCodeReq request) {
        return phoneVerificationService.sendCode(request, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
    }

    @Transactional(readOnly = true)
    public PhoneVerificationDto.VerifyCodeRes verifyCode(Provider provider, PhoneVerificationDto.VerifyCodeReq request) {
        Boolean isValidCode = phoneVerificationService.isValidCode(request, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
        UserSyncDto userSync = checkSignUpUserNotOauthByProvider(provider, request.phone());

        phoneCodeService.extendTimeToLeave(request.phone(), PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

        return PhoneVerificationDto.VerifyCodeRes.valueOfOauth(isValidCode, userSync.isExistAccount(), userSync.username());
    }

    @Transactional
    public Pair<Long, Jwts> signUp(Provider provider, SignUpReq.OauthInfo request) {
        phoneVerificationService.isValidCode(PhoneVerificationDto.VerifyCodeReq.from(request), PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
        phoneCodeService.delete(request.phone(), PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

        UserSyncDto userSync = checkSignUpUserNotOauthByProvider(provider, request.phone());

        if (isValidRequestScenario(userSync, request)) {
            log.warn("유효한 소셜 회원가입 요청 플로우가 아닙니다.");
            throw new OauthException(OauthErrorCode.INVALID_OAUTH_SYNC_REQUEST);
        }

        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken(), request.nonce());
        User user = userOauthSignService.saveUser(request, userSync, provider, payload.sub());

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user));
    }

    /**
     * Oauth 회원가입 진행 도중, Provider로 가입한 사용자인지 지속적으로 검증하기 위한 메서드
     */
    private UserSyncDto checkSignUpUserNotOauthByProvider(Provider provider, String phone) {
        UserSyncDto userSync = userOauthSignService.isSignUpAllowed(provider, phone);

        if (!userSync.isSignUpAllowed()) {
            phoneCodeService.delete(phone, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            throw new OauthException(OauthErrorCode.ALREADY_SIGNUP_OAUTH);
        }

        return userSync;
    }

    /**
     * Oauth 회원가입 요청 시나리오가 유효한지 확인하는 메서드 <br/>
     * - 회원가입 이력이 없는 사용자는 Oauth 회원가입 요청만 가능하다. <br/>
     * - 이미 회원가입된 사용자(같은 Provider 소셜 회원가입이 이력이 없어야 함)는 계정 연동 요청만 가능하다.
     */
    private boolean isValidRequestScenario(UserSyncDto userSync, SignUpReq.OauthInfo request) {
        return (!userSync.isExistAccount() && !isOauthSyncRequest(request)) ||
                (userSync.isExistAccount() && isOauthSyncRequest(request));
    }

    private boolean isOauthSyncRequest(SignUpReq.OauthInfo request) {
        return request.username() != null;
    }
}
