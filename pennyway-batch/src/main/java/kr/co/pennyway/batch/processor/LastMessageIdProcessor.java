package kr.co.pennyway.batch.processor;

import kr.co.pennyway.batch.common.dto.KeyValue;
import kr.co.pennyway.domain.domains.chatstatus.domain.ChatMessageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LastMessageIdProcessor implements ItemProcessor<KeyValue, ChatMessageStatus> {

    @Override
    public ChatMessageStatus process(KeyValue item) throws Exception {
        String[] parts = item.key().split(":"); // key format: chat:last_read:{roomId}:{userId}

        if (parts.length != 4) {
            log.error("Invalid key format: {}", item.key());
            return null;
        }

        Long roomId = Long.parseLong(parts[2]);
        Long userId = Long.parseLong(parts[3]);
        Long messageId = Long.parseLong(item.value());

        return new ChatMessageStatus(userId, roomId, messageId);
    }
}
