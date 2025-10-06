package model;

import floris.io.ImportedSynapseMatrix;
import floris.io.NetworkParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SNN {
    private static final Logger LOGGER = LogManager.getLogger();


    // LIF neuron parameters:
    private byte potentialThreshold = -50;
    private byte restMembranePotential = -65; //65
    private byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.
    private byte initialMembranePotential = -60;
    private byte membraneLeak = 10; // 10 ms
    public float[][] vHistory;

    public double[] dv;
    public double[] v;

    // Simulatie parameters
    public float dt;
    public int simulationTime;
    public float simSteps;
    private double input[];
    public int neurons;

    public boolean[][] spikes; // Boolean array van spikes (true/false) per neuron per tijdstap
    public double[][] synapses; // Array met de sterkte van verbindingen tussen alle neuronen
    public double[] v_threshold; // Array voor homeostatic plasiticty

    private double threshold_adaptation = 0.5;
    private double threshold_decay = 0.98;


    public boolean[] isInput; // Index die aangeeft of een neuron input is.
    public boolean[] isOutput; // Index die aangeeft of een neuron een output is.
    public boolean[] isInhibitory; // Index die aangeeft of een neuron inhiberend is.

    public int inputNeurons;
    public int outputNeurons;
    public int inhibitoryNeurons; // Neuronen die een inhiberend signaal geven.

    public double[][] externalCurrent;

    //
    private double[] I_syn;
    private double tauSyn = 5.0;
    private double[] refracUntil;
    private double tRef = 2.0;


    public SNN(ImportedSynapseMatrix params) {
        LOGGER.debug("SNN constructor...");

        this.dt = params.dt();
        this.simulationTime = params.simulationTime();
        this.neurons = params.neurons();
        this.inputNeurons = params.inputNeurons();
        this.outputNeurons = params.outputNeurons();
        this.inhibitoryNeurons = params.inhibitoryNeurons();
        this.simSteps = this.simulationTime / this.dt;


        synapses = new double[neurons][neurons];
        spikes = new boolean[(int) simSteps][neurons];
        v = new double[neurons];
        dv = new double[neurons];
        input = new double[neurons];
        vHistory = new float[(int) simSteps][neurons];
        isInput = new boolean[neurons];
        isOutput = new boolean[neurons];
        isInhibitory = new boolean[neurons];
        externalCurrent = new double[(int) simSteps][inputNeurons];

        // Vul input/output/inhibitory arrays met waarden:
        for (int i = 0; i < inputNeurons; i++) {
            isInput[i]  = true;
        }

        for (int i = neurons - outputNeurons; i < neurons; i++){
            isOutput[i] = true;
        }

        v_threshold = new double[neurons];
        for (int i = 0; i < neurons; i++) {
            v_threshold[i] = potentialThreshold;
        }

        // Maak neuronen met de "isInhibitory" index true.
//        for (int i = (neurons - (inputNeurons + outputNeurons)) - inhibitoryNeurons; i < neurons - outputNeurons; i++) {
//            isInhibitory[i] = true;
//
//        }

        I_syn = new double[neurons];
        refracUntil = new double[neurons];
        inhibitoryNeuronArrayInit();

//        // Membraan potentiaal 0 voor alle neuronen.
        for (int i = 0; i < neurons; i++) {
            v[i] = initialMembranePotential;
            input[i] = 0;
        }

    }


    private void inhibitoryNeuronArrayInit() {
        // Test: 20% inhiberende neuronen random toevoegen:
        LOGGER.info("Initializing Neuron array...");
        int hiddenStart = inputNeurons;
        int hiddenEnd = neurons - outputNeurons;
        int hiddenCount = hiddenEnd - hiddenStart;

        if (inhibitoryNeurons > 0 && hiddenCount > 0) {
            Random rand = new Random();
            for (int i = 0; i < inhibitoryNeurons; i++) {
                int index;
                do {
                    index = hiddenStart + rand.nextInt(hiddenCount);
                } while (isInhibitory[index]);
                isInhibitory[index] = true;
            }
        }
        LOGGER.info("Initializing Neuron array finished");
    }

    /**
     * Bereken de nieuwe v waarde volgens de Leaky Integrate and Fire (LIF) formule.
     * @param index
     */
    private void LIFneuron(int index) {
        dv[index] = ((-(v[index] - restMembranePotential) + membraneResistance * I_syn[index])
                / membraneLeak) * dt;

        v[index] = dv[index] + v[index];
    }


//    public void resetInputs() {
//        for (int i = 0; i < neurons ; i++) {
//            //input[i] = input[i] * 0.05; // "Decay" de waarde om het geleidelijk te laten gaan...
//            input[i] = input[i] * Math.exp(-dt / 5);
//        }
//    }

    /**
     * Reset de input waarden voor alle neuronen.
     */
    private void resetInputs() {
        double decay = Math.exp(-dt / tauSyn);
        for (int i = 0; i < neurons; i++) {
            I_syn[i] *= decay;
            I_syn[i] += input[i];
            input[i] = 0;
        }
    }


    /**
     * Return boolean voor elke tijdstap of de neuron gevuurd heeft.
     * @param index
     * @return boolean
     */
    private boolean SpikeDetector(int index) {
        if (v[index] >= v_threshold[index]) {
            v[index] = restMembranePotential;

            v_threshold[index] += threshold_adaptation;
            return true;
        }
        return false;
    }


    /**
     * Zorg er voor dat neuronen niet te vaak achter elkaar spiken door een vuur threshold te verhogen.
     *
     */
    private void updateThresholds() {
        for (int i = 0; i < neurons; i++) {
            v_threshold[i] = potentialThreshold + (v_threshold[i] - potentialThreshold) * threshold_decay;
        }
    }


    /**
     * Laat een spike door het netwerk propageren.
     * @param preSynapticNeuron
     */
    private void propagateSpike(int preSynapticNeuron) {

        for (int postSynapticNeuron = 0; postSynapticNeuron < neurons; postSynapticNeuron++) {

            if (preSynapticNeuron != postSynapticNeuron) { // Mag niet met zichzelf verbinden...
                input[postSynapticNeuron] += synapses[preSynapticNeuron][postSynapticNeuron];

            }

        }
    }



//    public double[][] populateArrays(double[][] synapses) {
//        /**
//         * Maak de verbindingen tussen neuronen in de "synapses" array.
//         */
//        Random rng = new Random();
//
//        for (int presynaptic = 0; presynaptic < neurons; presynaptic++) {
//            for (int postsynaptic = 0; postsynaptic < neurons; postsynaptic++) {
//                if (presynaptic == postsynaptic) continue; // Maak geen verbinding met zichzelf...
//
//                //synapses[presynaptic][postsynaptic] = rng.nextDouble() * 25; // Vermenigvuldigd met 25 omdat het anders niet sterk genoeg is om te spiken.
//
//                // Als een neuron inhiberend is, gebruik een negatieve waarde:
//                synapses[presynaptic][postsynaptic] = isInhibitory[presynaptic] ? -rng.nextDouble() * 25 : rng.nextDouble() * 25;
//
//                if(isInput[postsynaptic]) {
//                    synapses[presynaptic][postsynaptic] = 0; // Geen input voor de input neuronen zelf.
//                }
//                if(isOutput[presynaptic]) {
//                    synapses[presynaptic][postsynaptic] = 0; // Geen output voor de output neuronen zelf.
//                }
//
//                }
//        }
//        return synapses;
//    }


    /**
     * Vul de synapse array op basis van de Euclidische afstand tussen neuronen.
     * Exponentiele afname van de verbindingssterkte
     * @param synapses
     * @return
     */
        public double[][] populateArrays(double[][] synapses) {
        Random rng = new Random();

        int gridSize = (int) Math.sqrt(neurons);
        double lambda = 0.1;

        for (int presynaptic = 0; presynaptic < neurons; presynaptic++) {

            for (int postsynaptic = 0; postsynaptic < neurons; postsynaptic++) {

                if (presynaptic == postsynaptic) continue; // Maak geen verbinding met zichzelf...

                double distance = Math.hypot(presynaptic/gridSize - postsynaptic/gridSize, presynaptic%gridSize - postsynaptic%gridSize);

                double connectionProbability = Math.exp(-lambda * distance);

                if (rng.nextDouble() < connectionProbability) {
                    // Als een neuron inhiberend is, gebruik een negatieve waarde:
                    synapses[presynaptic][postsynaptic] = isInhibitory[presynaptic] ?
                       //     -rng.nextDouble() * 0.2: rng.nextDouble() * 3;
                            -rng.nextDouble() * 3.1 - 1: rng.nextDouble() * 1;
                    synapses[presynaptic][postsynaptic] *= connectionProbability;
                } else {

                    synapses[presynaptic][postsynaptic] = 0;
                }

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


    /**
     * Sla het neuron voltage per tijdsstap op in de vHistory array.
     * @param timeStep
     * @param neuronIndex
     */
    public void recordVoltage(int timeStep, int neuronIndex) {
        LOGGER.debug("Recording voltage for neuron " + neuronIndex);
        vHistory[timeStep][neuronIndex] = (float) v[neuronIndex];
    }


    /**
     * Laat een input neuron spiken.
     * @param current
     */
    public void injectCurrent(double[] current) {
        LOGGER.debug("Injecting current...");
        for (int i = 0 ; i < inputNeurons; i++) {
            I_syn[i] += current[i];
        }
    }



//    public void step(int i){
//        resetInputs();
//        updateThresholds();
//        injectCurrent(externalCurrent[i]);
//
//        for (int j = 0; j < neurons; j++) {
//        LIFneuron(j);
//        boolean fire = SpikeDetector(j);
//        spikes[i][j] = fire;
//        recordVoltage(i, j);
//            if (j == 150 && i > 50) { // Debug statement, laat neuron 150 zien:
//                System.out.println(i + " " + j + " " + fire + " " + v[j] + " " + v_threshold[j] + " " + input[j]);
//            }
//        if (fire) {
//            propagateSpike(j);
//        }
//    }
//
//    }

    /**
     * Update neuron staat voor elke neuron.
     *
     * @param i
     */
    public void step(int i){
    LOGGER.debug("Step #" + i);
    double tNow = i * dt;

    resetInputs();
    updateThresholds();
    injectCurrent(externalCurrent[i]);
    List<Integer> firedThisStep = new ArrayList<>();

    for (int j = 0; j < neurons; j++) {
        if (refracUntil[j] > tNow) {
            v[j] = restMembranePotential;
            spikes[i][j] = false;
            recordVoltage(i, j);
            continue;
        }

        LIFneuron(j);
        boolean fire = SpikeDetector(j); // Als het voltage hoger is dan de vuur threshold, fire = true voor deze stap / neuron.
        spikes[i][j] = fire;
        recordVoltage(i, j);

        if (fire) {
            refracUntil[j] = tNow + tRef;
            firedThisStep.add(j);
            }

        LOGGER.debug(i + " " + j + " " + fire + " " + v[j] + " " + v_threshold[j] + " " + I_syn[j]);

    }

    for (int pre : firedThisStep) {
        propagateSpike(pre);
    }
} }

