package floris;
import floris.visualizer.NetworkHeatmap;
import floris.model.SNN;

import floris.io.ImportedSynapseMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    public void runNetwork(ImportedSynapseMatrix params){
    Logger LOGGER = LogManager.getLogger();

        LOGGER.info("Starting the simulation...");
        LOGGER.info(params.neurons() + " neurons used.");
        // Maak nieuwe instantie van SNN()

        SNN network = new SNN(params);
        // Vul synapse array:
        network.populateArrays(network.lifNeuronArray.synapses);

        // Visualisatie:
        NetworkHeatmap heatmap1 = new NetworkHeatmap();
        heatmap1.initialize(network);


        stimulateInputNeurons(network);


        stepNetwork(network, heatmap1);
        analyzeOutputNeurons(network);
    }

    /**
     * Ga een tijdstap verder en maar heatmap visualisatie:
     * @param network
     * @param heatmap1
     */
    private static void stepNetwork(SNN network, NetworkHeatmap heatmap1) {
        for (int i = 0; i < network.simulationParameters.simSteps; i++) {
            // Volgende tijdsstap...
            network.step(i);

            // Visualisatie
            heatmap1.update(network.lifNeuronArray.spikes[i]);
            heatmap1.addDelay(3);

        }
    }

    /**
     * Stimuleer de input neuronen
     * @param network
     */
    private static void stimulateInputNeurons(SNN network) {
        for (int t = 0; t < network.lifNeuronArray.externalCurrent.length; t++) {
            if (t % 10 == 0) { // Modulo 10 om elke 10e iteratie de input neuronen te stimuleren.
            for (int i = 0; i < network.synapseArray.inputNeurons; i++){
                network.lifNeuronArray.externalCurrent[t][i] = 5;

                   }
            }
        }
    }

    private void analyzeOutputNeurons(SNN network) {
        Logger LOGGER = LogManager.getLogger();
        LOGGER.info("--- Analyzing Output Neuron Activity ---");

        int totalOutputNeurons = network.synapseArray.outputNeurons;
        if (totalOutputNeurons == 0) {
            LOGGER.warn("No output neurons to analyze.");
            return;
        }

        int[] spikeCounts = new int[network.simulationParameters.neurons];
        int maxSpikes = -1;
        int winningNeuron = -1;

        // Iterate through each neuron to find the output neurons
        for (int neuronIndex = 0; neuronIndex < network.simulationParameters.neurons; neuronIndex++) {
            // Check if the current neuron is an output neuron
            if (network.synapseArray.isOutput[neuronIndex]) {
                int currentSpikeCount = 0;
                // Iterate through all time steps of the simulation
                for (int timeStep = 0; timeStep < network.simulationParameters.simSteps; timeStep++) {
                    if (network.lifNeuronArray.spikes[timeStep][neuronIndex]) {
                        currentSpikeCount++;
                    }
                }
                spikeCounts[neuronIndex] = currentSpikeCount;
                LOGGER.info("Output Neuron #" + neuronIndex + " fired " + currentSpikeCount + " times.");

                // Check if this neuron is the new "winner"
                if (currentSpikeCount > maxSpikes) {
                    maxSpikes = currentSpikeCount;
                    winningNeuron = neuronIndex;
                }
            }
        }

        if (winningNeuron != -1) {
            LOGGER.info("--- Result ---");
            LOGGER.info("Winning Neuron: #" + winningNeuron + " with " + maxSpikes + " spikes.");
        } else {
            LOGGER.info("--- Result ---");
            LOGGER.info("No output neurons spiked during the simulation.");
        }


}
}


