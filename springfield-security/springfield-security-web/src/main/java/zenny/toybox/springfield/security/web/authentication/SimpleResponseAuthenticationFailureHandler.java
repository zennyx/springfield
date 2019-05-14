package zenny.toybox.springfield.security.web.authentication;

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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;

import zenny.toybox.springfield.security.web.ResponseStrategy;
import zenny.toybox.springfield.security.web.ResponseStrategy.ResponseContent;
import zenny.toybox.springfield.security.web.support.DefaultResponseStrategy;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.StringUtils;

public class SimpleResponseAuthenticationFailureHandler implements AuthenticationFailureHandler {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final ResponseStrategy responseStrategy;

  private final AuthenticationFailureHandler fullback;

  private final BiFunction<HttpServletRequest, HttpServletResponse, Function<AuthenticationException, ResponseContent>> contentSupplier;

  public SimpleResponseAuthenticationFailureHandler(String failureUrl) {
    this(failureUrl, new DefaultResponseStrategy(HttpStatus.UNAUTHORIZED), (request, response) -> {
      return (error) -> {
        Map<String, String> payload = new HashMap<>();
        payload.put("optionalUrl", failureUrl);

        return ResponseContent.forInstance(payload);
      };
    });
  }

  public SimpleResponseAuthenticationFailureHandler(String failureUrl, ResponseStrategy responseStrategy,
      BiFunction<HttpServletRequest, HttpServletResponse, Function<AuthenticationException, ResponseContent>> contentSupplier) {
    Assert.isTrue(StringUtils.hasText(failureUrl) && UrlUtils.isValidRedirectUrl(failureUrl),
        "FailureUrl must be specified and must be a valid URL");
    Assert.notNull(responseStrategy, "ResponseStrategy must not be null");
    Assert.notNull(contentSupplier, "ContentSupplier must not be null");

    this.responseStrategy = responseStrategy;
    this.fullback = new SimpleUrlAuthenticationFailureHandler(failureUrl);
    this.contentSupplier = contentSupplier;
  }

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    if (response.isCommitted()) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Response has already been committed, unable to respond again");
      }

      return;
    }

    this.responseStrategy.response(request, response,
        this.contentSupplier.apply(request, response).apply(authException), () -> {
          this.fullback.onAuthenticationFailure(request, response, authException);
        });
  }
}
