package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.api.common.query.ViewType;
import org.springframework.core.convert.converter.Converter;

public class ViewTypeConverter implements Converter<String, ViewType> {
    @Override
    public ViewType convert(String type) {
        try {
            return ViewType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ViewType.SUMMARY;
        }
    }
}
