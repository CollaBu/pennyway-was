package kr.co.pennyway.domain.common.redis.phone;

import kr.co.pennyway.domain.config.ContainerRedisTestConfig;
import kr.co.pennyway.domain.config.RedisConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("휴대폰 검증 Redis 서비스 테스트")
@SpringBootTest(classes = {PhoneVerificationRepository.class, RedisConfig.class})
@ActiveProfiles("local")
public class PhoneValidationDaoTest extends ContainerRedisTestConfig {
    @Autowired
    private PhoneVerificationRepository phoneVerificationRepository;
    private String phone;
    private String code;
    private Code codeType;

    @BeforeEach
    void setUp() {
        phone = "01012345678";
        code = "123456";
        codeType = Code.SIGN_UP;
    }

    @AfterEach
    void tearDown() {
        phoneVerificationRepository.remove(phone, codeType);
    }

    @Test
    @DisplayName("Redis에 데이터를 저장하면 {'codeType:phone':code}로 데이터가 저장된다.")
    void codeSaveTest() {
        // given
        phoneVerificationRepository.save(phone, code, codeType);

        // when
        String savedCode = phoneVerificationRepository.findCodeByPhone(phone, codeType);

        // then
        assertEquals(code, savedCode);
        System.out.println("savedCode = " + savedCode);
    }

    @Test
    @DisplayName("Redis에 'codeType:phone'에 해당하는 값이 없으면 NullPointerException이 발생한다.")
    void codeReadError() {
        // given
        phoneVerificationRepository.remove(phone, codeType);
        String wrongPhone = "01087654321";

        // when - then
        assertThrows(NullPointerException.class, () -> phoneVerificationRepository.findCodeByPhone(wrongPhone, codeType));
    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 삭제하면 해당 데이터가 삭제된다.")
    void codeRemoveTest() {
        // given
        phoneVerificationRepository.save(phone, code, codeType);

        // when
        phoneVerificationRepository.remove(phone, codeType);

        // then
        assertThrows(NullPointerException.class, () -> phoneVerificationRepository.findCodeByPhone(phone, codeType));
    }
}
