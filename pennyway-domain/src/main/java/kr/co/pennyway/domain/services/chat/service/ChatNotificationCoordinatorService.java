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
public class ChatNotificationCoordinatorService {
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
     * @return {@link ChatPushNotificationContext} 전송자와 푸시 알림을 받아야 하는 사용자들의 정보를 담은 컨텍스트
     */
    @Transactional(readOnly = true)
    public ChatPushNotificationContext determineRecipients(Long senderId, Long chatRoomId) {
        User sender = userService.readUser(senderId).orElseThrow(() -> new IllegalArgumentException("전송자 정보를 찾을 수 없습니다."));

        Map<Long, Set<UserSession>> participants = getUserSessionGroupByUserId(senderId, chatRoomId);
        Set<UserSession> targets = filterNotificationEnabledUserSessions(participants, chatRoomId);
        List<String> deviceTokens = getDeviceTokens(targets);

        return ChatPushNotificationContext.of(sender.getName(), sender.getProfileImageUrl(), deviceTokens);
    }

    /**
     * <pre>
     * [STEP]
     * 1. 채팅방에 참여 중인 사용자 세션들을 가져옴 (사용자 별로 여러 세션이 존재할 수 있음)
     * 2. 사용자 세션 중에서 전송자는 제외하고, 채팅방에 참여 중 혹은 채팅방 리스트 뷰를 보고 있지 않은 사용자들만 필터링
     * 3. 사용자 세션을 사용자 아이디 별로 그룹핑
     * </pre>
     *
     * @return 사용자 아이디 별로 사용자 세션들을 그룹핑한 맵
     */
    private Map<Long, Set<UserSession>> getUserSessionGroupByUserId(Long senderId, Long chatRoomId) {
        Set<Long> userIds = chatMemberService.readUserIdsByChatRoomId(chatRoomId);

        List<Map<String, UserSession>> userSessions = userIds.stream()
                .map(userSessionService::readAll)
                .toList();

        return userSessions.stream()
                .flatMap(userSessionMap -> userSessionMap.entrySet().stream())
                .filter(entry -> isTargetStatus(entry, senderId, chatRoomId))
                .collect(Collectors.groupingBy(entry -> entry.getValue().getUserId(), Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));
    }

    /**
     * 사용자 세션의 상태가 푸시 알림을 받아야 하는 상태인지 판별합니다.
     *
     * @return 세션 정보가 '전송자'거나, '채팅방에 참여 중'이 아니거나, '채팅방 리스트 뷰'를 보고 있지 않은 경우 false를 반환합니다.
     */
    private boolean isTargetStatus(Map.Entry<String, UserSession> entry, Long senderId, Long chatRoomId) {
        if (entry.getValue().getUserId().equals(senderId)) {
            return false;
        }

        return !(UserStatus.ACTIVE_CHAT_ROOM.equals(entry.getValue().getStatus()) && chatRoomId.equals(entry.getValue().getCurrentChatRoomId()) &&
                !(UserStatus.ACTIVE_CHAT_ROOM_LIST.equals(entry.getValue().getStatus())));
    }

    /**
     * <pre>
     * [STEP]
     * 1. 사용자 아이디로 채팅 알림 off 여부 판단. 만약 false면, 해당 사용자는 모두 제외
     * 2. 사용자 아이디로 채팅방의 알림 off 여부 판단. 만약 false면, 해당 사용자는 모두 제외
     * 3. 사용자 아이디로 디바이스 토큰을 가져옴
     * </pre>
     *
     * @return 푸시 알림을 받아야 하는 사용자 세션들
     */
    private Set<UserSession> filterNotificationEnabledUserSessions(Map<Long, Set<UserSession>> participants, Long chatRoomId) {
        return participants.entrySet().stream()
                .filter(entry -> isChatNotifyEnabled(entry.getKey())) // N개 쿼리 발생
                .filter(entry -> isChatRoomNotifyEnabled(entry.getKey(), chatRoomId)) // N개 쿼리 발생
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean isChatNotifyEnabled(Long userId) {
        Optional<User> user = userService.readUser(userId);
        return user.isPresent() && user.get().getNotifySetting().isChatNotify();
    }

    private boolean isChatRoomNotifyEnabled(Long userId, Long chatRoomId) {
        Optional<ChatMember> chatMember = chatMemberService.readChatMember(userId, chatRoomId);
        return chatMember.isPresent() && chatMember.get().isNotifyEnabled();
    }

    /**
     * 사용자 세션들 중에서 기기별 활성화된 디바이스 토큰들을 가져옵니다.
     *
     * @return 활성화된 디바이스 토큰들
     */
    private List<String> getDeviceTokens(Iterable<UserSession> targets) {
        List<String> deviceTokens = new ArrayList<>();

        for (UserSession target : targets) {
            deviceTokenService.readAllByUserId(target.getUserId()).stream()
                    .filter(DeviceToken::isActivated)
                    .filter(deviceToken -> deviceToken.getDeviceId().equals(target.getDeviceId()))
                    .findFirst()
                    .map(DeviceToken::getToken)
                    .ifPresent(deviceTokens::add);
        }

        return deviceTokens;
    }
}
