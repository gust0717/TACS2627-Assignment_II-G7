package pt.feup.tacs.tunnel.system;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.Suite;

/** Runs every scenario in {@code scenarios/}, one after another; tracecheck compares the logs. */
@Suite
@IncludeEngines("cucumber")
@SelectDirectories("scenarios")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "pt.feup.tacs.tunnel.system")
// TEST.md: scenarios never run in parallel.
@ConfigurationParameter(key = PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, value = "false")
public class RunScenariosIT {}
