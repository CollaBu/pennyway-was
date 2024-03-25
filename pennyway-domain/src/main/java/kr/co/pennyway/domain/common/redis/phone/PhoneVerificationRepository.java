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
        redisTemplate.expire(codeType.getPrefix() + ":" + phone, Duration.between(LocalDateTime.now(), expiresAt));
        redisTemplate.opsForHash().put(codeType.getPrefix(), phone, code);
        return expiresAt;
    }

    public String findCodeByPhone(String phone, PhoneVerificationCode codeType) throws NullPointerException {
        return Objects.requireNonNull(redisTemplate.opsForHash().get(codeType.getPrefix(), phone)).toString();
    }

    public void remove(String phone, PhoneVerificationCode codeType) {
        redisTemplate.opsForHash().delete(codeType.getPrefix(), phone);
    }
}
