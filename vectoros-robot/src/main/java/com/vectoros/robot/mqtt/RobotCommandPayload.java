package com.vectoros.robot.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Wire format for inbound MQTT command payloads.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class RobotCommandPayload {

    private String type;
    private String missionId;
    private List<MissionStepPayload> steps = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public List<MissionStepPayload> getSteps() {
        return steps;
    }

    public void setSteps(List<MissionStepPayload> steps) {
        this.steps = steps == null ? new ArrayList<>() : steps;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class MissionStepPayload {
        private String stepId;
        private String type;
        private Integer x;
        private Integer y;
        private Integer waitTicks;

        public String getStepId() {
            return stepId;
        }

        public void setStepId(String stepId) {
            this.stepId = stepId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        public Integer getWaitTicks() {
            return waitTicks;
        }

        public void setWaitTicks(Integer waitTicks) {
            this.waitTicks = waitTicks;
        }
    }
}
