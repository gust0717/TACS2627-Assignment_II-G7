package pt.feup.tacs.tunnel.application.actor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An actor's named timers, kept as deadlines that its loop waits for (SPEC.md 3.1.1). Used by the
 * actor's own thread only.
 */
// Implements: D11, D12
final class Timers<T> {

  private record Timer<T>(long deadlineNanos, T message) {}

  private final Map<String, Timer<T>> byKey = new ConcurrentHashMap<>();

  /** Starts the timer {@code key}, replacing any running timer with that key. */
  void start(String key, long deadlineNanos, T message) {
    byKey.put(key, new Timer<>(deadlineNanos, message));
  }

  /** Cancels the timer {@code key}, if it is running. */
  void stop(String key) {
    byKey.remove(key);
  }

  /** How long to wait at {@code nowNanos}: until the first deadline, at most {@code pollNanos}. */
  long waitNanos(long nowNanos, long pollNanos) {
    Map.Entry<String, Timer<T>> first = first();
    return first == null
        ? pollNanos
        : Math.max(0, Math.min(pollNanos, first.getValue().deadlineNanos() - nowNanos));
  }

  /**
   * Removes and returns the message of the first timer that expired before {@code nanos}; null when
   * none did.
   */
  T takeExpiredBefore(long nanos) {
    Map.Entry<String, Timer<T>> first = first();
    if (first == null || first.getValue().deadlineNanos() - nanos >= 0) {
      return null;
    }
    byKey.remove(first.getKey());
    return first.getValue().message();
  }

  private Map.Entry<String, Timer<T>> first() {
    Map.Entry<String, Timer<T>> first = null;
    for (Map.Entry<String, Timer<T>> entry : byKey.entrySet()) {
      if (first == null
          || entry.getValue().deadlineNanos() - first.getValue().deadlineNanos() < 0) {
        first = entry;
      }
    }
    return first;
  }
}
