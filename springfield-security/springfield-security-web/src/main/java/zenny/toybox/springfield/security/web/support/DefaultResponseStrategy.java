package zenny.toybox.springfield.security.web.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;
import zenny.toybox.springfield.util.StringUtils;

public class DefaultResponseStrategy extends ResponseStrategySupport {

  private final HttpStatus status;

  @Nullable
  private final HttpHeaders headers;

  public DefaultResponseStrategy(Collection<HttpMessageConverter<?>> messageConverters) {
    this(messageConverters, HttpStatus.OK, null);
  }

  public DefaultResponseStrategy(HttpStatus status) {
    this(status, null);
  }

  public DefaultResponseStrategy(HttpStatus status, @Nullable HttpHeaders headers) {
    this(Collections.singleton(new MappingJackson2HttpMessageConverter()), status, headers);
  }

  public DefaultResponseStrategy(Collection<HttpMessageConverter<?>> messageConverters, HttpStatus status,
      @Nullable HttpHeaders headers) {
    super(messageConverters);

    Assert.notNull(status, "Status must not be null");

    this.status = status;
    this.headers = headers;
    this.setPositiveResponsing(false);
  }

  @Override
  protected void doResponse(HttpServletRequest request, HttpServletResponse response, @Nullable ResponseContent content)
      throws IOException, ServletException {
    if (content == null) {
      return;
    }

    ServletServerHttpRequest inputMessage = this.createInputMessage(request);
    ServletServerHttpResponse outputMessage = this.createOutputMessage(response);

    if (response.containsHeader(HttpHeaders.VARY) && this.headers != null
        && this.headers.containsKey(HttpHeaders.VARY)) {
      Collection<String> varyHeadsToAdd = this.getVaryResponseHeadersToAdd(response);
      if (!varyHeadsToAdd.isEmpty()) {
        response.addHeader(HttpHeaders.VARY, String.join(", ", varyHeadsToAdd));
      }
    }

    if (!CollectionUtils.isEmpty(this.headers)) {
      this.headers.entrySet().forEach(entry -> {
        if (!response.containsHeader(entry.getKey())) {
          response.setHeader(entry.getKey(), String.join(", ", entry.getValue()));
        }
      });
    }

    response.setStatus(this.status.value());
    this.writeWithMessageConverters(inputMessage, outputMessage, content);

    outputMessage.flush();
  }

  private Collection<String> getVaryResponseHeadersToAdd(HttpServletResponse response) {
    List<String> entityHeadersVary = this.headers.getVary();
    List<String> result = new ArrayList<>(entityHeadersVary);
    for (String header : response.getHeaders(HttpHeaders.VARY)) {
      for (String existing : StringUtils.tokenizeToStringArray(header, ",")) {
        if ("*".equals(existing)) {
          return Collections.emptyList();
        }

        for (String value : entityHeadersVary) {
          if (value.equalsIgnoreCase(existing)) {
            result.remove(value);
          }
        }
      }
    }

    return result;
  }

  public HttpStatus getStatus() {
    return this.status;
  }

  public HttpHeaders getHeaders() {
    return this.headers;
  }
}
