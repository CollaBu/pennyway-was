package kr.co.pennyway.domain.common.redis.phone;

import kr.co.pennyway.domain.common.annotation.DomainRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Repository
public class PhoneVerificationRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    public PhoneVerificationRepository(@DomainRedisTemplate RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public LocalDateTime save(String phone, String code, PhoneVerificationCode codeType) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        redisTemplate.opsForValue().set(codeType.getPrefix() + ":" + phone, code, Duration.between(LocalDateTime.now(), expiresAt));
        return expiresAt;
    }

    public String findCodeByPhone(String phone, PhoneVerificationCode codeType) throws NullPointerException {
        return Objects.requireNonNull(redisTemplate.opsForValue().get(codeType.getPrefix() + ":" + phone)).toString();
    }

    public void extendTimeToLeave(String phone, PhoneVerificationCode codeType) {
        redisTemplate.expire(codeType.getPrefix() + ":" + phone, Duration.ofMinutes(5));
    }

    public void remove(String phone, PhoneVerificationCode codeType) {
        redisTemplate.opsForValue().getAndDelete(codeType.getPrefix() + ":" + phone);
    }
}
