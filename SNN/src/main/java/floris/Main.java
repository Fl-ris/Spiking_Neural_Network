package floris;
import floris.visualizer.*;
import model.SNN;

import floris.io.ImportedSynapseMatrix;
import floris.io.NetworkParameters;

public class Main {

    public void runNetwork(ImportedSynapseMatrix params){
        System.out.println("Starting the simulation...");
        System.out.println(params.neurons() + " neurons used.");
        // Maak nieuwe instantie van SNN()

        SNN network = new SNN(params);
        // Vul synapse array:
        network.populateArrays(network.synapses);

        // Visualisatie:
        NetworkHeatmap heatmap1 = new NetworkHeatmap();
        heatmap1.initialize(network);


        // Test: Spike de input neuronen automatisch:
        for (int t = 0; t < network.externalCurrent.length; t++) {
            //   if (t % 10 == 0) {
            for (int i = 0; i < network.inputNeurons; i++){
                network.externalCurrent[t][i] = 5;

                //   }
            }
        }


        for (int i = 0; i < network.simSteps; i++) {
            // Volgende tijdsstap...
            network.step(i);

            // Visualizatie
            heatmap1.update(network.spikes[i], i, network.vHistory);
            heatmap1.addDelay(30);

        }
    }

    }


