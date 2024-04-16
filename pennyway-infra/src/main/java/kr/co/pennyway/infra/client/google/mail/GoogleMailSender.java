package kr.co.pennyway.infra.client.google.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GoogleMailSender {
    private final JavaMailSender javaMailSender;
    private final String adminAddress;

    GoogleMailSender(JavaMailSender javaMailSender, @Value("${app.question-address}") String adminAddress) {
        this.javaMailSender = javaMailSender;
        this.adminAddress = adminAddress;
    }

    public void sendMail(String email, String content, String category) {
        log.info("야 이까지 잘실행된다야");
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(adminAddress);
            helper.setSubject(createSubject(email));
            helper.setText(createContent(email, content, category), true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.warn(e.getMessage());

            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String createSubject(String email) {
        return email + "님께서 문의사항을 남겨 주셨어요.";
    }

    private String createContent(String email, String content, String category) {
        String fromField = "<h2>문의자 : " + email + "</h2>";
        String categoryField = "<h2>카테고리 : " + category + "</h2><br>";
        String contentField = "문의 내용 : " + content;

        return fromField + categoryField + contentField;
    }
}
