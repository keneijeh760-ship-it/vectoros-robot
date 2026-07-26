package com.vectoros.robot.mqtt;

@FunctionalInterface
public interface MqttMessageListener {

    void onMessage(String topic, byte[] payload);
}
