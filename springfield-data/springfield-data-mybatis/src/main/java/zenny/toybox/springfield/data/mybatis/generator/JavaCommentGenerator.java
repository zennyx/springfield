package zenny.toybox.springfield.data.mybatis.generator;

import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

public interface JavaCommentGenerator {

  void addJavaFileComment(CompilationUnit compilationUnit);

  void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable);

  void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete);

  void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable);

  void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn);

  void addFieldComment(Field field, IntrospectedTable introspectedTable);

  void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable);

  void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn);

  void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn);

  void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable);

  void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable,
      Set<FullyQualifiedJavaType> imports);

  void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports);

  void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn,
      Set<FullyQualifiedJavaType> imports);

  void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
      Set<FullyQualifiedJavaType> imports);

  void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable,
      IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports);
}
