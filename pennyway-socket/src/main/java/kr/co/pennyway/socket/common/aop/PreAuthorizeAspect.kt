package kr.co.pennyway.socket.common.aop;

import kr.co.pennyway.socket.common.annotation.PreAuthorize
import kr.co.pennyway.socket.common.exception.PreAuthorizeErrorCode
import kr.co.pennyway.socket.common.exception.PreAuthorizeErrorException
import kr.co.pennyway.socket.common.util.PreAuthorizeSpELParser
import kr.co.pennyway.socket.common.util.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.security.Principal

@Aspect
@Component
class PreAuthorizeAspect(private val applicationContext: ApplicationContext) {
    private val log = logger()

    /**
     * {@link PreAuthorize} 어노테이션이 붙은 메서드를 가로채고 인증/인가를 수행합니다.
     *
     * @param joinPoint 가로챈 메서드의 실행 지점
     * @return 인증/인가가 성공하면 원래 메서드의 실행 결과, 실패하면 UnauthorizedResponse
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("@annotation(kr.co.pennyway.socket.common.annotation.PreAuthorize)")
    fun execute(joinPoint: ProceedingJoinPoint): Any = with(joinPoint) {
        (signature as? MethodSignature)
            ?.method
            ?.let { method -> validateAccess(method, this) }
            ?: throw IllegalStateException("PreAuthorize는 메서드에만 적용할 수 있습니다")
    }

    private fun validateAccess(method: Method, joinPoint: ProceedingJoinPoint): Any {
        val preAuthorize = method.requireAnnotation<PreAuthorize>()
        val principal = joinPoint.args.findPrincipal()

        return evaluateAccess(
            principal = principal,
            preAuthorize = preAuthorize,
            method = method,
            args = joinPoint.args
        ).let { joinPoint.proceed() }
    }

    private fun evaluateAccess(
        principal: Principal?,
        preAuthorize: PreAuthorize,
        method: Method,
        args: Array<Any>
    ) = PreAuthorizeSpELParser
        .evaluate(preAuthorize.value, method, args, applicationContext)
        .also { result -> handleEvaluationResult(result, principal) }

    private fun handleEvaluationResult(
        result: PreAuthorizeSpELParser.EvaluationResult,
        principal: Principal?
    ) = when (result) {
        is PreAuthorizeSpELParser.EvaluationResult.Permitted -> Unit
        is PreAuthorizeSpELParser.EvaluationResult.Denied.Unauthenticated -> {
            log.warn("인증 실패: {}", principal)
            throw PreAuthorizeErrorException(PreAuthorizeErrorCode.UNAUTHENTICATED)
        }

        is PreAuthorizeSpELParser.EvaluationResult.Denied.Unauthorized -> {
            log.warn("인가 실패: {}", principal)
            throw PreAuthorizeErrorException(PreAuthorizeErrorCode.FORBIDDEN)
        }
    }

    private companion object {
        inline fun <reified T : Annotation> Method.requireAnnotation(): T =
            getAnnotation(T::class.java)
                ?: throw IllegalStateException("Required annotation ${T::class.simpleName} not found")

        fun Array<Any>.findPrincipal(): Principal? = asSequence()
            .filterIsInstance<Principal>()
            .firstOrNull()
    }
}