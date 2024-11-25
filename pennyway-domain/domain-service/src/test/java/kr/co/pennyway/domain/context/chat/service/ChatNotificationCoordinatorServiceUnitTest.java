package kr.co.pennyway.domain.context.chat.service;

import kr.co.pennyway.domain.context.chat.dto.ChatPushNotificationContext;
import kr.co.pennyway.domain.context.common.fixture.ChatRoomFixture;
import kr.co.pennyway.domain.context.common.fixture.UserFixture;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenRdbService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberRdbService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.session.domain.UserSession;
import kr.co.pennyway.domain.domains.session.service.UserSessionRedisService;
import kr.co.pennyway.domain.domains.session.type.UserStatus;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserRdbService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ChatNotificationCoordinatorServiceUnitTest {
    @Mock
    private UserRdbService userService;

    @Mock
    private ChatMemberRdbService chatMemberService;

    @Mock
    private DeviceTokenRdbService deviceTokenService;

    @Mock
    private UserSessionRedisService userSessionService;

    private ChatNotificationCoordinatorService service;

    @BeforeEach
    public void setUp() {
        service = new ChatNotificationCoordinatorService(userService, chatMemberService, deviceTokenService, userSessionService);
    }

    @Test
    @DisplayName("Happy Path: 채팅방 참여자 중 푸시 알림을 받을 수 있는 사용자가 있는 경우 정상적으로 처리된다.")
    public void determineRecipientsSuccessfully() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);

        ChatNotificationTestFlow.DeviceTokenInfo senderToken = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(1L, "token1", "deviceId1", "iPhone Pro 15");
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(2L, "token2", "deviceId2", "iPhone SE");

        ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
                .givenSender(1L)
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .inChatRoom(chatRoom)
                .withStatus(UserStatus.ACTIVE_CHAT_ROOM, 1L)
                .withDeviceTokens(List.of(senderToken))
                .and()
                .givenRecipient(2L)
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .inChatRoom(chatRoom)
                .withSessionStatuses(Map.of(
                        recipientToken.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP)
                ))
                .withDeviceTokens(List.of(recipientToken))
                .and()
                .whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, 1L);

        // then
        assertThat(context.deviceTokens()).contains(recipientToken.token());
    }

    @Test
    @DisplayName("전송자 정보가 없는 경우 IllegalArgumentException가 발생한다.")
    public void notFoundSender() {
        // given
        given(userService.readUser(anyLong())).willReturn(Optional.empty());

        // when - then
        assertThrows(IllegalArgumentException.class, () -> service.determineRecipients(1L, 1L));

        verify(chatMemberService, never()).readUserIdsByChatRoomId(anyLong());
    }

    @Test
    @DisplayName("전송자는 푸시 알림 대상에서 제외된다.")
    public void excludeSender() {
        // given
        Long chatRoomId = 1L;

        User sender = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "sender", "발신자", NotifySetting.of(true, true, true));
        given(userService.readUser(anyLong())).willReturn(Optional.of(sender));
        DeviceToken deviceToken = DeviceToken.of("token1", "deviceId1", "iPhone Pro 15", sender);
        UserSession senderSession = UserSession.of(sender.getId(), deviceToken.getDeviceId(), deviceToken.getDeviceName());
        senderSession.updateStatus(UserStatus.ACTIVE_CHAT_ROOM, chatRoomId);

        given(chatMemberService.readUserIdsByChatRoomId(anyLong())).willReturn(new HashSet<>(List.of(sender.getId())));

        // when
        ChatPushNotificationContext context = service.determineRecipients(sender.getId(), chatRoomId);

        // then
        assertThat(context.deviceTokens()).isEmpty();

        verify(userSessionService, never()).readAll(anyLong());
    }

    @Test
    @DisplayName("채팅방 알림이 비활성화된 사용자의 모든 디바이스 토큰은 제외된다.")
    public void excludeUserWithDisabledChatRoomNotification() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken1 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(2L, "token2", "deviceId2", "iPhone SE");
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken2 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(3L, "token3", "deviceId3", "Galaxy S21");

        ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
                .givenSender(1L)
                .withNotifyEnabled()
                .inChatRoom(chatRoom)
                .and()
                .givenRecipient(2L)
                .withNotifyDisabled()  // 채팅 알림 비활성화
                .withChatRoomNotifyEnabled()
                .withDeviceTokens(List.of(recipientToken1, recipientToken2))
                .withSessionStatuses(Map.of(
                        recipientToken1.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP),
                        recipientToken2.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP)
                ))
                .inChatRoom(chatRoom)
                .and()
                .whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, chatRoom.getId());

        // then
        assertThat(context.deviceTokens()).isEmpty();
    }

    @Test
    @DisplayName("채팅 알림이 비활성화된 사용자의 모든 디바이스 토큰은 제외된다.")
    public void excludeUserWithDisabledChatNotification() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken1 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(2L, "token2", "deviceId2", "iPhone SE");
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken2 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(3L, "token3", "deviceId3", "Galaxy S21");

        ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
                .givenSender(1L)
                .withNotifyEnabled()
                .inChatRoom(chatRoom)
                .and()
                .givenRecipient(2L)
                .withNotifyEnabled()
                .withChatRoomNotifyDisabled()  // 채팅 알림 비활성화
                .withDeviceTokens(List.of(recipientToken1, recipientToken2))
                .withSessionStatuses(Map.of(
                        recipientToken1.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP),
                        recipientToken2.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP)
                ))
                .inChatRoom(chatRoom)
                .and()
                .whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, chatRoom.getId());

        // then
        assertThat(context.deviceTokens()).isEmpty();
    }

    @Test
    @DisplayName("같은 채팅방 혹은 채팅방 리스트 뷰를 보고 있는 사용자는 대상에서 제외된다.")
    public void excludeUserViewingChatRoom() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        ChatRoom otherChatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(2L);

        ChatNotificationTestFlow.DeviceTokenInfo recipientToken1 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(2L, "token2", "deviceId2", "iPhone SE");
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken2 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(3L, "token3", "deviceId3", "Galaxy S21");
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken3 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(4L, "token4", "deviceId4", "Galaxy Flip 6");

        ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
                .givenSender(1L)
                .withNotifyEnabled()
                .inChatRoom(chatRoom)
                .and()
                .givenRecipient(2L) // 다른 채팅방에 참여 중
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .withDeviceTokens(List.of(recipientToken1))
                .withSessionStatuses(Map.of(
                        recipientToken1.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_CHAT_ROOM, otherChatRoom.getId())
                ))
                .inChatRoom(otherChatRoom)
                .and()
                .givenRecipient(3L) // 채팅방 리스트 뷰
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .withDeviceTokens(List.of(recipientToken2))
                .withSessionStatuses(Map.of(
                        recipientToken2.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_CHAT_ROOM_LIST)
                ))
                .inChatRoom(chatRoom)
                .and()
                .givenRecipient(4L) // 같은 채팅방 참여 중
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .withDeviceTokens(List.of(recipientToken3))
                .withSessionStatuses(Map.of(
                        recipientToken3.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_CHAT_ROOM, chatRoom.getId())
                ))
                .inChatRoom(chatRoom)
                .and()
                .whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, chatRoom.getId());

        // then
        assertThat(context.deviceTokens()).contains(recipientToken1.token());
    }

    @Test
    @DisplayName("사용자 세션 중 하나라도 해당 채팅방을 보고 있을 경우, 사용자의 모든 디바이스 토큰은 제외된다.")
    public void excludeUserViewingChatRoomAtLeastOne() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken1 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(2L, "token2", "deviceId2", "iPhone SE");
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken2 = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(3L, "token3", "deviceId3", "Galaxy S21");

        ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
                .givenSender(1L)
                .withNotifyEnabled()
                .inChatRoom(chatRoom)
                .and()
                .givenRecipient(2L)
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .withDeviceTokens(List.of(recipientToken1, recipientToken2))
                .withSessionStatuses(Map.of(
                        recipientToken1.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP),
                        recipientToken2.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_CHAT_ROOM, chatRoom.getId())
                ))
                .inChatRoom(chatRoom)
                .and()
                .whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, chatRoom.getId());

        // then
        assertThat(context.deviceTokens()).isEmpty();
    }

    @Test
    @DisplayName("경계 테스트: 채팅방에 참여한 사용자가 없는 경우 빈 디바이스 토큰 리스트를 반환한다.")
    public void noParticipantsInChatRoom() {
        // given
        User sender = UserFixture.GENERAL_USER.toUser();
        given(userService.readUser(anyLong())).willReturn(Optional.of(sender));
        given(chatMemberService.readUserIdsByChatRoomId(anyLong())).willReturn(Collections.emptySet());

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, 1L);

        // then
        assertThat(context.deviceTokens()).isEmpty();
    }

    @Test
    @DisplayName("비활성화된 디바이스 토큰은 푸시 알림 대상에서 제외된다")
    public void excludeInactiveDeviceTokens() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        ChatNotificationTestFlow.DeviceTokenInfo activeToken = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(2L, "token2", "deviceId2", "iPhone SE");
        ChatNotificationTestFlow.DeviceTokenInfo inactiveToken = ChatNotificationTestFlow.DeviceTokenInfo.createDeactivateToken(3L, "token3", "deviceId3", "iPad Pro");

        ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
                .givenSender(1L)
                .withNotifyEnabled()
                .inChatRoom(chatRoom)
                .and()
                .givenRecipient(2L)
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .withDeviceTokens(List.of(activeToken, inactiveToken))
                .withSessionStatuses(Map.of(
                        activeToken.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP),
                        inactiveToken.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP)
                ))
                .inChatRoom(chatRoom)
                .and()
                .whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, chatRoom.getId());

        // then
        assertThat(context.deviceTokens()).containsExactly(activeToken.token());
    }

    @Test
    @DisplayName("사용자가 매우 많은 디바이스를 가지고 있는 경우에도 정상적으로 처리된다")
    public void handleUserWithManyDevices() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        List<ChatNotificationTestFlow.DeviceTokenInfo> manyDevices = IntStream.range(0, 10)
                .mapToObj(i -> ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(
                        (long) (i + 2),
                        "token" + (i + 2),
                        "deviceId" + (i + 2),
                        "Device " + (i + 2)
                ))
                .collect(Collectors.toList());

        Map<String, ChatNotificationTestFlow.SessionStatus> deviceStatuses = manyDevices.stream()
                .collect(Collectors.toMap(
                        ChatNotificationTestFlow.DeviceTokenInfo::deviceId,
                        deviceToken -> ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP)
                ));

        ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
                .givenSender(1L)
                .withNotifyEnabled()
                .inChatRoom(chatRoom)
                .and()
                .givenRecipient(2L)
                .withNotifyEnabled()
                .withChatRoomNotifyEnabled()
                .withDeviceTokens(manyDevices)
                .withSessionStatuses(deviceStatuses)
                .inChatRoom(chatRoom)
                .and()
                .whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, chatRoom.getId());

        // then
        assertThat(context.deviceTokens())
                .hasSize(10)
                .containsExactlyInAnyOrderElementsOf(
                        manyDevices.stream().map(ChatNotificationTestFlow.DeviceTokenInfo::token).collect(Collectors.toList())
                );
    }

    @Test
    @DisplayName("채팅방에 매우 많은 사용자가 있는 경우에도 정상적으로 처리된다")
    public void handleChatRoomWithManyUsers() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);
        ChatNotificationTestFlow flow = ChatNotificationTestFlow.init(
                        userService, chatMemberService, userSessionService, deviceTokenService
                )
                .givenSender(1L)
                .withNotifyEnabled()
                .inChatRoom(chatRoom)
                .and();

        // 100명의 사용자 추가
        IntStream.range(0, 100).forEach(i -> {
            ChatNotificationTestFlow.DeviceTokenInfo token = ChatNotificationTestFlow.DeviceTokenInfo.createActivateToken(
                    (long) (i + 2),
                    "token" + (i + 2),
                    "deviceId" + (i + 2),
                    "Device " + (i + 2)
            );

            flow.givenRecipient((long) (i + 2))
                    .withNotifyEnabled()
                    .withChatRoomNotifyEnabled()
                    .withDeviceTokens(List.of(token))
                    .withSessionStatuses(Map.of(
                            token.deviceId(), ChatNotificationTestFlow.SessionStatus.of(UserStatus.ACTIVE_APP)
                    ))
                    .inChatRoom(chatRoom)
                    .and();
        });

        flow.whenMocking();

        // when
        ChatPushNotificationContext context = service.determineRecipients(1L, chatRoom.getId());

        // then
        assertThat(context.deviceTokens()).hasSize(100);
    }
}

/**
 * 채팅 알림 기능에 대한 테스트를 쉽게 작성할 수 있도록 도와주는 테스트 유틸리티 클래스입니다.
 * BDD 스타일의 플루언트 인터페이스를 제공하여 테스트 시나리오를 쉽게 구성할 수 있습니다.
 *
 * <p>기본 사용 예시:
 * <pre>{@code
 * @Test
 * void testExample() {
 *     ChatNotificationTestFlow.init(userService, chatMemberService, userSessionService, deviceTokenService)
 *         .defaultScenario()  // 기본 시나리오 설정
 *         .whenMocking();     // 모킹 설정
 *
 *     // when
 *     ChatPushNotificationContext result = service.determineRecipients(1L, 1L);
 *
 *     // then
 *     assertThat(result.deviceTokens()).hasSize(1);
 * }
 * }</pre>
 *
 * <p>커스텀 시나리오 예시:
 * <pre>{@code
 * ChatNotificationTestFlow.init(...)
 *     .givenSender(1L)           // 발신자 설정
 *         .withNotifyEnabled()    // 알림 활성화 상태
 *         .and()
 *     .givenRecipient(2L)        // 수신자 설정
 *         .withNotifyEnabled()    // 알림 활성화
 *         .withActiveAppStatus()  // 앱 사용 중 상태
 *     .whenMocking();
 * }</pre>
 */
@Slf4j
@TestComponent
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ChatNotificationTestFlow {
    private final UserRdbService userService;
    private final ChatMemberRdbService chatMemberService;
    private final UserSessionRedisService userSessionService;
    private final DeviceTokenRdbService deviceTokenService;

    private final Long DEFAULT_SENDER_ID = 1L;
    private final Long DEFAULT_CHAT_ROOM_ID = 1L;
    private final Long DEFAULT_RECIPIENT_ID = 2L;
    private final Set<Long> recipientIds = new HashSet<>();
    private final Map<Long, User> recipients = new HashMap<>();
    private final Map<Long, ChatMember> chatMembers = new HashMap<>();
    private final Map<Long, List<UserSession>> sessions = new HashMap<>();
    private final Map<Long, List<DeviceToken>> deviceTokens = new HashMap<>();
    private final Map<Long, ChatRoom> chatRooms = new HashMap<>();  // 채팅방 ID를 키로 사용
    private Long senderId = DEFAULT_SENDER_ID;
    private Long chatRoomId = DEFAULT_CHAT_ROOM_ID;
    private User sender;

    /**
     * ChatNotificationTestFlow 인스턴스를 생성하고 초기화합니다.
     * 모든 필수 서비스 의존성을 주입받습니다.
     *
     * @param userService        사용자 서비스
     * @param chatMemberService  채팅방 멤버 서비스
     * @param userSessionService 사용자 세션 서비스
     * @param deviceTokenService 디바이스 토큰 서비스
     * @return {@link ChatNotificationTestFlow}
     */
    public static ChatNotificationTestFlow init(
            UserRdbService userService,
            ChatMemberRdbService chatMemberService,
            UserSessionRedisService userSessionService,
            DeviceTokenRdbService deviceTokenService
    ) {
        return new ChatNotificationTestFlow(userService, chatMemberService, userSessionService, deviceTokenService);
    }

    /**
     * 가장 일반적인 테스트 시나리오를 설정합니다.
     * 기본 설정:
     * <ul>
     *   <li>발신자(ID: 1): 알림 활성화 상태</li>
     *   <li>수신자(ID: 2): 알림 활성화 상태, 앱 사용 중</li>
     * </ul>
     *
     * @return {@link ChatNotificationTestFlow}
     */
    public ChatNotificationTestFlow defaultScenario() {
        givenSender(DEFAULT_SENDER_ID)
                .withNotifyEnabled()
                .and()
                .givenRecipient(DEFAULT_RECIPIENT_ID)
                .withNotifyEnabled()
                .withStatus(UserStatus.ACTIVE_CHAT_ROOM, DEFAULT_CHAT_ROOM_ID);

        return this;
    }

    /**
     * 테스트할 발신자를 설정합니다.
     * 발신자의 상태는 반환된 SenderBuilder를 통해 추가로 구성할 수 있습니다.
     *
     * @param senderId 발신자 ID
     * @return {@link SenderBuilder} 인스턴스
     */
    public SenderBuilder.NotificationSettingStep givenSender(Long senderId) {
        this.senderId = senderId;
        return new SenderBuilder().new SenderBuilderImpl(this, senderId);
    }

    /**
     * 테스트할 수신자를 설정합니다.
     * 수신자의 상태는 반환된 RecipientBuilder를 통해 추가로 구성할 수 있습니다.
     *
     * @param recipientId 수신자 ID
     * @return {@link RecipientBuilder} 인스턴스
     */
    public RecipientBuilder.NotificationSettingStep givenRecipient(Long recipientId) {
        this.recipientIds.add(recipientId);
        return new RecipientBuilder().new RecipientBuilderImpl(this, recipientId);
    }

    /**
     * 설정된 시나리오에 따라 모든 필요한 모킹을 수행합니다.
     * 이 메서드는 시나리오 구성의 마지막 단계로 호출되어야 합니다.
     *
     * @return {@link ChatNotificationTestFlow}
     */
    public ChatNotificationTestFlow whenMocking() {
        printScenarioPreCondition();

        // 기본 모킹 설정 (언제나 수행)
        // 발신자 정보 반환
        given(userService.readUser(senderId)).willReturn(Optional.ofNullable(sender));

        // 모든 수신자 ID 반환
        given(chatMemberService.readUserIdsByChatRoomId(chatRoomId)).willReturn(recipientIds);

        // 사용자별 세션 정보 반환
        sessions.forEach((userId, userSessions) -> {
            if (userId.equals(senderId)) {
                return;
            }

            log.debug("User ID: {}, Sessions: {}", userId, userSessions);
            given(userSessionService.readAll(userId)).willReturn(userSessions.stream()
                    .collect(Collectors.toMap(UserSession::getDeviceId, session -> session)));
        });

        // 4. 수신자의 채팅방 알림 설정과 디바이스 토큰은 필요한 경우에만 모킹
        recipients.forEach((userId, recipient) -> {
            // 발신자는 제외
            if (userId.equals(senderId)) {
                return;
            }

            if (!isRequireMoking(userId)) {
                return;
            }

            given(userService.readUser(userId)).willReturn(Optional.ofNullable(recipient));

            // 채팅 알림 설정이 비활성화된 사용자는 제외
            if (!recipient.getNotifySetting().isChatNotify()) {
                return;
            }

            ChatMember chatMember = chatMembers.get(userId);
            given(chatMemberService.readChatMember(userId, chatRoomId)).willReturn(Optional.of(chatMember));

            // 채팅방 알림 설정이 비활성화된 사용자는 제외
            if (!chatMember.isNotifyEnabled()) {
                return;
            }

            List<DeviceToken> tokens = deviceTokens.get(userId);
            if (tokens != null && !tokens.isEmpty()) {
                log.debug("User ID: {}, Device Tokens: {}", userId, tokens);
                given(deviceTokenService.readAllByUserId(userId)).willReturn(tokens);
            }
        });

        return this;
    }

    // 사용자 세션 중 하나라도 해당 채팅방을 보고 있거나, 모든 세션이 모킹 대상이 아닐 경우
    private boolean isRequireMoking(Long userId) {
        boolean flag = false;

        for (UserSession session : sessions.get(userId)) {
            if (UserStatus.ACTIVE_CHAT_ROOM.equals(session.getStatus()) && chatRoomId.equals(session.getCurrentChatRoomId())) {
                flag = false;
                break;
            }

            if (!UserStatus.ACTIVE_CHAT_ROOM_LIST.equals(session.getStatus())) {
                flag = true;
            }
        }

        return flag;
    }

    private void printScenarioPreCondition() {
        log.debug("Scenario Pre-Condition");
        log.debug("Sender: {}", sender);
        log.debug("Recipients: {}", recipients);
        log.debug("Chat Members: {}", chatMembers);
        log.debug("Sessions: {}", sessions);
        log.debug("Device Tokens: {}", deviceTokens);
        log.debug("Chat Rooms: {}", chatRooms);
    }

    public record DeviceTokenInfo(Long id, String token, String deviceId, String deviceName, Boolean activated) {
        public static DeviceTokenInfo createActivateToken(Long id, String token, String deviceId, String deviceName) {
            return new DeviceTokenInfo(id, token, deviceId, deviceName, true);
        }

        public static DeviceTokenInfo createDeactivateToken(Long id, String token, String deviceId, String deviceName) {
            return new DeviceTokenInfo(id, token, deviceId, deviceName, false);
        }
    }

    /**
     * 세션 상태 정보를 담는 레코드
     */
    public record SessionStatus(UserStatus status, Long chatRoomId) {
        public static SessionStatus of(UserStatus status) {
            return new SessionStatus(status, null);
        }

        public static SessionStatus of(UserStatus status, Long chatRoomId) {
            return new SessionStatus(status, chatRoomId);
        }
    }

    /**
     * 발신자의 상태를 설정하기 위한 빌더 클래스입니다.
     */
    private class SenderBuilder {
        public interface NotificationSettingStep {
            /**
             * 발신자의 채팅 알림을 활성화 상태로 설정합니다.
             *
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withNotifyEnabled();

            /**
             * 발신자의 채팅 알림을 비활성화 상태로 설정합니다.
             *
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withNotifyDisabled();
        }

        public interface ConfigurationStep {
            /**
             * 발신자의 채팅방 알림을 활성화 상태로 설정합니다.
             *
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withChatRoomNotifyEnabled();

            /**
             * 발신자의 채팅방 알림을 비활성화 상태로 설정합니다.
             *
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withChatRoomNotifyDisabled();

            /**
             * 발신자의 세션 상태를 설정합니다.
             * 채팅방 뷰 상태를 설정하고 싶다면, {@link #withStatus(UserStatus, Long)} 메서드를 사용하세요.
             *
             * @param status 사용자 세션 상태
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withStatus(UserStatus status);

            /**
             * 발신자의 채팅방 뷰 세션 상태를 설정합니다.
             *
             * @param status     사용자 세션 상태
             * @param chatRoomId 채팅방 ID
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withStatus(UserStatus status, Long chatRoomId);

            /**
             * 발신자의 디바이스 토큰을 설정합니다.
             *
             * @param tokenInfos 설정할 디바이스 토큰 정보 목록
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withDeviceTokens(List<DeviceTokenInfo> tokenInfos);

            /**
             * 사용자가 속한 채팅방을 설정합니다.
             *
             * @param chatRoom 채팅방 정보
             * @return ConfigurationStep
             */
            ConfigurationStep inChatRoom(ChatRoom chatRoom);

            /**
             * 설정한 정보를 저장하고, 다음 설정을 위한 빌더 인스턴스를 반환합니다.
             *
             * @return {@link ChatNotificationTestFlow}
             */
            ChatNotificationTestFlow and();
        }

        private class SenderBuilderImpl implements NotificationSettingStep, ConfigurationStep {
            private final ChatNotificationTestFlow flow;
            private final Long senderId;
            private final List<DeviceToken> deviceTokens = new ArrayList<>();
            private User sender;
            private UserStatus status = UserStatus.ACTIVE_APP;
            private Long activeChatRoomId;
            private boolean notifyEnabled = true;
            private ChatRoom chatRoom;

            private SenderBuilderImpl(ChatNotificationTestFlow flow, Long senderId) {
                this.flow = flow;
                this.senderId = senderId;
            }

            @Override
            public ConfigurationStep withNotifyEnabled() {
                this.sender = UserFixture.GENERAL_USER.toUserWithCustomSetting(
                        senderId, "sender", "발신자",
                        NotifySetting.of(true, true, true)
                );
                return this;
            }

            @Override
            public ConfigurationStep withNotifyDisabled() {
                this.sender = UserFixture.GENERAL_USER.toUserWithCustomSetting(
                        senderId, "sender", "발신자",
                        NotifySetting.of(true, true, false)
                );
                return this;
            }

            @Override
            public ConfigurationStep withChatRoomNotifyEnabled() {
                this.notifyEnabled = true;
                return this;
            }

            @Override
            public ConfigurationStep withChatRoomNotifyDisabled() {
                this.notifyEnabled = false;
                return this;
            }

            @Override
            public ConfigurationStep withStatus(UserStatus status) {
                this.status = status;
                this.activeChatRoomId = null;
                return this;
            }

            @Override
            public ConfigurationStep withStatus(UserStatus status, Long chatRoomId) {
                this.status = status;
                this.activeChatRoomId = chatRoomId;
                return this;
            }

            @Override
            public ConfigurationStep withDeviceTokens(List<DeviceTokenInfo> deviceTokens) {
                deviceTokens.forEach(deviceToken -> {
                    DeviceToken token = DeviceToken.of(
                            deviceToken.token(),
                            deviceToken.deviceId(),
                            deviceToken.deviceName(),
                            sender
                    );
                    ReflectionTestUtils.setField(token, "id", deviceToken.id());

                    this.deviceTokens.add(token);
                });
                return this;
            }

            @Override
            public ConfigurationStep inChatRoom(ChatRoom chatRoom) {
                this.chatRoom = chatRoom;
                flow.chatRooms.putIfAbsent(chatRoom.getId(), chatRoom);
                return this;
            }

            @Override
            public ChatNotificationTestFlow and() {
                if (sender == null) {
                    withNotifyEnabled();
                }

                flow.sender = sender;

                if (!deviceTokens.isEmpty()) {
                    flow.deviceTokens.put(senderId, new ArrayList<>(deviceTokens));

                    List<UserSession> sessions = deviceTokens.stream()
                            .map(token -> {
                                UserSession session = UserSession.of(
                                        senderId,
                                        token.getDeviceId(),
                                        token.getDeviceName()
                                );
                                session.updateStatus(status, activeChatRoomId);
                                return session;
                            })
                            .collect(Collectors.toList());

                    flow.sessions.put(senderId, sessions);
                }

                // ChatMember 정보 저장 시 설정된 채팅방 사용
                ChatRoom targetChatRoom = chatRoom != null ? chatRoom : flow.chatRooms.get(flow.chatRoomId);
                ChatMember chatMember = ChatMember.of(
                        sender,
                        targetChatRoom,
                        ChatMemberRole.MEMBER
                );
                ReflectionTestUtils.setField(chatMember, "id", senderId);

                if (!notifyEnabled) {
                    chatMember.disableNotify();
                }
                flow.chatMembers.put(senderId, chatMember);

                return flow;
            }
        }
    }

    /**
     * 수신자의 상태를 설정하기 위한 빌더 클래스입니다.
     */
    private class RecipientBuilder {
        public interface NotificationSettingStep {
            /**
             * 수신자의 채팅 알림을 활성화 상태로 설정합니다.
             *
             * @return {@link RecipientBuilder}
             */
            ConfigurationStep withNotifyEnabled();

            /**
             * 수신자의 채팅 알림을 비활성화 상태로 설정합니다.
             *
             * @return {@link RecipientBuilder}
             */
            ConfigurationStep withNotifyDisabled();
        }

        public interface ConfigurationStep {
            /**
             * 수신자의 채팅방 알림을 활성화 상태로 설정합니다.
             */
            ConfigurationStep withChatRoomNotifyEnabled();

            /**
             * 수신자의 채팅방 알림을 비활성화 상태로 설정합니다.
             */
            ConfigurationStep withChatRoomNotifyDisabled();

            /**
             * 수신자의 세션 상태를 모두 동일하게 설정합니다.
             * 채팅방 뷰 상태를 설정하고 싶다면, {@link #withStatus(UserStatus, Long)} 메서드를 사용하세요.
             *
             * @return {@link RecipientBuilder}
             */
            ConfigurationStep withStatus(UserStatus status);

            /**
             * 수신자의 채팅방 뷰 세션 상태를 모두 동일하게 설정합니다.
             *
             * @return {@link RecipientBuilder}
             */
            ConfigurationStep withStatus(UserStatus status, Long chatRoomId);

            /**
             * 수신자의 디바이스 토큰을 설정합니다.
             * 이 메서드는 기존 디바이스 토큰을 모두 제거하고 새로 설정합니다.
             * <p>
             * 반드시 #withNotifyEnabled() 혹은 #withNotifyDisabled() 메서드를 통해 알림 설정을 먼저 해야 합니다.
             *
             * @param deviceTokens 설정할 디바이스 토큰 정보 목록
             * @return {@link RecipientBuilder}
             */
            ConfigurationStep withDeviceTokens(List<DeviceTokenInfo> deviceTokens);

            /**
             * 사용자가 속한 채팅방을 설정합니다.
             *
             * @param chatRoom 채팅방 정보
             * @return ConfigurationStep
             */
            ConfigurationStep inChatRoom(ChatRoom chatRoom);

            /**
             * 수신자의 디바이스별 세션 상태를 설정합니다.
             * <p>
             * 예시:
             * <pre>
             * .withSessionStatuses(Map.of(
             *     "deviceId1", new SessionStatus(UserStatus.ACTIVE_CHAT_ROOM, chatRoom.getId()),
             *     "deviceId2", new SessionStatus(UserStatus.ACTIVE_APP, null)
             * ))
             * </pre>
             *
             * @param deviceStatuses 디바이스ID를 키로, 세션 상태 정보를 값으로 하는 Map
             * @return {@link ConfigurationStep}
             */
            ConfigurationStep withSessionStatuses(Map<String, SessionStatus> deviceStatuses);

            /**
             * 설정이 완료되었으며, 다음 설정을 위해 {@link ChatNotificationTestFlow}로 돌아갑니다.
             *
             * @return {@link ChatNotificationTestFlow}
             */
            ChatNotificationTestFlow and();
        }

        private class RecipientBuilderImpl implements NotificationSettingStep, ConfigurationStep {
            private final ChatNotificationTestFlow flow;
            private final Long recipientId;
            private final List<DeviceToken> deviceTokens = new ArrayList<>();
            private Map<String, SessionStatus> sessionStatuses = new HashMap<>();
            private User recipient;
            private UserStatus status = UserStatus.ACTIVE_APP;
            private Long activeChatRoomId;
            private boolean notifyEnabled = true;
            private ChatRoom chatRoom;

            private RecipientBuilderImpl(ChatNotificationTestFlow flow, Long recipientId) {
                this.flow = flow;
                this.recipientId = recipientId;
            }

            @Override
            public ConfigurationStep withNotifyEnabled() {
                this.recipient = UserFixture.GENERAL_USER.toUserWithCustomSetting(
                        recipientId,
                        "recipient" + recipientId,
                        "수신자" + recipientId,
                        NotifySetting.of(true, true, true)
                );
                return this;
            }

            @Override
            public ConfigurationStep withNotifyDisabled() {
                this.recipient = UserFixture.GENERAL_USER.toUserWithCustomSetting(
                        recipientId,
                        "recipient" + recipientId,
                        "수신자" + recipientId,
                        NotifySetting.of(true, true, false)
                );
                return this;
            }

            @Override
            public ConfigurationStep withChatRoomNotifyEnabled() {
                this.notifyEnabled = true;
                return this;
            }

            @Override
            public ConfigurationStep withChatRoomNotifyDisabled() {
                this.notifyEnabled = false;
                return this;
            }

            @Override
            public ConfigurationStep withStatus(UserStatus status) {
                this.status = status;
                this.activeChatRoomId = null;

                return this;
            }

            @Override
            public ConfigurationStep withStatus(UserStatus status, Long chatRoomId) {
                this.status = status;
                this.activeChatRoomId = chatRoomId;

                return this;
            }

            @Override
            public ConfigurationStep withDeviceTokens(List<DeviceTokenInfo> deviceTokens) {
                deviceTokens.forEach(deviceToken -> {
                    DeviceToken token = DeviceToken.of(
                            deviceToken.token(),
                            deviceToken.deviceId(),
                            deviceToken.deviceName(),
                            recipient
                    );
                    ReflectionTestUtils.setField(token, "id", deviceToken.id());

                    if (!deviceToken.activated()) {
                        token.deactivate();
                    }

                    this.deviceTokens.add(token);
                });

                return this;
            }

            @Override
            public ConfigurationStep withSessionStatuses(Map<String, SessionStatus> deviceStatuses) {
                this.sessionStatuses = new HashMap<>(deviceStatuses);
                return this;
            }

            @Override
            public ConfigurationStep inChatRoom(ChatRoom chatRoom) {
                this.chatRoom = chatRoom;
                flow.chatRooms.putIfAbsent(chatRoom.getId(), chatRoom);
                return this;
            }

            @Override
            public ChatNotificationTestFlow and() {
                if (recipient == null) {
                    withNotifyEnabled();
                }

                flow.recipientIds.add(recipientId);
                flow.recipients.put(recipientId, recipient);

                if (!deviceTokens.isEmpty()) {
                    flow.deviceTokens.put(recipientId, new ArrayList<>(deviceTokens));

                    List<UserSession> sessions = deviceTokens.stream()
                            .map(token -> {
                                UserSession session = UserSession.of(
                                        recipientId,
                                        token.getDeviceId(),
                                        token.getDeviceName()
                                );

                                // 디바이스별 상태 설정
                                SessionStatus sessionStatus = sessionStatuses.getOrDefault(
                                        token.getDeviceId(),
                                        new SessionStatus(UserStatus.ACTIVE_APP, null)  // 기본값
                                );
                                session.updateStatus(sessionStatus.status(), sessionStatus.chatRoomId());

                                return session;
                            })
                            .collect(Collectors.toList());

                    flow.sessions.put(recipientId, sessions);
                }

                // ChatMember 정보 저장 시 설정된 채팅방 사용
                ChatRoom targetChatRoom = chatRoom != null ? chatRoom : flow.chatRooms.get(flow.chatRoomId);
                ChatMember chatMember = ChatMember.of(
                        recipient,
                        targetChatRoom,
                        ChatMemberRole.MEMBER
                );
                ReflectionTestUtils.setField(chatMember, "id", recipientId);

                if (!notifyEnabled) {
                    chatMember.disableNotify();
                }
                flow.chatMembers.put(recipientId, chatMember);

                return flow;
            }
        }
    }
}