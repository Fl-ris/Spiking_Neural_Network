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
    public final LifNeuronArray lifNeuronArray = new LifNeuronArray();
    private final LifNeuronParameters lifNeuronParameters = new LifNeuronParameters();
    private final StdpParameters stdpParameters = new StdpParameters();

    boolean enableSTDP;
    boolean enableLateralInhibition;

    private double threshold_adaptation = 0.5;
    private double threshold_decay = 0.98;

    private double[] synapseCurrent;
    private double tauSyn = 5.0;
    private double[] refracUntil;
    private double tRef = 2.0;


    private final double lateralInhibitionRadius = 2.0;
    private final double lateralInhibitionStrength = 2.5;



    public SNN(ImportedSynapseMatrix params) {
        LOGGER.debug("SNN constructor...");

        setParameters(params);
        initArrays();
        inhibitoryNeuronArrayInit();

    }


    /**
     * Set the simulation parameters to be used.
     * @param params
     */
    private void setParameters(ImportedSynapseMatrix params) {
        this.simulationParameters.dt = params.dt();
        this.simulationParameters.simulationTime = params.simulationTime();
        this.simulationParameters.neurons = params.neurons();
        this.synapseArray.inputNeurons = params.inputNeurons();
        this.synapseArray.outputNeurons = params.outputNeurons();
        this.synapseArray.inhibitoryNeurons = params.inhibitoryNeurons();
        this.simulationParameters.simSteps = this.simulationParameters.simulationTime / this.simulationParameters.dt;

        this.enableSTDP = params.enableSTDP();

        if(enableSTDP == true) {
            LOGGER.info("STDP enabled...");
        } else {
            LOGGER.info("STDP disabled...");
        }

        this.enableLateralInhibition = params.enableLateralInhibition();
        if(enableSTDP == true) {
            LOGGER.info("Lateral inhibition enabled...");
        } else {
            LOGGER.info("Lateral inhibition disabled...");
        }

        lifNeuronArray.synapses = new double[simulationParameters.neurons][simulationParameters.neurons];
        lifNeuronArray.spikes = new boolean[(int) simulationParameters.simSteps][simulationParameters.neurons];
        lifNeuronArray.voltage = new double[simulationParameters.neurons];
        lifNeuronArray.deltaVoltage = new double[simulationParameters.neurons];
        simulationParameters.input = new double[simulationParameters.neurons];
        lifNeuronArray.voltageHistory = new float[(int) simulationParameters.simSteps][simulationParameters.neurons];
        synapseArray.isInput = new boolean[simulationParameters.neurons];
        synapseArray.isOutput = new boolean[simulationParameters.neurons];
        synapseArray.isInhibitory = new boolean[simulationParameters.neurons];
        lifNeuronArray.externalCurrent = new double[(int) simulationParameters.simSteps][synapseArray.inputNeurons];
    }

    /**
     * Initialiseer de arrays met beginwaarden.
     */
    private void initArrays() {
        // Vul input/output/inhibitory arrays met waarden:
        for (int i = 0; i < synapseArray.inputNeurons; i++) {
            synapseArray.isInput[i] = true;
        }

        for (int i = simulationParameters.neurons - synapseArray.outputNeurons; i < simulationParameters.neurons; i++) {
            synapseArray.isOutput[i] = true;
        }

        lifNeuronArray.voltageThreshold = new double[simulationParameters.neurons];
        for (int i = 0; i < simulationParameters.neurons; i++) {
            lifNeuronArray.voltageThreshold[i] = lifNeuronParameters.potentialThreshold;
        }

        stdpParameters.lastSpikeTime = new double[simulationParameters.neurons];


        synapseCurrent = new double[simulationParameters.neurons];
        refracUntil = new double[simulationParameters.neurons];

        // Membraan potentiaal 0 voor alle neuronen.
        for (int i = 0; i < simulationParameters.neurons; i++) {
            lifNeuronArray.voltage[i] = lifNeuronParameters.initialMembranePotential;
            simulationParameters.input[i] = 0;
            stdpParameters.lastSpikeTime[i] = -1e9;
        }
    }

    /**
     * Willekeuringe initialisatie van de inhiberende neuronen array.
     */
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
        lifNeuronArray.deltaVoltage[index] = ((-(lifNeuronArray.voltage[index] - lifNeuronParameters.restMembranePotential) + lifNeuronParameters.membraneResistance * synapseCurrent[index])
                / lifNeuronParameters.membraneLeak) * simulationParameters.dt;

        lifNeuronArray.voltage[index] = lifNeuronArray.deltaVoltage[index] + lifNeuronArray.voltage[index];
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
        if (lifNeuronArray.voltage[index] >= lifNeuronArray.voltageThreshold[index]) {
            lifNeuronArray.voltage[index] = lifNeuronParameters.restMembranePotential;

            lifNeuronArray.voltageThreshold[index] += threshold_adaptation;
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
            lifNeuronArray.voltageThreshold[i] = lifNeuronParameters.potentialThreshold + (lifNeuronArray.voltageThreshold[i] - lifNeuronParameters.potentialThreshold) * threshold_decay;
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
                simulationParameters.input[postSynapticNeuron] += lifNeuronArray.synapses[preSynapticNeuron][postSynapticNeuron];

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

                // 1D array naar 2D neuron grid mapping, index/gridsize om de juiste rij te krijgen en index % gridsize om de juiste kolom te krijgen.
                double distance = Math.hypot(presynaptic / gridSize - postsynaptic / gridSize, presynaptic % gridSize - postsynaptic % gridSize);

                double connectionProbability = Math.exp(-lambda * distance);

                // Bepaal of de twee neuronen dicht genoeg bij elkaar liggen om een synapse verbinding > 0 te krijgen:
                determineIfSynapseConnectionShouldBeMade(synapses, rng, connectionProbability, presynaptic, postsynaptic, distance);
            }
        }
        return synapses;
    }

    private void determineIfSynapseConnectionShouldBeMade(double[][] synapses, Random rng, double connectionProbability, int presynaptic, int postsynaptic, double distance) {
        // If neurons are close, create a fixed inhibitory synapse for lateral inhibition.
        if (distance <= lateralInhibitionRadius) {
            synapses[presynaptic][postsynaptic] = -lateralInhibitionStrength;
        }
        // Otherwise, use the original probabilistic connection logic for more distant neurons.
        else if (rng.nextDouble() < connectionProbability) {
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


    /**
     * Sla het neuron voltage per tijdsstap op in de vHistory array.
     *
     * @param timeStep
     * @param neuronIndex
     */
    public void recordVoltage(int timeStep, int neuronIndex) {
        LOGGER.debug("Recording voltage for neuron " + neuronIndex);
        lifNeuronArray.voltageHistory[timeStep][neuronIndex] = (float) lifNeuronArray.voltage[neuronIndex];
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
        injectCurrent(lifNeuronArray.externalCurrent[i]);
        List<Integer> firedThisStep = new ArrayList<>();

        for (int j = 0; j < simulationParameters.neurons; j++) {
            if (refracUntil[j] > tNow) {
                lifNeuronArray.voltage[j] = lifNeuronParameters.restMembranePotential;
                lifNeuronArray.spikes[i][j] = false;
                recordVoltage(i, j);
                continue;
            }

            LIFneuron(j);
            boolean fire = spikeDetector(j); // Als het voltage hoger is dan de vuur threshold: fire = true voor deze stap / neuron.
            lifNeuronArray.spikes[i][j] = fire;
            recordVoltage(i, j);

            if (fire) {
                refracUntil[j] = tNow + tRef;
                firedThisStep.add(j);

                // STDP:
                if(enableSTDP == true) {
                    stdpParameters.lastSpikeTime[j] = tNow;
                    applySTDP(j, tNow);
                }

            }

            LOGGER.debug("{} {} {} {} {} {}", i, j, fire, lifNeuronArray.voltage[j], lifNeuronArray.voltageThreshold[j], synapseCurrent[j]);

        }

        for (int pre : firedThisStep) {
            propagateSpike(pre);
        }
    }

    private void applySTDP(int neuronIndex, double spikeTime) {
        // LTP:
        for (int preSynapticNeuron = 0; preSynapticNeuron < simulationParameters.neurons; preSynapticNeuron++) {
            if (!synapseArray.isInhibitory[preSynapticNeuron] && lifNeuronArray.synapses[preSynapticNeuron][neuronIndex] > 0) {
                double timeDiff = spikeTime - stdpParameters.lastSpikeTime[preSynapticNeuron];
                if (timeDiff > 0) {
                    double delta_w = stdpParameters.A_plus * Math.exp(-timeDiff / stdpParameters.tau_plus);
                    lifNeuronArray.synapses[preSynapticNeuron][neuronIndex] += delta_w;
                    if (lifNeuronArray.synapses[preSynapticNeuron][neuronIndex] > stdpParameters.maxWeightStdp) {
                        lifNeuronArray.synapses[preSynapticNeuron][neuronIndex] = stdpParameters.maxWeightStdp;
                    }
                }
            }
        }

        // LTD:
        for (int postSynapticNeuron = 0; postSynapticNeuron < simulationParameters.neurons; postSynapticNeuron++) {
            if (!synapseArray.isInhibitory[neuronIndex] && lifNeuronArray.synapses[neuronIndex][postSynapticNeuron] > 0) {
                double timeDiff = spikeTime - stdpParameters.lastSpikeTime[postSynapticNeuron];
                if (timeDiff < 0) {
                    double delta_w = -stdpParameters.A_minus * Math.exp(timeDiff / stdpParameters.tau_minus);
                    lifNeuronArray.synapses[neuronIndex][postSynapticNeuron] += delta_w;
                    if (lifNeuronArray.synapses[neuronIndex][postSynapticNeuron] < 0) {
                        lifNeuronArray.synapses[neuronIndex][postSynapticNeuron] = 0;
                    }
                }
            }
        }
    }

}

