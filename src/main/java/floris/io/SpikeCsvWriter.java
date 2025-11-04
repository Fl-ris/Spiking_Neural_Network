package floris.io;

import floris.model.SNN;
import floris.model.ImportedSynapseMatrix;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SpikeCsvWriter {

    /**
     * Sla de staat van de output neuronen per tijdstap op.
     *
     * @param snn
     * @param filePath
     */
    public void writeSpikesToCsv(SNN snn, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            StringBuilder header = new StringBuilder("TimeStep");
            for (int i = 0; i < snn.simulationParameters.neurons; i++) {
                if (snn.synapseArray.isOutput[i]) {
                    header.append(",Neuron_").append(i); // Voeg output neuron + output neuron nummer toe.
                }
            }
            // Begin met het csv bestand schrijven, eerst de header:
            writer.println(header.toString());

            // Schrijf spike data van output spikes voor elke tijdsstap.
            for (int t = 0; t < snn.simulationParameters.simSteps; t++) {
                StringBuilder line = new StringBuilder(String.valueOf(t));
                for (int i = 0; i < snn.simulationParameters.neurons; i++) {
                    if (snn.synapseArray.isOutput[i]) {
                        line.append(",").append(snn.lifNeuronArray.spikes[t][i] ? 1 : 0); // Wanneer de neuron spiked in de huidige tijdsstap: 1 anders 0.
                    }
                }
                writer.println(line.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing spike data to CSV: " + e.getMessage());
        }
    }

    /**
     * Als de gebruiker de "--write-spike-output-csv" = true flag heeft meegegeven, schrijf data naar een
     * csv bestand met een unieke naam.
     *
     * @param snn
     * @param params
     */
    public void writeSpikesIfEnabled(SNN snn, ImportedSynapseMatrix params) {
        if (params.writeSpikeOutputCsv()) {
            // Om een bestaand output bestand niet te overschrijven, voeg de datum + tijd toe:
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            String filename = params.outputDirectory() + "SpikeOutputData_" + date + ".csv";
            writeSpikesToCsv(snn, filename);
        }
    }
}
