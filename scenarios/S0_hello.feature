@S0
Feature: S0 Hello world
  Scenario: the launcher starts and logs its greeting
    Given the system is started
    Then the log matches the expected log
