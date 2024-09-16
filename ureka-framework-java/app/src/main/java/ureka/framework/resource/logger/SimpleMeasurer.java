package ureka.framework.resource.logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import ureka.framework.Environment;

public class SimpleMeasurer {
    private static final Logger LOGGER = Logger.getLogger(SimpleMeasurer.class.getName());
    //////////////////////////////////////////////////////
    // Response Time Measurement (Process)
    //////////////////////////////////////////////////////
    // start timer
    private static long startProcess = 0;
    private static long startPerfCounter = 0;

    // end timer
    private static long endProcess = 0;
    private static long endPerfCounter = 0;

    // elapsed time
    private static long elapsedProcessTime = 0;
    private static long elapsedPerfTime = 0;

    //////////////////////////////////////////////////////
    // Response Time Measurement (RTT-based Comm.)
    //////////////////////////////////////////////////////
    // start timer
    private static long startComm = 0;

    // end timer
    private static long endComm = 0;
    private static long elapsedCommTime = 0;

    /////////////////////////////////////////////////////////////
    // NOTES: Time
    // System.currentTimeMillis() returns milliseconds as long.
    // System.nanoTime() returns nanoseconds as long.
    // The time unit in the original Python code is seconds with 3 decimal points.
    // IMPORTANT: elapsedProcessTime, elapsedPerfTime, and elapsedBlockTime are implemented in milliseconds.
    /////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////
    // NOTES: Generic types and functions
    // Function<T, R> for 1 parameter and 1 return value.
    // BiFunction<T, U, R> for 2 parameters and 1 return value.
    // TriFunction<T, U, V, R> for 3 parameters and 1 return value.
    // Supplier<R> for 0 parameter and 1 return value.
    // Consumer<T> for 1 parameter and 0 return value.
    // We need to implement additional interface to handle exceptions.
    /////////////////////////////////////////////////////////////

    // Functional interfaces.
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
    @FunctionalInterface
    public interface ThrowingTriConsumer<T, U, V, E extends Exception> {
        void accept(T t, U u, V v) throws E;
    }
    @FunctionalInterface
    public interface QuadConsumer<T, U, V, W> {
        void accept(T t, U u, V v, W w);
    }
    @FunctionalInterface
    public interface ThrowingSupplier<R> {
        R get() throws Exception;
    }
    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }
    @FunctionalInterface
    public interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws Exception;
    }
    @FunctionalInterface
    public interface ThrowingTriFunction<T, U, V, R> {
        R apply(T t, U u, V v) throws Exception;
    }
    @FunctionalInterface
    public interface ThrowingQuadFunction<T, U, V, W, R> {
        R apply(T t, U u, V v, W w) throws Exception;
    }
    @FunctionalInterface
    public interface ThrowingPentaFunction<T, U, V, W, X, R> {
        R apply(T t, U u, V v, W w, X x) throws Exception;
    }

    // MEASURE_WORKER_FUNC IMPLEMENTATION:
    // For methods with 0 parameter and no return value:
    public static void measureWorkerFunc(Runnable funcToBeMeasured) {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            funcToBeMeasured.run();
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }
        } else {
            funcToBeMeasured.run();
        }
    }

    // For methods with 1 parameter and no return value:
    public static <T> void measureWorkerFunc(Consumer<T> funcToBeMeasured, T t) {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            funcToBeMeasured.accept(t);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }
        } else {
            funcToBeMeasured.accept(t);
        }
    }

    // For methods with 2 parameters and no return value:
    public static <T, U> void measureWorkerFunc(BiConsumer<T, U> funcToBeMeasured, T t, U u) {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            funcToBeMeasured.accept(t, u);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }
        } else {
            funcToBeMeasured.accept(t, u);
        }
    }

    // For methods with 3 parameters and no return value:
    public static <T, U, V> void measureWorkerFunc(TriConsumer<T, U, V> funcToBeMeasured, T t, U u, V v){
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            funcToBeMeasured.accept(t, u, v);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }
        } else {
            funcToBeMeasured.accept(t, u, v);
        }
    }
    public static <T, U, V, E extends Exception> void measureWorkerFuncWithException(ThrowingTriConsumer<T, U, V, E> funcToBeMeasured, T t, U u, V v) throws E {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            funcToBeMeasured.accept(t, u, v);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }
        } else {
            funcToBeMeasured.accept(t, u, v);
        }
    }

    // For methods with 4 parameters and no return value:
    public static <T, U, V, W> void measureWorkerFunc(QuadConsumer<T, U, V, W> funcToBeMeasured, T t, U u, V v, W w) {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            funcToBeMeasured.accept(t, u, v, w);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }
        } else {
            funcToBeMeasured.accept(t, u, v, w);
        }
    }

    // For methods with 0 parameter and 1 return value:
    public static <R> R measureWorkerFunc(Supplier<R> funcToBeMeasured) {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.get();
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.get();
        }
    }

    // For methods with 1 parameter and 1 return value:
    public static <T, R> R measureWorkerFunc(Function<T, R> funcToBeMeasured, T t) {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.apply(t);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.apply(t);
        }
    }

    // MEASURE_RESOURCE_FUNC IMPLEMENTATION:
    // For methods with 0 parameter and 1 return value:
    public static <R> R measureResourceFunc(ThrowingSupplier<R> funcToBeMeasured) throws Exception {
        if (Environment.MORE_MEASURE_RESOURCE_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.get();
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.get();
        }
    }

    // For methods with 1 parameter and 1 return value:
    public static <T, R> R measureResourceFunc(ThrowingFunction<T, R> funcToBeMeasured, T t) throws Exception {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.apply(t);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.apply(t);
        }
    }

    // For methods with 2 parameters and 1 return value:
    public static <T, U, R> R measureResourceFunc(ThrowingBiFunction<T, U, R> funcToBeMeasured, T t, U u) throws Exception {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.apply(t, u);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.apply(t, u);
        }
    }

    // For methods with 3 parameters and 1 return value:
    public static <T, U, V, R> R measureResourceFunc(ThrowingTriFunction<T, U, V, R> funcToBeMeasured, T t, U u, V v) throws Exception {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.apply(t, u, v);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.apply(t, u, v);
        }
    }

    // For methods with 4 parameters and 1 return value:
    public static <T, U, V, W, R> R measureResourceFunc(ThrowingQuadFunction<T, U, V, W, R> funcToBeMeasured, T t, U u, V v, W w) throws Exception {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.apply(t, u, v, w);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.apply(t, u, v, w);
        }
    }

    // For methods with 4 parameters and 1 return value:
    public static <T, U, V, W, X, R> R measureResourceFunc(ThrowingPentaFunction<T, U, V, W, X, R> funcToBeMeasured, T t, U u, V v, W w, X x) throws Exception {
        if (Environment.MORE_MEASURE_WORKER_LOG == "OPEN") {
            // Measurement
            long startProcessTime = System.currentTimeMillis();
            long startPerfTime = System.nanoTime();
            R result = funcToBeMeasured.apply(t, u, v, w, x);
            long endProcessTime = System.currentTimeMillis();
            long endPerfTime = System.nanoTime();

            long elapsedPerfTime = endPerfTime - startPerfTime;
            long elapsedProcessTime = (long) ((endProcessTime - startProcessTime) / 1e6); // convert nanoseconds to milliseconds
            long elapsedBlockTime = Math.abs(elapsedPerfTime - elapsedProcessTime);

            // Measurement Log
            String funcName = funcToBeMeasured.getClass().getName();

            // Function Stack
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[2].getMethodName();

            // Can optionally set Threshold: Only show blocking overhead large enough to be noticed
            if (elapsedBlockTime >= Environment.MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME * 1e3) {
                LOGGER.info(String.format(
                    "[M-WORKER] : blockTime = %1$s -> %2$s : %3$d seconds",
                    callerName, funcName, elapsedBlockTime));
            }

            return result;
        } else {
            return funcToBeMeasured.apply(t, u, v, w, x);
        }
    }

    //////////////////////////////////////////////////////
    // Response Time Measurement (Process)
    //////////////////////////////////////////////////////
    public static void startProcessTimer() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        startProcess = bean.getCurrentThreadCpuTime();
    }

    public static void startPerfTimer() {
        startPerfCounter = System.nanoTime();
    }

    public static long getProcessTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        endProcess = bean.getCurrentThreadCpuTime();
        elapsedProcessTime = (long) ((startProcess - endProcess) / 1e6); // convert nanoseconds to milliseconds
        return elapsedProcessTime;
    }

    public static long getPerfTime() {
        endPerfCounter = System.nanoTime();
        elapsedPerfTime = (long) ((endPerfCounter - startPerfCounter) / 1e6); // convert nanoseconds to milliseconds
        return elapsedPerfTime;
    }

    //////////////////////////////////////////////////////
    // Response Time Measurement (RTT-based Comm.)
    //////////////////////////////////////////////////////
    public static void startCommTimer() {
        startComm = System.nanoTime();
    }

    public static long getCommTime() {
        endComm = System.nanoTime();
        elapsedCommTime = (long) ((endComm - startComm) / 1e6); // convert nanoseconds to milliseconds
        return elapsedCommTime;
    }

    //////////////////////////////////////////////////////
    // Data Size Measurement
    //////////////////////////////////////////////////////
    public static int simpleSizeCalculator(String message) {
        return message.getBytes(StandardCharsets.UTF_8).length;
    }
}
