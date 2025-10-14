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

}


