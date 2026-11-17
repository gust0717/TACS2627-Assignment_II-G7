package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TimerTest {

  private static final ActorId<String> CLOCK = new ActorId<>("Clock");
  private static final String TIMEOUT_K = "In(Clock, Timeout(k))";
  private static final long FLOOD_MS = 1500;
  private static final long FLOOD_EVERY_MS = 10;

  private final Recorder recorder = new Recorder();
  private final ActorSystem system = new ActorSystem(ActorSystemConfig.DEFAULT, recorder);
  private final ActorRef<String> clock = system.spawn(CLOCK, new Clock());

  /** start:key:ms starts a timer, stop:key stops it, busy:ms works that long. */
  private static final class Clock extends Actor<String> {
    @Override
    protected void onMessage(String message) {
      String[] parts = message.split(":");
      switch (parts[0]) {
        case "start" ->
            startTimer(
                parts[1], Duration.ofMillis(Long.parseLong(parts[2])), "Timeout(" + parts[1] + ")");
        case "stop" -> stopTimer(parts[1]);
        case "busy" -> sleep(Long.parseLong(parts[1]));
        default -> {
          // other messages are only reported
        }
      }
    }
  }

  private static void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private long after(String event, String start) {
    return recorder.timeOf(event) - recorder.timeOf(start);
  }

  @AfterEach
  void stop() {
    system.stop();
  }

  // Verifies: D11
  @Test
  void timerDeliversItsMessageNotEarly() throws InterruptedException {
    clock.tell("start:k:300");
    Runs.awaitEvent(recorder, TIMEOUT_K, Runs.SLACK.plusMillis(300));
    long late = after(TIMEOUT_K, "In(Clock, start:k:300)") - 300;
    assertTrue(late >= 0 && late <= Runs.SLACK.toMillis(), "not early, not too late: " + late);
  }

  // Verifies: D11
  @Test
  void startTimerReplacesSameKey() throws InterruptedException {
    clock.tell("start:k:300");
    clock.tell("start:k:800");
    Runs.awaitEvent(recorder, TIMEOUT_K, Runs.SLACK.plusMillis(800));
    sleep(300);
    List<String> events = recorder.events();
    assertEquals(
        List.of(1L, true),
        List.of(
            events.stream().filter(TIMEOUT_K::equals).count(),
            after(TIMEOUT_K, "In(Clock, start:k:800)") >= 800),
        "one Timeout, from the second start: " + events);
  }

  // Verifies: D11
  @Test
  void keysAreIndependent() throws InterruptedException {
    clock.tell("start:a:600");
    clock.tell("start:b:300");
    Runs.awaitEvent(recorder, "In(Clock, Timeout(a))", Runs.SLACK.plusMillis(600));
    assertEquals(
        List.of(
            "In(Clock, start:a:600)",
            "In(Clock, start:b:300)",
            "In(Clock, Timeout(b))",
            "In(Clock, Timeout(a))"),
        recorder.events(),
        "each key fires on its own deadline");
  }

  // Verifies: D11
  @Test
  void stopTimerCancelsOnlyItsKey() throws InterruptedException {
    clock.tell("start:a:300");
    clock.tell("start:b:400");
    clock.tell("stop:a");
    sleep(900);
    assertEquals(
        List.of(
            "In(Clock, start:a:300)",
            "In(Clock, start:b:400)",
            "In(Clock, stop:a)",
            "In(Clock, Timeout(b))"),
        recorder.events(),
        "a cancelled, b fired");
  }

  // Verifies: D12
  @Test
  void timersOrderedByArrivalTime() throws InterruptedException {
    clock.tell("start:k:300");
    clock.tell("busy:800");
    sleep(100);
    clock.tell("early");
    sleep(400);
    clock.tell("late");
    Runs.awaitEvent(recorder, "In(Clock, late)", Runs.SLACK.plusMillis(800));
    List<String> events = recorder.events();
    assertEquals(
        List.of("In(Clock, early)", TIMEOUT_K, "In(Clock, late)"),
        events.subList(events.indexOf("In(Clock, busy:800)") + 1, events.size()),
        "handled in the order they happened");
  }

  // Verifies: D12
  @Test
  void busyMailboxDoesNotStarveTimer() throws InterruptedException {
    clock.tell("start:k:200");
    Thread flood =
        new Thread(
            () -> {
              long end = System.nanoTime() + Duration.ofMillis(FLOOD_MS).toNanos();
              while (System.nanoTime() - end < 0) {
                clock.tell("busy:20");
                sleep(FLOOD_EVERY_MS);
              }
            });
    flood.start();
    Runs.awaitEvent(recorder, TIMEOUT_K, Runs.SLACK.plusMillis(200));
    flood.join();
    long late = after(TIMEOUT_K, "In(Clock, start:k:200)") - 200;
    assertTrue(late <= Runs.SLACK.toMillis(), "fired despite the flood, " + late + " ms late");
  }
}
