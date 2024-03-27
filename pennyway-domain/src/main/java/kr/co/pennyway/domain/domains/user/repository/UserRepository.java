package kr.co.pennyway.domain.domains.user.repository;

import kr.co.pennyway.domain.domains.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);

    Optional<User> findByUsername(String username);
}
