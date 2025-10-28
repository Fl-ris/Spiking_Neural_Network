package floris.model;

public class StdpParameters {
    final double potentation = 0.5;  // Potentiation
    final double depression = 0.55;    // Depression
    final double tauPotentation = 20.0;   // LTP tijdsconstante
    final double tauDepression = 20.0;  // LTD tijdsconstante
    final double maxWeightStdp = 5.0;   // Maximale synaptische sterkte.
    double[] lastSpikeTime;

    public StdpParameters() {
    }
}