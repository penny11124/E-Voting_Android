package com.example.urekaapp.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import ureka.framework.Environment;
import ureka.framework.resource.logger.SimpleLogger;

public class BLEManager {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;

    private final MutableLiveData<Boolean> connectionState = new MutableLiveData<>(false);
    private final MutableLiveData<String> receivedData = new MutableLiveData<>();
    private Context context;

    public BLEManager(Context context) {
        this.context = context.getApplicationContext();
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    public void startScan(String targetDeviceName, BLECallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            callback.onDisconnected();
            return;
        }

        bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (ActivityCompat.checkSelfPermission(BLEManager.this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (device.getName() != null && device.getName().equals(targetDeviceName)) {
                    bluetoothAdapter.getBluetoothLeScanner().stopScan(this);
                    connectToDevice(device, callback);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                connectionState.postValue(false);
                callback.onDisconnected();
            }
        });
    }

    public void connectToDevice(BluetoothDevice device, BLECallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            callback.onDisconnected();
            return;
        }

        bluetoothGatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    connectionState.postValue(true);
                    if (ActivityCompat.checkSelfPermission(BLEManager.this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    connectionState.postValue(false);
                    callback.onDisconnected();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    callback.onDisconnected();
                    return;
                }

                BluetoothGattService service = gatt.getService(UUID.fromString(Environment.SERVICE_UUID));
                if (service != null) {
                    writeCharacteristic = service.getCharacteristic(UUID.fromString(Environment.WRITE_CHARACTERISTIC_UUID));
                    notifyCharacteristic = service.getCharacteristic(UUID.fromString(Environment.NOTIFY_CHARACTERISTIC_UUID));

                    if (ActivityCompat.checkSelfPermission(BLEManager.this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    gatt.setCharacteristicNotification(notifyCharacteristic, true);
                    BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(UUID.fromString(Environment.CLIENT_CHARACTERISTIC_CONFIG));
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean success = gatt.writeDescriptor(descriptor);
                        if (!success) {
                            callback.onDisconnected();
                        }
                    }
                    callback.onConnected();
                } else {
                    callback.onDisconnected();
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                try {
                    String data = new String(characteristic.getValue(), StandardCharsets.UTF_8);
                    receivedData.postValue(data);
                    callback.onDataReceived(data);
                } catch (Exception e) {
                    callback.onDataReceived("Invalid Data Received");
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                }
            }
        });
    }

    public void sendData(String json) {
        if (writeCharacteristic != null && isConnected()) {
            writeCharacteristic.setValue(json.getBytes(StandardCharsets.UTF_8));
            if (ActivityCompat.checkSelfPermission(Environment.applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothGatt.writeCharacteristic(writeCharacteristic);
        } else {
            SimpleLogger.simpleLog("Error", "Not Connected");
        }
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            connectionState.postValue(false);
        }
    }

    public boolean isConnected() {
        return connectionState.getValue() != null && connectionState.getValue();
    }

    public LiveData<Boolean> getConnectionState() {
        return connectionState;
    }

    public LiveData<String> getReceivedData() {
        return receivedData;
    }

    public interface BLECallback {
        void onConnected();
        void onDisconnected();
        void onDataReceived(String data);
    }
}