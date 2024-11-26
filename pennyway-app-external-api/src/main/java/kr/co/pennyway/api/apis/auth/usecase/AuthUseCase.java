package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.dto.UserSyncDto;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.api.apis.auth.service.UserGeneralSignService;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.context.account.service.PhoneCodeService;
import kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType;
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

    public PhoneVerificationDto.VerifyCodeRes verifyCode(PhoneVerificationDto.VerifyCodeReq request) {
        Boolean isValidCode = phoneVerificationService.isValidCode(request, PhoneCodeKeyType.SIGN_UP);
        UserSyncDto userSync = checkOauthUserNotGeneralSignUp(request.phone());

        phoneCodeService.extendTimeToLeave(request.phone(), PhoneCodeKeyType.SIGN_UP);

        return PhoneVerificationDto.VerifyCodeRes.valueOfGeneral(isValidCode, userSync.isExistAccount(), userSync.username());
    }

    @Transactional
    public Pair<Long, Jwts> signUp(SignUpReq.Info request) {
        phoneVerificationService.isValidCode(PhoneVerificationDto.VerifyCodeReq.from(request), PhoneCodeKeyType.SIGN_UP);
        phoneCodeService.delete(request.phone(), PhoneCodeKeyType.SIGN_UP);

        UserSyncDto userSync = checkOauthUserNotGeneralSignUp(request.phone());
        User user = userGeneralSignService.saveUserWithEncryptedPassword(request, userSync);

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user, request.deviceId()));
    }

    @Transactional(readOnly = true)
    public Pair<Long, Jwts> signIn(SignInReq.General request) {
        User user = userGeneralSignService.readUserIfValid(request.username(), request.password());

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user, request.deviceId()));
    }

    public Pair<Long, Jwts> refresh(String refreshToken) {
        return jwtAuthHelper.refresh(refreshToken);
    }

    private UserSyncDto checkOauthUserNotGeneralSignUp(String phone) {
        UserSyncDto userSync = userGeneralSignService.isSignUpAllowed(phone);

        if (!userSync.isSignUpAllowed()) {
            phoneCodeService.delete(phone, PhoneCodeKeyType.SIGN_UP);
            throw new UserErrorException(UserErrorCode.ALREADY_SIGNUP);
        }

        return userSync;
    }
}
