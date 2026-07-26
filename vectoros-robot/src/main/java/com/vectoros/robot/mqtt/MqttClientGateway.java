package com.vectoros.robot.mqtt;

/**
 * Minimal MQTT publish/subscribe gateway so adapters stay independent of Paho details.
 */
public interface MqttClientGateway {

    void publish(String topic, byte[] payload);

    void subscribe(String topic, MqttMessageListener listener);
}
