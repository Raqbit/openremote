package org.openremote.agent.protocol.tradfri;

import org.openremote.agent.protocol.tradfri.device.Device;
import org.openremote.agent.protocol.tradfri.device.Gateway;
import org.openremote.agent.protocol.tradfri.device.Light;
import org.openremote.agent.protocol.tradfri.device.Plug;
import org.openremote.agent.protocol.tradfri.util.Credentials;

import org.openremote.agent.protocol.ProtocolExecutorService;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.value.ColorRGB;
import org.openremote.model.value.Values;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.openremote.agent.protocol.tradfri.TradfriLightAsset.convertBrightness;
import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;

/**
 * The class that represents the configuration for the IKEA TRÅDFRI connection.
 */
public class TradfriConnection {

    /**
     * The connection status, the default is disconnected
     */
    protected ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;

    /**
     * The connection status consumers of the IKEA TRÅDFRI connection
     */
    protected final List<Consumer<ConnectionStatus>> connectionStatusConsumers = new ArrayList<>();

    /**
     * The executor service of the IKEA TRÅDFRI connection
     */
    protected final ProtocolExecutorService executorService;

    /**
     * The IP address of the gateway
     */
    protected final String gatewayIp;

    /**
     * The security code that's needed to connect to the gateway.
     */
    protected final String securityCode;

    /**
     * The logger for the IKEA TRÅDFRI connection.
     */
    private static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, TradfriConnection.class);

    /**
     * The IKEA TRÅDFRI gateway.
     */
    private Gateway gateway;

    /**
     * Construct the TradfriConnection class.
     * @param gatewayIp the IP address of the gateway.
     * @param securityCode the security code to connect to the gateway.
     * @param executorService the executor service.
     */
    public TradfriConnection(String gatewayIp, String securityCode, ProtocolExecutorService executorService) {
        this.gatewayIp = gatewayIp;
        this.securityCode = securityCode;
        this.executorService = executorService;
    }

    /**
     * Handles the connection.
     * @return the gateway.
     */
    public synchronized Gateway connect() {
        if (connectionStatus == ConnectionStatus.CONNECTED || connectionStatus == ConnectionStatus.CONNECTING) {
            LOG.finest("Already connected or connection in progress");
        }

        onConnectionStatusChanged(ConnectionStatus.CONNECTING);

        try {
            Gateway gateway = new Gateway(gatewayIp);
            gateway.setTimeout(10000L);
            Credentials credentials = gateway.connect(securityCode);
            if (credentials != null) {
                if (gateway.enableObserve()) {
                    this.gateway = gateway;
                    onConnectionStatusChanged(ConnectionStatus.CONNECTED);
                    return gateway;
                }
            }
        }
        catch (Exception exception) {
            LOG.warning("An exception occurred when connecting to Tradfri gateway: " + exception);
        }
        return null;
    }

    /**
     * Updates the connection status.
     * @param connectionStatus the connection status.
     */
    protected synchronized void onConnectionStatusChanged(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;

        connectionStatusConsumers.forEach(
                consumer -> consumer.accept(connectionStatus)
        );
    }

    /**
     * Adds a connection status consumer.
     * @param connectionStatusConsumer the connection status consumer.
     */
    public synchronized void addConnectionStatusConsumer(Consumer<ConnectionStatus> connectionStatusConsumer) {
        if (!connectionStatusConsumers.contains(connectionStatusConsumer)) {
            connectionStatusConsumers.add(connectionStatusConsumer);
        }
    }

    /**
     * Handles the disconnection.
     */
    public synchronized void disconnect() {
        if (connectionStatus != ConnectionStatus.CONNECTED) return;
        LOG.finest("Disconnecting");
        onConnectionStatusChanged(ConnectionStatus.DISCONNECTING);
        if(gateway.disableObserve()){
            onConnectionStatusChanged(ConnectionStatus.DISCONNECTED);
        }
    }

    /**
     * Removes the connection status consumer.
     * @param connectionStatusConsumer the connection status consumer.
     */
    public synchronized void removeConnectionStatusConsumer(java.util.function.Consumer<ConnectionStatus> connectionStatusConsumer) {
        connectionStatusConsumers.remove(connectionStatusConsumer);
    }

    /**
     * Method to update the values of the device attributes,
     * based on the values entered in the User Interface.
     * @param device the device to which the update applies.
     * @param event the event representing the update.
     */
    public void controlDevice(Device device, AttributeEvent event) {
        try {
            if (this.connectionStatus == ConnectionStatus.CONNECTED && event.getValue().isPresent()) {
                if (device.isLight()){
                    Light light = device.toLight();

                    if (event.getAttributeName().equals(org.openremote.model.asset.impl.Light.BRIGHTNESS.getName())) {
                        int value = Values.getInteger(event.getValue()).orElse(0);
                        light.setBrightness(convertBrightness(value, false));
                    } else if (event.getAttributeName().equals(org.openremote.model.asset.impl.Light.ON_OFF.getName())) {
                        light.setOn(Values.getBooleanCoerced(event.getValue()).orElse(false));
                    } else if (event.getAttributeName().equals(org.openremote.model.asset.impl.Light.COLOR.getName())) {
                        light.setColour(Values.convert(ColorRGB.class, event.getValue()));
                    } else if (event.getAttributeName().equals(org.openremote.model.asset.impl.Light.TEMPERATURE.getName())) {
                        light.setColourTemperature(Values.getInteger(event.getValue()).orElse(0));
                    }
                }
                else if (device.isPlug()) {
                    Plug plug = device.toPlug();
                    if (event.getAttributeName().equals(org.openremote.model.asset.impl.Plug.ON_OFF.getName())) {
                        plug.setOn(Values.getBooleanCoerced(event.getValue()).orElse(false));
                    }
                }
            }
        } catch (Exception exception) {
            LOG.severe(exception.getMessage());
        }
    }
}
