package floris.model;

public class LifNeuronParameters {
    // LIF neuron parameters:
    byte potentialThreshold = -50;
    byte restMembranePotential = -65; //65
    byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.
    byte initialMembranePotential = -60;
    byte membraneLeak = 10; // 10 ms

    public LifNeuronParameters() {
    }
}