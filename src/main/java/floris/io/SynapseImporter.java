package floris.io;

import floris.model.NetworkParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Function;

public class SynapseImporter {

        public static void main(String[] args) {

            File myObj = new File(args[0]);

            try (Scanner myReader = new Scanner(myObj)) {
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    System.out.println(data);
                }

            } catch (FileNotFoundException e) {
                System.out.println("IO Error...");
            }

            verifyMatrix();

            //parseSynapseMatrix();

        }

    /**
     *  Controleer de grootte van de geimporteerde synapese matrix.
     */
    public static void verifyMatrix(){
             // test...
            double[][] synapsesMatrix = new double[][]{
                    {0.0, 0.0, 0.0},
            };

            System.out.println("Rows: " + synapsesMatrix.length);
            System.out.println("Cols:" + synapsesMatrix[0].length);
        }

    /**
     * Parse config bestand en sla de nieuwe parameters op:
      * @param configFilePath
     * @return
     */
    public static NetworkParameters importConfig(String configFilePath) {
        File file = new File(configFilePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException("Configuration file not found: " + configFilePath);
        }

        Properties props = new Properties();
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error occurred while parsing the config file: " + configFilePath, e);
        }

        Function<String, String> getRequiredProperty = (key) -> {
            String foundKey = props.stringPropertyNames().stream()
                    .filter(k -> k.equalsIgnoreCase(key))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Missing required parameter in the config file: " + key));

            String value = props.getProperty(foundKey).trim();

            return value;
        };

        try {
            float dt = Float.parseFloat(getRequiredProperty.apply("dt"));
            int simulationTime = Integer.parseInt(getRequiredProperty.apply("simulation_time"));
            int neurons = Integer.parseInt(getRequiredProperty.apply("neuron_count"));
            int inputNeurons = Integer.parseInt(getRequiredProperty.apply("input_neurons"));
            int outputNeurons = Integer.parseInt(getRequiredProperty.apply("output_neurons"));
            int inhibitoryNeurons = Integer.parseInt(getRequiredProperty.apply("inhibitory_neurons"));
            boolean enableSTDP = Boolean.parseBoolean(getRequiredProperty.apply("enable_STDP"));
            boolean enableLateralInhibition = Boolean.parseBoolean(getRequiredProperty.apply("enable_Lateral_Inhibition"));
            String imagePath = props.getProperty("image_path", "");
            double maxFiringRateHz = Double.parseDouble(props.getProperty("max_firing_rate", "0.0"));
            boolean writeSpikeOutputCsv = Boolean.parseBoolean(props.getProperty("write_spike_output_csv", "false"));

            return new NetworkParameters(dt,
                    simulationTime,
                    neurons,
                    inputNeurons,
                    outputNeurons,
                    inhibitoryNeurons,
                    enableSTDP,
                    enableLateralInhibition,
                    configFilePath,
                    imagePath,
                    maxFiringRateHz,
                    writeSpikeOutputCsv);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error occurred while parsing the config file: " + configFilePath, e);
        }
    }


}