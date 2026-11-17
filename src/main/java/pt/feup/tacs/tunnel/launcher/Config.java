package pt.feup.tacs.tunnel.launcher;

import java.nio.file.Path;

/**
 * The configuration the application is started with.
 *
 * @param eventLog the file every logged event is written to
 */
public record Config(Path eventLog) {

  /** Returns the configuration used when the application is run from the command line. */
  public static Config defaults() {
    return new Config(Path.of("target", "events.log"));
  }
}
