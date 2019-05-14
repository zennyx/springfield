package zenny.toybox.springfield.security.web.access;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.UrlUtils;

import zenny.toybox.springfield.security.web.ResponseStrategy;
import zenny.toybox.springfield.security.web.ResponseStrategy.ResponseContent;
import zenny.toybox.springfield.security.web.support.DefaultResponseStrategy;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.StringUtils;

public class SimpleResponseAccessDeniedHandler implements AccessDeniedHandler {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final ResponseStrategy responseStrategy;

  private final AccessDeniedHandler fullback;

  private final BiFunction<HttpServletRequest, HttpServletResponse, Function<AccessDeniedException, ResponseContent>> contentSupplier;

  public SimpleResponseAccessDeniedHandler() {
    this(null);
  }

  public SimpleResponseAccessDeniedHandler(String errorPage) {
    this(errorPage, new DefaultResponseStrategy(HttpStatus.FORBIDDEN), (request, response) -> {
      return (error) -> {
        Map<String, String> payload = new HashMap<>();
        payload.put("optionalUrl", errorPage);

        return ResponseContent.forInstance(payload);
      };
    });
  }

  public SimpleResponseAccessDeniedHandler(String errorPage, ResponseStrategy responseStrategy,
      BiFunction<HttpServletRequest, HttpServletResponse, Function<AccessDeniedException, ResponseContent>> contentSupplier) {
    Assert.isTrue(StringUtils.hasText(errorPage) && UrlUtils.isValidRedirectUrl(errorPage),
        "ErrorPage must be a valid URL");
    Assert.notNull(responseStrategy, "ResponseStrategy must not be null");
    Assert.notNull(contentSupplier, "ContentSupplier must not be null");

    this.responseStrategy = responseStrategy;
    this.fullback = new SimpleUrlAccessDeniedHandler(errorPage);
    this.contentSupplier = contentSupplier;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    if (response.isCommitted()) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Response has already been committed, unable to respond again");
      }

      return;
    }

    this.responseStrategy.response(request, response,
        this.contentSupplier.apply(request, response).apply(accessDeniedException), () -> {
          this.fullback.handle(request, response, accessDeniedException);
        });
  }
}
