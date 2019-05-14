package zenny.toybox.springfield.security.web.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;

import zenny.toybox.springfield.security.web.ResponseStrategy;
import zenny.toybox.springfield.security.web.ResponseStrategy.ResponseContent;
import zenny.toybox.springfield.security.web.support.DefaultResponseStrategy;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.StringUtils;

public class SimpleResponseAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final ResponseStrategy responseStrategy;

  private final AuthenticationSuccessHandler fullback;

  private final BiFunction<HttpServletRequest, HttpServletResponse, Function<Authentication, ResponseContent>> contentSupplier;

  public SimpleResponseAuthenticationSuccessHandler(String successUrl) {
    this(successUrl, new DefaultResponseStrategy(HttpStatus.OK), (request, response) -> {
      return (authentication) -> {
        Map<String, String> payload = new HashMap<>();
        payload.put("optionalUrl", successUrl);

        return ResponseContent.forInstance(payload);
      };
    });
  }

  public SimpleResponseAuthenticationSuccessHandler(String successUrl, ResponseStrategy responseStrategy,
      BiFunction<HttpServletRequest, HttpServletResponse, Function<Authentication, ResponseContent>> contentSupplier) {
    Assert.isTrue(StringUtils.hasText(successUrl) && UrlUtils.isValidRedirectUrl(successUrl),
        "SuccessUrl must be specified and must be a valid URL");
    Assert.notNull(responseStrategy, "ResponseStrategy must not be null");
    Assert.notNull(contentSupplier, "ContentSupplier must not be null");

    this.responseStrategy = responseStrategy;
    this.fullback = new SimpleUrlAuthenticationSuccessHandler(successUrl);
    this.contentSupplier = contentSupplier;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    if (response.isCommitted()) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Response has already been committed, unable to respond again");
      }

      return;
    }

    this.responseStrategy.response(request, response,
        this.contentSupplier.apply(request, response).apply(authentication), () -> {
          this.fullback.onAuthenticationSuccess(request, response, authentication);
        });
    this.clearAuthenticationAttributes(request);
  }

  /**
   * Removes temporary authentication-related data which may have been stored in
   * the session during the authentication process.
   */
  protected final void clearAuthenticationAttributes(HttpServletRequest request) {
    HttpSession session = request.getSession(false);

    if (session == null) {
      return;
    }

    session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
  }
}
