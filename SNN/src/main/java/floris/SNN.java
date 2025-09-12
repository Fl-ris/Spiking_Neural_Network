package floris;

import java.util.ArrayList;
import java.util.Arrays;
import org.knowm.xchart.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.IntStream;

public class SNN {
    byte potentialThreshold = -50;
    byte restMembranePotential = -65;
    byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.
    double input = 2;

    double initialMembranePotential = 60;
    double dv;
    double v;

    int membraneLeak = 10; // 10 ms
    int neurons = 5;

    double dt = 0.1;
    int simulationTime = 1000;
    double simSteps = simulationTime / dt;

    int[][] spikeArray;

    double[][] test = new double[neurons][neurons];

    public SNN(){
        this.v = initialMembranePotential;
    }

    public static void main(String[] args) {
        SNN network = new SNN();


        Vector<Double> vList = new Vector<>();

            for (int i = 0; i < network.simSteps; i++) {
            network.LIFneuron(null);
            vList.add(network.v);
    }

        int n = vList.size();
        double[] yData = new double[n];
        double[] xData = new double[n];
        for (int i = 0; i < n; i++) {
            yData[i] = vList.get(i);
            xData[i] = i * network.dt;
        }
        plotter(xData, yData);

    }

    public void LIFneuron(String[] args) {
        dv = (((-v - restMembranePotential) + membraneResistance * input)
                / membraneLeak) * dt;

        v = dv + v;
        System.out.println(v);
    }


    public static void plotter(double[] xData, double[] yData) {
        XYChart chart = QuickChart.getChart(
                "Membrane Potential (LIF neuron)",
                "Time (ms)", "V (mV)",
                "V(t)", xData, yData);

        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setMarkerSize(4);

        new SwingWrapper<>(chart).displayChart();
    }



}
