package pt.feup.tacs.tunnel.application.actor;

/**
 * What the runtime reports of a run (SPEC.md 3.1.1): the project's one way to see inside it. It is
 * called from every actor's thread, so it must be safe to call from several threads at once; an
 * exception it throws is a crash of the actor that called it.
 */
// Implements: D13
public interface ActorObserver {

  /** Does nothing. */
  ActorObserver NONE = new ActorObserver() {};

  /**
   * A message is delivered, an expired timer's included: called on the actor's own thread, before
   * {@code onMessage}, in the order the actor handles them.
   *
   * @param actor the actor's name
   * @param message the message
   */
  default void received(String actor, Object message) {
    // nothing by default
  }

  /**
   * An actor crashed: called before the run stops.
   *
   * @param actor the actor's name
   * @param problem what ended it
   */
  default void crashed(String actor, Throwable problem) {
    // nothing by default
  }
}
