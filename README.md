# Spiking_Neural_Network
A spiking neural network (SNN) written in Java

## 1. About this project: 

Spiking neural networks (SNNs) are members of the third generation of artificial neural networks. 
Unlike previous generations of neural networks, SNNs allow for temporal data processing and their sparse activity, like that of the biological brain, makes them more energy efficient compared to LLMs, perceptrons etc.

The code in this repository implements a leaky‑integrate‑and‑fire (LIF) neuron, optional spike‑timing‑dependent plasticity (STDP), and a simple lateral‑inhibition. All of the dynamics are expressed with ordinary differential equations that are integrated with a fixed time step (`dt`).  The whole simulator is written in Java.

---

## 2. Usage: ##

The simulator accepts a handful of numbered options. All of them have defaults, so you can launch the program without any arguments.

## 3. Commandline arguments: ##

### Core network parameters  

`-n` / `--neuron-count`: total number of neurons (default 5000).  

`-t` / `--simulation-time`: how many milliseconds the simulation runs (default 100 ms). 

`-dt` / `--time-step`: integration step used by the LIF equation (default 0.1 ms).  

`--input-neurons`: dedicated input cells that receive external current or Poisson spikes 
(default 10).  
`--output-neurons`: cells whose activity is usually monitored (default 5).  

`--inhibitory-neurons`: how many of the hidden cells are inhibitory (default 1000).  

`--config-file <path>`: a `.properties` file that contains all of the arguments.  When present, individual flags are ignored when this is used.

`--enable-STDP`: turn on spike‑timing‑dependent plasticity (default disabled).  

`--enable-lateral-inhibition`: enable the neighbourhood suppression rule (default disabled).  

`--image-path <path>`: path to an image for Poisson spike generation.  

`--max-firing-rate <Hz>`: the peak firing rate used for the image‑driven input.  

`--write-spike-output-csv"`: Write the state of the output neurons to a .csv file. (Default false)

`--inhibitory-strength`: The strength of inhibitory neurons (default 1.0).

`--excitatory-strength`: The strength of excitatory neurons (default 25.0).

`--lambda`: The connection strength factor (default 0.1).

`--refractory-period`: The refractory period of a neuron in ms (default 2.0).

`--output-directory`: The directory to save output files to (default `output/`).

`--enable-heatmap`: Enable the heatmap visualization (default `true`).



`-v`: each occurrence raises the logging verbosity (`-v` = WARN, `-vv` = INFO, `-vvv` = DEBUG).  


## 4.  Configuration file format  

Besides using a long CLI command, one can create a simple Java‑properties file with the following properties:

```
dt=0.05
simulation_time=250
neuron_count=12000
input_neurons=100
output_neurons=10
inhibitory_neurons=2000
enable_STDP=true
enable_Lateral_Inhibition=true
image_path=image.png
max_firing_rate=200.0
inhibitory_strength=1.0
excitatory_strength=25.0
lambda=0.1
refractory_period=2.0
output_directory=my_output_folder/
enable_heatmap=false
```

### 4.1 Example configuration files

The config directory contains several example configuration files that can be used with the `--config-file` command-line option.
Each config file demonstrates a different usecase (or network size).


*   `small_brain_1.conf`: A configuration for a small-sized network.
*   `medium_brain_1.conf`: A configuration for a medium-sized network.
*   `large_brain_1.conf`: A configuration for a large-sized network.
*   `poisson_input.conf`: A configuration that enables Poisson spike train spike inputs.
*   `outputTest.conf`: A configuration for testing the spike data CSV output.


## 5. how the simulation works  

### 5.1  Initialisation  

**Parameter collection**: either the CLI options or the config file are turned into a `NetworkParameters` record.

**Network object**: a new SNN is instantiated, it receives the parameters, allocates all arrays (voltage, synapseCurrent, spikes, etc.) and, if an image is supplied, creates a PoissonSpikeTrain.

**Inhibitory placement**: a user defined percentage of the hidden neurons are randomly flagged as inhibitory.

**Synapse connections**: populateArrays walks through every ordered pair of neurons, computes a Euclidean distance on a 2D grid, and draws a connection with probability `exp(-lambda*distance)`. Near neighbours may receive a pre‑computed negative weight when lateral inhibition is on. The weight is scaled by the same probability, so long‑range connections are typically weak.  

### 5.2  The per‑step loop:

**Input handling**: If an image is provided via the --image-path parameter, a PoissonSpikeTrain generator is created. In each simulation step, this generator provides a boolean array of spikes for the input neurons. If no image is provided, a static current of 5.0 is injected into all input neurons at every 10th time step. 

**Refractory check**: Neurons that are still inside their refractory window are forced to the resting potential and skipped for the rest of the step.  

**Leaky integration**: For every non‑refractory neuron the LIF differential equation is integrated using the forward Euler method over the tiny step. The result is stored in voltage.  

**Spike detection**: Whenever the membrane exceeds the adaptive threshold, a spike is recorded, the voltage is reset, the adaptive threshold is raised, and the refractory timer is set.  

**Learning**: If STDP is active, the current spike triggers a weight update for all presynaptic neurons that have spiked recently (potentiation) and for all postsynaptic targets that spiked shortly before (depression). The update respects upper and lower bounds defined in StdpParameters.

**Propagation**: After the whole population has been examined, propagateSpike adds the outgoing synaptic weight of each firing neuron to the input accumulator of every postsynaptic neuron. Because the weight matrix is dense, propagation is O(N^2).
**Decay**: The synaptic current on each cell decays exponentially with a time constant tauSyn. This models the finite duration of post‑synaptic potentials.

**Threshold relaxation**: The adaptive threshold slowly contracts back toward its baseline (thresholdDecay).  

All of the above steps are repeated `simulationSteps = simulationTime / dt` times. The visualiser is refreshed after every iteration, producing an animation of spiking activity.

---  

## 6.  Visualisation details and interpretation  

The heatmap displays the following:

Input neurons: a light green at rest, and a brighter green when spiking.  
Output neurons: deep blue at rest, cyan when spiking.  
Inhibitory neurons: red, both at rest and when spiking.  
Excitatory (hidden) neurons: dark grey at rest, yellow when they fire.  

---


## 7. System requirements:
To instantiate a network consisting of many tens of thousands of neurons, a lot of system RAM is needed. 


---

## 8.  License
The code is released under the GPL3 License. See the LICENSE file for more details.

---  

## 9.  References and further reading: 

https://www.zotero.org/groups/6207255/spikingneuralnetworks

---  
