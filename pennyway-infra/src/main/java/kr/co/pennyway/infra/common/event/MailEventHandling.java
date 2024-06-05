package kr.co.pennyway.infra.common.event;

import kr.co.pennyway.infra.client.google.mail.GoogleMailSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@AllArgsConstructor
public class MailEventHandling {
    GoogleMailSender googleMailSender;

    /**
     * 관리자의 메일로 문의사항을 발송합니다.
     * <br/>
     * {@link EventListener}를 통해 createQuestion 트랜잭션 발생시 이벤트를 받아서 메일을 전송합니다.
     *
     * @param event {@link MailEvent}
     */
    @TransactionalEventListener
    @Async
    public void handleMailEvent(MailEvent event) {
        log.info("문의 메일 전송 이벤트 발생: {}", event);
        googleMailSender.sendMail(event.email(), event.content(), event.category());
    }
}