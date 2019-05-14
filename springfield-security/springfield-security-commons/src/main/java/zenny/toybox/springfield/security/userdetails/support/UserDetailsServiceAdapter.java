package zenny.toybox.springfield.security.userdetails.support;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.CollectionUtils;
import zenny.toybox.springfield.util.ObjectUtils;
import zenny.toybox.springfield.util.function.Promise;

public class UserDetailsServiceAdapter implements UserDetailsService, MessageSourceAware {

  private final Function<String, UserDetails> userDetailsRetriever;

  @Nullable
  private final Function<String, Collection<? extends GrantedAuthority>> userAuthoritiesRetriever;

  @Nullable
  private final Promise serviceInitializer;

  private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

  public UserDetailsServiceAdapter(Function<String, UserDetails> userDetailsRetriever) {
    this(userDetailsRetriever, null, null);
  }

  public UserDetailsServiceAdapter(Function<String, UserDetails> userDetailsRetriever,
      @Nullable Function<String, Collection<? extends GrantedAuthority>> userAuthoritiesRetriever) {
    this(userDetailsRetriever, userAuthoritiesRetriever, null);
  }

  public UserDetailsServiceAdapter(Function<String, UserDetails> userDetailsRetriever,
      @Nullable Function<String, Collection<? extends GrantedAuthority>> userAuthoritiesRetriever,
      @Nullable Promise serviceInitializer) {
    Assert.notNull(userDetailsRetriever, "UserDetailsRetriever must not be null");

    this.userDetailsRetriever = userDetailsRetriever;
    this.userAuthoritiesRetriever = userAuthoritiesRetriever;
    this.serviceInitializer = serviceInitializer;
  }

  @PostConstruct
  private void initialize() {
    if (this.serviceInitializer != null) {
      this.serviceInitializer.fulfill();
    }
  }

  @Override
  public UserDetails loadUserByUsername(@Nullable String username) throws UsernameNotFoundException {
    UserDetails userDetails = this.userDetailsRetriever.apply(username);
    if (userDetails == null) {
      throw new UsernameNotFoundException(this.messages.getMessage("UserDetailsServiceAdapter.notFound",
          new Object[] { username }, "User {0} not found"));
    }

    Collection<? extends GrantedAuthority> userAuthorities = userDetails.getAuthorities();
    if (this.userAuthoritiesRetriever != null) {
      userAuthorities = this.userAuthoritiesRetriever.apply(username);
    }
    if (CollectionUtils.isEmpty(userAuthorities)) {
      throw new UsernameNotFoundException(this.messages.getMessage("UserDetailsServiceAdapter.noAuthority",
          new Object[] { username }, "User {0} has no granted authority"));
    }

    // Remove duplicates.
    Set<GrantedAuthority> actuallyAuthorities = new HashSet<>();
    actuallyAuthorities.addAll(userAuthorities);

    return this.createUserDetails(username, userDetails, actuallyAuthorities);
  }

  protected UserDetails createUserDetails(@Nullable String username, UserDetails userFromUserQuery,
      Collection<GrantedAuthority> combinedAuthorities) {
    return new User(this.redefineUsername(username, userFromUserQuery), userFromUserQuery.getPassword(),
        userFromUserQuery.isEnabled(), true, true, true, combinedAuthorities);
  }

  protected String redefineUsername(@Nullable String username, UserDetails userFromUserQuery) {
    return userFromUserQuery.getUsername();
  }

  protected MessageSourceAccessor getMessages() {
    return this.messages;
  }

  @Override
  public void setMessageSource(MessageSource messageSource) {
    Assert.notNull(messageSource, "MessageSource must not be null");

    this.messages = new MessageSourceAccessor(messageSource);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.userDetailsRetriever.hashCode();
    result = prime * result + ObjectUtils.nullSafeHashCode(this.userAuthoritiesRetriever);
    result = prime * result + this.messages.hashCode();

    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (!(other instanceof UserDetailsServiceAdapter)) {
      return false;
    }

    UserDetailsServiceAdapter otherAdapter = (UserDetailsServiceAdapter) other;
    return this.userDetailsRetriever.equals(otherAdapter.userDetailsRetriever)
        && this.messages.equals(otherAdapter.messages)
        && ObjectUtils.nullSafeEquals(this.userAuthoritiesRetriever, otherAdapter.userAuthoritiesRetriever);
  }
}
