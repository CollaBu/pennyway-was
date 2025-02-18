package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.service.AuthFindService;
import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.context.account.service.PhoneCodeService;
import kr.co.pennyway.domain.context.account.service.UserService;
import kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AuthCheckUseCase {
    private final UserService userService;
    private final AuthFindService authFindService;
    private final PhoneVerificationService phoneVerificationService;
    private final PhoneCodeService phoneCodeService;

    @Transactional(readOnly = true)
    public boolean checkUsernameDuplicate(String username) {
        return userService.isExistUsername(username);
    }

    @Transactional(readOnly = true)
    public AuthFindDto.FindUsernameRes findUsername(PhoneVerificationDto.VerifyCodeReq request) {
        phoneVerificationService.isValidCode(request, PhoneCodeKeyType.FIND_USERNAME);
        phoneCodeService.delete(request.phone(), PhoneCodeKeyType.FIND_USERNAME);
        return authFindService.findUsername(request.phone());
    }

    public void verifyCode(PhoneVerificationDto.VerifyCodeReq request) {
        phoneVerificationService.isValidCode(request, PhoneCodeKeyType.FIND_PASSWORD);
        authFindService.existsGeneralSignUpUser(request.phone());
        phoneCodeService.extendTimeToLeave(request.phone(), PhoneCodeKeyType.FIND_PASSWORD);
    }

    public void findPassword(PhoneVerificationDto.VerifyCodeReq request, String passwordReq) {
        phoneVerificationService.isValidCode(request, PhoneCodeKeyType.FIND_PASSWORD);
        phoneCodeService.delete(request.phone(), PhoneCodeKeyType.FIND_PASSWORD);
        authFindService.updatePassword(request.phone(), passwordReq);
    }
}