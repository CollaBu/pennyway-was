package kr.co.pennyway.socket.common.util;

import kr.co.pennyway.socket.common.security.authenticate.UserPrincipal;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket 인증 및 인가를 위한 Spring Expression Language (SpEL) 파서.
 * 이 클래스는 WebSocket 연결에서 사용되는 다양한 인증/인가 함수를 제공하고,
 * SpEL 표현식을 평가하는 기능을 제공합니다.
 *
 * @author YANG JAESEO
 * @version 1.0.0
 * @since 2024.09.26
 */
public final class PreAuthorizeSpELParser {
    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final StandardEvaluationContext context = new StandardEvaluationContext();

    static {
        initializeStaticContext();
    }

    private PreAuthorizeSpELParser() {
        throw new IllegalStateException("Utility class");
    }

    private static void initializeStaticContext() {
        for (SpELFunction function : SpELFunction.values()) {
            try {
                context.registerFunction(function.getName(),
                        PreAuthorizeSpELParser.class.getDeclaredMethod(function.getMethodName(), function.getParameterTypes()));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Error registering SpEL function: " + function.getName(), e);
            }
        }
    }

    /**
     * 주어진 SpEL 표현식을 평가합니다.
     *
     * @param expression         평가할 SpEL 표현식
     * @param method             평가 중인 메서드
     * @param args               메서드의 인자들
     * @param applicationContext Spring의 ApplicationContext
     * @return 표현식 평가 결과 (true/false)
     */
    public static synchronized boolean evaluate(String expression, Method method, Object[] args, ApplicationContext applicationContext) {
        populateContext(method, args, applicationContext);
        return Boolean.TRUE.equals(parser.parseExpression(expression).getValue(context, Boolean.class));
    }

    /**
     * SpEL 평가를 위해, 사용자의 Principal 객체와 메서드의 인자들을 EvaluationContext에 추가합니다.
     *
     * @param method             평가 중인 메서드
     * @param args               메서드의 인자들
     * @param applicationContext Spring의 ApplicationContext
     * @return 생성된 StandardEvaluationContext
     */
    private static void populateContext(Method method, Object[] args, ApplicationContext applicationContext) {
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            context.setVariable(parameters[i].getName(), args[i]);
        }
    }

    /**
     * 모든 사용자에게 접근을 허용합니다.
     *
     * @return 언제나 true를 반환한다.
     */
    public static boolean permitAll() {
        return true;
    }

    /**
     * 주어진 Principal이 인증된 사용자인지 확인합니다.
     *
     * @param principal 확인할 Principal 객체
     * @return 인증된 사용자이고 토큰이 만료되지 않았으면 true, 그렇지 않으면 false
     */
    public static boolean isAuthenticated(Principal principal) {
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getExpiresAt().isAfter(LocalDateTime.now());
        }
        return false;
    }

    /**
     * WebSocket 인증/인가에 사용되는 SpEL 함수들을 정의하는 열거형.
     * 각 함수는 이름, 메서드 이름, 파라미터 타입을 가집니다.
     */
    public enum SpELFunction {
        PERMIT_ALL("permitAll", "permitAll"),
        IS_AUTHENTICATED("isAuthenticated", "isAuthenticated", Principal.class);

        private final String name;
        private final String methodName;
        private final Class<?>[] parameterTypes;

        SpELFunction(String name, String methodName, Class<?>... parameterTypes) {
            this.name = name;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        public String getName() {
            return name;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }
    }
}