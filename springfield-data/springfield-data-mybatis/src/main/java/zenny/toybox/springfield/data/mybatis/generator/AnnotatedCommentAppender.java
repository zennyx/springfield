package zenny.toybox.springfield.data.mybatis.generator;

import java.lang.annotation.Annotation;

public interface AnnotatedCommentAppender extends CommentAppender {

  AnnotatedCommentAppender append(Annotation annotation);
}
