package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

/** The observer's contract (SPEC.md 3.1.1, ActorObserver). */
// Verifies: D7, D13
class ObserverTest {

  private static final ActorId<String> A = new ActorId<>("A");
  private static final String BAD = "bad";

  /** Notes, on its thread, every message it handles. */
  private static final class Noting extends Actor<String> {
    private final List<String> notes;

    Noting(List<String> notes) {
      this.notes = notes;
    }

    @Override
    protected void onMessage(String message) {
      notes.add("handled " + message + " on " + Thread.currentThread().getName());
    }
  }

  /** Notes every delivery with its thread; throws on the message "bad". */
  private static final class Watching implements ActorObserver {
    private final List<String> notes;

    Watching(List<String> notes) {
      this.notes = notes;
    }

    @Override
    public void received(String actor, Object message) {
      notes.add("received " + message + " on " + Thread.currentThread().getName());
      if (BAD.equals(message)) {
        throw new IllegalArgumentException(BAD);
      }
    }

    @Override
    public void crashed(String actor, Throwable problem) {
      notes.add("crashed " + actor + ": " + problem.getMessage());
    }
  }

  @Test
  void deliveryReportedOnTheActorsThreadBeforeOnMessage() throws InterruptedException {
    List<String> notes = new CopyOnWriteArrayList<>();
    ActorSystem system = new ActorSystem(ActorSystemConfig.DEFAULT, new Watching(notes));
    system.spawn(A, new Noting(notes)).tell("m");
    Runs.await(() -> notes.size() == 2, "m handled: " + notes);
    system.stop();
    assertEquals(
        List.of("received m on A", "handled m on A"),
        notes,
        "reported on A's thread, before onMessage");
  }

  @Test
  void exceptionFromObserverIsACrash() throws InterruptedException {
    List<String> notes = new CopyOnWriteArrayList<>();
    ActorSystem system = new ActorSystem(ActorSystemConfig.DEFAULT, new Watching(notes));
    system.spawn(A, new Noting(notes)).tell("bad");
    Runs.await(system::stopped, "the run stops: " + notes);
    system.stop();
    assertEquals(
        List.of("received bad on A", "crashed A: bad"),
        notes,
        "crash reported, onMessage not called");
  }

  @Test
  void noObserverRunsAsWell() throws InterruptedException {
    List<String> notes = new CopyOnWriteArrayList<>();
    ActorSystem system = new ActorSystem(ActorSystemConfig.DEFAULT);
    system.spawn(A, new Noting(notes)).tell("m");
    Runs.await(() -> !notes.isEmpty(), "m handled");
    system.stop();
    assertEquals(List.of("handled m on A"), notes, "handled with ActorObserver.NONE");
  }
}
