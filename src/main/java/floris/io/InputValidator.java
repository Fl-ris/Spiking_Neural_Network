package floris.io;

import floris.config.CommandlineProcessor;

import java.io.File;

public class InputValidator {

    public static boolean validate(CommandlineProcessor.commandlineProcessor processor) {
        if (processor.neurons <= 0) {
            System.err.println("Error: Number of neurons must be positive.");
            return false;
        }
        if (processor.simulationTime <= 0) {
            System.err.println("Error: Simulation time must be positive.");
            return false;
        }
        if (processor.dt <= 0) {
            System.err.println("Error: Time step (dt) must be positive.");
            return false;
        }
        if (processor.inputNeurons <= 0 || processor.inputNeurons >= processor.neurons) {
            System.err.println("Error: Number of input neurons must be positive and less than the total number of neurons.");
            return false;
        }
        if (processor.outputNeurons <= 0 || processor.outputNeurons >= processor.neurons) {
            System.err.println("Error: Number of output neurons must be positive and less than the total number of neurons.");
            return false;
        }
        if (processor.inhibitoryNeurons < 0 || processor.inhibitoryNeurons >= processor.neurons) {
            System.err.println("Error: Number of inhibitory neurons must not be negative and less than the total number of neurons.");
            return false;
        }
        if (processor.inputNeurons + processor.outputNeurons + processor.inhibitoryNeurons >= processor.neurons) {
            System.err.println("Error: The sum of input, output, and inhibitory neurons must be less than the total number of neurons.");
            return false;
        }
        if (processor.configFilePath != null && !processor.configFilePath.isBlank()) {
            File configFile = new File(processor.configFilePath);
            if (!configFile.exists()) {
                System.err.println("Error: Config file not found: " + processor.configFilePath);
                return false;
            }
        }
        if (processor.imagePath != null && !processor.imagePath.isBlank()) {
            File imageFile = new File(processor.imagePath);
            if (!imageFile.exists()) {
                System.err.println("Error: Image file not found: " + processor.imagePath);
                return false;
            }
        }
        return true;
    }
}
