package floris;

import floris.model.SNN;
import org.junit.jupiter.api.Test;

class SNNTest {
    /**
     * Test van de SpikeDector.
     * Input v onder -50 is, return false
     * input v boven of gelijk aan -50, return true.
     */

    public SNN snn = new SNN();


    @Test
    void SpikeDetector_belowThreshold() {

        int test1 = -40;
        assertTrue(snn.SpikeDetector(test1));
    }
        @Test
        void SpikeDetector_borderline() {

            int test1 = -50;
            assertTrue(snn.SpikeDetector(test1));
    }

    @Test
    void SpikeDetector_aboveThreshold() {

        int test1 = -60;
        assertFalse(snn.SpikeDetector(test1));

    }

//    @Test
//    void



}