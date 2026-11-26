package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ActorIdTest {

  private static final String NAME = "EndA";

  @Test
  void hasItsName() {
    assertEquals(NAME, new ActorId<String>(NAME).name(), "name");
  }

  @Test
  void printsItsName() {
    assertEquals(NAME, new ActorId<String>(NAME).toString(), "toString");
  }

  @Test
  void rejectsBlankName() {
    assertThrows(IllegalArgumentException.class, () -> new ActorId<String>(" "));
  }

  @Test
  void rejectsNullName() {
    assertThrows(NullPointerException.class, () -> new ActorId<String>(null));
  }

  @Test
  void comparedByIdentity() {
    assertNotEquals(new ActorId<String>("X"), new ActorId<Integer>("X"), "same name, two ids");
  }
}
