package kr.co.pennyway.api.apis.auth.usecase;

import org.springframework.transaction.annotation.Transactional;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.dto.PhoneVerificationDto;
import kr.co.pennyway.api.apis.auth.mapper.PhoneVerificationMapper;
import kr.co.pennyway.api.apis.auth.service.AuthFindService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AuthCheckUseCase {
	private final UserService userService;
	private final AuthFindService authFindService;
	private final PhoneVerificationMapper phoneVerificationMapper;
	private final PhoneVerificationService phoneVerificationService;

	@Transactional(readOnly = true)
	public boolean checkUsernameDuplicate(String username) {
		return userService.isExistUsername(username);
	}

	@Transactional(readOnly = true)
	public AuthFindDto.FindUsernameRes findUsername(String phone, String code) {
		phoneVerificationMapper.isValidCode(PhoneVerificationDto.VerifyCodeReq.of(phone, code), PhoneVerificationType.FIND_USERNAME);
		phoneVerificationService.delete(phone, PhoneVerificationType.FIND_USERNAME);
		return authFindService.findUsername(phone);
	}
}
