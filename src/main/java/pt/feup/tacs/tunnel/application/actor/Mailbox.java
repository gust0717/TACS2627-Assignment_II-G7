package pt.feup.tacs.tunnel.application.actor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** An actor's bounded mailbox, which is also its address (SPEC.md 3.1.1). */
// Implements: D11
final class Mailbox<T> implements ActorRef<T> {

  /** A message and the time it entered the mailbox. */
  record Envelope<T>(T message, long arrivalNanos) {}

  private final ActorId<T> actorId;
  private final RunContext context;
  private final long pollNanos;
  private final BlockingQueue<Envelope<T>> queue;
  private boolean spawned;

  Mailbox(ActorId<T> id, RunContext context, ActorSystemConfig config) {
    this.actorId = id;
    this.context = context;
    this.pollNanos = config.poll().toNanos();
    this.queue = new ArrayBlockingQueue<>(config.mailboxCapacity());
  }

  ActorId<T> id() {
    return actorId;
  }

  @Override
  public String name() {
    return actorId.name();
  }

  @Override
  public void tell(T message) {
    try {
      while (!context.stopped()) {
        if (queue.offer(
            new Envelope<>(message, System.nanoTime()), pollNanos, TimeUnit.NANOSECONDS)) {
          return;
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /** Returns the next envelope, waiting at most {@code timeoutNanos}; null when none came. */
  Envelope<T> poll(long timeoutNanos) throws InterruptedException {
    return queue.poll(timeoutNanos, TimeUnit.NANOSECONDS);
  }

  /** Attaches the mailbox's actor: true the first time only. Called under the system's lock. */
  boolean attach() {
    boolean first = !spawned;
    spawned = true;
    return first;
  }

  /** Whether an actor was attached. Called under the system's lock. */
  boolean attached() {
    return spawned;
  }
}
