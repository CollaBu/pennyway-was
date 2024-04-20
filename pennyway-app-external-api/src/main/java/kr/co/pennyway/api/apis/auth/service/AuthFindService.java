package kr.co.pennyway.api.apis.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationService;
import kr.co.pennyway.domain.common.redis.phone.PhoneVerificationType;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthFindService {
	private final UserService userService;
	private final PhoneVerificationService phoneVerificationService;

	@Transactional(readOnly = true)
	public AuthFindDto.FindUsernameRes findUsername(String phone) {
		try {
			phoneVerificationService.readByPhone(phone, PhoneVerificationType.FIND_USERNAME);
		} catch (IllegalArgumentException e) {
			log.info("Phone verification code not found by phone: {}", phone);
			throw new UserErrorException(UserErrorCode.NOT_FOUND);
		}

		User user = userService.readUserByPhone(phone).orElseThrow(() -> {
			log.info("User not found by phone: {}", phone);
			return new UserErrorException(UserErrorCode.NOT_FOUND);
		});

		if (user.getPassword() == null) {
			log.info("User not found by phone: {}", phone);
			throw new UserErrorException(UserErrorCode.NOT_FOUND);
		}

		phoneVerificationService.delete(phone, PhoneVerificationType.FIND_USERNAME);

		return AuthFindDto.FindUsernameRes.of(user);
	}
}
