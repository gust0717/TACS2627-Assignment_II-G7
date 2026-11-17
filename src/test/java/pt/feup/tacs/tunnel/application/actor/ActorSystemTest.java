package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

// Verifies: D11
class ActorSystemTest {

  private static final ActorId<String> A = new ActorId<>("A");
  private static final ActorId<String> B = new ActorId<>("B");
  private static final ActorId<String> C = new ActorId<>("C");

  private final Recorder recorder = new Recorder();
  private final ActorSystem system = new ActorSystem(ActorSystemConfig.DEFAULT, recorder);

  /** On "go", tells "hi" to the actor it was built with. */
  private static final class Greeter extends Actor<String> {
    private final ActorRef<String> friend;

    Greeter(ActorRef<String> friend) {
      this.friend = friend;
    }

    @Override
    protected void onMessage(String message) {
      friend.tell("hi");
    }
  }

  /** Sets its own thread's interrupt flag, as an actor does that catches and restores it. */
  private static final class SelfInterrupting extends Actor<String> {
    @Override
    protected void onMessage(String message) {
      Thread.currentThread().interrupt();
    }
  }

  /** Throws on every message. */
  private static final class Faulty extends Actor<String> {
    @Override
    protected void onMessage(String message) {
      throw new IllegalStateException(message);
    }
  }

  @Test
  void messageToldBeforeSpawnIsDelivered() throws InterruptedException {
    system.ref(B).tell("early");
    system.spawn(B, new Idle());
    Runs.awaitEvent(recorder, "In(B, early)", Runs.SLACK);
    system.stop();
    assertEquals(List.of("In(B, early)"), recorder.events(), "delivered once spawned");
  }

  @Test
  void actorsThatSendToEachOtherWireInAnyOrder() throws InterruptedException {
    system.spawn(A, new Greeter(system.ref(B)));
    system.spawn(B, new Idle());
    system.ref(A).tell("go");
    Runs.awaitEvent(recorder, "In(B, hi)", Runs.SLACK);
    system.stop();
    assertEquals(List.of("In(A, go)", "In(B, hi)"), recorder.events(), "A reached B");
  }

  @Test
  void refAndSpawnReturnTheSameAddress() {
    ActorRef<String> ref = system.ref(A);
    ActorRef<String> spawned = system.spawn(A, new Idle());
    system.stop();
    assertSame(ref, spawned, "one address per id");
  }

  @Test
  void stopEndsEveryThreadWithinSlack() {
    system.spawn(A, new Idle());
    system.spawn(B, new Idle());
    system.spawn(C, new Idle());
    long start = System.nanoTime();
    system.stop();
    long tookMs = (System.nanoTime() - start) / 1_000_000;
    boolean anyAlive =
        Thread.getAllStackTraces().keySet().stream()
            .anyMatch(t -> List.of("A", "B", "C").contains(t.getName()) && t.isAlive());
    assertEquals(
        List.of(true, false),
        List.of(tookMs <= Runs.SLACK.toMillis(), anyAlive),
        "stopped within SLACK (" + tookMs + " ms), no actor thread left");
  }

  @Test
  void interruptReportsCrashAndStopsRun() throws InterruptedException {
    system.spawn(C, new SelfInterrupting());
    system.ref(C).tell("x");
    Runs.awaitEvent(recorder, "Crash(C, java.lang.InterruptedException)", Runs.SLACK);
    long end = System.nanoTime() + Runs.SLACK.toNanos();
    while (!system.stopped() && System.nanoTime() - end < 0) {
      Thread.sleep(5);
    }
    boolean stopped = system.stopped();
    system.stop();
    assertTrue(stopped, "the run is stopped");
  }

  @Test
  void exceptionReportsCrashAndStopsRun() throws InterruptedException {
    system.spawn(C, new Faulty());
    system.ref(C).tell("boom");
    Runs.awaitEvent(recorder, "Crash(C, java.lang.IllegalStateException: boom)", Runs.SLACK);
    long end = System.nanoTime() + Runs.SLACK.toNanos();
    while (!system.stopped() && System.nanoTime() - end < 0) {
      Thread.sleep(5);
    }
    boolean stopped = system.stopped();
    system.stop();
    assertTrue(stopped, "the run is stopped");
  }
}
