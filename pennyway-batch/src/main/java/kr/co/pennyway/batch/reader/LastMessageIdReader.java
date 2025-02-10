package kr.co.pennyway.batch.reader;

import kr.co.pennyway.batch.common.dto.KeyValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@RequiredArgsConstructor
public class LastMessageIdReader implements ItemReader<KeyValue> {
    private final RedisTemplate<String, String> redisTemplate;
    private final Cursor<String> cursor;

    @Override
    public KeyValue read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!cursor.hasNext()) {
            log.debug("No more keys to read cursor: {}", cursor);
            return null;
        }

        String key = cursor.next();
        String value = redisTemplate.opsForValue().get(key);
        log.debug("Read key: {}, value: {}", key, value);

        if (value == null) {
            log.warn("Value not found for key: {}", key);
            return null;
        }

        return new KeyValue(key, value);
    }
}
