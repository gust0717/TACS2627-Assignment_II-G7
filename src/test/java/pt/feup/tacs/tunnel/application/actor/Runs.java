package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.function.BooleanSupplier;

/** Helpers for tests on real threads. */
final class Runs {

  /** How late a thread may be: tracecheck's default slack (TEST.md 3.1). */
  static final Duration SLACK = Duration.ofMillis(500);

  private static final long STEP_MS = 5;

  private Runs() {}

  /** The message of the {@link IllegalStateException} the action throws; empty if none. */
  static String refusal(Runnable action) {
    try {
      action.run();
      return "";
    } catch (IllegalStateException e) {
      return e.getMessage();
    }
  }

  /** Waits until the recorder has the event, failing after {@code timeout}. */
  static void awaitEvent(Recorder recorder, String event, Duration timeout)
      throws InterruptedException {
    await(() -> recorder.events().contains(event), timeout, event + " in " + recorder.events());
  }

  /** Waits until the condition holds, failing after {@link #SLACK}. */
  static void await(BooleanSupplier condition, String what) throws InterruptedException {
    await(condition, SLACK, what);
  }

  private static void await(BooleanSupplier condition, Duration timeout, String what)
      throws InterruptedException {
    long end = System.nanoTime() + timeout.toNanos();
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() - end > 0) {
        fail("not within " + timeout + ": " + what);
      }
      Thread.sleep(STEP_MS);
    }
  }
}
