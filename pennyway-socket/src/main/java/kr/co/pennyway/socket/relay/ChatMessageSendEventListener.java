package kr.co.pennyway.socket.relay;

import kr.co.pennyway.domain.common.redis.session.UserSession;
import kr.co.pennyway.domain.common.redis.session.UserSessionService;
import kr.co.pennyway.domain.common.redis.session.UserStatus;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.infra.common.event.NotificationEvent;
import kr.co.pennyway.socket.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSendEventListener {
    private final ApplicationEventPublisher eventPublisher;

    private final UserSessionService userSessionService;
    private final UserService userService;
    private final ChatMemberService chatMemberService;

    private final DeviceTokenService deviceTokenService;

    @RabbitListener(
            containerFactory = "simpleRabbitListenerContainerFactory",
            bindings = @QueueBinding(
                    value = @Queue("${pennyway.rabbitmq.chat.queue}"),
                    exchange = @Exchange(value = "${pennyway.rabbitmq.chat.exchange}", type = "topic"),
                    key = "${pennyway.rabbitmq.chat.routing-key}"
            ),
            exclusive = true,
            concurrency = "1"
    )
    public void handleSendEvent(ChatMessageDto.Response event) {
        log.info("ChatMessageSendEventListener.handleSendEvent: {}", event);

        User sender = userService.readUser(event.senderId())
                .orElseThrow(() -> new IllegalArgumentException("전송자 정보를 찾을 수 없습니다."));

        // [STEP]
        // 1. 채팅방에 참여 중인 사용자 세션들을 가져옴 (사용자 별로 여러 세션이 존재할 수 있음)
        // 2. 사용자 세션 중에서 전송자는 제외하고, 채팅방에 참여 중 혹은 채팅방 리스트 뷰를 보고 있지 않은 사용자들만 필터링
        // 3. 사용자 세션을 사용자 아이디 별로 그룹핑
        Map<Long, Set<UserSession>> participants = userSessionService.readAllByChatRoom(event.chatRoomId()).stream()
                .filter(userSession -> !userSession.getUserId().equals(event.senderId()))
                .filter(userSession ->
                        !(UserStatus.ACTIVE_CHAT_ROOM.equals(userSession.getStatus()) && event.chatRoomId().equals(userSession.getCurrentChatRoomId()) &&
                                !UserStatus.ACTIVE_CHAT_ROOM_LIST.equals(userSession.getStatus())))
                .collect(Collectors.groupingBy(UserSession::getUserId));

        // [STEP]
        // 1. 사용자 아이디로 채팅 알림 off 여부 판단. 만약 false면, 해당 사용자는 모두 제외
        // 2. 사용자 아이디로 채팅방의 알림 off 여부 판단. 만약 false면, 해당 사용자는 모두 제외
        // 3. 사용자 아이디로 디바이스 토큰을 가져옴
        Set<UserSession> targets = participants.entrySet().stream()
                .filter(entry -> {
                    Optional<User> user = userService.readUser(entry.getKey()); // N 쿼리 발생
                    return user.isPresent() && user.get().getNotifySetting().isChatNotify();
                })
                .filter(entry -> {
                    Optional<ChatMember> chatMember = chatMemberService.readChatMember(entry.getKey(), event.chatRoomId()); // N 쿼리 발생
                    return chatMember.isPresent() && chatMember.get().isNotifyEnabled();
                })
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toUnmodifiableSet());

        // 3. 푸시 알림 대상자들의 deviceToken 스캔 (userId, deviceTokens Map) : ㅋㅋㅋ 같은 사용자도 기기별로 구분해야 함.
        Map<Long, List<String>> deviceTokens = new HashMap<>();

        // UserSession의 deviceId, deviceName에 해당되는 deviceToken을 가져와야 함.
        for (UserSession target : targets) {
            List<DeviceToken> tokens = deviceTokenService.readActiveDeviceTokensByUserId(target.getUserId()).stream()
                    .filter(deviceToken -> deviceToken.getDeviceId().equals(target.getDeviceId()) && deviceToken.getDeviceName().equals(target.getDeviceName()))
                    .toList();

            deviceTokens.put(target.getUserId(), tokens.stream().map(DeviceToken::getToken).toList());
        }

        // 4. 해당 채팅방에 있는 사용자에게만 푸시 알림 전송 (sender 이름, 프로필 사진 url, 채팅 내용, 전송 시간, DeepLink 정보 포함)
        deviceTokens.entrySet().forEach(entry -> {
            NotificationEvent notificationEvent = NotificationEvent.of(
                    sender.getName(),
                    event.content(),
                    entry.getValue(),
                    sender.getProfileImageUrl(),
                    List.of("chatRoomId", event.chatRoomId().toString())
            );

            eventPublisher.publishEvent(notificationEvent);
        });
    }
}
