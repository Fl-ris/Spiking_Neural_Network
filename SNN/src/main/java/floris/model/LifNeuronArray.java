package floris.model;

public class LifNeuronArray {
    public float[][] voltageHistory;
    public double[] deltaVoltage;
    public double[] voltage;
    public boolean[][] spikes; // Boolean array van spikes (true/false) per neuron per tijdstap
    public double[][] synapses; // Array met de sterkte van verbindingen tussen alle neuronen
    public double[] voltageThreshold; // Array voor homeostatic plasiticty
    public double[][] externalCurrent;

    public LifNeuronArray() {
    }
}