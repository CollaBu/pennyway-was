package kr.co.infra.client.aws.sms;

public interface SmsProvider {
    /**
     * 인증번호를 수신자에게 SMS로 전송
     *
     * @param dto {@link SmsDto.To} : 수신자 번호
     * @return {@link SmsDto.Info} : SNS 전송 정보
     */
    SmsDto.Info sendCodeByPhoneNumber(SmsDto.To dto);
}
