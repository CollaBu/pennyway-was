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
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LastMessageIdReader implements ItemReader<KeyValue> {
    private final RedisTemplate<String, String> redisTemplate;
    private final String pattern;
    private Cursor<String> cursor;
    private boolean initialized = false;


    @Override
    public KeyValue read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
            cursor = redisTemplate.scan(options);
            initialized = true;
        }

        if (cursor == null || !cursor.hasNext()) {
            return null;
        }

        String key = cursor.next();
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return new KeyValue(key, value);
    }
}
