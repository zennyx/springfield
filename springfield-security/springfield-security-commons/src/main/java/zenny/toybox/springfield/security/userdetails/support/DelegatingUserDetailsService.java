package zenny.toybox.springfield.security.userdetails.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import zenny.toybox.springfield.util.Assert;

public class DelegatingUserDetailsService implements UserDetailsService, MessageSourceAware {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private final Set<UserDetailsService> userDetailsServices = new LinkedHashSet<>();

  private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

  public DelegatingUserDetailsService(UserDetailsService... userDetailsServices) {
    this(Arrays.asList(userDetailsServices));
  }

  public DelegatingUserDetailsService(Collection<UserDetailsService> userDetailsServices) {
    Assert.notEmpty(userDetailsServices, "UserDetailsServices must not be empty");

    for (UserDetailsService userDetailsService : userDetailsServices) {
      if (userDetailsService == null) {
        continue;
      }

      this.userDetailsServices.add(userDetailsService);
    }
  }

  @Override
  public UserDetails loadUserByUsername(@Nullable String username) throws UsernameNotFoundException {
    for (UserDetailsService userDetailsService : this.userDetailsServices) {
      try {
        return userDetailsService.loadUserByUsername(username);
      } catch (UsernameNotFoundException notFoundEx) {
        if (this.logger.isDebugEnabled()) {
          this.logger.debug(notFoundEx.getMessage());
        }

        continue;
      }
    }

    throw new UsernameNotFoundException(this.messages.getMessage("CompositeUserDetailsService.notFound",
        new Object[] { username }, "Username {0} not found in all detailsServices"));
  }

  protected MessageSourceAccessor getMessages() {
    return this.messages;
  }

  @Override
  public void setMessageSource(MessageSource messageSource) {
    Assert.notNull(messageSource, "MessageSource must not be null");

    this.messages = new MessageSourceAccessor(messageSource);
  }
}
