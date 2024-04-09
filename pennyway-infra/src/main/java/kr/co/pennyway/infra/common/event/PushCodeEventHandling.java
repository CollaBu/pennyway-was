package kr.co.pennyway.infra.common.event;

import kr.co.pennyway.infra.client.aws.sms.SmsDto;
import kr.co.pennyway.infra.client.aws.sms.SmsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushCodeEventHandling {
    private final SmsProvider awsSmsProvider;

    /**
     * 사용자의 전화번호로 인증코드를 발신합니다.
     * <br/>
     * {@link EventListener}를 통해 이벤트를 받아서 SMS 메시지를 전송합니다.
     *
     * @param event {@link PushCodeEvent}
     */
    @EventListener
    public void handlePhoneVerificationEvent(PushCodeEvent event) {
        log.debug("handlePhoneVerificationEvent: {}", event);
        SmsDto.Info result = awsSmsProvider.sendCode(SmsDto.Request.from(event));
        log.info("Successfully sent SMS message - sent id: {}", result.requestId());
    }
}
