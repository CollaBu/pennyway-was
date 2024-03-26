package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.dto.SignUpReq;
import kr.co.pennyway.api.apis.auth.helper.UserSyncHelper;
import kr.co.pennyway.api.apis.auth.mapper.JwtAuthMapper;
import kr.co.pennyway.api.apis.auth.mapper.PhoneVerificationMapper;
import kr.co.pennyway.api.common.security.jwt.Jwts;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AuthUseCase {
    private final UserService userService;
    private final UserSyncHelper userSyncHelper;

    private final JwtAuthMapper jwtAuthMapper;
    private final PhoneVerificationMapper phoneVerificationMapper;
    private final PhoneVerificationService phoneVerificationService;

    public PhoneVerificationDto.PushCodeRes sendCode(PhoneVerificationDto.PushCodeReq request) {
        return phoneVerificationMapper.sendCode(request, PhoneVerificationType.SIGN_UP);
    }

    public PhoneVerificationDto.VerifyCodeRes verifyCode(PhoneVerificationDto.VerifyCodeReq request) {
        Boolean isValidCode = phoneVerificationMapper.isValidCode(request, PhoneVerificationType.SIGN_UP);
        Pair<Boolean, String> isOauthUser = checkOauthUser(request.phone());

        phoneVerificationService.extendTimeToLeave(request.phone(), PhoneVerificationType.SIGN_UP);

        return PhoneVerificationDto.VerifyCodeRes.valueOf(isValidCode, isOauthUser.getKey(), isOauthUser.getValue());
    }

    @Transactional
    public Pair<Long, Jwts> signUp(SignUpReq.General request) {
        // TODO: 인증 번호 확인 로직 추가
        // phoneVerificationHelper.verify(request.phone(), request.code());

        User user = userService.createUser(request.toEntity());

        return Pair.of(user.getId(), jwtAuthMapper.createToken(user));
    }

    private Pair<Boolean, String> checkOauthUser(String phone) {
        try {
            return userSyncHelper.isSignedUserWhenGeneral(phone);
        } catch (UserErrorException e) {
            phoneVerificationService.delete(phone, PhoneVerificationType.SIGN_UP);
            throw e;
        }
    }
}
