package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignInReq;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.helper.JwtAuthHelper;
import kr.co.pennyway.api.apis.auth.mapper.PhoneVerificationMapper;
import kr.co.pennyway.api.apis.auth.mapper.UserGeneralSignMapper;
import kr.co.pennyway.api.apis.auth.mapper.UserSyncMapper;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
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
    private final UserSyncMapper userSyncMapper;
    private final UserGeneralSignMapper userGeneralSignMapper;

    private final JwtAuthHelper jwtAuthHelper;
    private final PhoneVerificationMapper phoneVerificationMapper;
    private final PhoneVerificationService phoneVerificationService;

    public PhoneVerificationDto.PushCodeRes sendCode(PhoneVerificationDto.PushCodeReq request) {
        return phoneVerificationMapper.sendCode(request, PhoneVerificationType.SIGN_UP);
    }

    public PhoneVerificationDto.VerifyCodeRes verifyCode(PhoneVerificationDto.VerifyCodeReq request) {
        Boolean isValidCode = phoneVerificationMapper.isValidCode(request, PhoneVerificationType.SIGN_UP);
        Pair<Boolean, String> isOauthUser = checkOauthUserNotGeneralSignUp(request.phone());

        phoneVerificationService.extendTimeToLeave(request.phone(), PhoneVerificationType.SIGN_UP);

        return PhoneVerificationDto.VerifyCodeRes.valueOf(isValidCode, isOauthUser.getLeft(), isOauthUser.getRight());
    }

    @Transactional
    public Pair<Long, Jwts> signUp(SignUpReq.Info request) {
        phoneVerificationMapper.isValidCode(PhoneVerificationDto.VerifyCodeReq.from(request), PhoneVerificationType.SIGN_UP);
        Pair<Boolean, String> isOauthUser = checkOauthUserNotGeneralSignUp(request.phone());

        User user = userGeneralSignMapper.saveUserWithEncryptedPassword(request, isOauthUser);
        phoneVerificationService.delete(request.phone(), PhoneVerificationType.SIGN_UP);

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user));
    }

    @Transactional(readOnly = true)
    public Pair<Long, Jwts> signIn(SignInReq.General request) {
        User user = userGeneralSignMapper.readUserIfValid(request.username(), request.password());

        return Pair.of(user.getId(), jwtAuthHelper.createToken(user));
    }

    public Pair<Long, Jwts> refresh(String refreshToken) {
        return jwtAuthHelper.refresh(refreshToken);
    }

    private Pair<Boolean, String> checkOauthUserNotGeneralSignUp(String phone) {
        Pair<Boolean, String> isGeneralSignUpAllowed = userSyncMapper.isGeneralSignUpAllowed(phone);

        if (isGeneralSignUpAllowed == null) {
            phoneVerificationService.delete(phone, PhoneVerificationType.SIGN_UP);
            throw new UserErrorException(UserErrorCode.ALREADY_SIGNUP);
        }

        return isGeneralSignUpAllowed;
    }
}
