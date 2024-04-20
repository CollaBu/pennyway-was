package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
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

    public Pair<Long, Jwts> signIn(Provider provider, SignInReq.Oauth request) {
        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken());
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
        Pair<Boolean, String> isSignUpUser = checkSignUpUserNotOauthByProvider(provider, request.phone());

        phoneCodeService.extendTimeToLeave(request.phone(), PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

        return PhoneVerificationDto.VerifyCodeRes.valueOfOauth(isValidCode, isSignUpUser.getLeft(), isSignUpUser.getRight());
    }

    @Transactional
    public Pair<Long, Jwts> signUp(Provider provider, SignUpReq.OauthInfo request) {
        phoneVerificationService.isValidCode(PhoneVerificationDto.VerifyCodeReq.from(request), PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
        phoneCodeService.delete(request.phone(), PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));

        Pair<Boolean, String> isSignUpUser = checkSignUpUserNotOauthByProvider(provider, request.phone());

        if (isSignUpUser.getLeft().equals(Boolean.FALSE) && !isOauthSyncRequest(request))
            throw new OauthException(OauthErrorCode.INVALID_OAUTH_SYNC_REQUEST);
        if (isSignUpUser.getLeft().equals(Boolean.TRUE) && isOauthSyncRequest(request))
            throw new OauthException(OauthErrorCode.INVALID_OAUTH_SYNC_REQUEST);

        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken());
        User user = userOauthSignService.saveUser(request, isSignUpUser, provider, payload.sub());

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user));
    }

    /**
     * Oauth 회원가입 진행 도중, Provider로 가입한 사용자인지 지속적으로 검증하기 위한 메서드
     */
    private Pair<Boolean, String> checkSignUpUserNotOauthByProvider(Provider provider, String phone) {
        Pair<Boolean, String> isOauthSignUpAllowed = userOauthSignService.isSignUpAllowed(provider, phone);

        if (isOauthSignUpAllowed == null) {
            phoneCodeService.delete(phone, PhoneCodeKeyType.getOauthSignUpTypeByProvider(provider));
            throw new OauthException(OauthErrorCode.ALREADY_SIGNUP_OAUTH);
        }

        return isOauthSignUpAllowed;
    }
    
    private boolean isOauthSyncRequest(SignUpReq.OauthInfo request) {
        return request.username() != null;
    }
}
