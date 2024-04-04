package kr.co.pennyway.api.apis.question.usecase;

import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.common.annotation.UseCase;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Slf4j
@UseCase
@AllArgsConstructor
public class QuestionUseCase {
    private final JavaMailSender javaMailSender;

    public void sendQuestion(QuestionReq.General request){
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(request.email());
            helper.setSubject("테스트용 이메일");
            helper.setText(request.content());
            javaMailSender.send(mimeMessage);

        } catch(Exception e) {
            throw new IllegalArgumentException("tset");
        }

    }
}
