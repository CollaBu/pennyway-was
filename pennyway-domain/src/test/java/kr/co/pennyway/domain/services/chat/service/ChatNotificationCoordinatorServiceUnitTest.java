package kr.co.pennyway.domain.services.chat.service;

import kr.co.pennyway.common.fixture.ChatRoomFixture;
import kr.co.pennyway.common.fixture.UserFixture;
import kr.co.pennyway.domain.common.redis.session.UserSession;
import kr.co.pennyway.domain.common.redis.session.UserSessionService;
import kr.co.pennyway.domain.common.redis.session.UserStatus;
import kr.co.pennyway.domain.domains.chatroom.domain.ChatRoom;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.member.type.ChatMemberRole;
import kr.co.pennyway.domain.domains.user.domain.NotifySetting;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.services.chat.context.ChatPushNotificationContext;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ChatNotificationCoordinatorServiceUnitTest {
    @Mock
    private UserService userService;

    @Mock
    private ChatMemberService chatMemberService;

    @Mock
    private DeviceTokenService deviceTokenService;

    @Mock
    private UserSessionService userSessionService;

    private ChatNotificationCoordinatorService service;

    @BeforeEach
    public void setUp() {
        service = new ChatNotificationCoordinatorService(userService, chatMemberService, deviceTokenService, userSessionService);
    }

    @Test
    @DisplayName("Happy Path: 채팅방 참여자 중 푸시 알림을 받을 수 있는 사용자가 있는 경우 정상적으로 처리된다.")
    public void determineRecipientsSuccessfully() {
        // given
        ChatRoom activeChatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntity();

        User sender = UserFixture.GENERAL_USER.toUserWithCustomSetting(1L, "sender", "발신자", NotifySetting.of(true, true, true));
        User recipient = UserFixture.GENERAL_USER.toUserWithCustomSetting(2L, "recipient1", "참여자1", NotifySetting.of(true, true, true));

        ChatMember senderMember = ChatMember.of(sender, activeChatRoom, ChatMemberRole.MEMBER);
        ChatMember recipientMember = ChatMember.of(recipient, activeChatRoom, ChatMemberRole.MEMBER);

        DeviceToken deviceTokenOfSender = DeviceToken.of("token1", "deviceId1", "iPhone Pro 15", sender);
        DeviceToken deviceTokenOfRecipient = DeviceToken.of("token2", "deviceId2", "iPhone SE", recipient);

        UserSession senderSession = UserSession.of(sender.getId(), deviceTokenOfSender.getDeviceId(), deviceTokenOfSender.getDeviceName());
        UserSession recipientSession = UserSession.of(recipient.getId(), deviceTokenOfRecipient.getDeviceId(), deviceTokenOfRecipient.getDeviceName());
        senderSession.updateStatus(UserStatus.ACTIVE_CHAT_ROOM, activeChatRoom.getId());
        recipientSession.updateStatus(UserStatus.ACTIVE_CHAT_ROOM, activeChatRoom.getId());

        given(userService.readUser(anyLong())).willReturn(Optional.of(sender));
        given(chatMemberService.readUserIdsByChatRoomId(anyLong())).willReturn(new HashSet<>(List.of(sender.getId(), recipient.getId())));
        given(userSessionService.readAll(anyLong())).will(invocation -> {
            Long userId = invocation.getArgument(0);
            if (userId.equals(sender.getId())) {
                return Map.of(deviceTokenOfSender.getDeviceId(), senderSession);
            } else {
                return Map.of(deviceTokenOfRecipient.getDeviceId(), recipientSession);
            }
        });
        given(chatMemberService.readChatMember(anyLong(), anyLong())).will(invocation -> {
            Long userId = invocation.getArgument(0);
            if (userId.equals(sender.getId())) {
                return Optional.of(senderMember);
            } else {
                return Optional.of(recipientMember);
            }
        });
        given(deviceTokenService.readAllByUserId(recipient.getId())).willReturn(List.of(deviceTokenOfRecipient));

        // when
        ChatPushNotificationContext context = service.determineRecipients(sender.getId(), activeChatRoom.getId());

        // then
        assertThat(context.deviceTokens()).contains(deviceTokenOfRecipient.getToken());
    }

    @Test
    @DisplayName("Happy Path: 채팅방 참여자 중 푸시 알림을 받을 수 있는 사용자가 있는 경우 정상적으로 처리된다.")
    public void determineRecipientsSuccessfully2() {
        // given
        ChatRoom chatRoom = ChatRoomFixture.PUBLIC_CHAT_ROOM.toEntityWithId(1L);

        ChatNotificationTestFlow.DeviceTokenInfo senderToken = ChatNotificationTestFlow.DeviceTokenInfo.of("token1", "deviceId1", "iPhone Pro 15");
        ChatNotificationTestFlow.DeviceTokenInfo recipientToken = ChatNotificationTestFlow.DeviceTokenInfo.of("token2", "deviceId2", "iPhone SE");

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
                .withStatus(UserStatus.ACTIVE_CHAT_ROOM, 1L)
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
        given(userSessionService.readAll(anyLong())).willReturn(Map.of(deviceToken.getDeviceId(), senderSession));

        // when
        ChatPushNotificationContext context = service.determineRecipients(sender.getId(), chatRoomId);

        // then
        assertThat(context.deviceTokens()).isEmpty();
    }

    @Test
    @DisplayName("채팅방 알림이 비활성화된 사용자는 제외된다.")
    public void excludeUserWithDisabledChatRoomNotification() {
        // given


        // when

        // then
    }

    @Test
    @DisplayName("채팅 알림이 비활성화된 사용자는 제외된다.")
    public void excludeUserWithDisabledChatNotification() {
        // given

        // when

        // then
    }

    @Test
    @DisplayName("채팅방 혹은 채팅방 리스트 뷰를 보고 있는 사용자는 대상에서 제외된다.")
    public void excludeUserViewingChatRoom() {
        // given

        // when

        // then
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
@TestComponent
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ChatNotificationTestFlow {
    private final UserService userService;
    private final ChatMemberService chatMemberService;
    private final UserSessionService userSessionService;
    private final DeviceTokenService deviceTokenService;

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
            UserService userService,
            ChatMemberService chatMemberService,
            UserSessionService userSessionService,
            DeviceTokenService deviceTokenService
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

    public ChatNotificationTestFlow and() {
        return this;
    }

    /**
     * 설정된 시나리오에 따라 모든 필요한 모킹을 수행합니다.
     * 이 메서드는 시나리오 구성의 마지막 단계로 호출되어야 합니다.
     *
     * @return {@link ChatNotificationTestFlow}
     */
    public ChatNotificationTestFlow whenMocking() {
        // 기본 모킹 설정
        given(userService.readUser(senderId))
                .willReturn(Optional.ofNullable(sender));

        // 모든 수신자 ID 반환
        given(chatMemberService.readUserIdsByChatRoomId(chatRoomId))
                .willReturn(recipientIds);

        // 사용자별 세션 정보 반환
        given(userSessionService.readAll(any()))
                .willAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    List<UserSession> userSessions = sessions.get(userId);

                    if (userSessions == null || userSessions.isEmpty()) {
                        return Collections.emptyMap();
                    }

                    return userSessions.stream()
                            .collect(Collectors.toMap(
                                    UserSession::getDeviceId,
                                    session -> session
                            ));
                });

        // 사용자별 디바이스 토큰 반환
        given(deviceTokenService.readAllByUserId(any()))
                .willAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    List<DeviceToken> tokens = deviceTokens.get(userId);
                    return tokens != null ? tokens : Collections.emptyList();
                });

        // 각 사용자별 채팅방 알림 설정 반환
        given(chatMemberService.readChatMember(any(), any()))
                .willAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    ChatMember chatMember = chatMembers.get(userId);
                    return Optional.ofNullable(chatMember);
                });

        return this;
    }

    public record DeviceTokenInfo(String token, String deviceId, String deviceName) {
        public static DeviceTokenInfo of(String token, String deviceId, String deviceName) {
            return new DeviceTokenInfo(token, deviceId, deviceName);
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
                        NotifySetting.of(false, true, true)
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
                if (!notifyEnabled) {
                    chatMember.notifyDisabled();
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
             * 수신자의 세션 상태를 설정합니다.
             * 채팅방 뷰 상태를 설정하고 싶다면, {@link #withStatus(UserStatus, Long)} 메서드를 사용하세요.
             *
             * @return {@link RecipientBuilder}
             */
            ConfigurationStep withStatus(UserStatus status);

            /**
             * 수신자의 채팅방 뷰 세션 상태를 설정합니다.
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
                        NotifySetting.of(false, true, true)
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
                                session.updateStatus(status, activeChatRoomId);
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
                if (!notifyEnabled) {
                    chatMember.notifyDisabled();
                }
                flow.chatMembers.put(recipientId, chatMember);

                return flow;
            }
        }
    }
}