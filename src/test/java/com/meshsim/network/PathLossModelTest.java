package com.meshsim.network;

import com.meshsim.model.Node;
import com.meshsim.model.NodeType;
import com.meshsim.model.Point;
import com.meshsim.model.Scenario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathLossModelTest {

    private Scenario scenario;
    private Node a;
    private Node b;

    @BeforeEach
    void setUp() {
        scenario = new Scenario("test", "test", 200, 200);
        a = Node.withId("A", NodeType.STATIC_RELAY, new Point(0, 0), 1.0);
        b = Node.withId("B", NodeType.STATIC_RELAY, new Point(30, 0), 1.0);
        scenario.addNode(a);
        scenario.addNode(b);
        PathLossModel.setShadowingEnabled(true);
    }

    @AfterEach
    void tearDown() {
        PathLossModel.setShadowingEnabled(false);
    }

    @Test
    void sameSeedProducesIdenticalQuality() {
        PathLossModel.setSeed(12345L);
        double q1 = PathLossModel.computeQuality(a, b, scenario);

        PathLossModel.setSeed(12345L);
        double q2 = PathLossModel.computeQuality(a, b, scenario);

        assertEquals(q1, q2, 1e-9, "Same seed must produce identical link quality");
    }

    @Test
    void differentSeedProducesDifferentQuality() {
        PathLossModel.setSeed(12345L);
        double q1 = PathLossModel.computeQuality(a, b, scenario);

        PathLossModel.setSeed(99999L);
        double q2 = PathLossModel.computeQuality(a, b, scenario);

        assertNotEquals(q1, q2, "Different seeds should produce different shadowing samples");
    }
}
