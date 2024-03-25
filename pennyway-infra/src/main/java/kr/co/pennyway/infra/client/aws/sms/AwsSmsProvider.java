package kr.co.pennyway.infra.client.aws.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

// TODO: AWS SNS 인프라 설정 후 내부 구현
@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSmsProvider implements SmsProvider {
    @Override
    public SmsDto.Info sendCode(SmsDto.To dto) {
        String code = issueVerificationCode();
        SmsDto.Response response = SmsDto.Response.builder().requestAt(LocalDateTime.now()).build();
        return SmsDto.Info.from(response, code);
    }

    private String issueVerificationCode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(0, 10));
        }
        return sb.toString();
    }
}
