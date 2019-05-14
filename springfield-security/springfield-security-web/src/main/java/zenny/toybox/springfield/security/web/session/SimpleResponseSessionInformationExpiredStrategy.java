package zenny.toybox.springfield.security.web.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.session.SimpleRedirectSessionInformationExpiredStrategy;
import org.springframework.security.web.util.UrlUtils;

import zenny.toybox.springfield.security.web.ResponseStrategy;
import zenny.toybox.springfield.security.web.ResponseStrategy.ResponseContent;
import zenny.toybox.springfield.security.web.support.DefaultResponseStrategy;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.StringUtils;

public class SimpleResponseSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final ResponseStrategy responseStrategy;

  private final SessionInformationExpiredStrategy fullback;

  private final Function<SessionInformationExpiredEvent, ResponseContent> contentSupplier;

  public SimpleResponseSessionInformationExpiredStrategy(String expiredUrl) {
    this(expiredUrl, new DefaultResponseStrategy(HttpStatus.UNAUTHORIZED), (event) -> {
      Map<String, String> payload = new HashMap<>();
      payload.put("optionalUrl", expiredUrl);

      return ResponseContent.forInstance(payload);
    });
  }

  public SimpleResponseSessionInformationExpiredStrategy(String expiredUrl, ResponseStrategy responseStrategy,
      Function<SessionInformationExpiredEvent, ResponseContent> contentSupplier) {
    Assert.isTrue(StringUtils.hasText(expiredUrl) && UrlUtils.isValidRedirectUrl(expiredUrl),
        "ExpiredUrl must be specified and must be a valid URL");
    Assert.notNull(responseStrategy, "ResponseStrategy must not be null");
    Assert.notNull(contentSupplier, "ContentSupplier must not be null");

    this.responseStrategy = responseStrategy;
    this.fullback = new SimpleRedirectSessionInformationExpiredStrategy(expiredUrl);
    this.contentSupplier = contentSupplier;
  }

  @Override
  public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
    if (event.getResponse().isCommitted()) {
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Response has already been committed, unable to respond again");
      }

      return;
    }

    this.responseStrategy.response(event.getRequest(), event.getResponse(), this.contentSupplier.apply(event), () -> {
      this.fullback.onExpiredSessionDetected(event);
    });
  }
}
