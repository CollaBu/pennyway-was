package kr.co.pennyway.api.apis.notification.usecase;

import kr.co.pennyway.api.apis.notification.dto.NotificationDto;
import kr.co.pennyway.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class NotificationUseCase {

    public NotificationDto.SliceRes getNotifications(Long userId, Pageable pageable) {
        return null;
    }
}
