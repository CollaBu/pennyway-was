package kr.co.pennyway.batch.common.reader;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * Querydsl을 이용한 커스텀 PagingItemReader
 * <p>
 * <a href="https://github.com/jojoldu/spring-batch-querydsl">이동욱님 깃헙 </a> 참고
 */
public class QuerydslPagingItemReader<T> extends AbstractPagingItemReader<T> {

    protected final Map<String, Object> jpaPropertyMap = new HashMap<>();
    protected EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;
    protected Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
    protected boolean transacted = true; // default value

    protected QuerydslPagingItemReader() {
        setName(ClassUtils.getShortName(QuerydslPagingItemReader.class));
    }

    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory,
                                    int pageSize,
                                    Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this(entityManagerFactory, pageSize, true, queryFunction);
    }

    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory,
                                    int pageSize,
                                    boolean transacted,
                                    Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this();
        this.entityManagerFactory = entityManagerFactory;
        this.queryFunction = queryFunction;
        setPageSize(pageSize);
        setTransacted(transacted);
    }

    /**
     * Reader의 트랜잭션격리 옵션 <br/>
     * - false: 격리 시키지 않고, Chunk 트랜잭션에 의존한다 <br/>
     * (hibernate.default_batch_fetch_size 옵션 사용가능) <br/>
     * - true: 격리 시킨다 <br/>
     * (Reader 조회 결과를 삭제하고 다시 조회했을때 삭제된게 반영되고 조회되길 원할때 사용한다.)
     */
    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();

        entityManager = entityManagerFactory.createEntityManager(jpaPropertyMap);
        if (entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain an EntityManager");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doReadPage() {
        EntityTransaction tx = getTxOrNull();

        JPQLQuery<T> query = createQuery()
                .offset(getPage() * getPageSize())
                .limit(getPageSize());

        initResults();

        fetchQuery(query, tx);
    }

    protected EntityTransaction getTxOrNull() {
        if (transacted) {
            EntityTransaction tx = entityManager.getTransaction();
            tx.begin();

            entityManager.flush();
            entityManager.clear();
            return tx;
        }

        return null;
    }

    protected JPAQuery<T> createQuery() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFunction.apply(queryFactory);
    }

    protected void initResults() {
        if (CollectionUtils.isEmpty(results)) {
            results = new CopyOnWriteArrayList<>();
        } else {
            results.clear();
        }
    }

    /**
     * where 의 조건은 id max/min 을 이용한 제한된 범위를 가지게 한다
     *
     * @param query
     * @param tx
     */
    protected void fetchQuery(JPQLQuery<T> query, EntityTransaction tx) {
        if (transacted) {
            results.addAll(query.fetch());
            if (tx != null) {
                tx.commit();
            }
        } else {
            List<T> queryResult = query.fetch();
            for (T entity : queryResult) {
                entityManager.detach(entity);
                results.add(entity);
            }
        }
    }

    @Override
    protected void doClose() throws Exception {
        entityManager.close();
        super.doClose();
    }
}