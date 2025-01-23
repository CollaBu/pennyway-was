package kr.co.pennyway.domain.context.chat.collection;

import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Objects;

@Slf4j
public class ChatRoomAdminDelegateOperation {
    private final ChatMember chatAdmin;
    private final ChatMember chatMember;

    public ChatRoomAdminDelegateOperation(@NonNull ChatMember chatAdmin, @NonNull ChatMember chatMember) {
        this.chatAdmin = Objects.requireNonNull(chatAdmin);
        this.chatMember = Objects.requireNonNull(chatMember);
    }

    public void execute() {
        chatAdmin.delegate(chatMember);

        log.info("방장 권한 위임: {} -> {}", chatAdmin, chatMember);
    }
}
