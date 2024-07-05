package kr.co.pennyway.domain.domains.device.repository;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceTokenCustomRepository {
    Page<DeviceTokenOwner> findActivatedDeviceTokenOwners(Pageable pageable);
}
