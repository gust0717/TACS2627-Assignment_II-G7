package pt.feup.tacs.tunnel.application.actor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

// Verifies: D11
class MailboxTest {

  private static final long BLOCKED_MS = 200;
  private static final long NO_WAIT = TimeUnit.MILLISECONDS.toNanos(1);
  private static final Duration POLL = Duration.ofMillis(2);

  private final RunContext context = new RunContext(ActorObserver.NONE);

  private Mailbox<String> mailbox(int capacity) {
    return new Mailbox<>(new ActorId<>("M"), context, new ActorSystemConfig(capacity, POLL));
  }

  private static void awaitEnd(Thread thread) throws InterruptedException {
    thread.join(Runs.SLACK.toMillis());
    assertFalse(thread.isAlive(), "the teller returned within SLACK");
  }

  @Test
  void tellKeepsOrderAndArrivalTime() throws InterruptedException {
    Mailbox<String> m = mailbox(4);
    m.tell("a");
    m.tell("b");
    Mailbox.Envelope<String> a = m.poll(NO_WAIT);
    Mailbox.Envelope<String> b = m.poll(NO_WAIT);
    assertEquals(
        List.of("a", "b", true),
        List.of(a.message(), b.message(), b.arrivalNanos() - a.arrivalNanos() >= 0),
        "first in, first out, arrival times in order");
  }

  @Test
  void isNamedByItsId() {
    assertEquals("M", mailbox(1).name(), "name");
  }

  @Test
  void pollReturnsNullWhenEmpty() throws InterruptedException {
    assertNull(mailbox(1).poll(NO_WAIT), "nothing to poll");
  }

  @Test
  void tellWaitsWhileFullUntilRoom() throws InterruptedException {
    Mailbox<String> m = mailbox(1);
    m.tell("a");
    Thread teller = new Thread(() -> m.tell("b"));
    long told = System.nanoTime();
    teller.start();
    Thread.sleep(BLOCKED_MS);
    boolean blocked = teller.isAlive();
    m.poll(NO_WAIT);
    awaitEnd(teller);
    Mailbox.Envelope<String> b = m.poll(NO_WAIT);
    long waitedMs = (b.arrivalNanos() - told) / 1_000_000;
    assertEquals(
        List.of(true, "b", true),
        List.of(blocked, b.message(), waitedMs >= BLOCKED_MS / 2),
        "blocked while full, delivered once there was room, stamped when it entered, not when"
            + " told ("
            + waitedMs
            + " ms later)");
  }

  @Test
  void tellReturnsWhenStoppedWhileFull() throws InterruptedException {
    Mailbox<String> m = mailbox(1);
    m.tell("a");
    Thread teller = new Thread(() -> m.tell("b"));
    teller.start();
    Thread.sleep(BLOCKED_MS);
    context.stop();
    awaitEnd(teller);
    m.poll(NO_WAIT);
    assertNull(m.poll(NO_WAIT), "the blocked message was dropped");
  }

  @Test
  void tellReturnsOnInterruptKeepingTheFlag() throws InterruptedException {
    Mailbox<String> m = mailbox(1);
    m.tell("a");
    AtomicBoolean flag = new AtomicBoolean();
    Thread teller =
        new Thread(
            () -> {
              m.tell("b");
              flag.set(Thread.currentThread().isInterrupted());
            });
    teller.start();
    Thread.sleep(BLOCKED_MS);
    teller.interrupt();
    awaitEnd(teller);
    assertTrue(flag.get(), "the interrupt flag is restored");
  }

  @Test
  void attachesOnce() {
    Mailbox<String> m = mailbox(1);
    boolean before = m.attached();
    boolean first = m.attach();
    boolean second = m.attach();
    assertEquals(
        List.of(false, true, false, true),
        List.of(before, first, second, m.attached()),
        "not attached, attached once, refused twice, attached");
  }
}
