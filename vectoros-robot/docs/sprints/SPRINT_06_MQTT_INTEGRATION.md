# Sprint 06 – MQTT Integration

Version: 1.0

Status: Ready for Implementation

Estimated Duration: 1 Day

---

# Sprint Goal

Integrate the Robot Runtime with the Fleet Platform using MQTT.

MQTT must be treated purely as a transport layer.

Business logic must remain independent of MQTT.

Communication must occur only through messaging interfaces.

---

# Business Context

The Fleet Platform assigns work to robots.

The Robot Runtime executes work.

MQTT provides asynchronous communication between the two systems.

The runtime must never depend directly on MQTT libraries.

---

# Responsibilities

Publish robot events.

Receive robot commands.

Translate MQTT messages into domain commands.

Translate domain events into MQTT messages.

---

# Out of Scope

Fleet scheduling

REST

Dashboard

Database

Charging

Obstacle avoidance

Multi-robot coordination

---

# Architecture

MissionManager

↓

RobotEventPublisher (Interface)

↓

MqttRobotEventPublisher

↓

MQTT Broker

↓

MqttRobotCommandReceiver

↓

RobotCommandReceiver (Interface)

↓

MissionManager

---

# Components

## RobotEventPublisher

Interface.

Publishes runtime events.

Examples

Mission started

Mission completed

Battery updated

Robot status changed

Position changed

---

## RobotCommandReceiver

Interface.

Receives commands from external systems.

Examples

Assign mission

Cancel mission

Future

Pause

Resume

Emergency stop

---

## MqttRobotEventPublisher

MQTT implementation of RobotEventPublisher.

Responsible only for serialization and publishing.

No business logic.

---

## MqttRobotCommandReceiver

MQTT subscriber.

Responsible only for deserialization and forwarding.

No business logic.

---

# MQTT Topics

robot/{robotId}/events/status

robot/{robotId}/events/mission

robot/{robotId}/events/battery

robot/{robotId}/events/position

robot/{robotId}/commands

---

# Acceptance Criteria

Messaging interfaces implemented.

MQTT adapters implemented.

MissionManager remains MQTT-independent.

Runtime can publish events.

Runtime can receive commands.

All tests pass.

---

# AI Coding Assistant Instructions

Implement only Sprint 06.

Keep MQTT isolated behind interfaces.

No business logic inside MQTT adapters.

Design for future Kafka, RabbitMQ or ROS2 adapters.