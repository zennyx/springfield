package zenny.toybox.springfield.data.mybatis.generator;

import java.util.Set;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.api.dom.kotlin.KotlinProperty;
import org.mybatis.generator.api.dom.kotlin.KotlinType;

public interface KotlinCommentGenerator {

  void addFileComment(KotlinFile kotlinFile);

  void addModelClassComment(KotlinType modelClass, IntrospectedTable introspectedTable);

  void addGeneralFunctionComment(KotlinFunction kf, IntrospectedTable introspectedTable, Set<String> imports);

  void addGeneralPropertyComment(KotlinProperty property, IntrospectedTable introspectedTable, Set<String> imports);
}
