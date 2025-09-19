package floris.io;

import picocli.CommandLine;


public class CommandlineProcessor {
    @CommandLine.Command(name = "SNN", version = "SNN-0.1", mixinStandardHelpOptions = true)
    public static class ASCIIArt implements Runnable {

        @CommandLine.Option(names = { "-n", "--neuron-amount" }, description = "The amount of neurons for the network.")
        int neurons = 50;

        @CommandLine.Parameters(paramLabel = "<word>", defaultValue = "Hello, picocli",
                description = "Words to be translated into ASCII art.")

        private String[] words = { "Hello,", "picocli" };

        @Override
        public void run() {

        }

        public static void main(String[] args) {
            int exitCode = new CommandLine(new ASCIIArt()).execute(args);
            System.exit(exitCode);
        }
    }
}
