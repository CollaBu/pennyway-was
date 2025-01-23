package kr.co.pennyway.domain.context.chat.integration;

import kr.co.pennyway.domain.common.repository.ExtendedRepositoryFactory;
import kr.co.pennyway.domain.config.DomainServiceIntegrationProfileResolver;
import kr.co.pennyway.domain.config.DomainServiceTestInfraConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.context.chat.service.ChatRoomAdminDelegateService;
import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.repository.ChatMemberRepository;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(classes = {ChatRoomAdminDelegateService.class, ChatMemberRdbService.class, ChatRoomRdbService.class})
@EntityScan(basePackageClasses = {User.class, ChatRoom.class, ChatMember.class})
@EnableJpaRepositories(
        basePackageClasses = {UserRepository.class, ChatRoomRepository.class, ChatMemberRepository.class},
        repositoryFactoryBeanClass = ExtendedRepositoryFactory.class
)
@ActiveProfiles(resolver = DomainServiceIntegrationProfileResolver.class)
@Import(value = {JpaTestConfig.class})
public class ChatRoomAdminDelegateServiceIntegrationTest extends DomainServiceTestInfraConfig {
    @Autowired
    private ChatRoomAdminDelegateService sut;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void shouldDelegateAdmin() {
        // given
        var chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L));
        var chatAdmin = createChatMember(ChatMemberRole.ADMIN, chatRoom);
        var chatMember = createChatMember(ChatMemberRole.MEMBER, chatRoom);

        // when
        sut.execute(chatRoom.getId(), chatAdmin.getUser().getId(), chatMember.getId());

        // then
        assertEquals(ChatMemberRole.ADMIN, chatMember.getRole());
        assertEquals(ChatMemberRole.MEMBER, chatAdmin.getRole());
    }

    private ChatMember createChatMember(ChatMemberRole role, ChatRoom chatRoom) {
        var user = userRepository.save(UserFixture.GENERAL_USER.toUser());

        return chatMemberRepository.save(ChatMember.of(user, chatRoom, role));
    }
}
