package floris.model;

import floris.io.ImportedSynapseMatrix;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SNN {
    private static final Logger LOGGER = LogManager.getLogger();

    public final SimulationParameters simulationParameters = new SimulationParameters();
    public final SynapseArray synapseArray = new SynapseArray();
    private final LifNeuronParameters lifNeuronParameters = new LifNeuronParameters();


    public float[][] vHistory;

    public double[] deltaVoltage;
    public double[] voltage;

    public boolean[][] spikes; // Boolean array van spikes (true/false) per neuron per tijdstap
    public double[][] synapses; // Array met de sterkte van verbindingen tussen alle neuronen
    public double[] voltageThreshold; // Array voor homeostatic plasiticty

    public double[][] externalCurrent;

    private double threshold_adaptation = 0.5;
    private double threshold_decay = 0.98;

    //
    private double[] synapseCurrent;
    private double tauSyn = 5.0;
    private double[] refracUntil;
    private double tRef = 2.0;


    public SNN(ImportedSynapseMatrix params) {
        LOGGER.debug("SNN constructor...");

        this.simulationParameters.dt = params.dt();
        this.simulationParameters.simulationTime = params.simulationTime();
        this.simulationParameters.neurons = params.neurons();
        this.synapseArray.inputNeurons = params.inputNeurons();
        this.synapseArray.outputNeurons = params.outputNeurons();
        this.synapseArray.inhibitoryNeurons = params.inhibitoryNeurons();
        this.simulationParameters.simSteps = this.simulationParameters.simulationTime / this.simulationParameters.dt;


        synapses = new double[simulationParameters.neurons][simulationParameters.neurons];
        spikes = new boolean[(int) simulationParameters.simSteps][simulationParameters.neurons];
        voltage = new double[simulationParameters.neurons];
        deltaVoltage = new double[simulationParameters.neurons];
        simulationParameters.input = new double[simulationParameters.neurons];
        vHistory = new float[(int) simulationParameters.simSteps][simulationParameters.neurons];
        synapseArray.isInput = new boolean[simulationParameters.neurons];
        synapseArray.isOutput = new boolean[simulationParameters.neurons];
        synapseArray.isInhibitory = new boolean[simulationParameters.neurons];
        externalCurrent = new double[(int) simulationParameters.simSteps][synapseArray.inputNeurons];

        initArrays();
        inhibitoryNeuronArrayInit();

    }

    private void initArrays() {
        // Vul input/output/inhibitory arrays met waarden:
        for (int i = 0; i < synapseArray.inputNeurons; i++) {
            synapseArray.isInput[i] = true;
        }

        for (int i = simulationParameters.neurons - synapseArray.outputNeurons; i < simulationParameters.neurons; i++) {
            synapseArray.isOutput[i] = true;
        }

        voltageThreshold = new double[simulationParameters.neurons];
        for (int i = 0; i < simulationParameters.neurons; i++) {
            voltageThreshold[i] = lifNeuronParameters.potentialThreshold;
        }

        synapseCurrent = new double[simulationParameters.neurons];
        refracUntil = new double[simulationParameters.neurons];

        // Membraan potentiaal 0 voor alle neuronen.
        for (int i = 0; i < simulationParameters.neurons; i++) {
            voltage[i] = lifNeuronParameters.initialMembranePotential;
            simulationParameters.input[i] = 0;
        }
    }


    private void inhibitoryNeuronArrayInit() {
        // Test: 20% inhiberende neuronen random toevoegen:
        LOGGER.info("Initializing Neuron array...");
        int hiddenStart = synapseArray.inputNeurons;
        int hiddenEnd = simulationParameters.neurons - synapseArray.outputNeurons;
        int hiddenCount = hiddenEnd - hiddenStart;

        if (synapseArray.inhibitoryNeurons > 0 && hiddenCount > 0) {
            Random rand = new Random();
            for (int i = 0; i < synapseArray.inhibitoryNeurons; i++) {
                int index;
                do {
                    index = hiddenStart + rand.nextInt(hiddenCount);
                } while (synapseArray.isInhibitory[index]);
                synapseArray.isInhibitory[index] = true;
            }
        }
        LOGGER.info("Initializing Neuron array finished");
    }

    /**
     * Bereken de nieuwe v waarde volgens de Leaky Integrate and Fire (LIF) formule.
     *
     * @param index
     */
    private void LIFneuron(int index) {
        deltaVoltage[index] = ((-(voltage[index] - lifNeuronParameters.restMembranePotential) + lifNeuronParameters.membraneResistance * synapseCurrent[index])
                / lifNeuronParameters.membraneLeak) * simulationParameters.dt;

        voltage[index] = deltaVoltage[index] + voltage[index];
    }


    /**
     * Reset de input waarden voor alle neuronen.
     */
    private void resetInputs() {
        double decay = Math.exp(-simulationParameters.dt / tauSyn);
        for (int i = 0; i < simulationParameters.neurons; i++) {
            synapseCurrent[i] *= decay;  // "Decay" de waarde om het geleidelijk te laten gaan...
            synapseCurrent[i] += simulationParameters.input[i];
            simulationParameters.input[i] = 0;
        }
    }


    /**
     * Return boolean voor elke tijdstap of de neuron gevuurd heeft.
     *
     * @param index
     * @return boolean
     */
    private boolean spikeDetector(int index) {
        if (voltage[index] >= voltageThreshold[index]) {
            voltage[index] = lifNeuronParameters.restMembranePotential;

            voltageThreshold[index] += threshold_adaptation;
            return true;
        }
        return false;
    }


    /**
     * Zorg er voor dat neuronen niet te vaak achter elkaar spiken door een vuur threshold te verhogen.
     *
     */
    private void updateThresholds() {
        for (int i = 0; i < simulationParameters.neurons; i++) {
            voltageThreshold[i] = lifNeuronParameters.potentialThreshold + (voltageThreshold[i] - lifNeuronParameters.potentialThreshold) * threshold_decay;
        }
    }


    /**
     * Laat een spike door het netwerk propageren.
     *
     * @param preSynapticNeuron
     */
    private void propagateSpike(int preSynapticNeuron) {

        for (int postSynapticNeuron = 0; postSynapticNeuron < simulationParameters.neurons; postSynapticNeuron++) {

            if (preSynapticNeuron != postSynapticNeuron) { // Mag niet met zichzelf verbinden...
                simulationParameters.input[postSynapticNeuron] += synapses[preSynapticNeuron][postSynapticNeuron];

            }

        }
    }


    /**
     * Vul de synapse array op basis van de Euclidische afstand tussen neuronen.
     * Exponentiele afname van de verbindingssterkte
     *
     * @param synapses
     * @return
     */
    public double[][] populateArrays(double[][] synapses) {
        Random rng = new Random();

        int gridSize = (int) Math.sqrt(simulationParameters.neurons);
        double lambda = 0.1;

        for (int presynaptic = 0; presynaptic < simulationParameters.neurons; presynaptic++) {

            for (int postsynaptic = 0; postsynaptic < simulationParameters.neurons; postsynaptic++) {

                if (presynaptic == postsynaptic) continue; // Maak geen verbinding met zichzelf...

                double distance = Math.hypot(presynaptic / gridSize - postsynaptic / gridSize, presynaptic % gridSize - postsynaptic % gridSize);

                double connectionProbability = Math.exp(-lambda * distance);

                if (rng.nextDouble() < connectionProbability) {
                    // Als een neuron inhiberend is, gebruik een negatieve waarde:
                    synapses[presynaptic][postsynaptic] = synapseArray.isInhibitory[presynaptic] ?
                            -rng.nextDouble() * 3.1 - 1 : rng.nextDouble() * 1;

                    synapses[presynaptic][postsynaptic] *= connectionProbability;

                } else {

                    synapses[presynaptic][postsynaptic] = 0;
                }

                if (synapseArray.isInput[postsynaptic]) {
                    synapses[presynaptic][postsynaptic] = 0; // Geen input voor de input neuronen zelf.
                }
                if (synapseArray.isOutput[presynaptic]) {
                    synapses[presynaptic][postsynaptic] = 0; // Geen output voor de output neuronen zelf.
                }
            }
        }
        return synapses;
    }


    /**
     * Sla het neuron voltage per tijdsstap op in de vHistory array.
     *
     * @param timeStep
     * @param neuronIndex
     */
    public void recordVoltage(int timeStep, int neuronIndex) {
        LOGGER.debug("Recording voltage for neuron " + neuronIndex);
        vHistory[timeStep][neuronIndex] = (float) voltage[neuronIndex];
    }


    /**
     * Laat een input neuron spiken.
     *
     * @param current
     */
    public void injectCurrent(double[] current) {
        LOGGER.debug("Injecting current...");
        for (int i = 0; i < synapseArray.inputNeurons; i++) {
            synapseCurrent[i] += current[i];
        }
    }

    /**
     * Update neuron staat voor elke neuron.
     *
     * @param i
     */
    public void step(int i) {
        LOGGER.debug("Step #" + i);
        double tNow = i * simulationParameters.dt;

        resetInputs();
        updateThresholds();
        injectCurrent(externalCurrent[i]);
        List<Integer> firedThisStep = new ArrayList<>();

        for (int j = 0; j < simulationParameters.neurons; j++) {
            if (refracUntil[j] > tNow) {
                voltage[j] = lifNeuronParameters.restMembranePotential;
                spikes[i][j] = false;
                recordVoltage(i, j);
                continue;
            }

            LIFneuron(j);
            boolean fire = spikeDetector(j); // Als het voltage hoger is dan de vuur threshold: fire = true voor deze stap / neuron.
            spikes[i][j] = fire;
            recordVoltage(i, j);

            if (fire) {
                refracUntil[j] = tNow + tRef;
                firedThisStep.add(j);
            }

            LOGGER.debug("{} {} {} {} {} {}", i, j, fire, voltage[j], voltageThreshold[j], synapseCurrent[j]);

        }

        for (int pre : firedThisStep) {
            propagateSpike(pre);
        }
    }
}

