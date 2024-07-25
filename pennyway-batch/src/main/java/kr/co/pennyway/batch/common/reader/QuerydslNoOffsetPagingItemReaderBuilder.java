package kr.co.pennyway.batch.common.reader;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManagerFactory;
import kr.co.pennyway.batch.common.reader.options.QuerydslNoOffsetOptions;

import java.util.function.Function;

/**
 * {@link QuerydslNoOffsetPagingItemReader}를 생성하기 위한 빌더 클래스
 * <p>
 * Step Builder 패턴을 사용하였으며, 각 메소드는 해당하는 설정값을 설정하고 다음 단계의 빌더를 반환한다.
 *
 * @author YANG JAESEO
 */
public class QuerydslNoOffsetPagingItemReaderBuilder<T> {
    private QuerydslNoOffsetPagingItemReaderBuilder() {
    }

    public static <T> EntityManagerFactoryStep<T> builder() {
        return new Steps<>();
    }

    public interface EntityManagerFactoryStep<T> {
        /**
         * The {@link EntityManagerFactory} to be used for executing the configured queryFunction.
         *
         * @param emf {@link EntityManagerFactory} used to create
         *            {@link jakarta.persistence.EntityManager}
         * @return this instance for method chaining
         */
        PageSizeStep<T> entityManagerFactory(EntityManagerFactory emf);
    }

    public interface PageSizeStep<T> {
        /**
         * The number of items to be read with each page.
         *
         * @param pageSize number of items
         * @return this instance for method chaining
         */
        OptionsStep<T> pageSize(int pageSize);
    }

    public interface OptionsStep<T> {
        /**
         * The {@link QuerydslNoOffsetOptions} to be used for configuring the reader.
         *
         * @param options {@link QuerydslNoOffsetOptions} to be used
         * @return this instance for method chaining
         */
        QueryFunctionStep<T> options(QuerydslNoOffsetOptions<T> options);
    }

    public interface QueryFunctionStep<T> {
        /**
         * The function that creates the query to be executed.
         *
         * @param queryFunction function that creates the query
         * @return this instance for method chaining
         */
        BuildStep<T> queryFunction(Function<JPAQueryFactory, JPAQuery<T>> queryFunction);
    }

    public interface BuildStep<T> {
        /**
         * The function that creates the query to be executed to select the currentId and lastId.
         * This is used to determine the currentId when the reader is not on the last page.
         * If this is not provided, the reader will use the queryFunction to determine the currentId and lastId.
         *
         * @param idSelectQuery function that creates the query to select the currentId and lastId
         * @return this instance for method chaining
         */
        BuildStep<T> idSelectQuery(Function<JPAQueryFactory, JPAQuery<T>> idSelectQuery);

        QuerydslNoOffsetPagingItemReader<T> build();
    }

    private static class Steps<T> implements
            EntityManagerFactoryStep<T>, PageSizeStep<T>, OptionsStep<T>, QueryFunctionStep<T>, BuildStep<T> {
        private EntityManagerFactory entityManagerFactory;
        private int pageSize;
        private QuerydslNoOffsetOptions<T> options;
        private Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
        private Function<JPAQueryFactory, JPAQuery<T>> idSelectQuery;

        @Override
        public PageSizeStep<T> entityManagerFactory(EntityManagerFactory emf) {
            this.entityManagerFactory = emf;
            return this;
        }

        @Override
        public OptionsStep<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        @Override
        public QueryFunctionStep<T> options(QuerydslNoOffsetOptions<T> options) {
            this.options = options;
            return this;
        }

        @Override
        public BuildStep<T> queryFunction(Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
            this.queryFunction = queryFunction;
            return this;
        }

        @Override
        public BuildStep<T> idSelectQuery(Function<JPAQueryFactory, JPAQuery<T>> idSelectQuery) {
            this.idSelectQuery = idSelectQuery;
            return this;
        }

        @Override
        public QuerydslNoOffsetPagingItemReader<T> build() {
            return new QuerydslNoOffsetPagingItemReader<>(
                    entityManagerFactory,
                    pageSize,
                    options,
                    queryFunction,
                    idSelectQuery
            );
        }
    }
}