package pt.feup.tacs.tunnel.application.actor;

import java.time.Duration;

/**
 * The base class of every actor (SPEC.md 3.1.1). Its loop reports each message to the observer and
 * calls {@link #onMessage}, one message at a time, until the run stops; expired timers and messages
 * are handled in the order they happened.
 *
 * @param <T> the type of the messages the actor receives
 */
// Implements: D10, D12, D13
public abstract class Actor<T> {

  private final Timers<T> timers = new Timers<>();
  private Mailbox<T> mailbox;
  private RunContext context;
  private long pollNanos;

  /**
   * Handles one message; called by the actor's own thread only.
   *
   * @param message the message, or the message of an expired timer
   */
  protected abstract void onMessage(T message);

  /**
   * Starts the timer {@code key}, replacing any running timer with that key; when it expires,
   * {@code message} is delivered like any other message.
   *
   * @param key the timer's name
   * @param after how long until it expires, as the run waits it
   * @param message the message it delivers
   */
  protected final void startTimer(String key, Duration after, T message) {
    requireSpawned();
    timers.start(key, System.nanoTime() + after.toNanos(), message);
  }

  /**
   * Cancels the timer {@code key}, if it is running.
   *
   * @param key the timer's name
   */
  protected final void stopTimer(String key) {
    requireSpawned();
    timers.stop(key);
  }

  final boolean bound() {
    return mailbox != null;
  }

  final void bind(Mailbox<T> mailbox, RunContext context, ActorSystemConfig config) {
    this.mailbox = mailbox;
    this.context = context;
    this.pollNanos = config.poll().toNanos();
  }

  final void run() {
    try {
      while (!context.stopped()) {
        step();
      }
    } catch (InterruptedException e) {
      // An interrupt is not how actors stop (the stop flag is): it ends this actor, so the run
      // stops with a crash reported rather than leave its mailbox unread.
      context.crash(mailbox.name(), e);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Waits for a message or the first timer; then delivers, in deadline order, every timer that
   * expired before the message arrived (or before now, when none came), then the message.
   */
  private void step() throws InterruptedException {
    Mailbox.Envelope<T> envelope = mailbox.poll(timers.waitNanos(System.nanoTime(), pollNanos));
    long before = envelope == null ? System.nanoTime() + 1 : envelope.arrivalNanos();
    for (T expired = timers.takeExpiredBefore(before);
        expired != null;
        expired = timers.takeExpiredBefore(before)) {
      deliver(expired);
    }
    if (envelope != null) {
      deliver(envelope.message());
    }
  }

  private void deliver(T message) {
    context.received(mailbox.name(), message);
    onMessage(message);
  }

  private void requireSpawned() {
    if (mailbox == null) {
      throw new IllegalStateException("the actor is not spawned yet");
    }
  }
}
