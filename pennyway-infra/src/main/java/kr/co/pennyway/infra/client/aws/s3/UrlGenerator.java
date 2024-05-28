package kr.co.pennyway.infra.client.aws.s3;

import java.util.Map;

public interface UrlGenerator {
	/**
	 * type에 해당하는 ObjectKeyTemplate을 적용하여 ObjectKey(S3에 저장하기 위한 정적 파일의 경로 및 이름)를 생성한다.
	 * @param type
	 * @param ext
	 * @param userId
	 * @param chatroomId
	 * @return ObjectKey
	 */
	Map<String, String> generate(String type, String ext, String userId, String chatroomId);
}
