# Resource (Bluetooth)
import sys
import bluetooth
import time

# Resource (Logger)
from ureka_framework.resource.logger.simple_logger import simple_log

# Resource (Measurer)
from ureka_framework.resource.logger.simple_measurer import measure_resource_func

########################################################################
# Program-specific Service Info
########################################################################
SERVICE_UUID = "0eb3055f-8ede-4485-961a-edf5bded5c70"  # Generated by uuid4
SERVICE_NAME = "UrekaBluetoothService"

########################################################################
# Reconnect if Discover Fails
########################################################################
RECONNECT_TIMES = 5  # times
RECONNECT_INTERVAL = 3  # sec

########################################################################
# Solve Bluetooth Buffer Size Constraint by Chunk-based Communication
########################################################################
COMM_BUFFER_SIZE = 1024  # byte
MSG_MAX_SIZE = 980  # byte
# COMM_BUFFER_SIZE = 128  # byte
# MSG_MAX_SIZE = 80  # byte
# COMM_BUFFER_SIZE = 64  # byte
# MSG_MAX_SIZE = 20  # byte

SPLIT_SIGN = "|||S|P|L|I|T|||"

# Too high: decrease performance
# Too low: message may not received when doing other processing (multi-thread may help)
WAIT_NEXT_CHUNK = 0.005  # sec
# WAIT_NEXT_CHUNK = 0.05  # sec


class ConnectionSocket:
    def __init__(self, connection_socket):
        ########################################################################
        # Socket
        ########################################################################
        self.connection_socket = connection_socket

    def _byte_backto_str(self, message_byte: bytes) -> str:
        return message_byte.decode("UTF-8")

    def _message_size(self, message: str) -> int:
        return len(message.encode("UTF-8"))

    # @measure_resource_func
    def recv_message(self) -> str:
        received_chunk_strs = []
        bytes_received = 0
        message_length = str(MSG_MAX_SIZE)
        while bytes_received < int(message_length):
            # Can optionally set Timeout
            # self.connection_socket.settimeout(second)

            # Receive
            chunk_with_length: bytes = self.connection_socket.recv(COMM_BUFFER_SIZE)
            chunk_with_length_str: str = self._byte_backto_str(chunk_with_length)
            # simple_log("cli", "")
            # simple_log("cli", f"Received Chunk With Length: {chunk_with_length_str}")

            # Combined Chunks into Message
            message_length = chunk_with_length_str.split(SPLIT_SIGN)[0]
            # simple_log("cli", f"Message Length: {message_length}")
            received_chunk_str = chunk_with_length_str.split(SPLIT_SIGN)[1]
            # simple_log("cli", f"Received Chunk: {received_chunk_str}")
            received_chunk_strs.append(received_chunk_str)
            bytes_received = bytes_received + self._message_size(received_chunk_str)

        # Combined Chunks into Message
        original_message_str = ""
        for received_chunk_str in received_chunk_strs:
            original_message_str += received_chunk_str
        # simple_log("debug", "")
        # simple_log("debug", f"Received Message: {original_message_str}")

        return original_message_str

    @measure_resource_func
    def send_message(self, original_message_str: str):
        # Combined Message
        # simple_log("debug", "")
        # simple_log("debug", f"Sent Message: {original_message_str}")

        # Divide message into Chunks
        message_length = self._message_size(original_message_str)
        # simple_log("debug", f"Message Length: {message_length}")
        sent_chunk_strs = list()
        for i in range(0, len(original_message_str), MSG_MAX_SIZE):
            sent_chunk_str = original_message_str[i : i + MSG_MAX_SIZE]
            sent_chunk_strs.append(sent_chunk_str)

        times = 0
        for sent_chunk_str in sent_chunk_strs:
            if (
                times != 0
            ):  # not resend too fast (may not received if peer cannot parallelizedly receive)
                time.sleep(WAIT_NEXT_CHUNK)
            message_with_length = f"{message_length}{SPLIT_SIGN}{sent_chunk_str}"
            # simple_log("debug", f"Sent Chunk: {sent_chunk_str}")
            # simple_log("debug", f"Sent Chunk With Length: {message_with_length}")

            # Send
            self.connection_socket.send(message_with_length)

            # Resend
            times = times + 1
            # simple_log("debug", f"Resend {times} times.")

    def close(self):
        ########################################################################
        # Connection Socket: Closed
        ########################################################################
        self.connection_socket.close()
        simple_log("cli", "+ Connection is closed.")


class ConnectingWorker:
    def __init__(self, service_uuid, service_name, reconnect_times, reconnect_interval):
        ########################################################################
        # Configure Service Info
        ########################################################################
        self.service_uuid = service_uuid
        self.service_name = service_name

        ########################################################################
        # Generated Connection Socket
        ########################################################################
        self.connection_socket = None
        self.reconnect_times = reconnect_times
        self.reconnect_interval = reconnect_interval

    def connect(self) -> ConnectionSocket:
        ########################################################################
        # Service Discovery Protocol (SDP): Find Device/Service
        #   Optionally add python args in .vscode/launch.json
        #     (in find_service, using specific bluetooth mac address as args can connect faster than using discover_devices)
        ########################################################################
        addr = None
        if len(sys.argv) < 2:
            simple_log(
                "cli",
                f"+ No device specified. Searching for {self.service_name} from all nearby bluetooth devices...",
            )
        else:
            addr = sys.argv[1]
            simple_log(
                "cli", f"+ Searching for {self.service_name} on address {addr}..."
            )

        # Search for the service
        #   Discover devices or search specific address, & only matched uuid will be showed
        #   Reconnect n times every m seconds
        for reconnect_num in range(1, 1 + self.reconnect_times):
            service_matches = bluetooth.find_service(
                uuid=self.service_uuid,
                address=addr,
            )
            if len(service_matches) == 0:
                simple_log(
                    "warning",
                    f"+ Re-connecting {self.service_name} services : {reconnect_num} attempt",
                )
                time.sleep(self.reconnect_interval)
            else:
                break

        if len(service_matches) == 0:
            failure_msg = f"Couldn't find the {self.service_name} service."
            # simple_log("error",failure_msg)
            raise RuntimeError(failure_msg)
        else:
            first_match = service_matches[0]
            port = first_match["port"]
            name = first_match["name"]
            host = first_match["host"]

        ########################################################################
        # Connection Socket: Connect to Device/Service
        ########################################################################
        simple_log(
            "cli", f"+ Connecting to {name} through port {port} on address {host}..."
        )
        self.connection_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self.connection_socket.connect((host, port))
        simple_log("cli", f"+ Connection is generated with {host}.")

        return ConnectionSocket(self.connection_socket)


class AcceptSocket:
    def __init__(self, service_uuid, service_name):
        ########################################################################
        # Configure Service Info
        ########################################################################
        self.service_uuid = service_uuid
        self.service_name = service_name

        ########################################################################
        # Accept Socket & Generated Connection Socket
        ########################################################################
        self.accept_socket = None
        self.connection_socket = None

    def accept(self) -> ConnectionSocket:
        ########################################################################
        # Accept Socket: Open Advertise Service
        ########################################################################
        self.accept_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self.accept_socket.bind(("", bluetooth.PORT_ANY))
        self.accept_socket.listen(1)
        service_port = self.accept_socket.getsockname()[1]

        bluetooth.advertise_service(
            self.accept_socket,
            self.service_name,
            service_id=self.service_uuid,
            service_classes=[self.service_uuid, bluetooth.SERIAL_PORT_CLASS],
            profiles=[bluetooth.SERIAL_PORT_PROFILE],
            # protocols=[bluetooth.OBEX_UUID]
        )

        ########################################################################
        # Connection Socket: Created through Server Socket
        ########################################################################
        # # Can opitionally set Timeout
        # self.accept_socket.settimeout(second)

        simple_log(
            "cli",
            f"+ Waiting for {self.service_name} services: Connection on RFCOMM port {service_port}...",
        )
        self.connection_socket, client_info = self.accept_socket.accept()
        simple_log("cli", f"+ Connection is generated with {client_info}.")

        return ConnectionSocket(self.connection_socket)

    def close(self):
        ########################################################################
        # Accept Socket: Closed
        ########################################################################
        self.accept_socket.close()
        simple_log("cli", "+ Stop accepting new connections.")
