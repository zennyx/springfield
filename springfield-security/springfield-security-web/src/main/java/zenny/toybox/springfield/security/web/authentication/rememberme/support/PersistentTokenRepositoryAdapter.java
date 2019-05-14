package zenny.toybox.springfield.security.web.authentication.rememberme.support;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.springframework.lang.Nullable;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.function.Promise;

public class PersistentTokenRepositoryAdapter implements PersistentTokenRepository {

  private final Consumer<PersistentRememberMeToken> tokenCreator;

  private final Function<String, PersistentRememberMeToken> tokenRetriever;

  private final Consumer<PersistentRememberMeToken> tokenUpdator;

  private final Consumer<String> tokenRemover;

  @Nullable
  private final Promise repoInitializer;

  public PersistentTokenRepositoryAdapter(Consumer<PersistentRememberMeToken> tokenCreator,
      Function<String, PersistentRememberMeToken> tokenRetriever, Consumer<PersistentRememberMeToken> tokenUpdator,
      Consumer<String> tokenRemover) {
    this(tokenCreator, tokenRetriever, tokenUpdator, tokenRemover, null);
  }

  public PersistentTokenRepositoryAdapter(Consumer<PersistentRememberMeToken> tokenCreator,
      Function<String, PersistentRememberMeToken> tokenRetriever, Consumer<PersistentRememberMeToken> tokenUpdator,
      Consumer<String> tokenRemover, @Nullable Promise repoInitializer) {
    Assert.notNull(tokenCreator, "TokenCreator must not be null");
    Assert.notNull(tokenRetriever, "TokenRetriever must not be null");
    Assert.notNull(tokenUpdator, "TokenUpdator must not be null");
    Assert.notNull(tokenRemover, "TokenRemover must not be null");

    this.tokenCreator = tokenCreator;
    this.tokenRetriever = tokenRetriever;
    this.tokenUpdator = tokenUpdator;
    this.tokenRemover = tokenRemover;
    this.repoInitializer = repoInitializer;
  }

  @PostConstruct
  private void initialize() {
    if (this.repoInitializer != null) {
      this.repoInitializer.fulfill();
    }
  }

  @Override
  public void createNewToken(PersistentRememberMeToken token) {
    this.tokenCreator.accept(token);
  }

  @Override
  public void updateToken(String series, String tokenValue, Date lastUsed) {
    this.tokenUpdator.accept(new PersistentRememberMeToken(null, series, tokenValue, lastUsed) {
      @Override
      public String getTokenValue() {
        throw new IllegalStateException("Unable to get token value at this moment");
      }
    });
  }

  @Override
  public PersistentRememberMeToken getTokenForSeries(String series) {
    return this.tokenRetriever.apply(series);
  }

  @Override
  public void removeUserTokens(String username) {
    this.tokenRemover.accept(username);
  }
}
