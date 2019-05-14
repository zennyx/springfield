package zenny.toybox.springfield.security.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;

@FunctionalInterface
public interface ResponseStrategy {

  void response(HttpServletRequest request, HttpServletResponse response, @Nullable ResponseContent content,
      @Nullable Callback fullback) throws IOException, ServletException;

  public static final class ResponseContent {

    @Nullable
    private final Object payload;

    private final ResolvableType payloadType;

    private ResponseContent(@Nullable Object payload, ResolvableType payloadType) {
      Assert.notNull(payloadType, "PayloadType must not be null");

      this.payload = payload;
      this.payloadType = payloadType;
    }

    public static ResponseContent forInstance(Object payload) {
      return new ResponseContent(payload, ResolvableType.forInstance(payload));
    }

    public static ResponseContent forClass(Class<?> payloadClass) {
      Assert.notNull(payloadClass, "PayloadClass must not be null");

      return new ResponseContent(null, ResolvableType.forClass(payloadClass));
    }

    public static ResponseContent forType(ResolvableType payloadType) {
      Assert.notNull(payloadType, "PayloadType must not be null");

      return new ResponseContent(null, payloadType);
    }

    public Object getPayload() {
      return this.payload;
    }

    public ResolvableType getPayloadType() {
      return this.payloadType;
    }

    @Override
    public String toString() {
      return "ResponseContent [payload=" + payload + ", payloadType=" + payloadType + "]";
    }
  }

  @FunctionalInterface
  public interface Callback {

    void call() throws IOException, ServletException;
  }
}
