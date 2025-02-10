package kr.co.pennyway.infra.common.util;

import kr.co.pennyway.infra.common.jwt.JwtClaims;

import java.util.function.Function;

public class JwtClaimsParserUtil {
    /**
     * JwtClaims에서 key에 해당하는 값을 반환하는 메서드
     *
     * @return key에 해당하는 값이 없거나, 타입이 일치하지 않을 경우 null을 반환한다.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getClaimsValue(JwtClaims claims, String key, Class<T> type) {
        Object value = claims.getClaims().get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }

    /**
     * JwtClaims에서 valueConverter를 이용하여 key에 해당하는 값을 반환하는 메서드
     *
     * @param valueConverter : String 타입의 값을 T 타입으로 변환하는 함수
     * @return key에 해당하는 값이 없을 경우 null을 반환한다.
     */
    public static <T> T getClaimsValue(JwtClaims claims, String key, Function<String, T> valueConverter) {
        Object value = claims.getClaims().get(key);
        if (value != null) {
            return valueConverter.apply((String) value);
        }
        return null;
    }
}
