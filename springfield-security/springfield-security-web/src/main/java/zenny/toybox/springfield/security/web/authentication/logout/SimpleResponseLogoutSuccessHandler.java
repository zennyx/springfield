package zenny.toybox.springfield.security.web.authentication.logout;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.util.UrlUtils;

import zenny.toybox.springfield.security.web.ResponseStrategy;
import zenny.toybox.springfield.security.web.ResponseStrategy.ResponseContent;
import zenny.toybox.springfield.security.web.support.DefaultResponseStrategy;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.StringUtils;

public class SimpleResponseLogoutSuccessHandler implements LogoutSuccessHandler {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final ResponseStrategy responseStrategy;

  private final LogoutSuccessHandler fullback;

  private final BiFunction<HttpServletRequest, HttpServletResponse, Function<Authentication, ResponseContent>> contentSupplier;

  public SimpleResponseLogoutSuccessHandler(String logoutUrl) {
    this(logoutUrl, new DefaultResponseStrategy(HttpStatus.OK), (request, response) -> {
      return (authentication) -> {
        Map<String, String> payload = new HashMap<>();
        payload.put("optionalUrl", logoutUrl);

        return ResponseContent.forInstance(payload);
      };
    });
  }

  public SimpleResponseLogoutSuccessHandler(String logoutUrl, ResponseStrategy responseStrategy,
      BiFunction<HttpServletRequest, HttpServletResponse, Function<Authentication, ResponseContent>> contentSupplier) {
    Assert.isTrue(StringUtils.hasText(logoutUrl) && UrlUtils.isValidRedirectUrl(logoutUrl),
        "SuccessUrl must be specified and must be a valid URL");
    Assert.notNull(responseStrategy, "ResponseStrategy must not be null");
    Assert.notNull(contentSupplier, "ContentSupplier must not be null");

    this.responseStrategy = responseStrategy;
    this.fullback = new SimpleUrlLogoutSuccessHandler() {
      {
        this.setDefaultTargetUrl(logoutUrl);
      }
    };
    this.contentSupplier = contentSupplier;
  }

  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    if (response.isCommitted()) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Response has already been committed, unable to respond again");
      }

      return;
    }

    this.responseStrategy.response(request, response,
        this.contentSupplier.apply(request, response).apply(authentication), () -> {
          this.fullback.onLogoutSuccess(request, response, authentication);
        });
  }
}
