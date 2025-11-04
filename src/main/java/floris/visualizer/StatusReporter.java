package floris.visualizer;

import floris.model.SNN;

public class StatusReporter {

    public void report(SNN snn, long duration) {
        long outputNeuronSpikes = 0;
        int outputNeuronCount = 0;

        for (int i = 0; i < snn.simulationParameters.neurons; i++) {
            if (snn.synapseArray.isOutput[i]) {
                outputNeuronCount++;
                for (int t = 0; t < snn.simulationParameters.simSteps; t++) {
                    if (snn.lifNeuronArray.spikes[t][i]) {
                        outputNeuronSpikes++;
                    }
                }
            }
        }

        double averageFiringRate = 0;
        if (outputNeuronCount > 0 && snn.simulationParameters.simulationTime > 0) {
            averageFiringRate = (double) outputNeuronSpikes / outputNeuronCount / (snn.simulationParameters.simulationTime / 1000.0);
        }

        System.out.println("\n Simulation Report:");
        System.out.println("Real time duration: " + duration + " ms");
        System.out.println("Simulation time: " + snn.simulationParameters.simulationTime + " ms");
        System.out.println("\n Output neuron statistics:");
        System.out.println("Total spikes (output neurons): " + outputNeuronSpikes);
        System.out.printf("Average firing rate (output neurons): %.2f Hz%n", averageFiringRate);
    }
}
