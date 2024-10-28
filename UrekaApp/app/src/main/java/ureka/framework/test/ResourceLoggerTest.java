package ureka.framework.test;

import org.junit.jupiter.api.Test;

import ureka.framework.Environment;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;

public class ResourceLoggerTest {
    // auxiliary function
    private void foo() {
        System.out.println("foo");
    }

    // resource.logger - COMPLETED
    @Test
    public void simpleLoggerTest() {
        System.out.println("Environment.DEPLOYMENT_ENV = " + Environment.DEPLOYMENT_ENV);
        System.out.println("Environment.DEBUG_LOG = " + Environment.DEBUG_LOG);
        System.out.println("Environment.CLI_LOG = " + Environment.CLI_LOG);
        System.out.println("Environment.MEASURE_LOG = " + Environment.MEASURE_LOG);

        SimpleLogger.simpleLog("debug", "This is a debug level log.");
        SimpleLogger.simpleLog("info", "This is an info level log.");
        SimpleLogger.simpleLog("warning", "This is a warning level log.");
        SimpleLogger.simpleLog("error", "This is an error level log.");
        SimpleLogger.simpleLog("critical", "This is a critical level log.");
        SimpleLogger.simpleLog("cli", "This is a cli log.");
        SimpleLogger.simpleLog("measure", "This is a measure log.");
    }

    @Test
    public void simpleMeasurerTest() {
        SimpleMeasurer.measureWorkerFunc(this::foo);
    }
}
