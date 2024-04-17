package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.domain.Device;
import org.springframework.data.repository.CrudRepository;

public interface DeviceRepository extends CrudRepository<Device, Long> {
}
