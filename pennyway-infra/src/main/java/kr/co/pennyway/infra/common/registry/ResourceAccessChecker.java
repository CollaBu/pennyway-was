package kr.co.pennyway.infra.common.registry;

import java.security.Principal;

/**
 * 리소스 접근 권한을 확인하는 인터페이스
 */
public interface ResourceAccessChecker {
    /**
     * 리소스에 대한 접근 권한을 확인한다.
     *
     * @param path      : 요청 경로
     * @param principal : 요청자
     * @return 접근 권한 여부
     */
    boolean hasPermission(String path, Principal principal);
}
