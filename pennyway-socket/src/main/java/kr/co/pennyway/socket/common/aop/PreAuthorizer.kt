package kr.co.pennyway.socket.common.aop

import kr.co.pennyway.socket.common.exception.PreAuthorizeErrorCode
import kr.co.pennyway.socket.common.exception.PreAuthorizeErrorException
import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import kr.co.pennyway.socket.common.util.logger
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.lang.reflect.ParameterizedType
import java.security.Principal
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.javaType

@Component
class PreAuthorizer(
    _preAuthorizeAdvice: PreAuthorizeAdvice
) {
    init {
        preAuthorizeAdvice = _preAuthorizeAdvice
    }

    companion object {
        private lateinit var preAuthorizeAdvice: PreAuthorizeAdvice
        private val log = logger()

        /**
         * 모든 요청을 허용합니다.
         */
        fun <T> permitAll(function: () -> T): T = function.invoke()

        /**
         * 사용자의 인증 여부만 검증합니다.
         * @param principal 사용자 정보
         * @throws 인증되지 않은 사용자인 경우 {@link PreAuthorizeErrorException} 예외를 발생시킵니다.
         */
        fun <T> authenticate(
            principal: Principal,
            function: () -> T
        ): T {
            when (isAuthenticated(principal)) {
                true -> return function.invoke()
                false -> {
                    log.warn("인증 실패: {}", principal)
                    throw PreAuthorizeErrorException(PreAuthorizeErrorCode.UNAUTHENTICATED)
                }
            }
        }

        /**
         * 사용자의 인가 여부만 검증합니다.
         * @param serviceClass 서비스 클래스
         * @param args 메서드 참조에 필요한 인자
         * @throws 인가되지 않은 사용자인 경우 {@link PreAuthorizeErrorException} 예외를 발생시킵니다.
         */
        fun <T, R> authorize(
            serviceClass: KClass<T>,
            methodName: String,
            vararg args: Any?,
            function: () -> R
        ): R where T : Any {
            return preAuthorizeAdvice.run(
                serviceClass = serviceClass,
                methodName = methodName,
                function = function,
                args = args
            )
        }

        /**
         * 사용자의 인증 및 인가 여부를 검증합니다.
         * @param principal 사용자 정보
         * @param methodReference 인가 검증을 위한 메서드 참조
         * @param args 메서드 참조에 필요한 인자
         * @throws 인증되지 않은 사용자인 경우 {@link PreAuthorizeErrorException} 예외를 발생시킵니다.
         * @throws 인가되지 않은 사용자인 경우 {@link PreAuthorizeErrorException} 예외를 발생시킵니다.
         */
        fun <T, R> authorize(
            principal: Principal,
            serviceClass: KClass<T>,
            methodName: String,
            vararg args: Any?,
            function: () -> R
        ): R where T : Any {
            when (isAuthenticated(principal)) {
                true -> return preAuthorizeAdvice.run(
                    serviceClass = serviceClass,
                    methodName = methodName,
                    function = function,
                    args = args
                )

                false -> {
                    log.warn("인증 실패: {}", principal)
                    throw PreAuthorizeErrorException(PreAuthorizeErrorCode.UNAUTHENTICATED)
                }
            }
        }

        /**
         * 현재 사용자가 인증되어 있는지 확인합니다.
         * @return principal 초기화되지 않았거나, 인증된 상태라면 true, 그렇지 않으면 false
         */
        private fun isAuthenticated(principal: Principal?): Boolean = when (principal) {
            is UserPrincipal -> principal.isAuthenticated()
            else -> throw IllegalArgumentException("Principal must be UserPrincipal")
        }
    }

    @Component
    class PreAuthorizeAdvice(private val applicationContext: ApplicationContext) {
        @OptIn(ExperimentalStdlibApi::class)
        fun <T, R> run(
            serviceClass: KClass<T>,
            methodName: String,
            vararg args: Any?,
            function: () -> R
        ): R where T : Any {
            val manager = applicationContext.getBean(serviceClass.java)

            val method = serviceClass.memberFunctions.find { it.name == methodName }
                ?: throw NoSuchMethodException("$methodName not found in ${serviceClass.qualifiedName}")
            val parameterTypes = method.parameters.drop(1).map { it.type.javaType }

            val javaMethod = serviceClass.java.getDeclaredMethod(
                methodName,
                *parameterTypes.map {
                    when (it) {
                        is Class<*> -> it  // 이미 Class 객체라면 그대로 사용
                        is ParameterizedType -> it.rawType as Class<*>  // 제네릭 타입이라면 raw type 사용
                        else -> throw IllegalStateException("Unsupported type: $it")
                    }
                }.toTypedArray()
            )

            val result = javaMethod.invoke(manager, *args) as? Boolean
                ?: throw IllegalArgumentException("Method must return Boolean")

            if (!result) {
                log.warn("인가 실패: {}", args)
                throw PreAuthorizeErrorException(PreAuthorizeErrorCode.FORBIDDEN)
            }

            return function.invoke()
        }
    }
}