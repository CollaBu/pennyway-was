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
     * MimeMessageHelper 객체에 필요한 값들을 채워넣고 반환한다.
     */
    public MimeMessage createMessage(QuestionReq.General request, String address){
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try{
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(address);
            helper.setSubject(createSubject(request));
            helper.setText(createContent(request), true);

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

    private String createSubject(QuestionReq.General request){

        return request.email() + "님께서 문의사항을 남겨 주셨어요.";
    }

    private String createContent(QuestionReq.General request){
        String from = "<h2>문의자 : " + request.email() + "</h2>";
        String category = "<h2>카테고리 : " + request.category().getTitle() + "</h2><br>";
        String content = "문의 내용 : " + request.content();

        return from + category + content;
    }
}
