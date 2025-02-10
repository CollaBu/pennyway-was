package kr.co.pennyway.api.apis.storage.adapter;

import kr.co.pennyway.api.apis.storage.dto.PresignedUrlDto;
import kr.co.pennyway.infra.client.aws.s3.AwsS3Provider;
import kr.co.pennyway.infra.client.aws.s3.url.properties.PresignedUrlPropertyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresignedUrlGenerateAdapter {
    private final AwsS3Provider awsS3Provider;

    public URI execute(Long userId, PresignedUrlDto.Req request) {
        PresignedUrlPropertyFactory factory = PresignedUrlPropertyFactory.createInstance(request.ext(), request.type(), userId, request.chatroomId());

        return awsS3Provider.generatedPresignedUrl(factory);
    }
}
