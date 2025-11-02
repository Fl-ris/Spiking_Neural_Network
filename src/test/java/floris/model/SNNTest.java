package floris.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class SNNTest {
    static class TestImportedSynapseMatrix implements floris.model.ImportedSynapseMatrix {
        int neurons;
        int inputNeurons;
        int outputNeurons;
        int inhibitoryNeurons;
        float dt;
        int simulationTime;
        boolean enableSTDP;
        boolean enableLateralInhibition;
        String configFilePath;
        String imagePath;
        double maxFiringRateHz;

        public TestImportedSynapseMatrix(int neurons, int inputNeurons, int outputNeurons, int inhibitoryNeurons, float dt, int simulationTime, boolean enableSTDP, boolean enableLateralInhibition, String configFilePath, String imagePath, double maxFiringRateHz) {
            this.neurons = neurons;
            this.inputNeurons = inputNeurons;
            this.outputNeurons = outputNeurons;
            this.inhibitoryNeurons = inhibitoryNeurons;
            this.dt = dt;
            this.simulationTime = simulationTime;
            this.enableSTDP = enableSTDP;
            this.enableLateralInhibition = enableLateralInhibition;
            this.configFilePath = configFilePath;
            this.imagePath = imagePath;
            this.maxFiringRateHz = maxFiringRateHz;
        }

        @Override
        public int neurons() { return neurons; }
        @Override
        public int inputNeurons() { return inputNeurons; }
        @Override
        public int outputNeurons() { return outputNeurons; }
        @Override
        public int inhibitoryNeurons() { return inhibitoryNeurons; }
        @Override
        public float dt() { return dt; }
        @Override
        public int simulationTime() { return simulationTime; }
        @Override
        public boolean enableSTDP() { return enableSTDP; }
        @Override
        public boolean enableLateralInhibition() { return enableLateralInhibition; }
        @Override
        public String configFilePath() { return configFilePath; }
        @Override
        public String imagePath() { return imagePath; }
        @Override
        public double maxFiringRateHz() { return maxFiringRateHz; }
        @Override
        public boolean writeSpikeOutputCsv() { return false; }
    }

    private SNN snn;
    private TestImportedSynapseMatrix testParams;

    @BeforeEach
    void setUp() {
        testParams = new TestImportedSynapseMatrix(
                10, // neurons
                2,  // inputNeurons
                2,  // outputNeurons
                1,  // inhibitoryNeurons
                0.1f, // dt
                100, // simulationTime
                false, // enableSTDP
                true,   // enableLateralInhibition
                "test/path/config.conf", // configFilePath
                "", // imagePath
                0.0   // maxFiringRateHz
        );
        snn = new SNN(testParams);
    }

    @Test
    void testPopulateArraysdimensionsAndSelfConnections() {
        double[][] initialSynapses = new double[testParams.neurons()][testParams.neurons()];
        double[][] populatedSynapses = snn.populateArrays(initialSynapses);

        assertNotNull(populatedSynapses, "Populated synapses array should not be null");
        assertEquals(testParams.neurons(), populatedSynapses.length, "Row dimension should match number of neurons");
        assertEquals(testParams.neurons(), populatedSynapses[0].length, "Column dimension should match number of neurons");

        for (int i = 0; i < testParams.neurons(); i++) {
            assertEquals(0.0, populatedSynapses[i][i], "Self-connections should be zero");
        }
    }

    @Test
    void testPopulateArraysinhibitoryNeuronWeights() {
        testParams = new TestImportedSynapseMatrix(
                10, // neurons
                2,  // inputNeurons
                2,  // outputNeurons
                1,  // inhibitoryNeurons
                0.1f, // dt (changed to float)
                100, // simulationTime (changed to int)
                false, // enableSTDP
                true,   // enableLateralInhibition
                "test/path/config.conf", // configFilePath
                "", // imagePath
                0.0   // maxFiringRateHz (default 0.0)
        );
        snn = new SNN(testParams);

        int inhibitoryNeuronIndex = -1;
        for (int i = 0; i < testParams.neurons(); i++) {
            if (snn.synapseArray.isInhibitory[i]) {
                inhibitoryNeuronIndex = i;
                break;
            }
        }

            double[][] initialSynapses = new double[testParams.neurons()][testParams.neurons()];
            double[][] populatedSynapses = snn.populateArrays(initialSynapses);

            // Zijn inhiberende neuronen wel negatief?
            for (int postSynaptic = 0; postSynaptic < testParams.neurons(); postSynaptic++) {
                if (inhibitoryNeuronIndex != postSynaptic) { // Met zichzelf verbonden neuronen.
                    double weight = populatedSynapses[inhibitoryNeuronIndex][postSynaptic];
                    assertTrue(weight <= 0.0, "Weight from inhibitory neuron should be non-positive");
                }
            }
    }

    @Test
    void testSpikeDetectorFiresWhenAboveThreshold() {
        int neuronIndex = 0;
        // Voltage boven threshold:
        snn.lifNeuronArray.voltage[neuronIndex] = -49;
        snn.lifNeuronArray.voltageThreshold[neuronIndex] = -50;

        snn.step(0);

        assertTrue(snn.lifNeuronArray.spikes[0][neuronIndex], "Neuron should have fired");
        // Is het voltage gereset?
        assertEquals(-65.0, snn.lifNeuronArray.voltage[neuronIndex], "Voltage should reset to resting potential after firing");
    }

    @Test
    void testSpikeDetectorDoesNotFireWhenBelowThreshold() {
        int neuronIndex = 0;
        // Zet het voltage onder de threshold:
        snn.lifNeuronArray.voltage[neuronIndex] = -51;
        snn.lifNeuronArray.voltageThreshold[neuronIndex] = -50;

        snn.step(0);

        assertFalse(snn.lifNeuronArray.spikes[0][neuronIndex], "Neuron should not have fired");
    }

    @Test
    void testUpdateThresholdsDecaysTowardBase() {
        int neuronIndex = 0;
        double initialThreshold = -40;
        snn.lifNeuronArray.voltageThreshold[neuronIndex] = initialThreshold;

        // Rust threshold is -50, decay is 0.98
        double expectedThreshold = -50 + (initialThreshold - -50) * 0.98;

        snn.step(0);

        assertEquals(expectedThreshold, snn.lifNeuronArray.voltageThreshold[neuronIndex], 0.001, "Threshold should decay towards the base value");
    }

    @Test
    void testLIFNeuronVoltageIntegratesInput() {
        int neuronIndex = 2; // Geen input neuron.
        snn.lifNeuronArray.voltage[neuronIndex] = -65; // Rust potentiaal
        snn.simulationParameters.input[neuronIndex] = 1.0;

        // Verwachte voltage van LIF neuron:
        // deltaVoltage = ((-(voltage - rest) + R * I) / leak) * dt
        // deltaVoltage = ((-(-65 - -65) + 10 * 1.0) / 10) * 0.1 = ((0 + 10) / 10) * 0.1 = 1 * 0.1 = 0.1
        // new voltage = old voltage + deltaVoltage = -65 + 0.1 = -64.9
        double expectedVoltage = -64.9;

        snn.step(0);

        assertEquals(expectedVoltage, snn.lifNeuronArray.voltage[neuronIndex], 0.001, "Wrong voltage...");
    }
}