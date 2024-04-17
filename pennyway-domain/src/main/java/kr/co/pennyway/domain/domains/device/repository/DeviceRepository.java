package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.domain.Device;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface DeviceRepository extends ListCrudRepository<Device, Long> {
    List<Device> findAllByUser_Id(Long userId);
}
