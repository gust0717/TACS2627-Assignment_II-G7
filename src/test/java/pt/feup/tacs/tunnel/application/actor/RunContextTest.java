package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

// Verifies: D3, D13
class RunContextTest {

  private static final IllegalStateException PROBLEM = new IllegalStateException("boom");

  @Test
  void runsAtFirst() {
    assertFalse(new RunContext(ActorObserver.NONE).stopped(), "running at first");
  }

  @Test
  void stopSetsTheFlag() {
    RunContext context = new RunContext(ActorObserver.NONE);
    context.stop();
    assertTrue(context.stopped(), "stopped after stop()");
  }

  @Test
  void receivedIsPassedToTheObserver() {
    List<String> seen = new ArrayList<>();
    RunContext context =
        new RunContext(
            new ActorObserver() {
              @Override
              public void received(String actor, Object message) {
                seen.add(actor + " <- " + message);
              }
            });
    context.received("A", "m");
    assertEquals(List.of("A <- m"), seen, "passed on as it came");
  }

  @Test
  void crashIsReportedBeforeTheRunStops() {
    AtomicReference<RunContext> holder = new AtomicReference<>();
    List<Object> seen = new ArrayList<>();
    RunContext context =
        new RunContext(
            new ActorObserver() {
              @Override
              public void crashed(String actor, Throwable problem) {
                seen.addAll(List.of(actor, problem, holder.get().stopped()));
              }
            });
    holder.set(context);
    context.crash("A", PROBLEM);
    assertEquals(
        List.of("A", PROBLEM, false, true),
        List.of(seen.get(0), seen.get(1), seen.get(2), context.stopped()),
        "reported while running, then stopped");
  }

  @Test
  void crashStopsTheRunEvenIfTheObserverThrows() {
    RunContext context =
        new RunContext(
            new ActorObserver() {
              @Override
              public void crashed(String actor, Throwable problem) {
                throw new IllegalArgumentException("observer failed");
              }
            });
    String thrown = "";
    try {
      context.crash("A", PROBLEM);
    } catch (IllegalArgumentException e) {
      thrown = e.getMessage();
    }
    assertEquals(
        List.of("observer failed", true),
        List.of(thrown, context.stopped()),
        "the observer's exception passes on, and the run stops all the same");
  }
}
