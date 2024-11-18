package ureka.framework;

import android.content.Context;

public class Environment {
    //////////////////////////////////////////////////////
    // Deployment Environment
    //////////////////////////////////////////////////////
    // "TEST": Test Mode (e.g., logging, etc.)
    // "PRODUCTION": Production Mode (e.g., print, etc.)
    public static String DEPLOYMENT_ENV = "TEST";

    //////////////////////////////////////////////////////
    // Communication Channel
    //////////////////////////////////////////////////////
    // "SIMULATED": Exchange message through shared memory
    // "BLUETOOTH": Exchange message through bluetooth communication
    public static String COMMUNICATION_CHANNEL = "SIMULATED";

    // "TEST": No Delay (complete tests faster)
    // "PRODUCTION": With Delay (make local simulation interactive)
    public static final double SIMULATED_COMM_INTERRUPT_CYCLE_TIME = 0.01;
    public static final int SIMULATED_COMM_DELAY_COUNT = 3;
    // public static final double SIMULATED_COMM_DELAY_DURATION = 0.3;
    public static final double SIMULATED_COMM_DELAY_DURATION = 0;

    // "TEST": No Time Out (Pytest terminates all daemon threads when main thread is finished)
    // "PRODUCTION": Terminates the worker threads through Timeout
    //               and make sure TIME_OUT must be larger than DELAY_COUNT * DELAY_DURATION + Process Time in worker thread
    public static final double SIMULATED_COMM_TIME_OUT = 2;

    //////////////////////////////////////////////////////
    // Log
    //////////////////////////////////////////////////////
    // "OPEN": Print Log
    // "CLOSED": Not Print Log
    public static String DEBUG_LOG = "OPEN";
    public static String CLI_LOG = "OPEN";
    public static String MEASURE_LOG = "OPEN";
    public static String MORE_MEASURE_WORKER_LOG = "CLOSED";
    public static String MORE_MEASURE_RESOURCE_LOG = "CLOSED";

    //////////////////////////////////////////////////////
    // Measurement
    //////////////////////////////////////////////////////
    public static final double IO_BLOCKING_TOLERANCE_TIME = 0.015;
    public static final double MORE_MEASUREMENT_BLOCKING_THRESHOLD_TIME = 0.001;
    public static final double MORE_MEASUREMENT_PERF_THRESHOLD_TIME = 0.015;
    public static final double COMM_BLOCKING_TOLERANCE_TIME = 0.070;
    public static final String MEASUREMENT_TIME_PRECISION = ".3f";
    public static final String MORE_MEASUREMENT_TIME_PRECISION = "6.3f";
    public static final int MEASUREMENT_REPEAT_TIMES = 5;

    //////////////////////////////////////////////////////
    // Transmitted Data in Simulation
    //////////////////////////////////////////////////////
    public static final String SERVICE_UUID = "0000FFE0-0000-1000-8000-00805F9B34FB"; //
    public static final String WRITE_CHARACTERISTIC_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
    public static final String NOTIFY_CHARACTERISTIC_UUID = "0000FFE1-0000-1000-8000-00805F9B34FB";
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805F9B34FB";
//    public static String transmittedMessage;
    public static Context applicationContext;

    public static String connectedEndpointId = null;


    public static void initialize(Context context) {
        applicationContext = context.getApplicationContext();
    }
}