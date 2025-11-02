package floris.config;

public class StdpParameters {
    public final double potentation = 0.5;  // Potentiation
    public final double depression = 0.55;    // Depression
    public final double tauPotentation = 20.0;   // LTP tijdsconstante
    public final double tauDepression = 20.0;  // LTD tijdsconstante
    public final double maxWeightStdp = 5.0;   // Maximale synaptische sterkte.
    public double[] lastSpikeTime;

    public StdpParameters() {
    }
}