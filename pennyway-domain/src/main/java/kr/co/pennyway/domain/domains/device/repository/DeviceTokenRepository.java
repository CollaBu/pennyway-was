package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long>, DeviceTokenCustomRepository {
    Optional<DeviceToken> findByUser_IdAndToken(Long userId, String token);

    List<DeviceToken> findAllByUser_Id(Long userId);

    Page<DeviceToken> findByActivatedIsTrue(Pageable pageable);
}
