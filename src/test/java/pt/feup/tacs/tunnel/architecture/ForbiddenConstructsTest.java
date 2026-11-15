package pt.feup.tacs.tunnel.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.codeUnits;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.TryCatchBlock;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.time.InstantSource;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The constructs AGENTS.md forbids and SPEC.md section 3.4 confines to one package: threads, locks
 * and monitors outside application.actor, catch (Exception), and the clock in domain.
 */
// Verifies: D3, D4
@AnalyzeClasses(
    packages = "pt.feup.tacs.tunnel",
    importOptions = ImportOption.DoNotIncludeTests.class)
final class ForbiddenConstructsTest {

  private static final String DOMAIN = "pt.feup.tacs.tunnel.domain..";
  private static final String ACTOR = "pt.feup.tacs.tunnel.application.actor..";
  private static final String LOCKS = "java.util.concurrent.locks..";
  private static final Set<String> MONITOR_METHODS = Set.of("wait", "notify", "notifyAll");

  private ForbiddenConstructsTest() {}

  // Verifies: D3
  @ArchTest
  static final ArchRule NO_THREADS_OUTSIDE_ACTOR =
      noClasses()
          .that()
          .resideOutsideOfPackage(ACTOR)
          .should()
          .dependOnClassesThat()
          .areAssignableTo(Thread.class)
          .orShould()
          .callMethod(TimeUnit.class, "sleep", long.class)
          .because("SPEC.md D3: application.actor is the only place with thread handling");

  // Verifies: D3
  @ArchTest
  static final ArchRule NO_LOCKS_OUTSIDE_ACTOR =
      noClasses()
          .that()
          .resideOutsideOfPackage(ACTOR)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(LOCKS)
          .because("AGENTS.md Forbidden: locks outside application.actor");

  // Verifies: D3
  @ArchTest
  static final ArchRule NO_MONITOR_CALLS_OUTSIDE_ACTOR =
      noClasses()
          .that()
          .resideOutsideOfPackage(ACTOR)
          .should()
          .callMethodWhere(objectMonitorMethod())
          .because("SPEC.md D3: wait and notify belong to application.actor");

  // Verifies: D3
  @ArchTest
  static final ArchRule NO_SYNCHRONIZED_METHODS_OUTSIDE_ACTOR =
      noMethods()
          .that()
          .areDeclaredInClassesThat()
          .resideOutsideOfPackage(ACTOR)
          .should()
          .haveModifier(JavaModifier.SYNCHRONIZED)
          .because("AGENTS.md Forbidden: synchronized outside application.actor");

  // Verifies: D3
  // Exception only (TEST.md 3.1): javac compiles try-with-resources to a hidden catch of
  // Throwable, which ArchUnit cannot tell apart from a written one, so catch (Throwable) is
  // checked by Checkstyle IllegalCatch, at source level.
  @ArchTest
  static final ArchRule NO_CATCH_OF_EXCEPTION =
      codeUnits()
          .should(notCatch(Exception.class))
          .because("AGENTS.md Forbidden: catching Exception (Throwable: Checkstyle IllegalCatch)");

  // Verifies: D4
  @ArchTest
  static final ArchRule NO_CLOCK_OR_SLEEP_IN_DOMAIN =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .callMethod(System.class, "currentTimeMillis")
          .orShould()
          .callMethod(System.class, "nanoTime")
          .orShould()
          .callMethodWhere(javaTimeNow())
          .orShould()
          .callMethodWhere(threadSleep())
          .orShould()
          .callMethod(TimeUnit.class, "sleep", long.class)
          .orShould()
          .dependOnClassesThat()
          .areAssignableTo(InstantSource.class)
          .orShould()
          .dependOnClassesThat()
          .areAssignableTo(Date.class)
          .because("SPEC.md D4 and AGENTS.md Forbidden: no clock or sleep in a decision function");

  private static DescribedPredicate<JavaMethodCall> objectMonitorMethod() {
    return DescribedPredicate.describe(
        "Object.wait, Object.notify or Object.notifyAll",
        call ->
            MONITOR_METHODS.contains(call.getName())
                && call.getTarget()
                    .resolveMember()
                    .map(method -> method.getOwner().isEquivalentTo(Object.class))
                    .orElse(true));
  }

  private static DescribedPredicate<JavaMethodCall> javaTimeNow() {
    return DescribedPredicate.describe(
        "a now method of java.time",
        call ->
            "now".equals(call.getName())
                && call.getTargetOwner().getPackageName().startsWith("java.time"));
  }

  private static DescribedPredicate<JavaMethodCall> threadSleep() {
    return DescribedPredicate.describe(
        "Thread.sleep",
        call ->
            "sleep".equals(call.getName()) && call.getTargetOwner().isAssignableTo(Thread.class));
  }

  private static ArchCondition<JavaCodeUnit> notCatch(Class<? extends Throwable> forbidden) {
    return new ArchCondition<>("not catch " + forbidden.getSimpleName()) {
      @Override
      public void check(JavaCodeUnit codeUnit, ConditionEvents events) {
        for (TryCatchBlock block : codeUnit.getTryCatchBlocks()) {
          for (JavaClass caught : block.getCaughtThrowables()) {
            if (caught.isEquivalentTo(forbidden)) {
              events.add(
                  SimpleConditionEvent.violated(
                      block,
                      codeUnit.getFullName()
                          + " catches "
                          + caught.getName()
                          + " in "
                          + block.getSourceCodeLocation()));
            }
          }
        }
      }
    };
  }
}
