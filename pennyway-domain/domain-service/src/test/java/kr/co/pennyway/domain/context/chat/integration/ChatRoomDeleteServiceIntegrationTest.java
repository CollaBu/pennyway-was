package kr.co.pennyway.domain.context.chat.integration;

import kr.co.pennyway.domain.common.repository.ExtendedRepositoryFactory;
import kr.co.pennyway.domain.config.DomainServiceIntegrationProfileResolver;
import kr.co.pennyway.domain.config.DomainServiceTestInfraConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.context.chat.dto.ChatRoomDeleteCommand;
import kr.co.pennyway.domain.context.chat.service.ChatRoomDeleteService;
import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(classes = {ChatRoomDeleteService.class, ChatMemberRdbService.class, ChatRoomRdbService.class})
@EntityScan(basePackageClasses = {User.class, ChatRoom.class, ChatMember.class})
@EnableJpaRepositories(
        basePackageClasses = {UserRepository.class, ChatRoomRepository.class, ChatMemberRepository.class},
        repositoryFactoryBeanClass = ExtendedRepositoryFactory.class
)
@ActiveProfiles(resolver = DomainServiceIntegrationProfileResolver.class)
@Import(value = {JpaTestConfig.class})
public class ChatRoomDeleteServiceIntegrationTest extends DomainServiceTestInfraConfig {
    @Autowired
    private ChatRoomDeleteService chatRoomDeleteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @AfterEach
    void tearDown() {
        chatMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("관리자는 채팅방을 삭제할 수 있다.")
    void shouldChatRoomDeletedWhenAdminExecute() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L));
        var admin = chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN));

        ChatRoomDeleteCommand command = ChatRoomDeleteCommand.of(user.getId(), chatRoom.getId());

        // when
        chatRoomDeleteService.execute(command);

        // then
        var chatMembers = chatMemberRepository.findAll();
        assertThat(chatMembers).hasSize(1);
        assertTrue(chatMembers.stream().noneMatch(ChatMember::isActive));
        assertThat(chatRoomRepository.findById(chatRoom.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 멤버가 채팅방 삭제를 시도하면 예외가 발생한다.")
    void shouldThrowExceptionWhenNonExistMemberExecute() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(2L));

        ChatRoomDeleteCommand command = ChatRoomDeleteCommand.of(user.getId(), chatRoom.getId());

        // when & then
        assertThatThrownBy(() -> chatRoomDeleteService.execute(command))
                .isInstanceOf(ChatMemberErrorException.class);
    }

    @Test
    @DisplayName("일반 사용자는 채팅방을 삭제할 수 없다.")
    void shouldThrowExceptionWhenGeneralUserExecute() {
        // given
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(3L));
        chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER));

        ChatRoomDeleteCommand command = ChatRoomDeleteCommand.of(user.getId(), chatRoom.getId());

        // when & then
        assertThatThrownBy(() -> chatRoomDeleteService.execute(command))
                .isInstanceOf(ChatMemberErrorException.class);
    }

    @Test
    @DisplayName("채팅방에 속한 모든 멤버가 삭제된다.")
    void shouldAllChatMembersDeletedWhenExecute() {
        // given
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(4L));
        var users = createUsers(10);
        var admin = chatMemberRepository.save(ChatMember.of(users.get(0), chatRoom, ChatMemberRole.ADMIN));
        var members = createGeneralChatMembers(users.subList(1, users.size()), chatRoom);

        ChatRoomDeleteCommand command = ChatRoomDeleteCommand.of(admin.getUserId(), chatRoom.getId());

        // when
        chatRoomDeleteService.execute(command);

        // then
        var chatMembers = chatMemberRepository.findAll();
        assertThat(chatMembers).hasSize(10);
        assertTrue(chatMembers.stream().noneMatch(ChatMember::isActive));
        assertThat(chatRoomRepository.findById(chatRoom.getId())).isEmpty();
    }

    private List<User> createUsers(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> userRepository.save(UserFixture.GENERAL_USER.toUser()))
                .toList();
    }

    private List<ChatMember> createGeneralChatMembers(List<User> users, ChatRoom chatRoom) {
        return users.stream()
                .map(user -> chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER)))
                .toList();
    }
}
