package floris.io;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;
import floris.Main;

public class CommandlineProcessor {
    private static final Logger logger = LogManager.getLogger(CommandlineProcessor.class.getName());


    @CommandLine.Command(name = "SNN", version = "SNN-0.1", mixinStandardHelpOptions = true)
    public static class commandlineProcessor implements Runnable {

        @CommandLine.Option(names = {"-n", "--neuron-count"}, description = "The number of neurons in the network.")
        int neurons = 5000;

        @CommandLine.Option(names = {"-t", "--simulation-time"}, description = "The amount of time in ms the SNN should run.")
        int simulationTime = 10000;

        @CommandLine.Option(names = {"-dt", "--time-step"}, description = "The size of dt for the LIF equation.")
        float dt = 0.1F;

        @CommandLine.Option(names = {"--input-neurons"}, description = "Amount of input neurons.")
        int inputNeurons = 50;

        @CommandLine.Option(names = {"--output-neurons"}, description = "Amount of output neurons.")
        int outputNeurons = 5;

        @CommandLine.Option(names = {"--inhibitory-neurons"}, description = "Amount of inhibitory neurons.")
        int inhibitoryNeurons = 1000;

        @CommandLine.Option(names = {"--config-file"}, description = "Path to a SNN configuration file.")
        String configFilePath = "";

        @CommandLine.Option(names = {"-v"}, description = "Verbosity level")
        private boolean[] verbose = new boolean[0];


        @Override
        public void run() {

            if (verbose.length > 2) {
                Configurator.setRootLevel(Level.DEBUG);
                logger.debug("Verbosity level changed to DEBUG");
            } else if (verbose.length > 1) {
                Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
                logger.info("Verbosity level set to INFO");
            } else if (verbose.length > 0) {
                Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.WARN);
                logger.warn("Verbosity level set to WARN");
            }

            ImportedSynapseMatrix params;
            if (configFilePath != null && !configFilePath.isBlank()) {
                try {
                    params = SynapseImporter.importConfig(configFilePath);
                    logger.info("Simulation parameters loaded from config file: '{}'", configFilePath);
                } catch (Exception e) {
                    logger.error("Error reading config file: '{}': {}", configFilePath, e.getMessage());
                    return;
                }
            } else {
                params = new NetworkParameters(dt,
                        simulationTime,
                        neurons,
                        inputNeurons,
                        outputNeurons,
                        inhibitoryNeurons,
                        "");
                logger.info("No config file given, using commandline parameters...");
            }

            Main simulation = new Main();
            simulation.runNetwork(params);



        }

    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new commandlineProcessor()).execute(args);
        System.exit(exitCode);
    }
}
