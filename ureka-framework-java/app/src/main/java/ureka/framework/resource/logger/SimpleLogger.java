package ureka.framework.resource.logger;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import ureka.framework.Environment;

public class SimpleLogger {

    private static final Logger LOGGER = Logger.getLogger(SimpleLogger.class.getName());

    public static void simpleLog(String logLevel, String logInfo) {
        // pragma: no cover -> PRODUCTION
        if (Environment.DEPLOYMENT_ENV == "TEST") {
            if (Environment.DEBUG_LOG == "OPEN") {
                if (Objects.equals(logLevel, "debug")) {
                    LOGGER.log(Level.FINE, logInfo);
                } else if (Objects.equals(logLevel, "info")) {
                    LOGGER.log(Level.INFO, logInfo);
                }
            }

            if (Environment.DEBUG_LOG == "OPEN" || Environment.DEBUG_LOG == "CLOSED") {
                if (Objects.equals(logLevel, "warning")) {
                    LOGGER.log(Level.WARNING, logInfo);
                } else if (Objects.equals(logLevel, "error")) {
                    LOGGER.log(Level.SEVERE, logInfo);
                } else if (Objects.equals(logLevel, "critical")) {
                    LOGGER.log(Level.SEVERE, logInfo);
                }
            }

            if (Environment.CLI_LOG == "OPEN") {
                if (Objects.equals(logLevel, "cli")) {
                    LOGGER.log(Level.FINE, logInfo);
                }
            }

            if (Environment.MEASURE_LOG == "OPEN") {
                if (Objects.equals(logLevel, "measure")) {
                    LOGGER.log(Level.FINE, logInfo);
                }
            }
        } else if (Objects.equals(Environment.DEPLOYMENT_ENV, "PRODUCTION")) {
            if (Objects.equals(Environment.DEBUG_LOG, "OPEN")) {
                if (Objects.equals(logLevel, "debug")) {
                    System.out.println("[   DEBUG] : {" + logInfo + "}");
                } else if (Objects.equals(logLevel, "info")) {
                    System.out.println("[    INFO] : {" + logInfo + "}");
                }
            }

            if (Objects.equals(Environment.DEBUG_LOG, "OPEN") || Objects.equals(Environment.DEBUG_LOG, "CLOSED")) {
                if (Objects.equals(logLevel, "warning")) {
                    System.out.println("[ WARNING] : {" + logInfo + "}");
                } else if (Objects.equals(logLevel, "error")) {
                    System.out.println("[   ERROR] : {" + logInfo + "}");
                } else if (Objects.equals(logLevel, "critical")) {
                    System.out.println("[CRITICAL] : {" + logInfo + "}");
                }
            }

            if (Objects.equals(Environment.CLI_LOG, "OPEN")) {
                if (Objects.equals(logLevel, "cli")) {
                    System.out.println("[     CLI] : {" + logInfo + "}");
                }
            }

            if (Objects.equals(Environment.MEASURE_LOG, "OPEN")) {
                if (Objects.equals(logLevel, "measure")) {
                    System.out.println("[ MEASURE] : {" + logInfo + "}");
                }
            }
        } else {
            String error = "Deployment Environment: {" + Environment.DEPLOYMENT_ENV + "} is not supported.";
            throw new RuntimeException(error);
        }
    }
}
