package kr.co.pennyway.socket.controller;

import kr.co.pennyway.infra.common.exception.JwtErrorCode
import kr.co.pennyway.infra.common.exception.JwtErrorException
import kr.co.pennyway.socket.common.annotation.PreAuthorize
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import kr.co.pennyway.socket.common.util.logger
import kr.co.pennyway.socket.service.AuthService
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class AuthController(private val authService: AuthService) {
    private val log = logger()

    @MessageMapping("auth.refresh")
    @PreAuthorize("#principal instanceof T(kr.co.pennyway.socket.common.security.authenticate.UserPrincipal)")
    fun refreshPrincipal(
        @Header("Authorization") authorization: String?,
        principal: Principal,
        accessor: StompHeaderAccessor
    ) {
        val token = authorization
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)
            ?: run {
                log.warn("Authorization header is null or invalid")
                throw JwtErrorException(JwtErrorCode.EMPTY_ACCESS_TOKEN)
            }

        val userPrincipal = principal as? UserPrincipal
            ?: run {
                log.warn("Principal is not an instance of UserPrincipal")
                throw IllegalArgumentException("Principal must be UserPrincipal")
            }

        authService.refreshPrincipal(token, userPrincipal, accessor)
    }
}
