package kr.co.pennyway.api.apis.auth.mapper;

import kr.co.pennyway.api.apis.auth.dto.AuthFindDto;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
@RequiredArgsConstructor
public class AuthFindMapper {
	private final UserService userService;

	public AuthFindDto.FindUsernameRes findUsername(String phone) {
		return AuthFindDto.FindUsernameRes.of(userService.readUserByPhone(phone).orElseThrow(() -> {
			log.error("User not found by phone: {}", phone);
			return new RuntimeException("User not found by phone: " + phone);
		}));
	}
}
