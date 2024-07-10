package kr.co.pennyway.infra.common.event;

/**
 * 푸시 알림을 처리하는 핸들러 인터페이스
 * <p>
 * 푸시 알림을 포함한 기능을 테스트할 때는 해당 인터페이스를 구현한 Mock 객체를 사용한다.
 *
 * @author YANG JAESEO
 * @since 2024-07-09
 */
public interface NotificationEventHandler {
    void handleEvent(NotificationEvent event);
}
