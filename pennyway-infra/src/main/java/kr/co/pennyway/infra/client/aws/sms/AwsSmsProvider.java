package kr.co.pennyway.infra.client.aws.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSmsProvider implements SmsProvider {
    private final SnsClient snsClient;

    @Override
    public SmsDto.Info sendCode(SmsDto.Request dto) {
        PublishResponse response = publishCodeSms(dto.parsePhone(), dto.code());
        
        return SmsDto.Info.of(response.messageId(), dto.code(), LocalDateTime.now());
    }

    private PublishResponse publishCodeSms(String phone, String code) {
        PublishRequest request = PublishRequest.builder()
                .message("[Pennyway] 인증번호 [" + code + "]를 입력해주세요.")
                .phoneNumber(phone)
                .build();

        try {
            return snsClient.publish(request);
        } catch (SnsException e) {
            log.error("SMS 전송 실패: {}", e.getMessage());
            throw new RuntimeException("SMS 전송에 실패했습니다.");
        }
    }
}
