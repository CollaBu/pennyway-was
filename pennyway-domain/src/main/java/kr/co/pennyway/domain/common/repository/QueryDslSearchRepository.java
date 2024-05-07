package kr.co.pennyway.domain.common.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * QueryDsl을 이용한 검색 조건을 처리하는 기본적인 메서드를 선언한 인터페이스
 *
 * @author YANG JAESEO
 */
public interface QueryDslSearchRepository<T> {

    /**
     * 검색 조건에 해당하는 도메인 리스트를 조회하는 메서드
     *
     * @param predicate : 검색 조건
     * @param queryHandler : 검색 조건에 추가적으로 적용할 조건
     * @param sort : 정렬 조건
     *
     * // @formatter:off
     * <pre>
     * {@code
     * @Component
     * class SearchService {
     *      private final QEntity entity = QEntity.entity;
     *      private final QEntityChild entityChild = QEntityChild.entityChild;
     *
     *      private Entity select() {
     *          Predicate predicate = new BooleanBuilder();
     *          predicate.and(entity.id.eq(1L));
     *
     *          QueryHandler queryHandler = query -> query.leftJoin(entityChild).on(entity.id.eq(entityChild.entity.id));
     *          Sort sort = Sort.by(Sort.Direction.DESC, entity.id);
     *
     *          return searchRepository.findList(predicate, queryHandler, sort);
     *      }
     * }
     * }
     * </pre>
     * // @formatter:on
     *
     * @see Predicate
     * @see QueryHandler
     * @see org.springframework.data.domain.PageRequest
     */
    List<T> findList(Predicate predicate, QueryHandler queryHandler, Sort sort);

    /**
     * 검색 조건에 해당하는 도메인 페이지를 조회하는 메서드
     *
     * @param predicate : 검색 조건
     * @param queryHandler : 검색 조건에 추가적으로 적용할 조건
     * @param pageable : 페이지 정보
     *
     * // @formatter:off
     * <pre>
     * {@code
     * @Component
     * class SearchService {
     *      private final QEntity entity = QEntity.entity;
     *      private final QEntityChild entityChild = QEntityChild.entityChild;
     *
     *      private Entity select() {
     *          Predicate predicate = new BooleanBuilder();
     *          predicate.and(entity.id.eq(1L));
     *
     *          QueryHandler queryHandler = query -> query.leftJoin(entityChild).on(entity.id.eq(entityChild.entity.id));
     *          Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, entity.id));
     *
     *          return searchRepository.findList(predicate, queryHandler, pageable);
     *      }
     * }
     * }
     * </pre>
     * // @formatter:on
     *
     * @see Predicate
     * @see QueryHandler
     * @see org.springframework.data.domain.PageRequest
     */
    Page<T> findPage(Predicate predicate, QueryHandler queryHandler, Pageable pageable);

    /**
     * 검색 조건에 해당하는 DTO 리스트를 조회하는 메서드
     *
     * @param predicate : 검색 조건
     * @param type : 조회할 도메인(혹은 DTO) 타입
     * @param bindings : 검색 조건에 해당하는 도메인(혹은 DTO)의 필드
     * @param queryHandler : 검색 조건에 추가적으로 적용할 조건
     * @param sort : 정렬 조건
     *
     * // @formatter:off
     * <pre>
     * {@code
     * @Component
     * class SearchService {
     *      private final QEntity entity = QEntity.entity;
     *      private final QEntityChild entityChild = QEntityChild.entityChild;
     *
     *      private EntityDto select() {
     *          Predicate predicate = new BooleanBuilder();
     *          predicate.and(entity.id.eq(1L));
     *
     *          QueryHandler queryHandler = query -> query.leftJoin(entityChild).on(entity.id.eq(entityChild.entity.id));
     *          Sort sort = Sort.by(Sort.Direction.DESC, entity.id);
     *
     *          return searchRepository.findList(predicate, EntityDto.class, this.buildBindings(), queryHandler, sort);
     *      }
     *
     *      private Map<String, Expression<?>> buildBindings() {
     *          Map<String, Expression<?>> bindings = new HashMap<>();
     *
     *          bindings.put("id", entity.id);
     *          bindings.put("name", entity.name);
     *
     *          return bindings;
     *      }
     * }
     * }
     * </pre>
     * // @formatter:on
     *
     * @see Predicate
     * @see QueryHandler
     * @see org.springframework.data.domain.PageRequest
     */
    <P> List<P> selectList(Predicate predicate, Class<P> type, Map<String, Expression<?>> bindings, QueryHandler queryHandler, Sort sort);

    /**
     * 검색 조건에 해당하는 DTO 페이지를 조회하는 메서드
     *
     * @param predicate : 검색 조건
     * @param type : 조회할 도메인(혹은 DTO) 타입
     * @param bindings : 검색 조건에 해당하는 도메인(혹은 DTO)의 필드
     * @param queryHandler : 검색 조건에 추가적으로 적용할 조건
     * @param pageable : 페이지 정보
     *
     * // @formatter:off
     * <pre>
     * {@code
     * @Component
     * class SearchService {
     *      private final QEntity entity = QEntity.entity;
     *      private final QEntityChild entityChild = QEntityChild.entityChild;
     *
     *      private EntityDto select() {
     *          Predicate predicate = new BooleanBuilder();
     *          predicate.and(entity.id.eq(1L));
     *          QueryHandler queryHandler = query -> query.leftJoin(entityChild).on(entity.id.eq(entityChild.entity.id));
     *          Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, entity.id));
     *
     *          return searchRepository.findPage(predicate, EntityDto.class, this.buildBindings(), queryHandler, pageable);
     *      }
     *
     *      private Map<String, Expression<?>> buildBindings() {
     *          Map<String, Expression<?>> bindings = new HashMap<>();
     *          bindings.put("id", entity.id);
     *          bindings.put("name", entity.name);
     *          return bindings;
     *      }
     *  }
     *  }
     *  </pre>
     * // @formatter:on
     *
     * @see Predicate
     * @see QueryHandler
     * @see org.springframework.data.domain.PageRequest
     */
    <P> Page<P> selectPage(Predicate predicate, Class<P> type, Map<String, Expression<?>> bindings, QueryHandler queryHandler, Pageable pageable);
}
