package pt.feup.tacs.tunnel.launcher;

import java.io.IOException;

/** The command-line entry point. */
public final class Main {

  private Main() {}

  /**
   * Starts the application with its default configuration.
   *
   * @param args ignored
   * @throws IOException if the event log cannot be written
   */
  public static void main(String[] args) throws IOException {
    Launcher.start(Config.defaults());
  }
}
