package floris.io;

import floris.config.NetworkParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Function;

public class SynapseImporter {

    /**
     * Parse config bestand en sla de nieuwe parameters op:
     *
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
            double inhibitoryStrength = Double.parseDouble(props.getProperty("inhibitory_strength", "1.0"));
            double excitatoryStrength = Double.parseDouble(props.getProperty("excitatory_strength", "25.0"));
            double lambda = Double.parseDouble(props.getProperty("lambda", "0.1"));
            double refractoryPeriod = Double.parseDouble(props.getProperty("refractory_period", "2.0"));
            String outputDirectory = props.getProperty("output_directory", "output/");
            boolean enableHeatmap = Boolean.parseBoolean(props.getProperty("enable_heatmap", "true"));

            return new NetworkParameters(dt,
                    simulationTime,
                    neurons,
                    inputNeurons,
                    outputNeurons,
                    inhibitoryNeurons,
                    enableSTDP,
                    enableLateralInhibition,
                    "",
                    imagePath,
                    maxFiringRateHz,
                    writeSpikeOutputCsv,
                    inhibitoryStrength,
                    excitatoryStrength,
                    lambda,
                    refractoryPeriod,
                    outputDirectory,
                    enableHeatmap);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error occurred while parsing the config file: " + configFilePath, e);
        }
    }


}