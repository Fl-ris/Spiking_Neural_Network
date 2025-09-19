package floris;


import java.util.Random;

public class SNN {
    // LIF neuron parameters:
    private byte potentialThreshold = -50;
    private byte restMembranePotential = -65; //65
    private byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.
    private byte initialMembranePotential = -60;
    private byte membraneLeak = 10; // 10 ms
    public double[][] vHistory;

    private double[] dv;
    private double[] v;

    // Simulatie parameters
    public float dt = 0.1F;
    public int simulationTime = 100000;
    public float simSteps = simulationTime / dt;
    private double input[];
    public int neurons = 100;

    boolean[][] spikes; // Boolean array van spikes (true/false) per neuron per tijdstap
    double[][] synapses; // Array met de sterkte van verbindingen tussen alle neuronen

    public boolean[] isInput; // Index die aangeeft of een neuron input is.
    public boolean[] isOutput; // Index die aanageeft of een neuron een output is.

    public int inputNeurons = 4;
    public int outputNeurons = 2;
    public double[][] externalCurrent = new double[(int) simSteps][inputNeurons];



    public SNN() {
        synapses = new double[neurons][neurons];
        spikes = new boolean[(int) simSteps][neurons];
        v = new double[neurons];
        dv = new double[neurons];
        input = new double[neurons];
        vHistory = new double[(int) simSteps][neurons];
        isInput = new boolean[neurons];
        isOutput = new boolean[neurons];
        double[][] externalCurrent = new double[(int) simSteps][inputNeurons];

        // Vul input/output arrays met waarden:
        for (int i = 0; i < inputNeurons; i++) {
            isInput[i]  = true;
        }

        for (int i = neurons - outputNeurons; i < neurons; i++){
            isOutput[i] = true;
        }

        // Membraan potentiaal 1 voor alle neuronen.
        for (int i = 0; i < neurons; i++) {
            v[i] = initialMembranePotential;
            input[i] = 0;
        }
    }


    public static void main(String[] args) {


    }


    public void LIFneuron(int index) {
        dv[index] = ((-(v[index] - restMembranePotential) + membraneResistance * input[index])
                / membraneLeak) * dt;

        v[index] = dv[index] + v[index];
    }

    public void resetInputs() {
        for (int i = 0; i < neurons ; i++) {
            input[i] = 0;
        }
    }

//    public static void plotter(double[][] data) {
//        /**
//         * Plot het verloop van het membraan potentiaal over tijd.
//         * @param xData
//         * @param yData
//         *
//         */
//        new SwingWrapper<>(QuickChart.getChart("Plot", "x", "y", "data",
//                Arrays.stream(data).mapToDouble(p -> p[0]).toArray(),
//                Arrays.stream(data).mapToDouble(p -> p[1]).toArray()
//        )).displayChart();
//    }


    public boolean SpikeDetector(int index) {
        /**
         * Return boolean voor elke tijdstap of de neuron gevuurd heeft.
         * @param v Membraan potentiaal
         * @return boolean
         */
        if (v[index] >= potentialThreshold) {
            v[index] = restMembranePotential;
            return true;
        }
        return false;
    }


    public void propagateSpike(int preSynapticNeuron) {

        for (int postSynapticNeuron = 0; postSynapticNeuron < neurons; postSynapticNeuron++) {

            if (preSynapticNeuron != postSynapticNeuron) { // Mag niet met zichzelf verbinden...
                input[postSynapticNeuron] += synapses[preSynapticNeuron][postSynapticNeuron];

            }

        }
    }

    public double[][] populateArrays(double[][] synapses) {
        /**
         * Maak de verbindingen tussen neuronen in de "synapses" array.
         */
        Random rng = new Random();

        for (int presynaptic = 0; presynaptic < neurons; presynaptic++) {
            for (int postsynaptic = 0; postsynaptic < neurons; postsynaptic++) {
                if (presynaptic == postsynaptic) continue; // Maak geen verbinding met zichzelf...

                synapses[presynaptic][postsynaptic] = rng.nextDouble() * 25; // Vermenigvuldigd met 25 omdat het anders niet sterk genoeg is om te spiken.

                if(isInput[postsynaptic]) {
                    synapses[presynaptic][postsynaptic] = 0; // Geen input voor de input neuronen zelf.
                }
                if(isOutput[presynaptic]) {
                    synapses[presynaptic][postsynaptic] = 0; // Geen output voor de output neuronen zelf.
                }

                }
        }
        return synapses;
    }

    public void recordVoltage(int timeStep, int neuronIndex) {
        vHistory[timeStep][neuronIndex] = v[neuronIndex];
    }

    public void injectCurrent(double[] current) {
        for (int i = 0 ; i < inputNeurons; i++) {
            input[i] = current[i];
        }
    }

}
