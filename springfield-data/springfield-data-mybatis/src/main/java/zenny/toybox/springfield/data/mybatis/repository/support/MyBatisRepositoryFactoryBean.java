package zenny.toybox.springfield.data.mybatis.repository.support;

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;

public class MyBatisRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

  public MyBatisRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  @Override
  protected RepositoryFactorySupport doCreateRepositoryFactory() {
    // TODO Auto-generated method stub
    return null;
  }

}
