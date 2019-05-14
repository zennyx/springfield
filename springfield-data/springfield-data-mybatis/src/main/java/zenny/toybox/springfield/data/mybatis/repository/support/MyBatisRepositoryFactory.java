package zenny.toybox.springfield.data.mybatis.repository.support;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class MyBatisRepositoryFactory extends RepositoryFactorySupport {

  @Override
  public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Object getTargetRepository(RepositoryInformation metadata) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    // TODO Auto-generated method stub
    return null;
  }

}
