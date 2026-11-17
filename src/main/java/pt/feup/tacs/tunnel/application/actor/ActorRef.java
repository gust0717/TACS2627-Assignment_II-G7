package pt.feup.tacs.tunnel.application.actor;

/**
 * How an actor is reached.
 *
 * @param <T> the type of the messages the actor receives
 */
public interface ActorRef<T> {

  /** Returns the actor's name. */
  String name();

  /**
   * Puts a message in the actor's mailbox, waiting while it is full until there is room or the run
   * stops; a message still waiting at stop is dropped.
   *
   * @param message the message
   */
  void tell(T message);
}
