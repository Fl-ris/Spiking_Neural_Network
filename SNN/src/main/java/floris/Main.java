package floris;
import floris.visualizer.*;
import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {

        SNN network = new SNN();

        network.populateArrays(network.synapses);


        for (int i = 0; i < network.simSteps; i++) {
            for (int j = 0; j < network.neurons; j++) {
                network.LIFneuron(j);
                boolean fire = network.SpikeDetector(j);
                network.spikes[i][j] = fire;

                if (fire) {
                    network.propagateSpike(j);
                }
            }

            network.resetInputs();
        }


        //network.synapses[1][2] = 1;

        // Plot een heatmap van het netwerk.
        heatmap.plot((network.synapses));




    }


}
