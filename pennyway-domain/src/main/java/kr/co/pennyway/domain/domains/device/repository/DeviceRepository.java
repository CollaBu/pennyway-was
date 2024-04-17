package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.domain.Device;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends ListCrudRepository<Device, Long> {
    Optional<Device> findByUser_IdAndToken(Long userId, String token);

    List<Device> findAllByUser_Id(Long userId);
}
