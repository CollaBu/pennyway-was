package kr.co.pennyway.api.apis.question.mapper;

import jakarta.mail.internet.MimeMessage;
import kr.co.pennyway.api.apis.question.dto.QuestionReq;
import kr.co.pennyway.common.annotation.Mapper;
import kr.co.pennyway.domain.domains.question.domain.Question;
import kr.co.pennyway.domain.domains.question.exception.QuestionErrorCode;
import kr.co.pennyway.domain.domains.question.exception.QuestionErrorException;
import kr.co.pennyway.domain.domains.question.service.QuestionService;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Mapper
@AllArgsConstructor
public class QuestionMapper {
    private final JavaMailSender javaMailSender;
    private final QuestionService questionService;

    /**
     *
     */
    public MimeMessage createMessage(QuestionReq.General request, String address){
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(address);
            helper.setSubject("테스트용 이메일");
            helper.setText(request.content());

            return mimeMessage;
        } catch(Exception e) {
            throw new QuestionErrorException(QuestionErrorCode.INTERNAL_MAILERROR);
        }

    }

    /**
     * Question 객체를 생성해 반환하며, DB에 저장한다.
     */
    public Question createQuestion(QuestionReq.General request){
        Question question = request.toEntity();
        Question response = questionService.createQuestion(question);

        return response;
    }

}
