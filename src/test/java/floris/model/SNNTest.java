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
    void testPopulateArrays_dimensionsAndSelfConnections() {
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
    void testPopulateArrays_inhibitoryNeuronWeights() {
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
                "", // imagePath (default empty)
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

        if (inhibitoryNeuronIndex != -1) {
            double[][] initialSynapses = new double[testParams.neurons()][testParams.neurons()];
            double[][] populatedSynapses = snn.populateArrays(initialSynapses);

            // Zijn inhiberende neuronen wel negatief?
            for (int postSynaptic = 0; postSynaptic < testParams.neurons(); postSynaptic++) {
                if (inhibitoryNeuronIndex != postSynaptic) { // Met zichzelf verbonden neuronen.
                    double weight = populatedSynapses[inhibitoryNeuronIndex][postSynaptic];
                    assertTrue(weight <= 0.0, "Weight from inhibitory neuron should be non-positive");
                }
            }
        } else {
            System.out.println("Warning: No inhibitory neuron found for testing. Skipping inhibitory neuron weight check.");
        }
    }
}