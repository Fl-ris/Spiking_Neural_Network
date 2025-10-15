package floris.model;

public class StdpParameters {
    final double A_plus = 0.5;  // Potentiation
    final double A_minus = 0.55;    // Depression
    final double tau_plus = 20.0;   // LTP tijdsconstante
    final double tau_minus = 20.0;  // LTD tijdsconstante
    final double maxWeightStdp = 5.0;   // Maximale synaptische sterkte.
    double[] lastSpikeTime;

    public StdpParameters() {
    }
}