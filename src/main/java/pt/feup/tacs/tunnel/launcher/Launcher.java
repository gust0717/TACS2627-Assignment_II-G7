package pt.feup.tacs.tunnel.launcher;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Starts the application with its configuration. */
public final class Launcher {

  /** The event the launcher logs when it starts. */
  public static final String HELLO = "Out(Launcher, Hello)";

  private Launcher() {}

  /**
   * Starts the application: replaces the event log with the launcher's hello event.
   *
   * @param config the configuration to start with
   * @throws IOException if the event log cannot be written
   */
  public static void start(Config config) throws IOException {
    Path log = config.eventLog();
    Path parent = log.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.writeString(log, HELLO + "  # 0 ms" + "\n", UTF_8);
  }
}
