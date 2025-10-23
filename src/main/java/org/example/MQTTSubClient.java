package org.example;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTSubClient {

    private MqttClient subscriberClient;

    private final String brokerUrl;
    private final String clientId;

    public MQTTSubClient(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    public void connect(String jwtToken) {
        try {

            subscriberClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(clientId);
            connOpts.setPassword(jwtToken.toCharArray());
            System.out.println("Connecting to broker: " + brokerUrl);
            subscriberClient.connect(connOpts);
            System.out.println("Connected with JWT");
        } catch (MqttException e) {
            System.err.println("Failed to connect to the MQTT broker: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the MQTT broker", e);
        }
    }

    public void refreshConnection(String newJwtToken) {
        try {
            if (subscriberClient != null && subscriberClient.isConnected()) {
                subscriberClient.disconnect();
            }
            System.out.println("Reconnecting with new JWT token...");
            connect(newJwtToken);
        } catch (MqttException e) {
            System.err.println("Failed to reconnect after token refresh: " + e.getMessage());
        }
    }


    public void subscribe(String topic) {
        try {
            if (subscriberClient != null && subscriberClient.isConnected()) {
                subscriberClient.subscribe(topic, (receivedTopic, message) -> {
                    System.out.println("Received message: " + new String(message.getPayload()) + " from topic: " + receivedTopic);
                });
                System.out.println("Subscribed to topic: " + topic);
            } else {
                throw new IllegalStateException("Client is not connected.");
            }
        } catch (MqttException e) {
            System.err.println("Failed to subscribe to topic: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to subscribe to topic", e);
        }
    }
}