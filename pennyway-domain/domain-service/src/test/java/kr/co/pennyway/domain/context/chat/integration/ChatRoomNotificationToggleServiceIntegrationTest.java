package kr.co.pennyway.domain.context.chat.integration;

import kr.co.pennyway.domain.common.repository.ExtendedRepositoryFactory;
import kr.co.pennyway.domain.config.DomainServiceIntegrationProfileResolver;
import kr.co.pennyway.domain.config.DomainServiceTestInfraConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomToggleCommand;
import kr.co.pennyway.domain.context.chat.service.ChatRoomNotificationToggleService;
import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(classes = {ChatRoomNotificationToggleService.class, ChatMemberRdbService.class})
@EntityScan(basePackageClasses = {User.class, ChatRoom.class, ChatMember.class})
@EnableJpaRepositories(
        basePackageClasses = {UserRepository.class, ChatRoomRepository.class, ChatMemberRepository.class},
        repositoryFactoryBeanClass = ExtendedRepositoryFactory.class
)
@ActiveProfiles(resolver = DomainServiceIntegrationProfileResolver.class)
@Import(value = {JpaTestConfig.class})
public class ChatRoomNotificationToggleServiceIntegrationTest extends DomainServiceTestInfraConfig {
    @Autowired
    private ChatRoomNotificationToggleService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Test
    @DisplayName("채팅방 알림을 켜면 알림이 켜진다.")
    void shouldTurnOnWhenChatMemberExists() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L));
        var chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
        chatMember.disableNotify();
        chatMember = chatMemberRepository.save(chatMember);

        // when
        sut.turnOn(ChatRoomToggleCommand.of(user.getId(), chatRoom.getId()));

        // then
        var updatedChatMember = chatMemberRepository.findById(chatMember.getId()).get();
        assertTrue(updatedChatMember.isNotifyEnabled());
    }

    @Test
    @DisplayName("채팅방 알림을 끄면 알림이 꺼진다.")
    void shouldTurnOffWhenChatMemberExists() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(2L));
        var chatMember = ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER);
        chatMember.enableNotify();
        chatMember = chatMemberRepository.save(chatMember);

        // when
        sut.turnOff(ChatRoomToggleCommand.of(user.getId(), chatRoom.getId()));

        // then
        var updatedChatMember = chatMemberRepository.findById(chatMember.getId()).get();
        assertFalse(updatedChatMember.isNotifyEnabled());
    }
}
