package zenny.toybox.springfield.security.web.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;
import org.springframework.security.web.util.UrlUtils;

import zenny.toybox.springfield.security.web.ResponseStrategy;
import zenny.toybox.springfield.security.web.ResponseStrategy.ResponseContent;
import zenny.toybox.springfield.security.web.support.DefaultResponseStrategy;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.StringUtils;

public class SimpleResponseInvalidSessionStrategy implements InvalidSessionStrategy {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final boolean createNewSession;

  private final ResponseStrategy responseStrategy;

  private final InvalidSessionStrategy fullback;

  private final BiFunction<HttpServletRequest, HttpServletResponse, ResponseContent> contentSupplier;

  public SimpleResponseInvalidSessionStrategy(String invalidUrl) {
    this(invalidUrl, true);
  }

  public SimpleResponseInvalidSessionStrategy(String invalidUrl, boolean createNewSession) {
    this(invalidUrl, createNewSession, new DefaultResponseStrategy(HttpStatus.UNAUTHORIZED), (request, response) -> {
      Map<String, String> payload = new HashMap<>();
      payload.put("optionalUrl", invalidUrl);

      return ResponseContent.forInstance(payload);
    });
  }

  public SimpleResponseInvalidSessionStrategy(String invalidUrl, boolean createNewSession,
      ResponseStrategy responseStrategy,
      BiFunction<HttpServletRequest, HttpServletResponse, ResponseContent> contentSupplier) {
    Assert.isTrue(StringUtils.hasText(invalidUrl) && UrlUtils.isValidRedirectUrl(invalidUrl),
        "InvalidUrl must be specified and must be a valid URL");
    Assert.notNull(responseStrategy, "ResponseStrategy must not be null");
    Assert.notNull(contentSupplier, "ContentSupplier must not be null");

    this.createNewSession = createNewSession;
    this.responseStrategy = responseStrategy;
    this.fullback = new SimpleRedirectInvalidSessionStrategy(invalidUrl);
    this.contentSupplier = contentSupplier;
  }

  @Override
  public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (this.createNewSession) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Creating a new session for the request");
      }

      request.getSession();
    }

    if (response.isCommitted()) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Response has already been committed, unable to respond again");
      }

      return;
    }

    this.responseStrategy.response(request, response, this.contentSupplier.apply(request, response), () -> {
      this.fullback.onInvalidSessionDetected(request, response);
    });
  }

  public boolean shouldCreateNewSession() {
    return this.createNewSession;
  }
}
