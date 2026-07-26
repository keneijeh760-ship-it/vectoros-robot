package com.vectoros.robot.runtime.mission;

import com.vectoros.robot.runtime.world.Coordinate;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MissionTest {

    private static final Instant CREATED = Instant.parse("2026-07-26T11:00:00Z");

    @Test
    void missionStartsCreatedWithOrderedSteps() {
        MissionStep first = MissionStep.navigate("a", new Coordinate(1, 0));
        MissionStep second = MissionStep.waitTicks("b", 3);
        Mission mission = new Mission("m-1", List.of(first, second), CREATED);

        assertThat(mission.status()).isEqualTo(MissionStatus.CREATED);
        assertThat(mission.steps()).containsExactly(first, second);
        assertThat(mission.currentStep()).contains(first);
        assertThat(mission.completedAt()).isEmpty();
    }

    @Test
    void lifecycleTransitionsAreValidated() {
        Mission mission = Mission.of("m-2", CREATED, MissionStep.navigate("n", new Coordinate(0, 1)));

        mission.markQueued();
        assertThat(mission.status()).isEqualTo(MissionStatus.QUEUED);

        mission.markRunning();
        assertThat(mission.status()).isEqualTo(MissionStatus.RUNNING);

        mission.advanceStep();
        assertThat(mission.hasMoreSteps()).isFalse();

        mission.markCompleted(CREATED.plusSeconds(1));
        assertThat(mission.status()).isEqualTo(MissionStatus.COMPLETED);
        assertThat(mission.completedAt()).contains(CREATED.plusSeconds(1));
        assertThat(mission.isTerminal()).isTrue();
    }

    @Test
    void cannotCompleteFromCreated() {
        Mission mission = Mission.of("m-3", CREATED, MissionStep.waitTicks("w", 1));

        assertThatThrownBy(() -> mission.markCompleted(CREATED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void navigateAndWaitFactories() {
        MissionStep nav = MissionStep.navigate("n", new Coordinate(2, 3));
        MissionStep wait = MissionStep.waitTicks("w", 5);

        assertThat(nav.type()).isEqualTo(MissionStepType.NAVIGATE);
        assertThat(nav.target()).contains(new Coordinate(2, 3));
        assertThat(wait.type()).isEqualTo(MissionStepType.WAIT);
        assertThat(wait.waitTicks()).isEqualTo(5);
        assertThat(wait.target()).isEmpty();
    }

    @Test
    void stepTypeEnumIncludesFutureExtensionPoints() {
        assertThat(MissionStepType.values()).contains(
                MissionStepType.NAVIGATE,
                MissionStepType.WAIT,
                MissionStepType.PICKUP,
                MissionStepType.DROPOFF,
                MissionStepType.DOCK,
                MissionStepType.CHARGE);
    }
}
