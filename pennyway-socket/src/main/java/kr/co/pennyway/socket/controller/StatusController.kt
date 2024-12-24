package kr.co.pennyway.socket.controller;

import kr.co.pennyway.socket.common.annotation.PreAuthorize
import kr.co.pennyway.socket.common.dto.StatusMessage
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import kr.co.pennyway.socket.service.StatusService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller

@Controller
class StatusController(private val statusService: StatusService) {
    @MessageMapping("status.me")
    @PreAuthorize("#isAuthenticated(#principal)")
    fun updateStatus(principal: UserPrincipal, message: StatusMessage, accessor: StompHeaderAccessor) {
        statusService.updateStatus(principal.userId, principal.deviceId, message, accessor);
    }
}
