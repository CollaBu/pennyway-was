package kr.co.pennyway.domain.common.redis.phone;

import kr.co.pennyway.domain.common.annotation.DomainRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Objects;

@Repository
public class PhoneVerificationRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    public PhoneVerificationRepository(@DomainRedisTemplate RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String phone, String code, Code codeType) {
        redisTemplate.expire(codeType.getPrefix() + ":" + phone, Duration.ofMinutes(5));
        redisTemplate.opsForHash().put(codeType.getPrefix(), phone, code);
    }

    public String findCodeByPhone(String phone, Code codeType) throws NullPointerException {
        return Objects.requireNonNull(redisTemplate.opsForHash().get(codeType.getPrefix(), phone)).toString();
    }

    public void remove(String phone, Code codeType) {
        redisTemplate.opsForHash().delete(codeType.getPrefix(), phone);
    }
}
