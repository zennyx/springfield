package zenny.toybox.springfield.security.web.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

import zenny.toybox.springfield.security.web.ResponseStrategy;
import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;

public abstract class ResponseStrategySupport implements ResponseStrategy {

  private static final MediaType MEDIA_TYPE_APPLICATION = new MediaType("application");

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final Collection<HttpMessageConverter<?>> messageConverters;

  private final Collection<MediaType> allSupportedMediaTypes;

  private final Collection<MediaType> mediaTypeBlackList;

  private boolean positiveResponsing = true;

  public ResponseStrategySupport(Collection<HttpMessageConverter<?>> messageConverters) {
    Assert.notEmpty(messageConverters, "MessageConverters must not be empty");

    this.messageConverters = new LinkedHashSet<>(messageConverters);
    this.allSupportedMediaTypes = this.getAllSupportedMediaTypes();
    this.mediaTypeBlackList = this.getAllMediaTypeBlackList();
  }

  @Override
  public final void response(HttpServletRequest request, HttpServletResponse response,
      @Nullable ResponseContent content, @Nullable Callback fullback) throws IOException, ServletException {
    try {
      this.doResponse(request, response, content);
    } catch (Exception ex) {
      if (fullback == null) {
        throw ex;
      }

      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Unable to send a response to the client cause " + ex + ", try the fullback operation");
      }

      fullback.call();
    }
  }

  protected Collection<MediaType> getAllSupportedMediaTypes() {
    Set<MediaType> allSupportedMediaTypes = new HashSet<>();
    this.messageConverters.forEach(converter -> {
      allSupportedMediaTypes.addAll(converter.getSupportedMediaTypes());
    });

    List<MediaType> result = new ArrayList<>(allSupportedMediaTypes);
    MediaType.sortBySpecificity(result);

    return result;
  }

  protected Collection<MediaType> getAllMediaTypeBlackList() {
    Set<MediaType> blackList = new HashSet<>();
    blackList.add(MediaType.parseMediaType("image/vnd.wap.wbmp"));

    return blackList;
  }

  protected abstract void doResponse(HttpServletRequest request, HttpServletResponse response,
      @Nullable ResponseContent content) throws IOException, ServletException;

  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void writeWithMessageConverters(ServletServerHttpRequest inputMessage,
      ServletServerHttpResponse outputMessage, @Nullable ResponseContent content) throws IOException, ServletException {

    if (content == null) {
      return;
    }

    Object payload;
    Class<?> payloadType;
    if (content.getPayload() instanceof CharSequence) {
      payload = content.getPayload().toString();
      payloadType = String.class;
    } else {
      payload = content.getPayload();
      payloadType = content.getPayloadType().resolve(Object.class);
    }

    Collection<MediaType> requestedMediaTypes = this.getAcceptableMediaTypes(inputMessage.getServletRequest());
    Collection<MediaType> producibleMediaTypes = this.getProducibleMediaTypes(payloadType);

    if (payload != null && producibleMediaTypes.isEmpty()) {
      throw new HttpMediaTypeNotAcceptableException(
          "Unable to send a response to the client cause no supported HttpMessageConverter found for the given content ["
              + content + "]");
    }

    Set<MediaType> compatibleMediaTypes = new LinkedHashSet<>();
    requestedMediaTypes.forEach(requestedType -> {
      producibleMediaTypes.forEach(producibleType -> {
        compatibleMediaTypes.add(this.getMostSpecificMediaType(requestedType, producibleType));
      });
    });

    if (payload != null && compatibleMediaTypes.isEmpty()) {
      throw new HttpMediaTypeNotAcceptableException(
          "Unable to send a response to the client cause no supported HttpMessageConverter found for the requested media type");
    }

    List<MediaType> mediaTypes = new ArrayList<>(compatibleMediaTypes);
    MediaType.sortBySpecificityAndQuality(mediaTypes);

    MediaType selectedMediaType = null;
    for (MediaType mediaType : mediaTypes) {
      if (mediaType.isConcrete()) {
        selectedMediaType = mediaType;
        break;
      } else if (mediaType.equals(MediaType.ALL) || mediaType.equals(MEDIA_TYPE_APPLICATION)) {
        selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
        break;
      }
    }

    if (selectedMediaType != null) {
      selectedMediaType = selectedMediaType.removeQualityValue();
      for (HttpMessageConverter<?> messageConverter : this.messageConverters) {
        if (messageConverter.canWrite(payloadType, selectedMediaType)) {
          ((HttpMessageConverter) messageConverter).write(payload, selectedMediaType, outputMessage);

          if (this.logger.isDebugEnabled()) {
            this.logger
                .debug("Written [" + payload + "] as \"" + selectedMediaType + "\" using [" + messageConverter + "]");
          }

          return;
        }
      }
    }

    throw new HttpMediaTypeNotAcceptableException("Unable to find any acceptable representation");
  }

  private Collection<MediaType> getAcceptableMediaTypes(HttpServletRequest request) throws ServletException {
    String headerValueArray = request.getHeader(HttpHeaders.ACCEPT);
    if (!StringUtils.hasText(headerValueArray)) {
      return this.filterAcceptableMediaTypes(null);
    }

    Collection<MediaType> mediaTypes;
    try {
      mediaTypes = MediaType.parseMediaTypes(headerValueArray);
    } catch (InvalidMediaTypeException ex) {
      throw new HttpMediaTypeNotAcceptableException(
          "Unable to parse 'Accept' header [" + headerValueArray + "] cause " + ex);
    }

    return this.filterAcceptableMediaTypes(mediaTypes);
  }

  protected Collection<MediaType> filterAcceptableMediaTypes(@Nullable Collection<MediaType> acceptableMediaTypes) {
    if (CollectionUtils.isEmpty(acceptableMediaTypes)) {
      return this.isPositiveResponsing() ? Collections.singleton(MediaType.ALL) : Collections.emptySet();
    }

    if (this.isPositiveResponsing()) {
      return acceptableMediaTypes;
    }

    return acceptableMediaTypes.stream().filter(mediaType -> !(MediaType.ALL.getType().equals(mediaType.getType())
        && MediaType.ALL.getSubtype().equals(mediaType.getSubtype()))).collect(Collectors.toSet());
  }

  private Collection<MediaType> getProducibleMediaTypes(Class<?> payloadType) {
    if (!this.allSupportedMediaTypes.isEmpty()) {
      Set<MediaType> result = new HashSet<>();
      this.messageConverters.forEach(converter -> {
        if (converter.canWrite(payloadType, null)) {
          result.addAll(converter.getSupportedMediaTypes());
        }
      });

      return this.filterProducibleMediaTypes(result);
    }

    return Collections.singleton(MediaType.ALL);
  }

  protected Collection<MediaType> filterProducibleMediaTypes(Collection<MediaType> producibleMediaTypes) {
    if (producibleMediaTypes.isEmpty()) {
      return Collections.singleton(MediaType.ALL);
    }

    return producibleMediaTypes.stream().filter(mediaType -> !this.mediaTypeBlackList.contains(mediaType))
        .collect(Collectors.toSet());
  }

  private MediaType getMostSpecificMediaType(MediaType acceptType, MediaType produceType) {
    MediaType produceTypeToUse = produceType.copyQualityValue(acceptType);

    return MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceTypeToUse) <= 0 ? acceptType : produceTypeToUse;
  }

  protected ServletServerHttpRequest createInputMessage(HttpServletRequest request) {
    return new ServletServerHttpRequest(request);
  }

  protected ServletServerHttpResponse createOutputMessage(HttpServletResponse response) {
    return new ServletServerHttpResponse(response);
  }

  public void setMessageConverters(Collection<HttpMessageConverter<?>> converters) {
    Assert.notEmpty(converters, "At least one HttpMessageConverter required");
    Assert.noNullElements(converters, "Converters must contain no null elements");

    if (this.messageConverters != converters) {
      this.messageConverters.clear();
      this.messageConverters.addAll(converters);

      this.allSupportedMediaTypes.clear();
      this.allSupportedMediaTypes.addAll(this.getAllSupportedMediaTypes());
    }
  }

  protected Collection<MediaType> getMediaTypeBlackList() {
    return Collections.unmodifiableCollection(this.mediaTypeBlackList);
  }

  public void setMediaTypeBlackList(Collection<MediaType> mediaTypeBlackList) {
    Assert.notEmpty(mediaTypeBlackList, "At least one MediaType required");
    Assert.noNullElements(mediaTypeBlackList, "Converters must contain no null elements");

    if (this.mediaTypeBlackList != mediaTypeBlackList) {
      this.mediaTypeBlackList.clear();
      this.mediaTypeBlackList.addAll(mediaTypeBlackList);
    }
  }

  protected boolean isPositiveResponsing() {
    return this.positiveResponsing;
  }

  public void setPositiveResponsing(boolean positiveResponsing) {
    this.positiveResponsing = positiveResponsing;
  }
}
