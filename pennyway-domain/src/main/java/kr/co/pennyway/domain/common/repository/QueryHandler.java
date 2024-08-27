package kr.co.pennyway.domain.common.repository;

import com.querydsl.jpa.impl.JPAQuery;

/**
 * QueryDsl의 명시적 조인을 위한 함수형 인터페이스
 *
 * @author YANG JAESEO
 */
@FunctionalInterface
public interface QueryHandler {
    JPAQuery<?> apply(JPAQuery<?> query);
}
