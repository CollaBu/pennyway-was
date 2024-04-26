package kr.co.pennyway.api.common.converter;

import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import org.springframework.core.convert.converter.Converter;

public class NotifyTypeConverter implements Converter<String, NotifySetting.NotifyType> {
    @Override
    public NotifySetting.NotifyType convert(String type) {
        try {
            return NotifySetting.NotifyType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UserErrorException(UserErrorCode.INVALID_NOTIFY_TYPE);
        }
    }
}
