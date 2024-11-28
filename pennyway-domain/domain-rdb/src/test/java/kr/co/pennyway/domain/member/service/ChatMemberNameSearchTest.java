package kr.co.pennyway.domain.member.service;

import kr.co.pennyway.domain.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.common.fixture.UserFixture;
import kr.co.pennyway.domain.config.ContainerMySqlTestConfig;
import kr.co.pennyway.domain.config.JpaConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.dto.ChatMemberResult;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create"})
@ContextConfiguration(classes = {JpaConfig.class, ChatMemberRdbService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({JpaTestConfig.class})
public class ChatMemberNameSearchTest extends ContainerMySqlTestConfig {
    @Autowired
    private ChatMemberRdbService chatMemberRdbService;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    @Transactional
    @DisplayName("채팅방 멤버 단일 조회에 성공한다.")
    public void successReadChatMember() {
        // given
        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity());
        ChatMember chatMember = chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER));

        // when
        Optional<ChatMember> result = chatMemberRdbService.readChatMember(user.getId(), chatRoom.getId());

        // then
        log.debug("result: {}", result);
        assertNotNull(result.get());
        assertEquals(chatMember.getId(), result.get().getId());
    }

    @Test
    @Transactional
    @DisplayName("채팅방 관리자 조회에 성공한다.")
    public void successReadAdmin() {
        // given
        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity());
        ChatMember chatMember = chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN));

        // when
        Optional<ChatMemberResult.Detail> result = chatMemberRdbService.readAdmin(chatRoom.getId());

        // then
        log.debug("result: {}", result);
        assertNotNull(result.get());
        assertEquals(chatMember.getId(), result.get().id());
    }

    @Test
    @Transactional
    @DisplayName("멤버 아이디 리스트로 멤버 조회에 성공한다.")
    public void successReadChatMembersByIdIn() {
        // given
        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity());
        ChatMember chatMember1 = chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN));

        User user2 = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatMember chatMember2 = chatMemberRepository.save(ChatMember.of(user2, chatRoom, ChatMemberRole.MEMBER));

        Set<Long> chatMemberIds = Set.of(chatMember1.getId(), chatMember2.getId());

        // when
        List<ChatMemberResult.Detail> result = chatMemberRdbService.readChatMembersByIdIn(chatRoom.getId(), chatMemberIds);

        // then
        log.debug("result: {}", result);
        assertEquals(2, result.size());
    }

    @Test
    @Transactional
    @DisplayName("사용자 아이디 리스트로 멤버 조회에 성공한다.")
    public void successReadChatMembersByUserIds() {
        // given
        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity());
        ChatMember chatMember1 = chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN));

        User user2 = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatMember chatMember2 = chatMemberRepository.save(ChatMember.of(user2, chatRoom, ChatMemberRole.MEMBER));

        Set<Long> userIds = Set.of(user.getId(), user2.getId());

        // when
        List<ChatMemberResult.Detail> result = chatMemberRdbService.readChatMembersByUserIdIn(chatRoom.getId(), userIds);

        // then
        log.debug("result: {}", result);
        assertEquals(2, result.size());
    }
}
