package kr.co.pennyway.domain.common.repository;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.lang.NonNull;

public class QueryDslSearchRepositoryFactory<T extends Repository<E, ID>, E, ID> extends JpaRepositoryFactoryBean<T, E, ID> {
    /**
     * Creates a new {@link JpaRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public QueryDslSearchRepositoryFactory(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    @NonNull
    protected RepositoryFactorySupport createRepositoryFactory(@NonNull EntityManager em) {
        return new InnerRepositoryFactory(em);
    }

    private static class InnerRepositoryFactory extends JpaRepositoryFactory {
        private final EntityManager em;

        public InnerRepositoryFactory(EntityManager em) {
            super(em);
            this.em = em;
        }

        @Override
        @NonNull
        protected RepositoryComposition.RepositoryFragments getRepositoryFragments(@NonNull RepositoryMetadata metadata) {
            RepositoryComposition.RepositoryFragments fragments = super.getRepositoryFragments(metadata);

            if (QueryDslSearchRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
                var implExtendedJpa = super.instantiateClass(
                        QueryDslSearchRepositoryImpl.class,
                        this.getEntityInformation(metadata.getDomainType()),
                        this.em
                );
                fragments = fragments.append(RepositoryComposition.RepositoryFragments.just(implExtendedJpa));
            }

            return fragments;
        }
    }
}
