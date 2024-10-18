package kr.co.pennyway.api.common.aop;

import kr.co.pennyway.infra.common.jwt.AuthConstants;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Exteranl Api의 Request, Response 로그를 남기기 위한 Aspect
 *
 * @author YANG JAESEO
 */
@Slf4j
@Aspect
@Component
public class ExternalApiLogAspect {
    /**
     * kr.co.pennyway.api.apis 패키지 하위의 모든 Controller 클래스의 모든 메서드를 대상으로 한다. <br/>
     * 단, 클래스명의 접미사가 Controller로 끝나는 클래스만 대상으로 한다.
     */
    @Pointcut("execution(* kr.co.pennyway.api.apis.*.controller.*Controller.*(..))")
    private void cut() {
    }

    @Before("cut()")
    public void beforeRequest(JoinPoint joinPoint) {
        Method method = getMethod(joinPoint);

        log.info("================================= Request =================================");
        log.info("요청 메서드 이름 : {}", method.getName());
        log.debug("요청 메서드 경로 : {}", method.getDeclaringClass() + "." + method.getName());

        Object[] args = joinPoint.getArgs();

        if (args.length == 0) {
            log.info("None Request Parameter");
        }

        for (Object arg : args) {
            if (arg == null) continue;

            if (method.isAnnotationPresent(RequestHeaderLog.class) && method.getAnnotation(RequestHeaderLog.class).hasCookie()) {
                log.debug("요청 헤더 : {} ⇾ 값 : {}", HttpHeaders.COOKIE, arg);
                continue;
            }

            if (arg instanceof String param && param.startsWith(AuthConstants.TOKEN_TYPE.getValue())) {
                log.debug("요청 헤더 : {} ⇾ 값 : {}", HttpHeaders.AUTHORIZATION, param);
            } else {
                log.info("요청 파라미터 타입 : {} ⇾ 값 : {}", arg.getClass().getSimpleName(), arg);
            }
        }
        log.info("===========================================================================");
    }

    @AfterReturning(pointcut = "cut()", returning = "returnObject")
    public void afterResponse(JoinPoint joinPoint, Object returnObject) {
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnObject;

        if (responseEntity == null) {
            return;
        }

        HttpHeaders headers = responseEntity.getHeaders();

        log.info("================================= Response =================================");
        log.info("응답 상태 : {}", responseEntity.getStatusCode());

        for (Map.Entry<String, String> entry : headers.toSingleValueMap().entrySet()) {
            if (entry.getKey().equals(HttpHeaders.SET_COOKIE) || entry.getKey().equals(HttpHeaders.AUTHORIZATION)) {
                log.debug("응답 헤더 : {} ⇾ 값 : {}", entry.getKey(), entry.getValue());
            } else {
                log.info("응답 헤더 : {} ⇾ 값 : {}", entry.getKey(), entry.getValue());
            }
        }

        log.info("응답 내용 : {}", responseEntity.getBody());
        log.info("============================================================================");
    }

    @AfterThrowing(pointcut = "cut()", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("================================= Exception ================================");
        log.error("예외 종류 : {} ⇾ 메시지 : {}", exception.getClass().getSimpleName(), exception.getMessage());
        log.error("============================================================================");
    }

    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }
}
