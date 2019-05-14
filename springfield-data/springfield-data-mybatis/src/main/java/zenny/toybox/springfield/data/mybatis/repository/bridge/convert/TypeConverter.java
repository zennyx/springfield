package zenny.toybox.springfield.data.mybatis.repository.bridge.convert;

public interface TypeConverter<S, T> {

  ResolvedType<T> convert(ResolvedType<S> source);
}