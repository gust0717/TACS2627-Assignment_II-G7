package pt.feup.tacs.tunnel.application.actor;

import java.time.Duration;
import java.util.Objects;

/**
 * The runtime's settings, given when an {@link ActorSystem} is created (SPEC.md 3.1.1).
 *
 * @param mailboxCapacity how many messages a mailbox holds
 * @param poll the longest an idle actor or a waiting {@code tell} goes without checking the stop
 *     flag
 */
public record ActorSystemConfig(int mailboxCapacity, Duration poll) {

  /** 16 messages, 2 ms. */
  public static final ActorSystemConfig DEFAULT = new ActorSystemConfig(16, Duration.ofMillis(2));

  /** Checks that every setting is positive. */
  public ActorSystemConfig {
    Objects.requireNonNull(poll, "poll");
    if (mailboxCapacity <= 0) {
      throw new IllegalArgumentException("mailboxCapacity must be positive: " + mailboxCapacity);
    }
    if (poll.isNegative() || poll.isZero()) {
      throw new IllegalArgumentException("poll must be positive: " + poll);
    }
  }
}
