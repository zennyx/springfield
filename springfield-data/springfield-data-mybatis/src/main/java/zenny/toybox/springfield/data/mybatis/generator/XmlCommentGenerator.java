package zenny.toybox.springfield.data.mybatis.generator;

import org.mybatis.generator.api.dom.xml.XmlElement;

public interface XmlCommentGenerator {

  void addComment(XmlElement xmlElement);

  void addRootComment(XmlElement rootElement);
}
