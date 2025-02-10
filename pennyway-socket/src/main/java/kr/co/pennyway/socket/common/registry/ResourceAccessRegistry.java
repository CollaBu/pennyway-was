package kr.co.pennyway.socket.common.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 리소스 접근 권한 체커를 관리하는 레지스트리
 * path에 대한 checker를 내부적으로 관리한다.
 */
public final class ResourceAccessRegistry {
    private final Map<Pattern, ResourceAccessChecker> checkers = new HashMap<>();

    public ResourceAccessRegistry() {
    }

    public void registerChecker(final String pathPattern, final ResourceAccessChecker checker) {
        checkers.put(Pattern.compile(pathPattern), checker);
    }

    /**
     * path에 대한 체커를 반환한다.
     *
     * @param path : 요청 경로
     * @return ResourceAccessChecker : path에 대한 체커
     * @throws IllegalArgumentException : 해당 경로에 대한 체커가 없는 경우
     */
    public ResourceAccessChecker getChecker(final String path) {
        return checkers.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(path).matches())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 경로에 대한 체커가 없습니다. path = " + path));
    }
}