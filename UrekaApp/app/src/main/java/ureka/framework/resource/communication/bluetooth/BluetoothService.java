package ureka.framework.resource.communication.bluetooth;

public class BluetoothService {
    ////////////////////////////////////////////////////////////////////////
    // Program-specific Service Info
    ////////////////////////////////////////////////////////////////////////
    public static final String SERVICE_UUID = "0eb3055f-8ede-4485-961a-edf5bded5c70"; // Generated by uuid4
    public static final String SERVICE_NAME = "UrekaBluetoothService";

    ////////////////////////////////////////////////////////////////////////
    // Reconnect if Discover Fails
    ////////////////////////////////////////////////////////////////////////
    public static final int RECONNECT_TIMES = 5; // times
    public static final int RECONNECT_INTERVAL = 3; // sec

    ////////////////////////////////////////////////////////////////////////
    // Solve Bluetooth Buffer Size Constraint by Chunk-based Communication
    ////////////////////////////////////////////////////////////////////////
    public static final int COMM_BUFFER_SIZE = 1024; // byte
    public static final int MSG_MAX_SIZE = 980; // byte

    public static final String SPLIT_SIGN = "|||S|P|L|I|T|||";

    // Too high: decrease performance
    // Too low: message may not received when doing other processing (multi-thread may help)
    public static final double WAIT_NEXT_CHUNK = 0.005; // sec

    public class ConnectionSocket {

    }

    public class ConnectingWorker {

    }

    public class AcceptSocket {

    }
}
