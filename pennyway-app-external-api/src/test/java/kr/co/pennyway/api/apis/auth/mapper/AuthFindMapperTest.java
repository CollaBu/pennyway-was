package kr.co.pennyway.api.apis.auth.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kr.co.pennyway.api.common.exception.AuthFindException;
import kr.co.pennyway.domain.domains.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthFindMapperTest {
	private AuthFindMapper authFindMapper;
	@Mock
	private UserService userService;

	@BeforeEach
	void setUp() {
		authFindMapper = new AuthFindMapper(userService);
	}

	@DisplayName("휴대폰 번호로 유저를 찾을 수 없을 때 AuthFinderException을 발생시킨다.")
	@Test
	void findUsernameIfUserNotFound() {
		// given
		String phone = "010-1234-5678";
		given(userService.readUserByPhone(phone)).willReturn(Optional.empty());

		// when - then
		AuthFindException exception = assertThrows(AuthFindException.class, () -> authFindMapper.findUsername(phone));
		System.out.println(exception.getExplainError());
	}

}