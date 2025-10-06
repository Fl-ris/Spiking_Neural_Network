package floris.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SynapseImporter {

        public static void main(String[] args, String filePath) {

            //File myObj = new File("/home/floris/Documenten/git_repo/Spiking_Neural_Network/config/small_brain_1.conf"); // test
            File myObj = new File(filePath);

            try (Scanner myReader = new Scanner(myObj)) {
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    System.out.println(data);
                }

            } catch (FileNotFoundException e) {
                System.out.println("IO Error...");
            }

            verifyMatrix();

            parseSynapseMatrix();

        }

    /**
     *  Controlleer de grootte van de geimporteerde synapese matrix.
     */
    public static void verifyMatrix(){
             // test...
            double[][] synapsesMatrix = new double[][]{
                    {0.0, 0.0, 0.0},
            };

            System.out.println("Rows: " + synapsesMatrix.length);
            System.out.println("Cols:" + synapsesMatrix[0].length);
        }

        public static String[] parseSynapseMatrix(){
            String teststring =
                    """
                       Neuron_Count: 120,
                       Input_Neurons: 23,
                       Output_Neurons: 23,
                       Inhibitory_Neurons: 32,
                       Simulation_Time: 1000,
                       dt: 0.1,
                    """;

            String[] neurons = (teststring.split("\\bNeuron_Count:\\s*(\\d+)")); // To-do: regex verder uitwerken...
            String[] inputNeurons = teststring.split("[A-Z]");
            String[] outputNeurons = teststring.split("[A-Z]");
            String[] inhibitoryNeurons = teststring.split("[A-Z]");

            for (String i : neurons) {
              //  System.out.println(i);
            }


            return neurons;
        }


}