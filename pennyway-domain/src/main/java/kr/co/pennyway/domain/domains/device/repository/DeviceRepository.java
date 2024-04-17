package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.domain.Device;
import org.springframework.data.repository.ListCrudRepository;

public interface DeviceRepository extends ListCrudRepository<Device, Long> {
}
