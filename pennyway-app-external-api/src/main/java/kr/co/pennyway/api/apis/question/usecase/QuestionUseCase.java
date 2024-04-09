package kr.co.pennyway.api.apis.question.usecase;

import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.api.apis.question.mapper.QuestionMapper;
import kr.co.pennyway.common.annotation.UseCase;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.exception.QuestionErrorCode;
import kr.co.pennyway.domain.domains.question.exception.QuestionErrorException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Slf4j
@UseCase
public class QuestionUseCase {
    private final JavaMailSender javaMailSender;
    private final QuestionMapper questionMapper;
    private String adminAddress;

    public QuestionUseCase(JavaMailSender javaMailSender, QuestionMapper questionMapper, @Value("${app.question-address}") String adminAddress){
        this.javaMailSender = javaMailSender;
        this.questionMapper = questionMapper;
        this.adminAddress = adminAddress;
    }

    public Question sendQuestion(QuestionReq request){
        MimeMessage mimeMessage = questionMapper.createMessage(request, adminAddress);
        // TODO : 일단 DB에 저장되는 부분은 보류
        // Question question = questionMapper.createQuestion(request);

        Question question = request.toEntity();

        try{
            javaMailSender.send(mimeMessage);
            return question;
        } catch(Exception e){
            throw new QuestionErrorException(QuestionErrorCode.INTERNAL_MAILERROR);
        }

    }
}
