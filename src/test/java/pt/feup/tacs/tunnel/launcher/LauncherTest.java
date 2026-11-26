package pt.feup.tacs.tunnel.launcher;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LauncherTest {

  private static final List<String> HELLO_LOG = List.of("Out(Launcher, Hello)  # 0 ms");
  private static final Path NO_PARENT_LOG = Path.of("events-noparent.log");

  @TempDir Path dir;

  @AfterEach
  void deleteLogWithoutParent() throws IOException {
    Files.deleteIfExists(NO_PARENT_LOG);
  }

  @Test
  void startWritesHelloEvent() throws IOException {
    Path log = dir.resolve("a/b/events.log");
    Launcher.start(new Config(log));
    assertEquals(
        HELLO_LOG, Files.readAllLines(log, UTF_8), "start creates the missing directories");
  }

  @Test
  void startOverwritesExistingLog() throws IOException {
    Path log = dir.resolve("events.log");
    Files.writeString(log, "old\n", UTF_8);
    Launcher.start(new Config(log));
    assertEquals(HELLO_LOG, Files.readAllLines(log, UTF_8), "start replaces the old log");
  }

  @Test
  void startAcceptsPathWithoutParent() throws IOException {
    Launcher.start(new Config(NO_PARENT_LOG));
    assertEquals(
        HELLO_LOG,
        Files.readAllLines(NO_PARENT_LOG, UTF_8),
        "start writes a log without a parent directory");
  }

  @Test
  void defaultsPointToTargetEventsLog() {
    assertEquals(
        Path.of("target", "events.log"),
        Config.defaults().eventLog(),
        "the default log is target/events.log");
  }

  @Test
  void mainWritesDefaultLog() throws IOException {
    Main.main(new String[0]);
    assertEquals(
        HELLO_LOG,
        Files.readAllLines(Path.of("target", "events.log"), UTF_8),
        "main writes the default log");
  }
}
