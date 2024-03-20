package kr.co.pennyway.common.converter;

import jakarta.persistence.AttributeConverter;
import kr.co.pennyway.common.util.LegacyEnumValueConvertUtil;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class AbstractLegacyEnumAttributeConverter<E extends Enum<E> & LegacyCommonType> implements AttributeConverter<E, String> {
    /**
     * 대상 Enum 클래스 {@link Class} 객체
     */
    private final Class<E> targetEnumClass;

    /**
     * <code>nullable = false</code>면, 변환할 값이 null로 들어왔을 때 예외를 발생시킨다.<br/>
     * <code>nullable = true</code>면, 변환할 값이 null로 들어왔을 때 예외 없이 실행하며,<br/>
     * legacy code로 변환 시엔 빈 문자열("")로 변환한다.
     */
    private final boolean nullable;

    /**
     * <code>nullable = false</code>일 때 출력할 오류 메시지에서 enum에 대한 설명을 위해 Enum의 설명적 이름을 받는다.
     */
    private final String enumName;

    public AbstractLegacyEnumAttributeConverter(Class<E> targetEnumClass, boolean nullable, String enumName) {
        this.targetEnumClass = targetEnumClass;
        this.nullable = nullable;
        this.enumName = enumName;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        if (!nullable && attribute == null) {
            throw new IllegalArgumentException(String.format("%s을(를) null로 변환할 수 없습니다.", enumName));
        }
        return LegacyEnumValueConvertUtil.toLegacyCode(attribute);
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (!nullable && !StringUtils.hasText(dbData)) {
            throw new IllegalArgumentException(String.format("%s(이)가 DB에 null 혹은 Empty로(%s) 저장되어 있습니다.", enumName, dbData));
        }
        return LegacyEnumValueConvertUtil.ofLegacyCode(targetEnumClass, dbData);
    }
}