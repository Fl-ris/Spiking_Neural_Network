package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SNN {
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

    public float dt = 0.1F;
    public int simulationTime = 2_00;
    public float simSteps = simulationTime / dt;
    private double input[];
    public int neurons = 20_000;

    public boolean[][] spikes; // Boolean array van spikes (true/false) per neuron per tijdstap
    public double[][] synapses; // Array met de sterkte van verbindingen tussen alle neuronen
    public double[] v_threshold; // Array voor homeostatic plasiticty
    private double threshold_adaptation = 0.5;
    private double threshold_decay = 0.99;


    public boolean[] isInput; // Index die aangeeft of een neuron input is.
    public boolean[] isOutput; // Index die aangeeft of een neuron een output is.
    public boolean[] isInhibitory; // Index die aangeeft of een neuron inhiberend is.

    public int inputNeurons = 1;
    public int outputNeurons = 2;
    public int inhibitoryNeurons = (int) (neurons * 0.20); // Neuronen die een inhiberend signaal geven.

    public double[][] externalCurrent = new double[(int) simSteps][inputNeurons];


    public SNN() {
        synapses = new double[neurons][neurons];
        spikes = new boolean[(int) simSteps][neurons];
        v = new double[neurons];
        dv = new double[neurons];
        input = new double[neurons];
        vHistory = new float[(int) simSteps][neurons];
        isInput = new boolean[neurons];
        isOutput = new boolean[neurons];
        isInhibitory = new boolean[neurons];

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
        for (int i = (neurons - (inputNeurons + outputNeurons)) - inhibitoryNeurons; i < neurons - outputNeurons; i++) {
            isInhibitory[i] = true;

        }


        // Test: 20% inhiberende neuronen random toevoegen:
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

//        // Membraan potentiaal 0 voor alle neuronen.
        for (int i = 0; i < neurons; i++) {
            v[i] = initialMembranePotential;
            input[i] = -65;
        }

    }


    public void LIFneuron(int index) {
        dv[index] = ((-(v[index] - restMembranePotential) + membraneResistance * input[index])
                / membraneLeak) * dt;

        v[index] = dv[index] + v[index];
    }

    public void resetInputs() {
        for (int i = 0; i < neurons ; i++) {
            input[i] = input[i] * 0.05; // "Decay" de waarde om het geleidelijk te laten gaan...
          //  input[i] = input[i] * Math.exp(-dt / 5);
        }
    }


    public boolean SpikeDetector(int index) {
        /**
         * Return boolean voor elke tijdstap of de neuron gevuurd heeft.
         * @param v Membraan potentiaal
         * @return boolean
         */
        if (v[index] >= potentialThreshold) {
            v[index] = restMembranePotential;

            v_threshold[index] += threshold_adaptation;
            return true;
        }
        return false;
    }

    public void updateThresholds() {
        for (int i = 0; i < neurons; i++) {
            v_threshold[i] = potentialThreshold + (v_threshold[i] - potentialThreshold) * threshold_decay;
        }
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

                //synapses[presynaptic][postsynaptic] = rng.nextDouble() * 25; // Vermenigvuldigd met 25 omdat het anders niet sterk genoeg is om te spiken.

                // Als een neuron inhiberend is, gebruik een negatieve waarde:
                synapses[presynaptic][postsynaptic] = isInhibitory[presynaptic] ? -rng.nextDouble() * 25 : rng.nextDouble() * 25;

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
        vHistory[timeStep][neuronIndex] = (float) v[neuronIndex];
    }


    public void injectCurrent(double[] current) {
        for (int i = 0 ; i < inputNeurons; i++) {
            input[i] = current[i];
        }
    }

    public void step(int i){
        resetInputs();
        updateThresholds();
        injectCurrent(externalCurrent[i]);

        for (int j = 0; j < neurons; j++) {
        LIFneuron(j);
        boolean fire = SpikeDetector(j);
        spikes[i][j] = fire;
        recordVoltage(i, j);
            if (j == 150 && i > 50) { // Debug statement, laat neuron 150 zien:
              //  System.out.println(i,j,fire,v[j],v_threshold[j],input[j]);
            }


        if (fire) {
            propagateSpike(j);
        }
    }

    }

}
