package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    @Query("SELECT d FROM DeviceToken d WHERE d.user.id = :userId AND d.token = :token")
    Optional<DeviceToken> findByUser_IdAndToken(Long userId, String token);

    List<DeviceToken> findAllByUser_Id(Long userId);

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findAllByUser_IdAndDeviceId(Long userId, String deviceId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE DeviceToken d SET d.activated = false WHERE d.user.id = :userId")
    void deleteAllByUserIdInQuery(Long userId);
}
