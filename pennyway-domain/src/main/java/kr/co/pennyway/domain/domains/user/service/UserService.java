package kr.co.pennyway.domain.domains.user.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@DomainService
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User readUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserErrorException(UserErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public boolean isExistUser(Long id) {
        return userRepository.existsById(id);
    }
}
