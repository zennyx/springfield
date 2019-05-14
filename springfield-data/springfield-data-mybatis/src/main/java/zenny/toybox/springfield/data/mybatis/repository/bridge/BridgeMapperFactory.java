package zenny.toybox.springfield.data.mybatis.repository.bridge;

import org.springframework.data.repository.core.RepositoryInformation;

public interface BridgeMapperFactory {

  Class<?> getBridgeMapper(RepositoryInformation information);
}
