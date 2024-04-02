package kr.co.pennyway.common.util;

import java.util.Map;

/**
 * Map 관련 유틸리티 클래스
 *
 * @author YANG JAESEO
 */
public class MapUtils {
    /**
     * key에 해당하는 값을 반환하고, key가 없을 경우 defaultValue를 반환한다.
     */
    public static <K, V> V getObject(Map<K, V> map, K key, V defaultValue) {
        if (map == null || key == null) {
            return defaultValue;
        }
        return map.getOrDefault(key, defaultValue);
    }
}