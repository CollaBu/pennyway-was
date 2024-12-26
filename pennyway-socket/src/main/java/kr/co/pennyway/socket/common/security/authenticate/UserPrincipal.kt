package kr.co.pennyway.socket.common.security.authenticate;

import kr.co.pennyway.domain.domains.user.domain.User
import kr.co.pennyway.domain.domains.user.type.Role
import java.security.Principal
import java.time.LocalDateTime

data class UserPrincipal(
    val userId: Long,
    private var _name: String,
    var username: String,
    var role: Role,
    var isChatNotify: Boolean,
    var expiresAt: LocalDateTime,
    var deviceId: String,
    var deviceName: String
) : Principal {
    fun isAuthenticated(): Boolean = !isExpired()

    fun updateExpiresAt(newExpiresAt: LocalDateTime) {
        this.expiresAt = newExpiresAt
    }

    override fun getName(): String = userId.toString()

    fun getDefaultName(): String = _name

    private fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    companion object {
        @JvmStatic
        fun of(
            user: User,
            expiresAt: LocalDateTime,
            deviceId: String,
            deviceName: String
        ): UserPrincipal = UserPrincipal(
            userId = user.id,
            _name = user.name,
            username = user.username,
            role = user.role,
            isChatNotify = user.notifySetting.isChatNotify,
            expiresAt = expiresAt,
            deviceId = deviceId,
            deviceName = deviceName
        )
    }
}