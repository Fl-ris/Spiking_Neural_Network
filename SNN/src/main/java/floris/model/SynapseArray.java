package floris.model;

public class SynapseArray {
    public boolean[] isInput; // Index die aangeeft of een neuron input is.
    public boolean[] isOutput; // Index die aangeeft of een neuron een output is.
    public boolean[] isInhibitory; // Index die aangeeft of een neuron inhiberend is.
    public int inputNeurons;
    public int outputNeurons;
    public int inhibitoryNeurons; // Neuronen die een inhiberend signaal geven.

    public SynapseArray() {
    }
}