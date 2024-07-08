package kr.co.pennyway.batch.processor;

import kr.co.pennyway.batch.dto.DailySpendingNotification;
import kr.co.pennyway.domain.domains.device.dto.DeviceTokenOwner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessor implements ItemProcessor<DeviceTokenOwner, DailySpendingNotification> {

    @Override
    public DailySpendingNotification process(@NonNull DeviceTokenOwner deviceTokenOwner) throws Exception {
        return DailySpendingNotification.from(deviceTokenOwner);
    }
}
