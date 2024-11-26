package kr.co.pennyway.domain.common.util;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Spring Expression Language (SpEL)을 사용한 커스텀 EL 파서
 */
public class CustomSpringELParser {
    /**
     * SpEL을 사용하여 동적으로 값을 평가한다.
     *
     * @param parameterNames : 메서드 파라미터 이름
     * @param args           : 메서드 파라미터 값
     * @param key            : SpEL 표현식
     * @return : 평가된 값
     */
    public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 메서드 파라미터 이름과 값을 SpEL 컨텍스트에 변수로 설정
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, Object.class);
    }
}
