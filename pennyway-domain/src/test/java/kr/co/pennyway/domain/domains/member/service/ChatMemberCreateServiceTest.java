package kr.co.pennyway.domain.domains.member.service;

import kr.co.pennyway.common.fixture.ChatRoomFixture;
import kr.co.pennyway.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ChatMemberCreateServiceTest {
    @Mock
    private ChatMemberRepository chatMemberRepository;
    private ChatMemberService chatMemberService;

    private User user;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        chatMemberService = new ChatMemberService(chatMemberRepository);
        user = UserFixture.GENERAL_USER.toUser();
        chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();
    }

    @Test
    @DisplayName("이미 가입한 회원은 가입에 실패한다.")
    void createMemberWhenAlreadyExist() {
        // given
        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
        given(chatMemberRepository.findByChat_Room_IdAndUser_Id(chatRoom.getId(), user.getId())).willReturn(Set.of(chatMember));

        // when
        ChatMemberErrorException exception = assertThrows(ChatMemberErrorException.class, () -> chatMemberService.createMember(user, chatRoom));

        // then
        assertEquals(ChatMemberErrorCode.ALREADY_JOINED, exception.getBaseErrorCode(), "에러 코드는 ALREADY_JOINED 여야 한다.");
    }

    @Test
    @DisplayName("추방 당한 이력이 있는 회원은 가입에 실패한다.")
    void createMemberWhenBanned() {
        // given
        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
        chatMember.ban();
        given(chatMemberRepository.findByChat_Room_IdAndUser_Id(chatRoom.getId(), user.getId())).willReturn(Set.of(chatMember));

        // when
        ChatMemberErrorException exception = assertThrows(ChatMemberErrorException.class, () -> chatMemberService.createMember(user, chatRoom));

        // then
        assertEquals(ChatMemberErrorCode.BANNED, exception.getBaseErrorCode(), "에러 코드는 BANNED 여야 한다.");
    }

    @Test
    @DisplayName("가입한 이력이 없는 사용자는 가입에 성공한다.")
    void createMemberWhenNotExist() {

        // given
        given(chatMemberRepository.findByChat_Room_IdAndUser_Id(chatRoom.getId(), user.getId())).willReturn(Set.of());

        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
        given(chatMemberRepository.save(any(ChatMember.class))).willReturn(chatMember);

        // when
        ChatMember result = chatMemberService.createMember(user, chatRoom);

        // then
        Assertions.assertNotNull(result);
    }

    @Test
    @DisplayName("탈퇴한 이력이 있지만, 사유가 추방이 아니라면 가입에 성공한다.")
    void createMemberWhenWithdrawn() {
        // given
        ChatMember original = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
        ReflectionTestUtils.setField(original, "deletedAt", LocalDateTime.now());

        ChatMember chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
        given(chatMemberRepository.save(any(ChatMember.class))).willReturn(chatMember);

        // when
        ChatMember result = chatMemberService.createMember(user, chatRoom);

        // then
        Assertions.assertNotNull(result);
    }
}
