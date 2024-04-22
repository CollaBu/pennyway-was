package kr.co.pennyway.api.apis.auth.usecase;

import org.springframework.transaction.annotation.Transactional;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.service.AuthFindService;
import kr.co.pennyway.api.apis.auth.service.PhoneVerificationService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeKeyType;
import kr.co.pennyway.domain.common.redis.phone.PhoneCodeService;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	public AuthFindDto.FindUsernameRes findUsername(String phone, String code) {
		phoneVerificationService.isValidCode(PhoneVerificationDto.VerifyCodeReq.of(phone, code), PhoneCodeKeyType.FIND_USERNAME);
		phoneCodeService.delete(phone, PhoneCodeKeyType.FIND_USERNAME);
		return authFindService.findUsername(phone);
	}
}
