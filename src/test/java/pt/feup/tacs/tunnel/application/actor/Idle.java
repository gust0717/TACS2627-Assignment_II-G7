package pt.feup.tacs.tunnel.application.actor;

/** A test actor that does nothing with its messages: only their delivery is reported. */
final class Idle extends Actor<String> {
  @Override
  protected void onMessage(String message) {
    // only its delivery is reported
  }
}
