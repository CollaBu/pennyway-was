package kr.co.pennyway.batch.processor;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessor implements ItemProcessor<DeviceTokenOwner, DeviceTokenOwner> {

    @Override
    public DeviceTokenOwner process(@NonNull DeviceTokenOwner deviceTokenOwner) throws Exception {
        return deviceTokenOwner;
    }
}
