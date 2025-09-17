package floris;

import java.util.ArrayList;
import java.util.Arrays;
import org.knowm.xchart.*;
import java.util.Vector;


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
    private float dt = 0.1F;
    public int simulationTime = 10;
    private float simSteps = simulationTime / dt;
    private double input[];
    private int neurons = 10;

    boolean[][] spikes; // Boolean array van spikes (true/false) per neuron per tijdstap
    double[][] synapses; // Array met de sterkte van verbindingen tussen alle neuronen


    public SNN() {
        synapses = new double[neurons][neurons];
        spikes = new boolean[(int)simSteps][neurons];
        v = new double[neurons];
        dv = new double[neurons];
        input = new double[neurons];

        for (int i = 0; i < neurons; i++) {
            v[i] = initialMembranePotential;
            input[i] = 1;
        }
    }


    public static void main(String[] args) {
        SNN network = new SNN();


        Vector<Double> vList = new Vector<>();

        for (int i = 0; i < network.simSteps; i++) {
            network.LIFneuron();
            boolean fire = network.SpikeDetector(network.v);
            vList.add(network.v);

            System.out.println("Fire: " + fire);
    }

        int n = vList.size();
        double[] yData = new double[n];
        double[] xData = new double[n];
        for (int i = 0; i < n; i++) {
            yData[i] = vList.get(i);
            xData[i] = i * network.dt;
        }

        plotter(xData, yData);

        network.populateArrays(network.synapses);

        for(double[] i : network.synapses){
            for (double d : i) {
                System.out.println("d = " + d);
            }
        }

    }


    public void LIFneuron() {
        dv = (((-v - restMembranePotential) + membraneResistance * input)
                / membraneLeak) * dt;

        v = dv + v;
    }


    public static void plotter(double[] xData, double[] yData) {
        /**
         * Plot het verloop van het membraan potentiaal over tijd.
         * @param xData
         * @param yData
         *
         */
        XYChart chart = QuickChart.getChart("Membraan Potentiaal", "Tijd (ms)", "V (mV)", "V(t)", xData, yData);
        new SwingWrapper<>(chart).displayChart();
    }


    public boolean SpikeDetector(double v) {
        /**
         * Return boolean voor elke tijdstap of de neuron gevuurd heeft.
         * @param v Membraan potentiaal
         * @return boolean
         */
        if (v >= potentialThreshold) {
            this.v = restMembranePotential;
            return true;
        }
    return false;
    }


    public double[][] populateArrays(double[][] synapses ) {
        /**
         * Maak de verbindingen tussen neuronen in de "synapses" array.
         */
        for (int i = 0; i < neurons; i++) {
            for (int j = 0; j < neurons; j++) {
                if(i == j){ // Maak geen verbinding met zichzelf...
                    continue;
                }
                synapses[i][j] = 1;

            }
        }
        return synapses;
    }





}
