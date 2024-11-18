package kr.co.pennyway.domain.services.chat.service;

import kr.co.pennyway.common.annotation.DomainService;
import kr.co.pennyway.domain.common.redis.session.UserSession;
import kr.co.pennyway.domain.common.redis.session.UserSessionService;
import kr.co.pennyway.domain.common.redis.session.UserStatus;
import kr.co.pennyway.domain.domains.device.domain.DeviceToken;
import kr.co.pennyway.domain.domains.device.service.DeviceTokenService;
import kr.co.pennyway.domain.domains.member.domain.ChatMember;
import kr.co.pennyway.domain.domains.member.service.ChatMemberService;
import kr.co.pennyway.domain.domains.user.domain.User;
import kr.co.pennyway.domain.domains.user.service.UserService;
import kr.co.pennyway.domain.services.chat.context.ChatPushNotificationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@DomainService
@RequiredArgsConstructor
public class ChatMemberForPushRelaySearchService {
    private final UserService userService;
    private final ChatMemberService chatMemberService;
    private final DeviceTokenService deviceTokenService;

    private final UserSessionService userSessionService;

    /**
     * 채팅방에 참여 중인 사용자들 중에서 푸시 알림을 받아야 하는 사용자들을 판별합니다.
     * <pre>
     * [판별 기준]
     * - 전송자는 푸시 알림을 받지 않습니다.
     * - 채팅방에 참여 중인 사용자 중에서 채팅방 리스트 뷰를 보고 있지 않는 사용자들만 필터링합니다.
     * - 채팅방에 참여 중인 사용자 중에서 채팅 알림을 받지 않는 사용자들은 제외합니다.
     * - 채팅방에 참여 중인 사용자 중에서 채팅방의 알림을 받지 않는 사용자들은 제외합니다.
     * </pre>
     *
     * @param senderId   Long 전송자 아이디. Must not be null.
     * @param chatRoomId Long 채팅방 아이디. Must not be null.
     * @return
     */
    @Transactional(readOnly = true)
    public ChatPushNotificationContext determineRecipients(Long senderId, Long chatRoomId) {
        User sender = userService.readUser(senderId).orElseThrow(() -> new IllegalArgumentException("전송자 정보를 찾을 수 없습니다."));

        // [STEP]
        // 1. 채팅방에 참여 중인 사용자 세션들을 가져옴 (사용자 별로 여러 세션이 존재할 수 있음)
        // 2. 사용자 세션 중에서 전송자는 제외하고, 채팅방에 참여 중 혹은 채팅방 리스트 뷰를 보고 있지 않은 사용자들만 필터링
        // 3. 사용자 세션을 사용자 아이디 별로 그룹핑
        Set<Long> userIds = chatMemberService.readUserIdsByChatRoomId(chatRoomId);

        // {deviceId, userSession} 맵 형태의 사용자 세션들을 가져옴
        List<Map<String, UserSession>> userSessions = userIds.stream()
                .map(userSessionService::readAll)
                .toList();

        // userId 별로 사용자 세션들을 그룹핑
        Map<Long, Set<UserSession>> participants = userSessions.stream()
                .flatMap(userSessionMap -> userSessionMap.entrySet().stream())
                .filter(entry -> !entry.getValue().getUserId().equals(senderId))
                .filter(entry -> !(UserStatus.ACTIVE_CHAT_ROOM.equals(entry.getValue().getStatus()) && chatRoomId.equals(entry.getValue().getCurrentChatRoomId()) &&
                        !(UserStatus.ACTIVE_CHAT_ROOM_LIST.equals(entry.getValue().getStatus()))))
                .collect(Collectors.groupingBy(entry -> entry.getValue().getUserId(), Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));

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
                    Optional<ChatMember> chatMember = chatMemberService.readChatMember(entry.getKey(), chatRoomId); // N 쿼리 발생
                    return chatMember.isPresent() && chatMember.get().isNotifyEnabled();
                })
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toUnmodifiableSet());

        List<String> deviceTokens = new ArrayList<>();

        for (UserSession target : targets) {
            List<String> tokens = deviceTokenService.readAllByUserId(target.getUserId()).stream()
                    .filter(deviceToken -> deviceToken.isActivated() && !deviceToken.isExpired())
                    .filter(deviceToken -> deviceToken.getDeviceId().equals(target.getDeviceId()) && deviceToken.getDeviceName().equals(target.getDeviceName()))
                    .map(DeviceToken::getToken)
                    .toList();

            deviceTokens.addAll(tokens);
        }

        return ChatPushNotificationContext.of(sender.getName(), sender.getProfileImageUrl(), deviceTokens);
    }
}
