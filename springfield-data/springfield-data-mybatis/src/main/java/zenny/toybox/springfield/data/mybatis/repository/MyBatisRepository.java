package zenny.toybox.springfield.data.mybatis.repository;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

/**
 * MyBatis specific extension of
 * {@link org.springframework.data.repository.Repository}.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 * @author Zenny Xu
 */
@NoRepositoryBean
public interface MyBatisRepository<T, ID> extends PagingAndSortingRepository<T, ID>, QueryByExampleExecutor<T> {

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#findAll()
   */
  @Override
  List<T> findAll();

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.data.repository.PagingAndSortingRepository#findAll(org.
   * springframework.data.domain.Sort)
   */
  @Override
  List<T> findAll(Sort sort);

  /*
   * (non-Javadoc)
   * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.
   * Iterable)
   */
  @Override
  List<T> findAllById(Iterable<ID> ids);

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
   */
  @Override
  <S extends T> List<S> saveAll(Iterable<S> entities);

  /**
   * Returns a reference to the entity with the given identifier.
   *
   * @param id must not be {@literal null}.
   * @return a reference to the entity with the given identifier.
   */
  T getOne(ID id);

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.data.repository.query.QueryByExampleExecutor#findAll(org.
   * springframework.data.domain.Example)
   */
  @Override
  <S extends T> List<S> findAll(Example<S> example);

  /*
   * (non-Javadoc)
   * @see
   * org.springframework.data.repository.query.QueryByExampleExecutor#findAll(org.
   * springframework.data.domain.Example, org.springframework.data.domain.Sort)
   */
  @Override
  <S extends T> List<S> findAll(Example<S> example, Sort sort);
}
