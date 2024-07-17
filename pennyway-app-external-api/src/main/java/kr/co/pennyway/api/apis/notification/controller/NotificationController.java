package kr.co.pennyway.api.apis.notification.controller;

import kr.co.pennyway.api.apis.notification.api.NotificationApi;
import kr.co.pennyway.api.apis.notification.usecase.NotificationUseCase;
import kr.co.pennyway.api.common.response.SuccessResponse;
import kr.co.pennyway.api.common.security.authentication.SecurityUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/notifications")
public class NotificationController implements NotificationApi {
    private static final String NOTIFICATIONS = "notifications";

    private final NotificationUseCase notificationUseCase;

    @Override
    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getNotifications(
            @PageableDefault(page = 0, size = 30) @SortDefault(sort = "notification.createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SecurityUserDetails user
    ) {
        return ResponseEntity.ok(SuccessResponse.from(NOTIFICATIONS, notificationUseCase.getNotifications(user.getUserId(), pageable)));
    }
}
