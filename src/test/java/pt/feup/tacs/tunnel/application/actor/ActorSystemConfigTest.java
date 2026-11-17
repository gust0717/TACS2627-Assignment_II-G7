package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ActorSystemConfigTest {

  private static final Duration POLL = Duration.ofMillis(2);

  @Test
  void defaultIs16Messages2Ms() {
    assertEquals(new ActorSystemConfig(16, POLL), ActorSystemConfig.DEFAULT, "defaults");
  }

  @Test
  void rejectsZeroCapacity() {
    assertThrows(IllegalArgumentException.class, () -> new ActorSystemConfig(0, POLL));
  }

  @Test
  void rejectsZeroPoll() {
    assertThrows(IllegalArgumentException.class, () -> new ActorSystemConfig(16, Duration.ZERO));
  }

  @Test
  void rejectsNegativePoll() {
    assertThrows(
        IllegalArgumentException.class, () -> new ActorSystemConfig(16, Duration.ofMillis(-1)));
  }

  @Test
  void rejectsNullPoll() {
    assertThrows(NullPointerException.class, () -> new ActorSystemConfig(16, null));
  }
}
