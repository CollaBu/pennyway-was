package kr.co.pennyway.domain.domains.phone.repository;

import kr.co.pennyway.domain.common.annotation.DomainRedisTemplate;
import kr.co.pennyway.domain.domains.phone.type.PhoneCodeKeyType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Repository
public class PhoneCodeRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    public PhoneCodeRepository(@DomainRedisTemplate RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public LocalDateTime save(String phone, String code, PhoneCodeKeyType codeType) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        redisTemplate.opsForValue().set(codeType.getPrefix() + ":" + phone, code, Duration.between(LocalDateTime.now(), expiresAt));
        return expiresAt;
    }

    public String findCodeByPhone(String phone, PhoneCodeKeyType codeType) throws NullPointerException {
        return Objects.requireNonNull(redisTemplate.opsForValue().get(codeType.getPrefix() + ":" + phone)).toString();
    }

    public void extendTimeToLeave(String phone, PhoneCodeKeyType codeType) {
        redisTemplate.expire(codeType.getPrefix() + ":" + phone, Duration.ofMinutes(5));
    }

    public void delete(String phone, PhoneCodeKeyType codeType) {
        redisTemplate.opsForValue().getAndDelete(codeType.getPrefix() + ":" + phone);
    }
}
