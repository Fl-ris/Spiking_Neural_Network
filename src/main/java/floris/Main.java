package floris;

import floris.visualizer.NetworkHeatmap;
import floris.model.SNN;
import floris.model.ImportedSynapseMatrix;

import floris.visualizer.StatusReporter;
import floris.io.SpikeCsvWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main {
    
    public void runNetwork(ImportedSynapseMatrix params) {
        Logger LOGGER = LogManager.getLogger();

        LOGGER.info("Starting the simulation...");
        LOGGER.info(params.neurons() + " neurons used.");

        SNN network = new SNN(params);
        network.populateArrays(network.lifNeuronArray.synapses);

        NetworkHeatmap heatmap1 = null;
        if (params.enableHeatmap()) {
            heatmap1 = new NetworkHeatmap();
            heatmap1.initialize(network);
        }

        stimulateInputNeurons(network);

        long startTime = System.currentTimeMillis();
        stepNetwork(network, heatmap1, params.enableHeatmap());
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;

        StatusReporter statusReporter = new StatusReporter();
        statusReporter.report(network, duration);

        SpikeCsvWriter csvWriter = new SpikeCsvWriter();
        csvWriter.writeSpikesIfEnabled(network, params);
    }


    /**
     * Ga een tijdstap verder en maar heatmap visualisatie:
     *
     * @param network
     * @param heatmap1
     */
    private static void stepNetwork(SNN network, NetworkHeatmap heatmap1, boolean enableHeatmap) {
        for (int i = 0; i < network.simulationParameters.simSteps; i++) {
            // Volgende tijdsstap...
            network.step(i);

            // Visualisatie
            if (enableHeatmap) {
                heatmap1.update(network.lifNeuronArray.spikes[i], network.lifNeuronArray.voltage);
                heatmap1.addDelay(30);
            }

        }
    }

    /**
     * Stimuleer de input neuronen
     *
     * @param network
     */
    private static void stimulateInputNeurons(SNN network) {
        for (int t = 0; t < network.lifNeuronArray.externalCurrent.length; t++) {
            if (t % 10 == 0) { // Modulo 10 om elke 10e iteratie de input neuronen te stimuleren.
                for (int i = 0; i < network.synapseArray.inputNeurons; i++) {
                    network.lifNeuronArray.externalCurrent[t][i] = 5;

                }
            }
        }
    }

}


