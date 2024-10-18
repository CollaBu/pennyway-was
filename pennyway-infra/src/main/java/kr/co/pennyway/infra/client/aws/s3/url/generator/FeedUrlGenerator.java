package kr.co.pennyway.infra.client.aws.s3.url.generator;

import kr.co.pennyway.common.util.UUIDUtil;

import java.util.HashMap;
import java.util.Map;

public class FeedUrlGenerator implements UrlGenerator {
    @Override
    public Map<String, String> generate(String type, String ext, String userId, String chatroomId) {
        Map<String, String> variablesMap = new HashMap<>();
        variablesMap.put("uuid", UUIDUtil.generateUUID());
        variablesMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
        variablesMap.put("ext", ext);
        variablesMap.put("feed_id", UUIDUtil.generateUUID());
        return variablesMap;
    }
}
