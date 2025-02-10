package kr.co.pennyway.domain.context.chat.integration;

import jakarta.transaction.Transactional;
import kr.co.pennyway.domain.common.repository.ExtendedRepositoryFactory;
import kr.co.pennyway.domain.config.DomainServiceIntegrationProfileResolver;
import kr.co.pennyway.domain.config.DomainServiceTestInfraConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.context.chat.dto.ChatMemberJoinCommand;
import kr.co.pennyway.domain.context.chat.dto.ChatMemberJoinResult;
import kr.co.pennyway.domain.context.chat.service.ChatMemberJoinService;
import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorCode;
import kr.co.pennyway.domain.domains.chatroom.exception.ChatRoomErrorException;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.exception.UserErrorCode;
import kr.co.pennyway.domain.domains.user.exception.UserErrorException;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(classes = {
        ChatMemberJoinService.class,
        UserRdbService.class,
        ChatRoomRdbService.class,
        ChatMemberRdbService.class
})
@EntityScan(basePackageClasses = {User.class, ChatRoom.class, ChatMember.class})
@EnableJpaRepositories(basePackageClasses = {
        UserRepository.class,
        ChatRoomRepository.class,
        ChatMemberRepository.class
}, repositoryFactoryBeanClass = ExtendedRepositoryFactory.class)
@ActiveProfiles(resolver = DomainServiceIntegrationProfileResolver.class)
@Import(JpaTestConfig.class)
public class ChatMemberJoinServiceIntegrationTest extends DomainServiceTestInfraConfig {
    @Autowired
    private ChatMemberJoinService sut;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @DisplayName("채팅방 가입 성공")
    void successJoinRoom() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity());
        var command = ChatMemberJoinCommand.of(user.getId(), chatRoom.getId(), null);

        // when
        ChatMemberJoinResult result = sut.execute(command);

        // then
        assertAll(
                () -> assertEquals(chatRoom, result.chatRoom()),
                () -> assertEquals(user.getName(), result.memberName()),
                () -> assertEquals(1L, result.currentMemberCount()),
                () -> assertTrue(chatMemberRepository.existsByChatRoomIdAndUserId(chatRoom.getId(), user.getId()))
        );
    }

    @Test
    @Transactional
    @DisplayName("이미 가입된 사용자는 재가입할 수 없다")
    void failWhenAlreadyJoined() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity());
        chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER));

        var command = ChatMemberJoinCommand.of(user.getId(), chatRoom.getId(), null);

        // when & then
        assertThatThrownBy(() -> sut.execute(command))
                .isInstanceOf(ChatMemberErrorException.class)
                .hasFieldOrPropertyWithValue("baseErrorCode", ChatMemberErrorCode.ALREADY_JOINED);
    }

    @Test
    @Transactional
    @DisplayName("채팅방 가입시 채팅방이 존재하지 않으면 가입할 수 없다")
    void failWhenChatRoomNotFound() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var command = ChatMemberJoinCommand.of(user.getId(), 1L, null);

        // when & then
        assertThatThrownBy(() -> sut.execute(command))
                .isInstanceOf(ChatRoomErrorException.class)
                .hasFieldOrPropertyWithValue("baseErrorCode", ChatRoomErrorCode.NOT_FOUND_CHAT_ROOM);
    }

    @Test
    @Transactional
    @DisplayName("채팅방 가입시 사용자가 존재하지 않으면 가입할 수 없다")
    void failWhenUserNotFound() {
        // given
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity());
        var command = ChatMemberJoinCommand.of(1L, chatRoom.getId(), null);

        // when & then
        assertThatThrownBy(() -> sut.execute(command))
                .isInstanceOf(UserErrorException.class)
                .hasFieldOrPropertyWithValue("baseErrorCode", UserErrorCode.NOT_FOUND);
    }
}
