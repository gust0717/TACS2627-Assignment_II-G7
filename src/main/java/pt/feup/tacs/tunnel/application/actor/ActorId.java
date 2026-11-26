package pt.feup.tacs.tunnel.application.actor;

import java.util.Objects;

/**
 * An actor's typed name, declared once as a constant. Ids are compared by identity, so a second id
 * with a name already in use is refused by {@link ActorSystem}.
 *
 * @param <T> the type of the messages the actor receives
 */
public final class ActorId<T> {

  private final String actorName;

  /**
   * Creates an id.
   *
   * @param name the actor's name, as logged; not blank
   */
  public ActorId(String name) {
    Objects.requireNonNull(name, "name");
    if (name.isBlank()) {
      throw new IllegalArgumentException("an actor's name may not be blank");
    }
    this.actorName = name;
  }

  /** Returns the actor's name. */
  public String name() {
    return actorName;
  }

  @Override
  public String toString() {
    return actorName;
  }
}
