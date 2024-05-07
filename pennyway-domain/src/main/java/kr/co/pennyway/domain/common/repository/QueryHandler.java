package kr.co.pennyway.domain.common.repository;

import com.querydsl.jpa.impl.JPAQuery;

/**
 * QueryDsl을 이용한 검색 조건을 처리하는 기본적인 메서드를 선언한 인터페이스
 *
 * @author YANG JAESEO
 */
@FunctionalInterface
public interface QueryHandler {
    JPAQuery<?> apply(JPAQuery<?> query);
}
