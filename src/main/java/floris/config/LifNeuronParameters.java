package floris.config;

public class LifNeuronParameters {
    // LIF neuron parameters:
    public final byte potentialThreshold = -50;
    public final byte restMembranePotential = -65; //65
    public final byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.
    public final byte initialMembranePotential = -60;
    public final byte membraneLeak = 10; // 10 ms

    public LifNeuronParameters() {
    }
}