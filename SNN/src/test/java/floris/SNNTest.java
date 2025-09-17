package floris;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SNNTest {
    /**
     * Test van de SpikeDector.
     * Input v onder -50 is, return false
     * input v boven of gelijk aan -50, return true.
     */

    SNN snn = new SNN();

    @Test
    void SpikeDetector_belowThreshold() {

        double test1 = -40;
        assertTrue(snn.SpikeDetector(test1));

    }
        @Test
        void SpikeDetector_borderline() {

            double test1 = -50;
            assertTrue(snn.SpikeDetector(test1));

    }

    @Test
    void SpikeDetector_aboveThreshold() {

        double test1 = -60;
        assertFalse(snn.SpikeDetector(test1));

    }





}