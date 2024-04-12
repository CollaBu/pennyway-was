package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.helper.OauthOidcHelper;
import kr.co.pennyway.api.apis.auth.mapper.PhoneVerificationMapper;
import kr.co.pennyway.api.apis.auth.mapper.UserOauthSignMapper;
import kr.co.pennyway.api.apis.auth.mapper.UserSyncMapper;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
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
    private final PhoneVerificationMapper phoneVerificationMapper;
    private final PhoneVerificationService phoneVerificationService;
    private final JwtAuthHelper jwtAuthHelper;
    private final UserOauthSignMapper userOauthSignMapper;
    private final UserSyncMapper userSyncMapper;

    public Pair<Long, Jwts> signIn(Provider provider, SignInReq.Oauth request) {
        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken());
        log.debug("payload : {}", payload);

        if (!request.oauthId().equals(payload.sub()))
            throw new OauthException(OauthErrorCode.NOT_MATCHED_OAUTH_ID);
        User user = userOauthSignMapper.readUser(request.oauthId(), provider);

        return (user != null) ? Pair.of(user.getId(), jwtAuthHelper.createToken(user)) : Pair.of(-1L, null);
    }

    public PhoneVerificationDto.PushCodeRes sendCode(Provider provider, PhoneVerificationDto.PushCodeReq request) {
        return phoneVerificationMapper.sendCode(request, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
    }

    @Transactional(readOnly = true)
    public PhoneVerificationDto.VerifyCodeRes verifyCode(Provider provider, PhoneVerificationDto.VerifyCodeReq request) {
        Boolean isValidCode = phoneVerificationMapper.isValidCode(request, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
        Pair<Boolean, String> isSignUpUser = checkSignUpUserNotOauthByProvider(provider, request.phone());

        phoneVerificationService.extendTimeToLeave(request.phone(), PhoneVerificationType.getOauthSignUpTypeByProvider(provider));

        return PhoneVerificationDto.VerifyCodeRes.valueOfOauth(isValidCode, isSignUpUser.getLeft(), isSignUpUser.getRight());
    }

    @Transactional
    public Pair<Long, Jwts> signUp(Provider provider, SignUpReq.OauthInfo request) {
        phoneVerificationMapper.isValidCode(PhoneVerificationDto.VerifyCodeReq.from(request), PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
        Pair<Boolean, String> isSignUpUser = checkSignUpUserNotOauthByProvider(provider, request.phone());

        OidcDecodePayload payload = oauthOidcHelper.getPayload(provider, request.idToken());
        User user = userOauthSignMapper.saveUser(request, isSignUpUser, provider, payload.sub());
        phoneVerificationService.delete(request.phone(), PhoneVerificationType.getOauthSignUpTypeByProvider(provider));

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user));
    }

    /**
     * Oauth 회원가입 진행 도중, Provider로 가입한 사용자인지 지속적으로 검증하기 위한 메서드
     */
    private Pair<Boolean, String> checkSignUpUserNotOauthByProvider(Provider provider, String phone) {
        Pair<Boolean, String> isOauthSignUpAllowed = userSyncMapper.isOauthSignUpAllowed(provider, phone);

        if (isOauthSignUpAllowed == null) {
            phoneVerificationService.delete(phone, PhoneVerificationType.getOauthSignUpTypeByProvider(provider));
            throw new OauthException(OauthErrorCode.ALREADY_SIGNUP_OAUTH);
        }

        return isOauthSignUpAllowed;
    }
}
