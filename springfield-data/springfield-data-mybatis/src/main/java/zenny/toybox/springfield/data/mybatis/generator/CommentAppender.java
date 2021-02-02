package zenny.toybox.springfield.data.mybatis.generator;

public interface CommentAppender {

  void done();

  CommentAppender append(String comment);

  interface CommentFilter {
    // TODO
  }
}
