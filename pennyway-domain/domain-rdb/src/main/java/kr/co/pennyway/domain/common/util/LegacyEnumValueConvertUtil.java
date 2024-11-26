package kr.co.pennyway.domain.common.util;

import kr.co.pennyway.domain.common.converter.LegacyCommonType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.EnumSet;

/**
 * {@link LegacyCommonType} enum을 String과 상호 변환하는 유틸리티 클래스
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LegacyEnumValueConvertUtil {
    public static <T extends Enum<T> & LegacyCommonType> T ofLegacyCode(Class<T> enumClass, String code) {
        if (!StringUtils.hasText(code)) return null;
        return EnumSet.allOf(enumClass).stream()
                .filter(e -> e.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("enum=[%s], code=[%s]가 존재하지 않습니다.", enumClass.getName(), code)));
    }

    public static <T extends Enum<T> & LegacyCommonType> String toLegacyCode(T enumValue) {
        if (enumValue == null) return "";
        return enumValue.getCode();
    }
}