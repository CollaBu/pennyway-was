package kr.co.pennyway.domains.phone;

import kr.co.pennyway.config.ContainerRedisTestConfig;
import kr.co.pennyway.domain.config.LettuceConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import kr.co.pennyway.domain.domains.phone.repository.PhoneCodeRepository;
import kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ContextConfiguration(classes = {RedisConfig.class, LettuceConfig.class})
@DataRedisTest(properties = "spring.config.location=classpath:application-domain-redis.yml")
@Import({PhoneCodeRepository.class})
@ActiveProfiles("test")
public class PhoneValidationDaoTest extends ContainerRedisTestConfig {
    @Autowired
    private PhoneCodeRepository phoneCodeRepository;
    private String phone;
    private String code;
    private PhoneCodeKeyType codeType;

    @BeforeEach
    void setUp() {
        phone = "01012345678";
        code = "123456";
        codeType = PhoneCodeKeyType.SIGN_UP;
    }

    @AfterEach
    void tearDown() {
        phoneCodeRepository.delete(phone, codeType);
    }

    @Test
    @DisplayName("Redis에 데이터를 저장하면 {'codeType:phone':code}로 데이터가 저장된다.")
    void codeSaveTest() {
        // given
        phoneCodeRepository.save(phone, code, codeType);

        // when
        String savedCode = phoneCodeRepository.findCodeByPhone(phone, codeType);

        // then
        assertEquals(code, savedCode);
        System.out.println("savedCode = " + savedCode);
    }

    @Test
    @DisplayName("Redis에 'codeType:phone'에 해당하는 값이 없으면 NullPointerException이 발생한다.")
    void codeReadError() {
        // given
        phoneCodeRepository.delete(phone, codeType);
        String wrongPhone = "01087654321";

        // when - then
        assertThrows(NullPointerException.class, () -> phoneCodeRepository.findCodeByPhone(wrongPhone, codeType));
    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 삭제하면 해당 데이터가 삭제된다.")
    void codeRemoveTest() {
        // given
        phoneCodeRepository.save(phone, code, codeType);

        // when
        phoneCodeRepository.delete(phone, codeType);

        // then
        assertThrows(NullPointerException.class, () -> phoneCodeRepository.findCodeByPhone(phone, codeType));
    }

    @Test
    @DisplayName("저장되지 않은 데이터를 삭제해도 에러가 발생하지 않는다.")
    void codeRemoveError() {
        // when - thengi
        assertThrows(NullPointerException.class, () -> phoneCodeRepository.findCodeByPhone(phone, codeType));
        phoneCodeRepository.delete(phone, codeType);
    }
}
