package kr.co.pennyway.domain.domains.user.repository;

import kr.co.pennyway.domain.common.repository.ExtendedRepository;
import kr.co.pennyway.domain.domains.user.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends ExtendedRepository<User, Long> {
    Optional<User> findByPhone(String phone);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.deletedAt = NOW() WHERE u.id = :userId")
    void deleteByIdInQuery(Long userId);
}
