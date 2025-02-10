package kr.co.pennyway.socket.service;

import kr.co.pennyway.infra.common.exception.JwtErrorException
import kr.co.pennyway.socket.common.dto.ServerSideMessage
import kr.co.pennyway.socket.common.event.ReceiptEvent
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import kr.co.pennyway.socket.common.security.jwt.AccessTokenProvider
import kr.co.pennyway.socket.common.util.logger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.messaging.Message
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val accessTokenProvider: AccessTokenProvider,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = logger()

    fun refreshPrincipal(
        token: String,
        principal: UserPrincipal,
        accessor: StompHeaderAccessor
    ) {
        val message = try {
            val expiresAt = accessTokenProvider.getExpiryDate(token)
            principal.updateExpiresAt(expiresAt)

            createMessage("2000", "토큰 갱신 성공", accessor)
        } catch (e: JwtErrorException) {
            log.warn("refresh failed: {}", e.errorCode.explainError)

            createMessage(code = e.causedBy().code, message = e.errorCode.explainError, accessor = accessor)
        }

        eventPublisher.publishEvent(ReceiptEvent.of(message))
    }

    private fun createMessage(
        code: String,
        message: String,
        accessor: StompHeaderAccessor
    ): Message<ServerSideMessage> {
        val payload = ServerSideMessage.of(code, message)
        return MessageBuilder.createMessage(payload, accessor.messageHeaders)
    }
}
