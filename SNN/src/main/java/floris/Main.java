package floris;
import floris.visualizer.*;
import floris.visualizer.heatmap;
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        SNN network = new SNN();
        network.populateArrays(network.synapses);

        // Visualisatie:
        NetworkHeatmap heatmap1 = new NetworkHeatmap();
        heatmap1.initialize(network);


        // Test: Spike de input neuronen automatisch:
        for (int t = 0; t < network.externalCurrent.length; t++) {
                if (t % 2 == 0) {
                    network.externalCurrent[t][0] = 15;
                }
                if (t % 3 == 0) {
                    network.externalCurrent[t][1] = 20;
                }
                if (t % 4 == 0) {
                    network.externalCurrent[t][2] = 20;
                }
                if (t % 5 == 0) {
                    network.externalCurrent[t][3] = 20;
                }

            }



        for (int i = 0; i < network.simSteps; i++) {

            network.injectCurrent(network.externalCurrent[i]);

            for (int j = 0; j < network.neurons; j++) {
                network.LIFneuron(j);
                boolean fire = network.SpikeDetector(j);
                network.spikes[i][j] = fire;
                network.recordVoltage(i, j);

                if (fire) {
                    network.propagateSpike(j);
                }
            }

            heatmap1.update(network.spikes[i], i, network.vHistory);
            heatmap1.addDelay(30);


            network.resetInputs();
        }

        // Plot een heatmap van het netwerk.
      //     heatmap.plot((network.synapses));

//        new NetworkTopologyVisualizer(network.synapses);
////
//
//        javax.swing.SwingUtilities.invokeLater(() -> {
//            new NetworkVisualizer(network.spikes, network.vHistory, network.synapses);
//        });
    }

}
