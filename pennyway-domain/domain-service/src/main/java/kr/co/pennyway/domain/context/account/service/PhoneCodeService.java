package kr.co.pennyway.domain.context.account.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.phone.service.PhoneCodeRedisService;
import kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class PhoneCodeService {
    private final PhoneCodeRedisService phoneCodeRedisService;

    public void create(String phone, String code, PhoneCodeKeyType codeType) {
        phoneCodeRedisService.create(phone, code, codeType);
    }

    public String readByPhone(String phone, PhoneCodeKeyType codeKeyType) {
        return phoneCodeRedisService.readByPhone(phone, codeKeyType);
    }

    public void extendTimeToLeave(String phone, PhoneCodeKeyType codeType) {
        phoneCodeRedisService.extendTimeToLeave(phone, codeType);
    }

    public void delete(String phone, PhoneCodeKeyType codeType) {
        phoneCodeRedisService.delete(phone, codeType);
    }
}
