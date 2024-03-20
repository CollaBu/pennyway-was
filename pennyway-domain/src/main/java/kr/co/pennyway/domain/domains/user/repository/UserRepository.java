package kr.co.pennyway.domain.domains.user.repository;

import kr.co.pennyway.domain.domains.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
