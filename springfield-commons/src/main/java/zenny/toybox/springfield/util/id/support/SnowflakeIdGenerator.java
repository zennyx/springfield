package zenny.toybox.springfield.util.id.support;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

import zenny.toybox.springfield.util.Assert;
import zenny.toybox.springfield.util.id.IdGenerator;

public class SnowflakeIdGenerator implements IdGenerator<Long> {

  /**
   * Logger used by this class. Available to subclasses.
   */
  protected final Log logger = LogFactory.getLog(this.getClass());

  private static final long IDENTIFIER_BITS = 5L;
  private static final long SUB_IDENTIFIER_BITS = 5L;
  private static final long SEQUENCE_BITS = 12L;

  private static final long MAX_IDENTIFIER = -1L ^ -1L << IDENTIFIER_BITS;
  private static final long MAX_SUB_IDENTIFIER = -1L ^ -1L << SUB_IDENTIFIER_BITS;
  private static final long SEQUENCE_MASK = -1L ^ -1L << SEQUENCE_BITS;

  private static final long IDENTIFIER_SHIFT = SEQUENCE_BITS + SUB_IDENTIFIER_BITS;
  private static final long SUB_IDENTIFIER_SHIFT = SEQUENCE_BITS;
  private static final long TIMESTAMP_LEFT_SHIFT = IDENTIFIER_SHIFT + IDENTIFIER_BITS;

  private static final BiConsumer<Long, Log> NO_OP_ALERTER = (t, l) -> {};

  private final long epoch;
  private final long identifier;
  private final long subIdentifier;

  private long sequence = 0L;
  private long lastTimestamp = -1L;
  private BiConsumer<Long, Log> alerter;

  public SnowflakeIdGenerator(long epoch) {
    this(epoch, null, null);
  }

  public SnowflakeIdGenerator(long epoch, @Nullable IdentifierLookup lookuper, @Nullable IdentifierLookup subLookuper) {
    this(epoch,
        Optional.ofNullable(lookuper).orElse(() -> 0L).lookup(),
        Optional.ofNullable(subLookuper).orElse(() -> 0L).lookup());
  }

  public SnowflakeIdGenerator(long epoch, long identifier, long subIdentifier) {
    Assert.isTrue(epoch >= 0 && epoch <= this.getCurrentTimestamp(),
        "Epcho can't be less than 0 or greater than the current time");
    Assert.isTrue(identifier >= 0 && identifier <= MAX_IDENTIFIER,
        "Identifier can't be less than 0 or greater than " + MAX_IDENTIFIER);
    Assert.isTrue(subIdentifier >= 0 && subIdentifier <= MAX_SUB_IDENTIFIER,
        "Worker ID can't be less than 0 or greater than " + MAX_SUB_IDENTIFIER);

    this.epoch = epoch;
    this.identifier = identifier;
    this.subIdentifier = subIdentifier;
    this.alerter = NO_OP_ALERTER;

    if (this.logger.isInfoEnabled()) {
      this.logger.info("SnowflakeIdGenerator has been created. The epoch is ["
          + this.epoch + "], the identifier is ["
          + this.identifier + "], and the sub-identifier is ["
          + this.subIdentifier + "]");
    }
  }

  @Override
  public final synchronized Long nextId() {
    return this.getNextId();
  }

  protected long getNextId() {
    long currentTimestamp = this.getCurrentTimestamp();
    if (currentTimestamp < this.lastTimestamp) {
      throw new IllegalStateException("Clock moved backwards. Refusing to generate id for "
          + (this.lastTimestamp - currentTimestamp) + " milliseconds");
    }

    if (currentTimestamp == this.lastTimestamp) {
      this.sequence = this.sequence + 1 & SEQUENCE_MASK; // AKA sequence = (sequence + 1) mod mask

      if (this.sequence == 0L) {
        currentTimestamp = this.tilNextMillis(this.lastTimestamp);
      }
    } else {
      this.sequence = 0L;
    }

    this.lastTimestamp = currentTimestamp;

    return currentTimestamp - this.epoch << TIMESTAMP_LEFT_SHIFT
        | this.identifier << IDENTIFIER_SHIFT
        | this.subIdentifier << SUB_IDENTIFIER_SHIFT
        | this.sequence;
  }

  protected long getCurrentTimestamp() {
    return System.currentTimeMillis();
  }

  protected long tilNextMillis(long lastTimestamp) {

    boolean alerted = false;
    long mill = this.getCurrentTimestamp();

    while (mill <= lastTimestamp) {
      if (alerted == false) {
        alerted = true;
        this.alerter.accept(lastTimestamp, this.logger); // Do something else and slow down the spin.
      }
      mill = this.getCurrentTimestamp();
    }

    return mill;
  }

  public void setAlerter(@Nullable BiConsumer<Long, Log> alerter) {
    if (alerter != null) {
      this.alerter = alerter;
    }
  }

  @FunctionalInterface
  public interface IdentifierLookup {

    long lookup();
  }
}
