package floris.config;

import floris.io.InputValidator;
import floris.io.SynapseImporter;
import floris.model.ImportedSynapseMatrix;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import picocli.CommandLine;
import floris.Main;

public class CommandlineProcessor {
    private static final Logger logger = LogManager.getLogger(CommandlineProcessor.class.getName());


    @CommandLine.Command(name = "SNN", version = "SNN-0.7", mixinStandardHelpOptions = true)
    public static class commandlineProcessor implements Runnable {

        @CommandLine.Option(names = {"-n", "--neuron-count"}, description = "The number of neurons in the network.")
        public int neurons = 5000;

        @CommandLine.Option(names = {"-t", "--simulation-time"}, description = "The amount of time in ms the SNN should run.")
        public int simulationTime = 100;

        @CommandLine.Option(names = {"-dt", "--time-step"}, description = "The size of dt for the LIF equation.")
        public float dt = 0.1F;

        @CommandLine.Option(names = {"--input-neurons"}, description = "Amount of input neurons.")
        public int inputNeurons = 10;

        @CommandLine.Option(names = {"--output-neurons"}, description = "Amount of output neurons.")
        public int outputNeurons = 5;

        @CommandLine.Option(names = {"--inhibitory-neurons"}, description = "Amount of inhibitory neurons.")
        public int inhibitoryNeurons = 1000;

        @CommandLine.Option(names = {"--config-file"}, description = "Path to a SNN configuration file.")
        public String configFilePath = "";

        @CommandLine.Option(names = {"--enable-STDP"}, description = "Use STDP for the model to learn.")
        boolean enableSTDP = false;

        @CommandLine.Option(names = {"--enable-lateral-inhibition"}, description = "Use lateral inhibition to silence nearby neurons.")
        boolean enableLateralInhibition = false;

        @CommandLine.Option(names = {"--image-path"}, description = "Path to the image file for Poisson spike generation.")
        public String imagePath = "";

        @CommandLine.Option(names = {"--max-firing-rate"}, description = "Maximum firing rate in Hz for Poisson spike generation.")
        double maxFiringRateHz = 0.0;

        @CommandLine.Option(names = {"--write-spike-output-csv"}, description = "Write spike output to a CSV file.")
        boolean writeSpikeOutputCsv = false;

        @CommandLine.Option(names = {"--inhibitory-strength"}, description = "The strength of inhibitory neurons.")
        public double inhibitoryStrength = 1.0;

        @CommandLine.Option(names = {"--excitatory-strength"}, description = "The strength of excitatory neurons.")
        public double excitatoryStrength = 25.0;

        @CommandLine.Option(names = {"--lambda"}, description = "The connection strength factor for neuron proximity connection.")
        public double lambda = 0.1;

        @CommandLine.Option(names = {"--refractory-period"}, description = "The refractory period of a neuron in ms.")
        public double refractoryPeriod = 2.0;

        @CommandLine.Option(names = {"--output-directory"}, description = "The directory to save output files to.")
        public String outputDirectory = "output/";

        @CommandLine.Option(names = {"--enable-heatmap"}, description = "Enable the heatmap visualization.", negatable = true)
        boolean enableHeatmap = true;

        @CommandLine.Option(names = {"-v"}, description = "Verbosity level")
        private boolean[] verbose = new boolean[0];


        @Override
        public void run() {

            if (!InputValidator.validate(this)) {
                return;
            }

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
