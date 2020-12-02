package org.openremote.agent.protocol.bluetooth;

import com.welie.blessed.*;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.value.Value;
import org.openremote.model.value.Values;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;

/**
 * Represents a connection to a Bluetooth Low Energy device
 */
public class BluetoothLEConnection {
    /**
     * The logger for the Bluetooth connection.
     */
    private static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, BluetoothLEConnection.class);

    /**
     * The connection status, the default is disconnected
     */
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    /**
     * The connection status consumers of the Bluetooth connection
     */
    private final List<Consumer<ConnectionStatus>> connectionStatusConsumers = new ArrayList<>();

    /**
     * Map of all characteristic states
     */
    private final MultiKeyMap<UUID, byte[]> charValueStateMap = new MultiKeyMap<>();

    /**
     * Map of all characteristic value consumers
     */
    private final MultiKeyMap<UUID, List<CharacteristicValueConsumer>> charValueConsumerMap = new MultiKeyMap<>();

    /**
     * Bluetooth Central
     */
    private final BluetoothCentral central;

    /**
     * Address of bluetooth device to connect to
     */
    private final String btAddress;

    /**
     * The Bluetooth Device
     */
    private BluetoothPeripheral device;

    private final BluetoothPeripheralCallback callbacks = new BluetoothPeripheralCallback() {

        @Override
        public void onServicesDiscovered(BluetoothPeripheral peripheral) {
            onConnection();
        }

        @Override
        public void onCharacteristicUpdate(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, BluetoothCommandStatus status) {
            synchronized (charValueStateMap) {
                BluetoothGattService service = characteristic.getService();

                if (service == null) {
                    return;
                }

                // Update the state map and notify consumers
                charValueStateMap.compute(new MultiKey<>(service.getUuid(), characteristic.getUuid()), (ga, oldValue) -> value);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothPeripheral peripheral, byte[] value, BluetoothGattCharacteristic characteristic, BluetoothCommandStatus status) {
            // TODO: handle
        }
    };

    /**
     * Create new BluetoothConnection
     *
     * @param central   - Bluetooth central instance used for connecting
     * @param btAddress - Bluetooth HW address to connect to
     */
    public BluetoothLEConnection(BluetoothCentral central, String btAddress) {
        this.central = central;
        this.btAddress = btAddress;
    }

    /**
     * Adds a connection status consumer.
     *
     * @param connectionStatusConsumer the connection status consumer.
     */
    public synchronized void addConnectionStatusConsumer(Consumer<ConnectionStatus> connectionStatusConsumer) {
        if (!connectionStatusConsumers.contains(connectionStatusConsumer)) {
            connectionStatusConsumers.add(connectionStatusConsumer);
        }
    }

    /**
     * Disconnect from device
     */
    public synchronized void disconnect() {
        if (connectionStatus != ConnectionStatus.CONNECTED) return;
        LOG.finest("Disconnecting");
        LOG.info(String.format("Disconnecting from %s", btAddress));
        onConnectionStatusChanged(ConnectionStatus.DISCONNECTING);
        device.cancelConnection();
    }

    /**
     * Removes the connection status consumer.
     *
     * @param connectionStatusConsumer the connection status consumer.
     */
    public synchronized void removeConnectionStatusConsumer(java.util.function.Consumer<ConnectionStatus> connectionStatusConsumer) {
        connectionStatusConsumers.remove(connectionStatusConsumer);
    }

    /**
     * Updates the connection status.
     *
     * @param connectionStatus the new connection status.
     */
    private synchronized void onConnectionStatusChanged(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;

        connectionStatusConsumers.forEach(
                consumer -> consumer.accept(connectionStatus)
        );
    }

    /**
     * Called when waiting before connecting
     */
    public synchronized void onWaiting() {
        onConnectionStatusChanged(ConnectionStatus.WAITING);
    }

    /**
     * Called when scanning for device and trying to connect
     */
    public synchronized void onScanning() {
        onConnectionStatusChanged(ConnectionStatus.CONNECTING);
    }

    /**
     * Called when device is successfully connected
     */
    public synchronized void onConnection() {
        onConnectionStatusChanged(ConnectionStatus.CONNECTED);
    }

    /**
     * Called when connection failed
     */
    public synchronized void onConnectionFailed() {
        LOG.info(String.format("Could not connect to %s", btAddress));
        this.device = null;
        onConnectionStatusChanged(ConnectionStatus.ERROR);
    }

    /**
     * Called when device is disconnected
     */
    public synchronized void onDisconnected(BluetoothCommandStatus status) {
        LOG.info(String.format("Disconnected from %s due to %s", btAddress, status.name()));
        this.device = null;

        if (status == BluetoothCommandStatus.COMMAND_SUCCESS) {
            // We disconnected
            onConnectionStatusChanged(ConnectionStatus.DISCONNECTED);
        } else {
            // Device disconnected
            onConnectionStatusChanged(ConnectionStatus.ERROR);
        }
    }

    /**
     * Called when device is found
     *
     * @param device - The found device
     */
    public synchronized void onDeviceFound(BluetoothPeripheral device) {
        // Ignore cases where a device is found multiple times
        if (this.device != null) {
            return;
        }

        LOG.info(String.format("Found %s, connecting", btAddress));
        this.device = device;
        central.connectPeripheral(device, callbacks);
    }

    /**
     * Add a consumer for the specified characteristic.
     */
    public void addCharacteristicValueConsumer(CharacteristicValueConsumer consumer) {
        synchronized (charValueConsumerMap) {
            List<CharacteristicValueConsumer> charValueConsumers = charValueConsumerMap
                    .computeIfAbsent(new MultiKey<>(consumer.serviceUuid, consumer.charUuid), key -> new ArrayList<>());

            charValueConsumers.add(consumer);

            // Look for existing value for this characteristic
            synchronized (charValueStateMap) {
                charValueStateMap.compute(new MultiKey<>(consumer.serviceUuid, consumer.charUuid), (groupAddress, stateValue) -> {
                    if (stateValue != null) {
                        updateConsumer(stateValue, consumer);
                    } else {
                        // State not available for this group address so request it
                        // TODO: Request & subscribe
//                        getGroupAddressValue(datapoint.getMainAddress(), datapoint.getPriority());
                    }

                    return stateValue;
                });
            }
        }
    }

    /**
     * Remove a consumer for the specified characteristic.
     */
    public void removeCharacteristicValueConsumer(CharacteristicValueConsumer consumer) {
        synchronized (charValueConsumerMap) {
            charValueConsumerMap.computeIfPresent(new MultiKey<>(consumer.serviceUuid, consumer.charUuid), (key, consumers) -> {
                if (consumers.removeIf(valueConsumer -> valueConsumer.subscriberId.equals(consumer.subscriberId))) {
                    if (consumers.isEmpty()) {
                        consumers = null;
                    }
                }
                return consumers;
            });
        }
    }

    /**
     * Update a characteristic consumer
     *
     * @param data     - Data to update the consumer with
     * @param consumer - Consumer to update
     */
    protected void updateConsumer(byte[] data, CharacteristicValueConsumer consumer) {
        // Convert to OR Value and notify the consumer
        Value value = null;

        if (data != null) {
            try {
                // TODO: support more data types than string
                value = Values.create(new String(data, StandardCharsets.UTF_8));
            } catch (Exception ex) {
                LOG.warning("Couldn't translate BLE value to OR Value: ");
            }
        }

        consumer.send(value);
    }
}