package kr.co.pennyway.api.apis.auth.usecase;

import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class AuthCheckUseCase {
    private final UserService userService;

    @Transactional(readOnly = true)
    public boolean checkUsernameDuplicate(String username) {
        return userService.isExistUsername(username);
    }
}
