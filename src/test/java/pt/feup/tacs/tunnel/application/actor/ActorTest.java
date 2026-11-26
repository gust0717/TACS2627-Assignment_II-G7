package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

// Verifies: D10, D13
class ActorTest {

  private static final ActorId<String> ECHO = new ActorId<>("Echo");
  private static final long BUSY_MS = 100;

  private final Recorder recorder = new Recorder();
  private final ActorSystem system = new ActorSystem(ActorSystemConfig.DEFAULT, recorder);

  /** Notes Seen(m) for each message m. */
  private static final class Echo extends Actor<String> {
    private final Recorder recorder;

    Echo(Recorder recorder) {
      this.recorder = recorder;
    }

    @Override
    protected void onMessage(String message) {
      recorder.note("Seen(" + message + ")");
    }
  }

  /** Takes BUSY_MS on each message, then notes Done(m). */
  private static final class Slow extends Actor<String> {
    private final Recorder recorder;

    Slow(Recorder recorder) {
      this.recorder = recorder;
    }

    @Override
    protected void onMessage(String message) {
      try {
        Thread.sleep(BUSY_MS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      recorder.note("Done(" + message + ")");
    }
  }

  @AfterEach
  void stop() {
    system.stop();
  }

  @Test
  void reportsThenHandsMessageInOrder() throws InterruptedException {
    ActorRef<String> echo = system.spawn(ECHO, new Echo(recorder));
    echo.tell("a");
    echo.tell("b");
    Runs.awaitEvent(recorder, "Seen(b)", Runs.SLACK);
    assertEquals(
        List.of("In(Echo, a)", "Seen(a)", "In(Echo, b)", "Seen(b)"),
        recorder.events(),
        "reported, then what onMessage notes, one message after the other");
  }

  @Test
  void oneMessageAtATime() throws InterruptedException {
    ActorRef<String> slow = system.spawn(ECHO, new Slow(recorder));
    slow.tell("a");
    slow.tell("b");
    Runs.awaitEvent(recorder, "Done(b)", Runs.SLACK.plusMillis(2 * BUSY_MS));
    List<String> events = recorder.events();
    assertTrue(
        events.indexOf("In(Echo, b)") > events.indexOf("Done(a)"),
        "b is handled only after a is done: " + events);
  }

  @Test
  void timerRefusedBeforeSpawn() {
    Echo echo = new Echo(recorder);
    assertThrows(
        IllegalStateException.class, () -> echo.startTimer("k", Duration.ofMillis(1), "t"));
  }
}
