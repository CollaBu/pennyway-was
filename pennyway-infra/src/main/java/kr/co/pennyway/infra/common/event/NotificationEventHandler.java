package kr.co.pennyway.infra.common.event;

/**
 * FCM 푸시 알림을 처리하는 핸들러 인터페이스
 * <p>
 * 테스트 환경에서 Fcm Config 빈 생성을 막기 위해 사용하며, 테스트 시 해당 인터페이스를 구현한 Mock 객체를 사용한다.
 *
 * @author YANG JAESEO
 * @since 2024-07-09
 */
public interface NotificationEventHandler {
    void handleEvent(NotificationEvent event);
}
