package kr.co.pennyway.api.apis.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
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

	/**
	 * 일반 회원 아이디 찾기
	 *
	 * @param phone 전화번호 (e.g. 010-1234-5678)
	 * @return AuthFindDto.FindPasswordRes 비밀번호 찾기 응답
	 */
	@Transactional(readOnly = true)
	public AuthFindDto.FindUsernameRes findUsername(String phone) {
		User user = userService.readUserByPhone(phone).orElseThrow(() -> {
			log.info("User not found by phone: {}", phone);
			return new UserErrorException(UserErrorCode.NOT_FOUND);
		});

		// 일반 회원 여부 검증
		if (user.getPassword() == null) {
			log.info("User not found by phone: {}", phone);
			throw new UserErrorException(UserErrorCode.NOT_FOUND);
		}

		return AuthFindDto.FindUsernameRes.of(user);
	}
}
