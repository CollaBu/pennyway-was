package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByUser_IdAndToken(Long userId, String token);

    List<DeviceToken> findAllByUser_Id(Long userId);
}
