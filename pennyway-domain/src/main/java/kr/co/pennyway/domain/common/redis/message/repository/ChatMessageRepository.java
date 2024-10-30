package kr.co.pennyway.domain.common.redis.message.repository;

import kr.co.pennyway.domain.common.redis.message.domain.ChatMessage;
import org.springframework.data.repository.CrudRepository;

public interface ChatMessageRepository extends CrudRepository<ChatMessage, String> {
}
