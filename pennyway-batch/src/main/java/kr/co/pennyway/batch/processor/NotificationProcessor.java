package kr.co.pennyway.batch.processor;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationProcessor implements ItemProcessor<DeviceTokenOwner, DeviceTokenOwner> {

    @Override
    public DeviceTokenOwner process(@NonNull DeviceTokenOwner deviceTokenOwner) throws Exception {
        log.info("NotificationProcessor 실행");
        return deviceTokenOwner;
    }
}
