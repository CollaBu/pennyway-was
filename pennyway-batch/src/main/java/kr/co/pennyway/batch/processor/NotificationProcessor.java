package kr.co.pennyway.batch.processor;

import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessor implements ItemProcessor<DeviceTokenOwner, DeviceTokenOwner> {

    @Override
    public DeviceTokenOwner process(DeviceTokenOwner deviceTokenOwner) throws Exception {
        // 맞춤형 작업 실행
        return deviceTokenOwner;
    }
}
