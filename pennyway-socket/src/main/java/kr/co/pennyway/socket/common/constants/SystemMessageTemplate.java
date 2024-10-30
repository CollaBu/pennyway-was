package kr.co.pennyway.socket.common.constants;

/**
 * 채팅방 가입, 퇴장 등 시스템 메시지 템플릿 클래스
 */
public enum SystemMessageTemplate {
    JOIN_MESSAGE_FORMAT("%s님이 입장하셨습니다."),
    ;

    private final String value;

    SystemMessageTemplate(String value) {
        this.value = value;
    }

    /**
     * 사용자 이름을 받아 시스템 메시지로 변환한다.
     *
     * @param userName String: 사용자 이름
     * @return 포맷팅된 시스템 메시지
     */
    public String convertToMessage(String userName) {
        return String.format(value, userName);
    }
}
