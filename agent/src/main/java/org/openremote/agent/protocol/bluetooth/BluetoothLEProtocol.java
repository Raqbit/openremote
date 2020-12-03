package org.openremote.agent.protocol.bluetooth;

import com.welie.blessed.*;
import org.openremote.agent.protocol.AbstractProtocol;
import org.openremote.container.Container;
import org.openremote.model.AbstractValueHolder;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetAttribute;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.*;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.util.Pair;
import org.openremote.model.value.Value;
import org.openremote.model.value.ValueType;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.openremote.container.concurrent.GlobalLock.withLock;
import static org.openremote.model.Constants.PROTOCOL_NAMESPACE;
import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;

/**
 * Bluetooth Low Energy (LE) Protocol
 */
public class BluetoothLEProtocol extends AbstractProtocol {

    private static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, BluetoothLEProtocol.class);

    // Protocol details
    public static final String PROTOCOL_NAME = PROTOCOL_NAMESPACE + ":ble";
    public static final String PROTOCOL_DISPLAY_NAME = "Bluetooth Low Energy";
    public static final String VERSION = "1.0";

    //Protocol specific configuration meta items
    public static final String META_BLE_ADDRESS = PROTOCOL_NAME + ":address";

    //Attribute specific configuration meta items
    public static final String META_BLE_SERVICE_UUID = PROTOCOL_NAME + ":serviceUUID";
    public static final String META_BLE_CHARACTERISTIC_UUID = PROTOCOL_NAME + ":characteristicUUID";

    // Patterns & error messages
    public static final String REGEXP_MAC = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    public static final String REGEXP_UUID = "^([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})$";
    public static final String PATTERN_FAILURE_UUID = "UUIDv4 (e.g. 175d8ebf-aeb6-44af-a41d-4afdab4f2483)";
    public static final String PATTERN_FAILURE_MAC = "Bluetooth HW Address, like a MAC address (e.g. 9A:A3:03:11:D7:7D)";

    protected static final List<MetaItemDescriptor> PROTOCOL_CONFIG_META_ITEM_DESCRIPTORS = Collections.singletonList(
            new MetaItemDescriptorImpl(META_BLE_ADDRESS, ValueType.STRING, true, REGEXP_MAC, PATTERN_FAILURE_MAC, 1, null, false, null, null, null)
    );

    protected static final List<MetaItemDescriptor> ATTRIBUTE_META_ITEM_DESCRIPTORS = Arrays.asList(
            new MetaItemDescriptorImpl(META_BLE_SERVICE_UUID, ValueType.STRING, true, REGEXP_UUID, PATTERN_FAILURE_UUID, 1, null, false, null, null, null),
            new MetaItemDescriptorImpl(META_BLE_CHARACTERISTIC_UUID, ValueType.STRING, true, REGEXP_UUID, PATTERN_FAILURE_UUID, 1, null, false, null, null, null)
    );

    /**
     * Time (in ms) to wait before scanning after a disconnect
     */
    protected final static int WAIT_FOR_SCAN_DELAY = 1000;

    /**
     * Default value type when attribute does not have type to convert to
     */
    private static final ValueType DEFAULT_VALUE_TYPE = ValueType.ARRAY;

    /**
     * Tasks for when waiting before scanning again
     */
    protected final Map<String, ScheduledFuture<?>> scanTasks = new HashMap<>();

    /**
     * Stores connections to bluetooth devices
     */
    protected final Map<String, BluetoothLEConnection> bluetoothConnections = new HashMap<>();

    /**
     * Stores addresses we're scanning for
     */
    protected final Set<String> scanningAddresses = new HashSet<>();

    /**
     * Stores reference to connection status consumers
     */
    protected final Map<AttributeRef, Consumer<ConnectionStatus>> statusConsumerMap = new HashMap<>();

    /**
     * Stores the link between attributes and their respective characteristic value consumers
     */
    protected final Map<AttributeRef, Pair<BluetoothLEConnection, CharacteristicValueConsumer>> attributeLinkMap = new HashMap<>();

    /**
     * Central bluetooth manager
     */
    private BluetoothCentral central;

    /**
     * Bluetooth central callbacks
     */
    private final BluetoothCentralCallback bluetoothCallbacks = new BluetoothCentralCallback() {

        @Override
        public void onDiscoveredPeripheral(BluetoothPeripheral peripheral, ScanResult scanResult) {
            String address = peripheral.getAddress();

            // Stop scanning for device
            removeDeviceFromScanning(address);

            synchronized (bluetoothConnections) {
                if (bluetoothConnections.containsKey(address)) {
                    BluetoothLEConnection connection = bluetoothConnections.get(address);
                    connection.onDeviceFound(peripheral);
                }
            }
        }

        @Override
        public void onConnectedPeripheral(BluetoothPeripheral peripheral) {
            String address = peripheral.getAddress();

            synchronized (bluetoothConnections) {
                if (bluetoothConnections.containsKey(address)) {
                    BluetoothLEConnection connection = bluetoothConnections.get(address);
                    connection.onConnection();
                }
            }
        }

        @Override
        public void onConnectionFailed(BluetoothPeripheral peripheral, BluetoothCommandStatus status) {
            String address = peripheral.getAddress();

            synchronized (bluetoothConnections) {
                if (bluetoothConnections.containsKey(address)) {
                    BluetoothLEConnection connection = bluetoothConnections.get(address);
                    connection.onConnectionFailed();

                    // Schedule scanning
                    scheduleDeviceForScanning(address, connection);
                }
            }
        }

        @Override
        public void onDisconnectedPeripheral(BluetoothPeripheral peripheral, BluetoothCommandStatus status) {
            String address = peripheral.getAddress();

            synchronized (bluetoothConnections) {
                if (bluetoothConnections.containsKey(address)) {
                    BluetoothLEConnection connection = bluetoothConnections.get(address);
                    connection.onDisconnected(status);

                    // Reschedule scanning if disconnect was not initiated by us
                    if (status != BluetoothCommandStatus.COMMAND_SUCCESS) {
                        scheduleDeviceForScanning(address, connection);
                    }
                }
            }
        }
    };

    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    @Override
    public String getProtocolDisplayName() {
        return PROTOCOL_DISPLAY_NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public AssetAttribute getProtocolConfigurationTemplate() {
        return super.getProtocolConfigurationTemplate()
                .addMeta(
                        new MetaItem(META_BLE_ADDRESS, null)
                );
    }

    @Override
    protected List<MetaItemDescriptor> getProtocolConfigurationMetaItemDescriptors() {
        return PROTOCOL_CONFIG_META_ITEM_DESCRIPTORS;
    }

    @Override
    protected List<MetaItemDescriptor> getLinkedAttributeMetaItemDescriptors() {
        return ATTRIBUTE_META_ITEM_DESCRIPTORS;
    }

    @Override
    public void init(Container container) throws Exception {
        super.init(container);
        central = new BluetoothCentral(bluetoothCallbacks);
    }

    @Override
    protected void doLinkProtocolConfiguration(Asset agent, AssetAttribute protocolConfiguration) {
        // TODO: validate btAddress is set in here or in the validation callback
        String btAddressParam = protocolConfiguration.getMetaItem(META_BLE_ADDRESS).flatMap(AbstractValueHolder::getValueAsString).orElse(null);

        AttributeRef protocolRef = protocolConfiguration.getReferenceOrThrow();

        synchronized (bluetoothConnections) {
            if (bluetoothConnections.containsKey(btAddressParam)) {
                LOG.severe("Device with given ID already has a connection");
                updateStatus(protocolRef, ConnectionStatus.ERROR_CONFIGURATION);
                return;
            }

            updateStatus(protocolRef, ConnectionStatus.CONNECTING);

            // Todo: possibly wrap in protocol lock?
            Consumer<ConnectionStatus> statusConsumer = status -> updateStatus(protocolRef, status);

            BluetoothLEConnection bleConnection = bluetoothConnections.computeIfAbsent(btAddressParam, btAddress ->
                    new BluetoothLEConnection(central, btAddress));

            bleConnection.addConnectionStatusConsumer(statusConsumer);

            addDeviceForScanning(btAddressParam);

            synchronized (statusConsumerMap) {
                statusConsumerMap.put(protocolRef, statusConsumer);
            }
        }
    }

    private void updateDeviceScanning() {
        synchronized (scanningAddresses) {
            if (!scanningAddresses.isEmpty()) {
                central.scanForPeripheralsWithAddresses(scanningAddresses.toArray(new String[0]));
            } else {
                central.stopScan();
            }
        }
    }

    private void addDeviceForScanning(String btAddress) {
        LOG.info(String.format("Starting scans for %s", btAddress));

        synchronized (scanningAddresses) {
            scanningAddresses.add(btAddress);
        }
        updateDeviceScanning();
    }

    private void removeDeviceFromScanning(String btAddress) {
        LOG.info(String.format("Stopping scans for %s", btAddress));
        synchronized (scanningAddresses) {
            scanningAddresses.remove(btAddress);
        }
        updateDeviceScanning();
    }

    private void scheduleDeviceForScanning(String btAddress, BluetoothLEConnection connection) {
        synchronized (scanningAddresses) {
            // We're already scanning for this device
            if (scanningAddresses.contains(btAddress)) {
                return;
            }

            synchronized (scanTasks) {
                // We've already scheduled to scan for this device
                if (scanTasks.containsKey(btAddress)) {
                    return;
                }

                LOG.info(String.format("Scheduling scans for %s", btAddress));

                connection.onWaiting();

                scanTasks.put(btAddress, executorService.schedule(() -> {
                    scanTasks.remove(btAddress);
                    connection.onScanning();
                    addDeviceForScanning(btAddress);
                }, WAIT_FOR_SCAN_DELAY));
            }
        }
    }

    private BluetoothLEConnection getConnection(String btAddress) {
        synchronized (bluetoothConnections) {
            return bluetoothConnections.get(btAddress);
        }
    }

    @Override
    protected void doUnlinkProtocolConfiguration(Asset agent, AssetAttribute protocolConfiguration) {
        Consumer<ConnectionStatus> statusConsumer;

        synchronized (statusConsumerMap) {
            statusConsumer = statusConsumerMap.get(protocolConfiguration.getReferenceOrThrow());
        }

        String btAddress = protocolConfiguration.getMetaItem(META_BLE_ADDRESS).flatMap(AbstractValueHolder::getValueAsString).orElse(null);

        synchronized (bluetoothConnections) {
            BluetoothLEConnection btConnection = bluetoothConnections.get(btAddress);

            if (btConnection != null) {
                removeDeviceFromScanning(btAddress);
                btConnection.removeConnectionStatusConsumer(statusConsumer);
                btConnection.disconnect();
                bluetoothConnections.remove(btAddress);
            }
        }

    }

    @Override
    protected void doLinkAttribute(AssetAttribute attribute, AssetAttribute protocolConfiguration) throws Exception {
        final AttributeRef attributeRef = attribute.getReferenceOrThrow();

        String btAddress = protocolConfiguration.getMetaItem(META_BLE_ADDRESS).flatMap(AbstractValueHolder::getValueAsString).orElse(null);
        Optional<String> serviceUuid = attribute.getMetaItem(META_BLE_SERVICE_UUID).flatMap(AbstractValueHolder::getValueAsString);
        Optional<String> charUuid = attribute.getMetaItem(META_BLE_CHARACTERISTIC_UUID).flatMap(AbstractValueHolder::getValueAsString);

        if (!serviceUuid.isPresent()) {
            LOG.severe("No META_BLE_SERVICE_UUID for protocol attribute: " + attributeRef);
            return;
        }

        if (!charUuid.isPresent()) {
            LOG.severe("No META_BLE_CHARACTERISTIC_UUID for protocol attribute: " + attributeRef);
            return;
        }

        BluetoothLEConnection bleConnection = getConnection(btAddress);

        if (bleConnection == null) {
            LOG.fine("Attribute being linked to configuration without connection");
            // The protocol configuration is disabled
            return;
        }

        // TODO: support various attribute types
//        ValueType type;
//
//        if (attribute.getType().isPresent()) {
//            type = attribute.getType().get().getValueType();
//        } else {
//            type = DEFAULT_VALUE_TYPE;
//        }

        synchronized (attributeLinkMap) {
            CharacteristicValueConsumer consumer = new CharacteristicValueConsumer(
                    attributeRef.getEntityId(),
                    UUID.fromString(serviceUuid.get()),
                    UUID.fromString(charUuid.get()),
                    (value) -> handleBLEValueChange(attributeRef, value)
            );

            // TODO: being called before we have an actual connection
            bleConnection.addCharacteristicValueConsumer(consumer);

            attributeLinkMap.put(attributeRef, new Pair<>(bleConnection, consumer));
            LOG.info("Attribute registered for status updates: " + attributeRef + " with consumer: " + consumer.subscriberId);
        }
    }

    private void handleBLEValueChange(AttributeRef attributeRef, Value value) {
        // Get protocol lock
        withLock(getProtocolName(), () -> {
            LOG.fine("BLE protocol received value '" + value + "' for : " + attributeRef);
            updateLinkedAttribute(new AttributeState(attributeRef, value));
        });
    }

    @Override
    protected void doUnlinkAttribute(AssetAttribute attribute, AssetAttribute protocolConfiguration) {
        final AttributeRef attributeRef = attribute.getReferenceOrThrow();

        synchronized (attributeLinkMap) {
            Pair<BluetoothLEConnection, CharacteristicValueConsumer> link = attributeLinkMap.remove(attributeRef);
            if (link != null) {
                link.key.removeCharacteristicValueConsumer(link.value);
            }
        }
    }

    @Override
    protected void processLinkedAttributeWrite(AttributeEvent event, Value processedValue, AssetAttribute protocolConfiguration) {
        if (!protocolConfiguration.isEnabled()) {
            LOG.fine("Protocol configuration is disabled so ignoring write request");
            return;
        }

        synchronized (attributeLinkMap) {
            Pair<BluetoothLEConnection, CharacteristicValueConsumer> link = attributeLinkMap.get(event.getAttributeRef());

            if (link == null) {
                LOG.fine("Attribute isn't linked to a BLE consumer so cannot process write: " + event);
                return;
            }

            Optional<Value> value = event.getValue();

            if (!value.isPresent()) {
                LOG.fine("Attribute Event has no value: " + event);
                return;
            }

            link.key.writeCharacteristic(link.value.serviceUuid, link.value.charUuid, event.getValue().get());

            updateLinkedAttribute(event.getAttributeState());
        }
    }



}
