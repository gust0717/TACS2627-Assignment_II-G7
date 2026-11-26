package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// Verifies: D11
class WiringTest {

  private static final ActorId<String> A = new ActorId<>("A");
  private static final ActorId<String> B = new ActorId<>("B");

  private final ActorSystem system = new ActorSystem(ActorSystemConfig.DEFAULT);

  @Test
  void spawnTwiceRefused() {
    system.spawn(A, new Idle());
    String refusal = Runs.refusal(() -> system.spawn(A, new Idle()));
    system.stop();
    assertTrue(refusal.contains("A"), "refused, naming the actor: " + refusal);
  }

  @Test
  void secondIdWithSameNameRefused() {
    system.ref(A);
    assertThrows(IllegalArgumentException.class, () -> system.ref(new ActorId<String>("A")));
  }

  @Test
  void refAfterStopRefused() {
    system.stop();
    assertThrows(IllegalStateException.class, () -> system.ref(A));
  }

  @Test
  void spawnAfterStopRefused() {
    system.stop();
    assertThrows(IllegalStateException.class, () -> system.spawn(A, new Idle()));
  }

  @Test
  void stopFailsOnMailboxNeverSpawned() {
    system.spawn(A, new Idle());
    system.ref(B);
    String refusal = Runs.refusal(system::stop);
    assertTrue(refusal.contains("B"), "refused, naming the actor: " + refusal);
  }

  @Test
  void sameActorUnderSecondIdRefusedNamingIt() {
    Idle idle = new Idle();
    system.spawn(A, idle);
    String refusal = Runs.refusal(() -> system.spawn(B, idle));
    system.stop();
    assertTrue(refusal.contains("B"), "refused, naming the second id: " + refusal);
  }

  @Test
  void sameActorUnderSecondIdLeavesItsMailboxUnspawned() {
    Idle idle = new Idle();
    system.ref(B).tell("waiting");
    system.spawn(A, idle);
    Runs.refusal(() -> system.spawn(B, idle));
    String refusal = Runs.refusal(system::stop);
    assertTrue(refusal.contains("B"), "stop reports B as never spawned: " + refusal);
  }
}
