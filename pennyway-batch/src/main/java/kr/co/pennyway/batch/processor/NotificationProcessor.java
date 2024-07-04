package kr.co.pennyway.batch.processor;

import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessor implements ItemProcessor<DeviceToken, DeviceToken> {

    @Override
    public DeviceToken process(DeviceToken deviceToken) throws Exception {
        // 맞춤형 작업 실행
        return deviceToken;
    }
}
