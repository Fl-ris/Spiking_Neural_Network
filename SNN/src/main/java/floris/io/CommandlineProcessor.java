package floris.io;

import picocli.CommandLine;


public class CommandlineProcessor {
    @CommandLine.Command(name = "SNN", version = "SNN-0.1", mixinStandardHelpOptions = true)
    public static class commandlineProcessor implements Runnable {

        @CommandLine.Option(names = { "-n", "--neuron-amount" }, description = "The amount of neurons for the network.")
        int neurons = 50;

        @CommandLine.Option(names = { "-t", "--simulation-time" }, description = "The amount of time in ms the SNN should run.")
        int simulationTime = 10000;

        @CommandLine.Option(names = { "-dt", "--time-step" }, description = "The size of dt for the LIF equation.")
        float dt = 0.1F;

        @CommandLine.Option(names = {"--input-neurons" }, description = "Amount of input neurons.")
        int inputNeurons = 5;

        @CommandLine.Option(names = {"--output-neurons" }, description = "Amount of output neurons.")
        int outputNeurons = 5;

        @CommandLine.Option(names = {"--inhibitory-neurons" }, description = "Amount of inhibitory neurons.")
        int inhibitoryNeurons = 5;


        @Override
        public void run() {
            System.out.println("test...");
            System.out.println(dt);
        }

        public static void main(String[] args) {
            int exitCode = new CommandLine(new commandlineProcessor()).execute(args);
            System.exit(exitCode);
        }
    }
}
