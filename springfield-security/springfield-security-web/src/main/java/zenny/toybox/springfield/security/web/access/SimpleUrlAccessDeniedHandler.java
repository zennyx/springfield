package zenny.toybox.springfield.security.web.access;

import org.springframework.lang.Nullable;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

public class SimpleUrlAccessDeniedHandler extends AccessDeniedHandlerImpl {

  public SimpleUrlAccessDeniedHandler(@Nullable String errorPage) {
    super();

    this.setErrorPage(errorPage);
  }
}
