package kr.co.pennyway.domain.context.chat.integration;

import kr.co.pennyway.domain.common.repository.ExtendedRepositoryFactory;
import kr.co.pennyway.domain.config.DomainServiceIntegrationProfileResolver;
import kr.co.pennyway.domain.config.DomainServiceTestInfraConfig;
import kr.co.pennyway.domain.config.JpaTestConfig;
import kr.co.pennyway.domain.context.chat.service.ChatRoomLeaveService;
import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.chatroom.repository.ChatRoomRepository;
import kr.co.pennyway.domain.domains.chatroom.service.ChatRoomRdbService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorCode;
import kr.co.pennyway.domain.domains.member.exception.ChatMemberErrorException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(classes = {ChatRoomLeaveService.class, ChatMemberRdbService.class, ChatRoomRdbService.class})
@EntityScan(basePackageClasses = {User.class, ChatRoom.class, ChatMember.class})
@EnableJpaRepositories(
        basePackageClasses = {UserRepository.class, ChatRoomRepository.class, ChatMemberRepository.class},
        repositoryFactoryBeanClass = ExtendedRepositoryFactory.class
)
@ActiveProfiles(resolver = DomainServiceIntegrationProfileResolver.class)
@Import(value = {JpaTestConfig.class})
public class ChatRoomLeaveServiceIntegrationTest extends DomainServiceTestInfraConfig {
    @Autowired
    private ChatRoomLeaveService chatRoomLeaveService;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("일반 멤버가 채팅방을 나가면 멤버는 삭제되고 채팅방은 유지된다")
    void whenNormalMemberLeaves_thenMemberIsDeletedAndRoomRemains() {
        // given
        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L));
        ChatMember normalMember = chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.MEMBER));

        // when
        chatRoomLeaveService.execute(normalMember.getId());

        // then
        ChatMember deletedMember = chatMemberRepository.findById(normalMember.getId()).orElseThrow();
        ChatRoom remainingRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();

        assertNotNull(deletedMember.getDeletedAt());
        assertNull(remainingRoom.getDeletedAt());
    }

    @Test
    @DisplayName("방장이 혼자 있는 채팅방을 나가면 방장과 채팅방 모두 삭제된다")
    void whenLastAdminLeaves_thenBothMemberAndRoomAreDeleted() {
        // given
        User user = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(2L));
        ChatMember adminMember = chatMemberRepository.save(ChatMember.of(user, chatRoom, ChatMemberRole.ADMIN));

        // when
        chatRoomLeaveService.execute(adminMember.getId());

        // then
        ChatMember deletedMember = chatMemberRepository.findById(adminMember.getId()).orElseThrow();
        Optional<ChatRoom> deletedRoom = chatRoomRepository.findById(chatRoom.getId()); // 조회 조건에서 삭제된 데이터는 조회되지 않음

        assertNotNull(deletedMember.getDeletedAt());
        assertTrue(deletedRoom.isEmpty());
    }

    @Test
    @DisplayName("다른 멤버가 있는 채팅방에서 방장이 나가려고 하면 예외가 발생한다")
    void whenAdminLeavesWithOtherMembers_thenThrowsException() {
        // given
        User adminUser = userRepository.save(UserFixture.GENERAL_USER.toUser());
        User normalUser = userRepository.save(UserFixture.GENERAL_USER.toUser());
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(3L));

        ChatMember adminMember = chatMemberRepository.save(ChatMember.of(adminUser, chatRoom, ChatMemberRole.ADMIN));
        chatMemberRepository.save(ChatMember.of(normalUser, chatRoom, ChatMemberRole.MEMBER));

        // when & then
        assertThatThrownBy(() -> chatRoomLeaveService.execute(adminMember.getId()))
                .isInstanceOf(ChatMemberErrorException.class)
                .hasFieldOrPropertyWithValue("chatMemberErrorCode", ChatMemberErrorCode.ADMIN_CANNOT_LEAVE);

        // 상태가 변경되지 않았는지 확인
        ChatMember unchangedMember = chatMemberRepository.findById(adminMember.getId()).orElseThrow();
        ChatRoom unchangedRoom = chatRoomRepository.findById(chatRoom.getId()).orElseThrow();
        assertNull(unchangedMember.getDeletedAt());
        assertNull(unchangedRoom.getDeletedAt());
    }
}
