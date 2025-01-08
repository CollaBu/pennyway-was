package kr.co.pennyway.api.common.security.authorization;

import kr.co.pennyway.domain.context.chat.service.ChatMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component("chatRoomManager")
@RequiredArgsConstructor
public class ChatRoomManager {
    private final ChatMemberService chatMemberService;

    /**
     * 사용자가 채팅방에 대한 접근 권한이 있는지 확인한다.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long chatRoomId) {
        return chatMemberService.isExists(chatRoomId, userId);
    }

    /**
     * 사용자가 채팅방과 특정 멤버에 대한 접근 권한이 있는지 확인한다.
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Long chatRoomId, Long chatMemberId) {
        return chatMemberService.isExists(chatRoomId, userId, chatMemberId);
    }
}
