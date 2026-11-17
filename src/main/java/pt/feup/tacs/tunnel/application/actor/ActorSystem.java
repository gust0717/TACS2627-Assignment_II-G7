package pt.feup.tacs.tunnel.application.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Starts and stops one run's actors (SPEC.md 3.1.1): one mailbox per {@link ActorId}, created by
 * its first {@link #ref} or {@link #spawn}, and one platform thread per actor.
 */
// Implements: D11
public final class ActorSystem {

  private final ActorSystemConfig config;
  private final RunContext context;
  private final Map<String, Mailbox<?>> mailboxes = new ConcurrentHashMap<>();
  private final List<Thread> threads = new ArrayList<>();
  private boolean closed;

  /**
   * Creates a system, which is one run, that reports to no one ({@link ActorObserver#NONE}).
   *
   * @param config the runtime's settings
   */
  public ActorSystem(ActorSystemConfig config) {
    this(config, ActorObserver.NONE);
  }

  /**
   * Creates a system, which is one run, that reports its deliveries and crashes to {@code
   * observer}.
   *
   * @param config the runtime's settings
   * @param observer what the run reports to
   */
  public ActorSystem(ActorSystemConfig config, ActorObserver observer) {
    this.config = Objects.requireNonNull(config, "config");
    this.context = new RunContext(Objects.requireNonNull(observer, "observer"));
  }

  /**
   * Returns the address of an actor, spawned or not yet, creating its mailbox if needed.
   *
   * @param id the actor's id
   * @param <T> the type of the messages it receives
   * @return its address
   */
  public synchronized <T> ActorRef<T> ref(ActorId<T> id) {
    return mailboxOf(id);
  }

  /**
   * Attaches an actor to its mailbox and starts its thread, once per id.
   *
   * @param id the actor's id
   * @param actor the actor
   * @param <T> the type of the messages it receives
   * @return its address
   */
  public synchronized <T> ActorRef<T> spawn(ActorId<T> id, Actor<T> actor) {
    Objects.requireNonNull(actor, "actor");
    if (actor.bound()) {
      throw new IllegalStateException(id + ": this actor is already spawned under another id");
    }
    Mailbox<T> mailbox = mailboxOf(id);
    if (!mailbox.attach()) {
      throw new IllegalStateException(id + " is already spawned");
    }
    actor.bind(mailbox, context, config);
    Thread thread = new Thread(actor::run, id.name());
    thread.setUncaughtExceptionHandler((t, e) -> context.crash(id.name(), e));
    threads.add(thread);
    thread.start();
    return mailbox;
  }

  /**
   * Stops every actor and waits for its thread to end; messages still waiting are dropped.
   *
   * @throws IllegalStateException if a mailbox was never spawned
   */
  public void stop() {
    List<Thread> running = close();
    context.stop();
    try {
      for (Thread thread : running) {
        thread.join();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    List<String> neverSpawned = neverSpawned();
    if (!neverSpawned.isEmpty()) {
      throw new IllegalStateException("never spawned: " + neverSpawned);
    }
  }

  boolean stopped() {
    return context.stopped();
  }

  private synchronized List<Thread> close() {
    closed = true;
    return List.copyOf(threads);
  }

  private synchronized List<String> neverSpawned() {
    return mailboxes.values().stream().filter(m -> !m.attached()).map(Mailbox::name).toList();
  }

  // The cast is safe: a mailbox is stored under its id's name, and ids are compared by identity.
  @SuppressWarnings("unchecked")
  private <T> Mailbox<T> mailboxOf(ActorId<T> id) {
    if (closed) {
      throw new IllegalStateException("the system is stopped: " + id);
    }
    Mailbox<?> mailbox =
        mailboxes.computeIfAbsent(id.name(), name -> new Mailbox<>(id, context, config));
    if (!mailbox.id().equals(id)) {
      throw new IllegalArgumentException("another ActorId is named " + id);
    }
    return (Mailbox<T>) mailbox;
  }
}
