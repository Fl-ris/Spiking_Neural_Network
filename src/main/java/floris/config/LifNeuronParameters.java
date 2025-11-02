package floris.config;

public class LifNeuronParameters {
    // LIF neuron parameters:
    public byte potentialThreshold = -50;
    public byte restMembranePotential = -65; //65
    public byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.
    public byte initialMembranePotential = -60;
    public byte membraneLeak = 10; // 10 ms

    public LifNeuronParameters() {
    }
}