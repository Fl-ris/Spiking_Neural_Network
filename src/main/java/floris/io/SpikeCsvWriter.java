package floris.io;

import floris.model.SNN;
import floris.model.ImportedSynapseMatrix;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SpikeCsvWriter {

    public void writeSpikesToCsv(SNN snn, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            StringBuilder header = new StringBuilder("TimeStep");
            for (int i = 0; i < snn.simulationParameters.neurons; i++) {
                if (snn.synapseArray.isOutput[i]) {
                    header.append(",Neuron_").append(i);
                }
            }
            writer.println(header.toString());

            // Schrijf spike data van output spikes voor elke tijdsstap.
            for (int t = 0; t < snn.simulationParameters.simSteps; t++) {
                StringBuilder line = new StringBuilder(String.valueOf(t));
                for (int i = 0; i < snn.simulationParameters.neurons; i++) {
                    if (snn.synapseArray.isOutput[i]) {
                        line.append(",").append(snn.lifNeuronArray.spikes[t][i] ? 1 : 0);
                    }
                }
                writer.println(line.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing spike data to CSV: " + e.getMessage());
        }
    }

    public void writeSpikesIfEnabled(SNN snn, ImportedSynapseMatrix params) {
        if (params.writeSpikeOutputCsv()) {
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String filename = "spike_output_" + date + ".csv";
            writeSpikesToCsv(snn, filename);
        }
    }
}
