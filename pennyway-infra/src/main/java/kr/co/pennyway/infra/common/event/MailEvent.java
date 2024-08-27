package kr.co.pennyway.infra.common.event;

public record MailEvent(
        String email,
        String content,
        String category
) {
    public static MailEvent of(String email, String content, String category) {
        return new MailEvent(email, content, category);
    }
}
