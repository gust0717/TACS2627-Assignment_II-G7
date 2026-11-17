package pt.feup.tacs.tunnel.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import pt.feup.tacs.tunnel.launcher.Config;
import pt.feup.tacs.tunnel.launcher.Launcher;

/** Steps of the feature files: Gherkin gives the inputs, tracecheck compares the logs. */
public class ScenarioSteps {

  private static final String FEATURE_SUFFIX = ".feature";

  private String name;

  @Before
  public void rememberScenarioName(Scenario scenario) {
    String path = scenario.getUri().getPath();
    String file = path.substring(path.lastIndexOf('/') + 1);
    name = file.substring(0, file.length() - FEATURE_SUFFIX.length());
  }

  @Given("the system is started")
  public void theSystemIsStarted() throws IOException {
    Launcher.start(new Config(Path.of("target", "scenarios", name + ".actual.txt")));
  }

  @Then("the log matches the expected log")
  public void theLogMatchesTheExpectedLog() throws IOException, InterruptedException {
    // TEST.md 3.1: a Crash line fails every scenario, since a "..." could hide it from tracecheck.
    String actual = Files.readString(Path.of("target", "scenarios", name + ".actual.txt"));
    assertFalse(actual.lines().anyMatch(line -> line.startsWith("Crash(")), actual);
    Process tracecheck =
        new ProcessBuilder(
                "python3",
                "tracecheck/tracecheck.py",
                "scenarios/" + name + ".expected.txt",
                "target/scenarios/" + name + ".actual.txt")
            .redirectErrorStream(true)
            .start();
    String output = new String(tracecheck.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    assertEquals(0, tracecheck.waitFor(), output);
  }
}
