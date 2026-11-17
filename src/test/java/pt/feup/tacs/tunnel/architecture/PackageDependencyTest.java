package pt.feup.tacs.tunnel.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Set;

/** The package dependencies of SPEC.md section 3.4 (Figure 7, Table 5). */
// Verifies: D1, D3, D4, D5
@AnalyzeClasses(
    packages = "pt.feup.tacs.tunnel",
    importOptions = ImportOption.DoNotIncludeTests.class)
final class PackageDependencyTest {

  private static final String DOMAIN = "pt.feup.tacs.tunnel.domain..";
  private static final String APPLICATION = "pt.feup.tacs.tunnel.application";
  private static final String PORT = "pt.feup.tacs.tunnel.application.port..";
  private static final String ACTOR = "pt.feup.tacs.tunnel.application.actor..";
  private static final String ADAPTERS = "pt.feup.tacs.tunnel.adapters..";
  private static final String LAUNCHER = "pt.feup.tacs.tunnel.launcher..";
  private static final String PROJECT = "pt.feup.tacs.tunnel..";
  private static final String JDK = "java..";
  private static final String[] DOMAIN_ALLOWED = {
    DOMAIN,
    "java.lang",
    "java.util",
    "java.util.function",
    "java.util.stream",
    "java.time",
    // ChronoUnit and the other time units and fields that java.time types take as arguments.
    "java.time.temporal",
  };
  private static final Set<String> SYSTEM_IO_METHODS =
      Set.of("exit", "getenv", "getProperty", "getProperties", "console", "getLogger");

  private PackageDependencyTest() {}

  // Verifies: D1, D4, D5
  // TEST.md 3.1: domain uses only the JDK's language, collection and time types: no threads,
  // queues, clock, logging or I/O. Packages without ".." are matched exactly, so
  // java.util.concurrent, java.util.logging, java.lang.reflect and java.lang.invoke stay out.
  // Records, enums, pattern switches, lambdas and string concatenation compile to invokedynamic
  // bootstraps in java.lang.invoke and java.lang.runtime (checked with javap); ArchUnit does not
  // count bootstrap methods as dependencies, so neither package is allowed.
  @ArchTest
  static final ArchRule DOMAIN_USES_ONLY_LANGUAGE_COLLECTIONS_AND_TIME =
      classes()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(DOMAIN_ALLOWED)
          .because(
              "SPEC.md Table 5 and TEST.md 3.1: domain uses only the JDK's language, collection"
                  + " and time types");

  // Verifies: D1, D4
  // TEST.md 3.1: no logging or I/O in domain. The whitelist above admits java.lang, which also
  // holds
  // the console streams, System.Logger, the environment, processes, VM exit and thread-local state;
  // those are forbidden here.
  @ArchTest
  static final ArchRule DOMAIN_DOES_NO_LOGGING_OR_IO =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .accessField(System.class, "out")
          .orShould()
          .accessField(System.class, "err")
          .orShould()
          .accessField(System.class, "in")
          .orShould()
          .callMethodWhere(systemIoMethod())
          .orShould()
          .callMethodWhere(printStackTrace())
          .orShould()
          .dependOnClassesThat()
          .belongToAnyOf(
              System.Logger.class,
              Runtime.class,
              ProcessBuilder.class,
              Process.class,
              ThreadLocal.class)
          .because("SPEC.md Table 5 and TEST.md 3.1: no logging or I/O in domain");

  // Verifies: D1, D5
  @ArchTest
  static final ArchRule PORT_USES_ONLY_JDK =
      classes()
          .that()
          .resideInAPackage(PORT)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(PORT, JDK)
          .because("SPEC.md Table 5: application.port may use nothing");

  // Verifies: D3, D5, D9
  @ArchTest
  static final ArchRule ACTOR_USES_ONLY_JDK =
      classes()
          .that()
          .resideInAPackage(ACTOR)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(ACTOR, JDK)
          .because("SPEC.md Table 5 and D9: application.actor is a library on the JDK only");

  // Verifies: D1, D5
  @ArchTest
  static final ArchRule APPLICATION_USES_ONLY_CORE =
      classes()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(APPLICATION, DOMAIN, PORT, ACTOR, JDK)
          .because(
              "SPEC.md Table 5 and D5: application (without its subpackages) may use domain,"
                  + " application.port, application.actor");

  // Verifies: D1, D5
  @ArchTest
  static final ArchRule ADAPTERS_USE_ONLY_CORE_PORTS_AND_RUNTIME =
      classes()
          .that()
          .resideInAPackage(ADAPTERS)
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(ADAPTERS, DOMAIN, PORT, ACTOR, JDK)
          .because("SPEC.md Table 5: adapters may use domain, application.port, application.actor");

  // Verifies: D1, D5
  @ArchTest
  static final ArchRule NOBODY_USES_LAUNCHER =
      noClasses()
          .that()
          .resideOutsideOfPackage(LAUNCHER)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(LAUNCHER)
          .because("SPEC.md Figure 7 and D1: every dependency points toward the core");

  // Verifies: D5
  @ArchTest
  static final ArchRule ONLY_PROJECT_AND_JDK =
      classes()
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage(PROJECT, JDK)
          .because("AGENTS.md Forbidden: no library beyond the standard one");

  private static DescribedPredicate<JavaMethodCall> systemIoMethod() {
    return DescribedPredicate.describe(
        "System.exit, getenv, getProperty, getProperties, console or getLogger",
        call ->
            SYSTEM_IO_METHODS.contains(call.getName())
                && call.getTargetOwner().isEquivalentTo(System.class));
  }

  private static DescribedPredicate<JavaMethodCall> printStackTrace() {
    return DescribedPredicate.describe(
        "Throwable.printStackTrace",
        call ->
            "printStackTrace".equals(call.getName())
                && call.getTargetOwner().isAssignableTo(Throwable.class));
  }
}
