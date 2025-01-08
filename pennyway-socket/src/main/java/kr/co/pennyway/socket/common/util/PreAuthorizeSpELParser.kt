package kr.co.pennyway.socket.common.util;

import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal
import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.lang.reflect.Method
import java.security.Principal

/**
 * WebSocket 인증 및 인가를 위한 Spring Expression Language (SpEL) 파서.
 * 이 클래스는 WebSocket 연결에서 사용되는 다양한 인증/인가 함수를 제공하고,
 * SpEL 표현식을 평가하는 기능을 제공합니다.
 *
 * @author YANG JAESEO
 * @version 1.1.0
 * @since 2024.12.25
 */
object PreAuthorizeSpELParser {
    private val parser = SpelExpressionParser()
    private val context = StandardEvaluationContext().apply {
        initializeContext()
    }

    sealed interface EvaluationResult {
        object Permitted : EvaluationResult
        sealed interface Denied : EvaluationResult {
            object Unauthenticated : Denied
            object Unauthorized : Denied
        }
    }

    private fun StandardEvaluationContext.initializeContext() = apply {
        SpELFunction.values().forEach { function -> registerSpELFunction(function) }
    }

    private fun StandardEvaluationContext.registerSpELFunction(function: SpELFunction) {
        runCatching {
            PreAuthorizeSpELParser::class.java
                .getDeclaredMethod(function.methodName, *function.parameterTypes)
                .let { method -> registerFunction(function.level, method) }
        }.onFailure { e ->
            throw RuntimeException("Error registering SpEL function: ${function.level}", e)
        }
    }

    /**
     * 주어진 SpEL 표현식을 평가합니다.
     */
    @Synchronized
    fun evaluate(
        expression: String,
        method: Method,
        args: Array<out Any>,
        applicationContext: ApplicationContext
    ): EvaluationResult = context.run {
        setupContext(method, args, applicationContext)
        evaluateExpression(expression)
    }

    /**
     * SpEL 평가를 위해, 사용자의 Principal 객체와 메서드의 인자들을 EvaluationContext에 추가합니다.
     */
    private fun setupContext(
        method: Method,
        args: Array<out Any>,
        applicationContext: ApplicationContext
    ) {
        with(context) {
            context.setBeanResolver(BeanFactoryResolver(applicationContext))

            method.parameters.forEachIndexed { index, parameter ->
                setVariable(parameter.name, args[index])
            }
        }
    }

    private fun StandardEvaluationContext.evaluateExpression(
        expression: String
    ): EvaluationResult {
        val isAuthenticationRequired = expression.contains(SpELFunction.IS_AUTHENTICATED.level)

        val authenticationResult = when {
            isAuthenticationRequired -> evaluateAuthentication()
            else -> true
        }

        val authorizationResult = evaluateAuthorization(expression)

        return when {
            authenticationResult.not() -> EvaluationResult.Denied.Unauthenticated
            authorizationResult.not() -> EvaluationResult.Denied.Unauthorized
            else -> EvaluationResult.Permitted
        }
    }

    private fun StandardEvaluationContext.evaluateAuthentication(): Boolean =
        parser.parseExpression("#isAuthenticated(#principal)")
            .getValue(this, Boolean::class.java) ?: false

    private fun StandardEvaluationContext.evaluateAuthorization(expression: String): Boolean =
        parser.parseExpression(expression)
            .getValue(this, Boolean::class.java) ?: false

    /**
     * 모든 사용자에게 접근을 허용합니다.
     */
    @JvmStatic
    fun permitAll(): Boolean = true

    /**
     * 주어진 Principal이 인증된 사용자인지 확인합니다.
     */
    @JvmStatic
    fun isAuthenticated(principal: Principal): Boolean = when (principal) {
        is UserPrincipal -> principal.isAuthenticated()
        else -> false
    }

    enum class SpELFunction(
        val level: String,
        val methodName: String,
        vararg val parameterTypes: Class<*>
    ) {
        PERMIT_ALL("permitAll", "permitAll"),
        IS_AUTHENTICATED("isAuthenticated", "isAuthenticated", Principal::class.java);
    }
}