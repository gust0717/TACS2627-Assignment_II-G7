package pt.feup.tacs.tunnel.application.actor;

import java.util.concurrent.atomic.AtomicBoolean;

/** What one run's threads share: the stop flag and the observer (SPEC.md 3.1.1). */
// Implements: D3, D13
final class RunContext {

  private final AtomicBoolean stopFlag = new AtomicBoolean();
  private final ActorObserver observer;

  RunContext(ActorObserver observer) {
    this.observer = observer;
  }

  /** Reports a delivery to the observer. */
  void received(String actor, Object message) {
    observer.received(actor, message);
  }

  /** Reports a crash to the observer, then stops the run, even if the observer throws. */
  void crash(String actor, Throwable problem) {
    try {
      observer.crashed(actor, problem);
    } finally {
      stop();
    }
  }

  void stop() {
    stopFlag.set(true);
  }

  boolean stopped() {
    return stopFlag.get();
  }
}
