package pt.feup.tacs.tunnel.application.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A test observer: keeps what the runtime reports, as {@code In(actor, message)} and {@code
 * Crash(actor, problem)}, and what test actors note, each with its time in ms since it was created.
 */
final class Recorder implements ActorObserver {

  /** One event and when it happened. */
  record Event(String text, long millis) {}

  private final long start = System.nanoTime();
  private final List<Event> lines = new ArrayList<>();

  @Override
  public void received(String actor, Object message) {
    note("In(" + actor + ", " + message + ")");
  }

  @Override
  public void crashed(String actor, Throwable problem) {
    note("Crash(" + actor + ", " + problem + ")");
  }

  /** Adds an event; the time is read under the lock, so events are in time order. */
  synchronized void note(String text) {
    lines.add(new Event(text, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
  }

  /** The events so far, without their times. */
  synchronized List<String> events() {
    return lines.stream().map(Event::text).toList();
  }

  /** The time, in ms, of the event's first occurrence; -1 when it did not happen. */
  synchronized long timeOf(String text) {
    return lines.stream()
        .filter(e -> e.text().equals(text))
        .mapToLong(Event::millis)
        .findFirst()
        .orElse(-1);
  }
}
