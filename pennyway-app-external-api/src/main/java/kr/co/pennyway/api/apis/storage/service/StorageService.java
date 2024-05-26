package kr.co.pennyway.api.apis.storage.service;

import java.net.URI;

import org.springframework.stereotype.Service;

import kr.co.pennyway.infra.client.aws.s3.AwsS3Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
	private final AwsS3Provider awsS3Provider;

	public URI getPresignedUrl(String type, String ext, String userId, String chatId, String chatroomId) {
		return awsS3Provider.generatedPresignedUrl(type, ext, userId, chatId, chatroomId);
	}
}
