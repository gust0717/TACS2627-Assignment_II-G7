package pt.feup.tacs.tunnel.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.BaseStream;
import java.util.stream.StreamSupport;

/**
 * The concurrency AGENTS.md forbids: thread pools, executors, virtual threads, parallel streams and
 * unbounded queues everywhere, and a second queue inside an actor (one platform thread per actor,
 * one bounded inbox).
 */
// Verifies: D3
@AnalyzeClasses(
    packages = "pt.feup.tacs.tunnel",
    importOptions = ImportOption.DoNotIncludeTests.class)
final class ConcurrencyConstructsTest {

  private static final String ACTOR = "pt.feup.tacs.tunnel.application.actor..";

  private ConcurrencyConstructsTest() {}

  // Verifies: D3
  @ArchTest
  static final ArchRule NO_EXECUTORS =
      noClasses()
          .should()
          .dependOnClassesThat()
          .areAssignableTo(Executor.class)
          .orShould()
          .dependOnClassesThat()
          .belongToAnyOf(Executors.class, CompletableFuture.class, Timer.class)
          .because("AGENTS.md Forbidden: thread pools, executors or async tasks");

  // Verifies: D3
  @ArchTest
  static final ArchRule NO_VIRTUAL_THREADS =
      noClasses()
          .should()
          .callMethod(Thread.class, "ofVirtual")
          .orShould()
          .callMethod(Thread.class, "startVirtualThread", Runnable.class)
          .because("AGENTS.md Forbidden: virtual threads (one platform thread per actor)");

  // Verifies: D3
  // TEST.md 3.1: no unbounded queue is created: an actor's inbox is its only queue, and timers use
  // it (the inbox's poll timeout), so a DelayQueue would be a second queue. java.util queues are
  // covered inside application.actor by the next rule; elsewhere they are not shared between
  // threads, since threads are confined to application.actor.
  @ArchTest
  static final ArchRule NO_UNBOUNDED_QUEUES =
      noClasses()
          .should()
          .callConstructor(LinkedBlockingQueue.class)
          .orShould()
          .callConstructor(LinkedBlockingQueue.class, Collection.class)
          .orShould()
          .callConstructor(LinkedBlockingDeque.class)
          .orShould()
          .callConstructor(LinkedBlockingDeque.class, Collection.class)
          .orShould()
          .dependOnClassesThat()
          .belongToAnyOf(
              ConcurrentLinkedQueue.class,
              ConcurrentLinkedDeque.class,
              LinkedTransferQueue.class,
              PriorityBlockingQueue.class,
              DelayQueue.class)
          .because(
              "AGENTS.md Forbidden and TEST.md 3.1: unbounded queues; the inbox is the only queue");

  // Verifies: D3
  // TEST.md 3.1: an actor's inbox is its only queue, and timers use it: a timer is the inbox's poll
  // timeout, not a second queue of pending events kept in a java.util queue.
  @ArchTest
  static final ArchRule NO_SECOND_QUEUE_IN_ACTOR =
      noClasses()
          .that()
          .resideInAPackage(ACTOR)
          .should()
          .dependOnClassesThat()
          .belongToAnyOf(ArrayDeque.class, PriorityQueue.class, LinkedList.class)
          .because("TEST.md 3.1: an actor's inbox is its only queue, and timers use it");

  // Verifies: D3
  // The parallel flag of StreamSupport is a run-time argument, which ArchUnit cannot read, so every
  // StreamSupport method taking a boolean (all of its stream factories) is forbidden.
  @ArchTest
  static final ArchRule NO_PARALLEL_STREAMS =
      noClasses()
          .should()
          .callMethodWhere(parallelStream())
          .because("AGENTS.md Forbidden: thread pools or async tasks (parallel streams use one)");

  private static DescribedPredicate<JavaMethodCall> parallelStream() {
    return DescribedPredicate.describe(
        "Collection.parallelStream, BaseStream.parallel, Arrays.parallel* or a StreamSupport"
            + " method taking a parallel flag",
        call ->
            "parallelStream".equals(call.getName())
                    && call.getTargetOwner().isAssignableTo(Collection.class)
                || "parallel".equals(call.getName())
                    && call.getTargetOwner().isAssignableTo(BaseStream.class)
                || call.getName().startsWith("parallel")
                    && call.getTargetOwner().isEquivalentTo(Arrays.class)
                || call.getTargetOwner().isEquivalentTo(StreamSupport.class)
                    && call.getTarget().getRawParameterTypes().stream()
                        .anyMatch(type -> type.isEquivalentTo(boolean.class)));
  }
}
