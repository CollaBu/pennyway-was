package kr.co.pennyway.api.apis.auth.usecase;

import org.springframework.transaction.annotation.Transactional;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.api.apis.auth.mapper.AuthFindService;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AuthCheckUseCase {
	private final UserService userService;
	private final AuthFindService authFindService;

	@Transactional(readOnly = true)
	public boolean checkUsernameDuplicate(String username) {
		return userService.isExistUsername(username);
	}

	@Transactional(readOnly = true)
	public AuthFindDto.FindUsernameRes findUsername(String phone) {
		return authFindService.findUsername(phone);
	}
}
