package zenny.toybox.springfield.data.mybatis.repository.bridge.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ResolvableType;
import org.springframework.data.repository.core.RepositoryInformation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ParameterDefinition.Annotatable;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ParameterDefinition.Initial;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import zenny.toybox.springfield.data.mybatis.repository.bridge.BridgeMapperFactory;
import zenny.toybox.springfield.data.mybatis.repository.bridge.ClassAlreadyExistsException;
import zenny.toybox.springfield.data.mybatis.repository.bridge.IllegalMethodArgumentException;
import zenny.toybox.springfield.data.mybatis.repository.bridge.NoBridgeMapperFoundException;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.ClassUtils;
import zenny.toybox.springfield.util.StringUtils;

public abstract class BridgeMapperFactorySupport implements BridgeMapperFactory {

  public static final String DEFAULT_BRIDGE_MAPPER_POSTFIX = "Bridge";

  private static final Map<Class<?>, Class<?>> BRIDGE_MAPPER_CACHE = new ConcurrentHashMap<>();

  /*
   * (non-Javadoc)
   * @see
   * zenny.toybox.springfield.data.mybatis.repository.bridge.BridgeMapperFactory#
   * getBridgeMapper(org.springframework.data.repository.core.
   * RepositoryInformation)
   */
  @Override
  public final Class<?> getBridgeMapper(RepositoryInformation information) {
    this.validate(information);

    Class<?> repositoryInterface = information.getRepositoryInterface();
    Class<?> bridgeMapper = BRIDGE_MAPPER_CACHE.get(repositoryInterface);

    if (bridgeMapper == null) {
      bridgeMapper = this.doGetBridgeMapper(information);

      if (bridgeMapper == null) {
        throw new NoBridgeMapperFoundException(
            "No suitable bridge mapper found for repository interface [" + repositoryInterface + "]");
      }

      BRIDGE_MAPPER_CACHE.put(repositoryInterface, bridgeMapper);
    }

    return bridgeMapper;
  }

  protected Class<?> doGetBridgeMapper(RepositoryInformation information) {
    final String mapperName = this.getMapperName(information);

    DynamicType.Builder<?> builder = new ByteBuddy().with(new NamingStrategy.AbstractBase() {
      @Override
      protected String name(net.bytebuddy.description.type.TypeDescription superClass) {
        return mapperName;
      }
    }).makeInterface().modifiers(information.getRepositoryInterface().getModifiers())
        .annotateType(this.getMapperAnnotations(information));

    Iterable<Method> methods = information.getQueryMethods();
    if (methods != null) {
      for (Method method : methods) {
        // Also see:
        // https://stackoverflow.com/questions/16635398/java-8-iterable-foreach-vs-foreach-loop/20177092#20177092
        if (method == null || !this.isCandidateMethod(method, information)) {
          continue;
        }

        Initial<?> initial = builder.defineMethod(method.getName(), this.getMethodReturnType(method, information),
            method.getModifiers());
        int parameterCount = method.getParameterCount();
        ReceiverTypeDefinition<?> receiverTypeDefinition = null;

        if (parameterCount > 0) {
          Annotatable<?> annotatable = null;
          Parameter[] parameters = method.getParameters();

          for (int index = 0; index < parameterCount; index++) {

            Parameter parameter = parameters[index];
            Type parameterType = this.getMethodParameterType(method, index, information);
            String parameterName = parameter.getName();
            int parameterModifiers = parameter.getModifiers();
            Annotation[] parameterAnnotations = parameter.getAnnotations(); // TODO

            annotatable = annotatable == null
                ? initial.withParameter(parameterType, parameterName, parameterModifiers)
                    .annotateParameter(parameterAnnotations)
                : annotatable.withParameter(parameterType, parameterName, parameterModifiers)
                    .annotateParameter(parameterAnnotations);
          }

          receiverTypeDefinition = annotatable.withoutCode();

        } else {
          receiverTypeDefinition = initial.withoutCode();
        }

        builder = receiverTypeDefinition.annotateMethod(this.getMethodAnnotations(method, information));
      }
    }

    return builder.make()
        .load(information.getRepositoryInterface().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
        .getLoaded();
  }

  private void validate(RepositoryInformation information) {
    Assert.notNull(information, "RepositoryInformation must not be null");

    Class<?> repositoryInterface = information.getRepositoryInterface();
    Assert.notNull(repositoryInterface, "RepositoryInterface must not be null");

    List<Method> methods = Arrays.asList(repositoryInterface.getMethods());
    for (Method queryMethod : information.getQueryMethods()) {
      Assert.isTrue(methods.contains(queryMethod), "QueryMethods must belong to the given repository interface");
    }

    Assert.isTrue(this.isValid(information), "The given repository information is invalid");
  }

  protected abstract boolean isValid(RepositoryInformation information);

  private String getMapperName(RepositoryInformation information) {
    String mapperPostfix = this.getMapperPostfix();
    mapperPostfix = StringUtils.hasLength(mapperPostfix) ? mapperPostfix : DEFAULT_BRIDGE_MAPPER_POSTFIX;

    String mapperName = String.format("%s%s", information.getRepositoryInterface().getCanonicalName(), "Bridge");
    if (ClassUtils.isPresent(mapperName, information.getRepositoryInterface().getClassLoader())) {
      throw new ClassAlreadyExistsException(
          "The class with name [" + mapperName + "] already exists. Try another mapper postfix?");
    }

    return mapperName;
  }

  protected abstract String getMapperPostfix();

  private Annotation[] getMapperAnnotations(RepositoryInformation information) {
    return information.getRepositoryInterface().getAnnotations(); // TODO
  }

  protected abstract boolean isCandidateMethod(Method method, RepositoryInformation information);

  private Type getMethodReturnType(Method method, RepositoryInformation information) {
    Type returnType = this.resolveMethodReturnType(method, information.getRepositoryInterface());
    boolean returnValueRequired = returnType.getClass().isPrimitive() && !Void.TYPE.equals(returnType);

    returnType = this.processType(returnType, method, information);

    if (returnType == null) {
      throw new IllegalMethodArgumentException("ReturnType must not be null");
    }
    if (returnValueRequired && Void.TYPE.equals(returnType)) {
      throw new IllegalMethodArgumentException("A null value cannot be assigned to a primitive type");
    }

    return returnType;
  }

  private Type resolveMethodReturnType(Method method, Class<?> implementationClass) {
    ResolvableType returnType = ResolvableType.forMethodReturnType(method, implementationClass);

    return this.resolveType(returnType);
  }

  private Type getMethodParameterType(Method method, int parameterIndex, RepositoryInformation information) {
    Type parameterType = this.resolveMethodParameterType(method, parameterIndex, information.getRepositoryInterface());
    parameterType = this.processType(parameterType, method, information);

    if (parameterType == null) {
      throw new IllegalMethodArgumentException("ParameterType must not be null");
    }
    if (Void.TYPE.equals(parameterType)) {
      throw new IllegalMethodArgumentException("ParameterType cannot be void");
    }

    return parameterType;
  }

  private Type resolveMethodParameterType(Method method, int parameterIndex, Class<?> implementationClass) {
    ResolvableType parameter = ResolvableType.forMethodParameter(method, parameterIndex, implementationClass);

    return this.resolveType(parameter);
  }

  private Type resolveType(ResolvableType typeToResolve) {
    if (!typeToResolve.hasGenerics()) {
      return typeToResolve.resolve();
    }

    // No need to check if it had unresolvable generics because those can be
    // resolved through the codes shown below:
    // if (typeToResolve.hasUnresolvableGenerics()) {
    // throw new MethodArgumentTypeMismatchException("Unable to resolve the given
    // type [" + typeToResolve + "].");
    // }
    Class<?> rawClass = typeToResolve.resolve();
    Class<?>[] generics = typeToResolve.resolveGenerics();

    return ResolvableType.forClassWithGenerics(rawClass, generics).getType();
  }

  protected abstract Type processType(Type type, Method method, RepositoryInformation information);

  private Annotation[] getMethodAnnotations(Method method, RepositoryInformation information) {
    Annotation[] annotations = method.getAnnotations();
    annotations = this.processMethodAnnotations(annotations, information);

    if (annotations == null) {
      throw new IllegalMethodArgumentException("Annotations must not be null");
    }
    if (annotations.length > 0) {
      for (Annotation annotation : annotations) {
        if (annotation == null) {
          throw new IllegalMethodArgumentException("Annotation must not be null");
        }
      }
    }

    return annotations;
  }

  protected abstract Annotation[] processMethodAnnotations(Annotation[] annotations, RepositoryInformation information);
}
