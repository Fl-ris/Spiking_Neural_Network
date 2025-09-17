package floris;

import java.util.Arrays;
import org.knowm.xchart.*;


public class SNN {
    // LIF neuron parameters:
    private byte potentialThreshold = -50;
    private byte restMembranePotential = -65;
    private byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.
    private byte initialMembranePotential = -60;
    private byte membraneLeak = 10; // 10 ms

    private double[] dv;
    private double[] v;

    // Simulatie parameters
    public float dt = 0.1F;
    public int simulationTime = 10;
    public float simSteps = simulationTime / dt;
    private double input[];
    public int neurons = 10;

    boolean[][] spikes; // Boolean array van spikes (true/false) per neuron per tijdstap
    double[][] synapses; // Array met de sterkte van verbindingen tussen alle neuronen


    public SNN() {
        synapses = new double[neurons][neurons];
        spikes = new boolean[(int) simSteps][neurons];
        v = new double[neurons];
        dv = new double[neurons];
        input = new double[neurons];

        for (int i = 0; i < neurons; i++) {
            v[i] = initialMembranePotential;
            input[i] = 1;
        }
    }


    public static void main(String[] args) {


    }


    public void LIFneuron(int index) {
        dv[index] = ((-(v[index] - restMembranePotential) + membraneResistance * input[index])
                / membraneLeak) * dt;

        v[index] = dv[index] + v[index];
    }

    public void resetInputs() {
        for (int i = 0; i < neurons ; i++) {
            input[1] = 0;
        }
    }

//    public static void plotter(double[][] data) {
//        /**
//         * Plot het verloop van het membraan potentiaal over tijd.
//         * @param xData
//         * @param yData
//         *
//         */
//        new SwingWrapper<>(QuickChart.getChart("Plot", "x", "y", "data",
//                Arrays.stream(data).mapToDouble(p -> p[0]).toArray(),
//                Arrays.stream(data).mapToDouble(p -> p[1]).toArray()
//        )).displayChart();
//    }


    public boolean SpikeDetector(int index) {
        /**
         * Return boolean voor elke tijdstap of de neuron gevuurd heeft.
         * @param v Membraan potentiaal
         * @return boolean
         */
        if (v[index] >= potentialThreshold) {
            v[index] = restMembranePotential;
            return true;
        }
        return false;
    }


    public void propagateSpike(int preSynapticNeuron) {

        for (int postSynapticNeuron = 0; postSynapticNeuron < neurons; postSynapticNeuron++) {

            if (preSynapticNeuron != postSynapticNeuron) { // Mag niet met zichzelf verbinden...
                input[postSynapticNeuron] += synapses[preSynapticNeuron][postSynapticNeuron];

            }

        }
    }

    public double[][] populateArrays(double[][] synapses) {
        /**
         * Maak de verbindingen tussen neuronen in de "synapses" array.
         */
        for (int i = 0; i < neurons; i++) {
            for (int j = 0; j < neurons; j++) {
                if (i == j) { // Maak geen verbinding met zichzelf...
                    continue;
                }
                synapses[i][j] = 1;

            }
        }
        return synapses;
    }

}
