package kr.co.pennyway.api.apis.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/notifications")
public class NotificationController {
    private final NotificationUseCase notificationUseCase;
}
