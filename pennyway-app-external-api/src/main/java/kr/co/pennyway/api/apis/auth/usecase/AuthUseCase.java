package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.api.apis.auth.service.UserGeneralSignService;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeKeyType;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AuthUseCase {
    private final UserGeneralSignService userGeneralSignService;

    private final JwtAuthHelper jwtAuthHelper;
    private final PhoneVerificationService phoneVerificationService;
    private final PhoneCodeService phoneCodeService;

    public PhoneVerificationDto.PushCodeRes sendCode(PhoneVerificationDto.PushCodeReq request) {
        return phoneVerificationService.sendCode(request, PhoneCodeKeyType.SIGN_UP);
    }

    public PhoneVerificationDto.VerifyCodeRes verifyCode(PhoneVerificationDto.VerifyCodeReq request) {
        Boolean isValidCode = phoneVerificationService.isValidCode(request, PhoneCodeKeyType.SIGN_UP);
        Pair<Boolean, String> isOauthUser = checkOauthUserNotGeneralSignUp(request.phone());

        phoneCodeService.extendTimeToLeave(request.phone(), PhoneCodeKeyType.SIGN_UP);

        return PhoneVerificationDto.VerifyCodeRes.valueOfGeneral(isValidCode, isOauthUser.getLeft(), isOauthUser.getRight());
    }

    @Transactional
    public Pair<Long, Jwts> signUp(SignUpReq.Info request) {
        phoneVerificationService.isValidCode(PhoneVerificationDto.VerifyCodeReq.from(request), PhoneCodeKeyType.SIGN_UP);
        Pair<Boolean, String> isOauthUser = checkOauthUserNotGeneralSignUp(request.phone());

        User user = userGeneralSignService.saveUserWithEncryptedPassword(request, isOauthUser);
        phoneCodeService.delete(request.phone(), PhoneCodeKeyType.SIGN_UP);

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user));
    }

    @Transactional(readOnly = true)
    public Pair<Long, Jwts> signIn(SignInReq.General request) {
        User user = userGeneralSignService.readUserIfValid(request.username(), request.password());

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user));
    }

    public Pair<Long, Jwts> refresh(String refreshToken) {
        return jwtAuthHelper.refresh(refreshToken);
    }

    private Pair<Boolean, String> checkOauthUserNotGeneralSignUp(String phone) {
        Pair<Boolean, String> isGeneralSignUpAllowed = userGeneralSignService.isSignUpAllowed(phone);

        if (isGeneralSignUpAllowed == null) {
            phoneCodeService.delete(phone, PhoneCodeKeyType.SIGN_UP);
            throw new UserErrorException(UserErrorCode.ALREADY_SIGNUP);
        }

        return isGeneralSignUpAllowed;
    }
}
