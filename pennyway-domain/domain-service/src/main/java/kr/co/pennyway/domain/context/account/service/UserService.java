package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class UserService {
    private final UserRdbService userRdbService;

    @Transactional
    public User createUser(User user) {
        return userRdbService.createUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> readUser(Long id) {
        return userRdbService.readUser(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> readUserByPhone(String phone) {
        return userRdbService.readUserByPhone(phone);
    }

    @Transactional(readOnly = true)
    public Optional<User> readUserByUsername(String username) {
        return userRdbService.readUserByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isExistUser(Long id) {
        return userRdbService.isExistUser(id);
    }

    @Transactional(readOnly = true)
    public boolean isExistUsername(String username) {
        return userRdbService.isExistUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isExistPhone(String phone) {
        return userRdbService.isExistPhone(phone);
    }

    @Transactional
    public void deleteUser(User user) {
        userRdbService.deleteUser(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRdbService.deleteUser(userId);
    }
}
