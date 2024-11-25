package kr.co.pennyway.domain.domains.chatstatus.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.domains.chatstatus.repository.ChatMessageStatusCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMessageStatusRedisService {
    private final ChatMessageStatusCacheRepository chatMessageStatusCacheRepository;

    public Optional<Long> readLastReadMessageId(Long userId, Long chatRoomId) {
        return chatMessageStatusCacheRepository.findLastReadMessageId(userId, chatRoomId);
    }

    public void saveLastReadMessageId(Long userId, Long chatRoomId, Long messageId) {
        validateInputs(userId, chatRoomId, messageId);

        chatMessageStatusCacheRepository.saveLastReadMessageId(userId, chatRoomId, messageId);
    }

    private void validateInputs(Long userId, Long chatRoomId, Long lastReadMessageId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid userId: " + userId);
        }
        if (chatRoomId == null || chatRoomId <= 0) {
            throw new IllegalArgumentException("Invalid chatRoomId: " + chatRoomId);
        }
        if (lastReadMessageId == null || lastReadMessageId <= 0) {
            throw new IllegalArgumentException("Invalid lastReadMessageId: " + lastReadMessageId);
        }
    }
}
