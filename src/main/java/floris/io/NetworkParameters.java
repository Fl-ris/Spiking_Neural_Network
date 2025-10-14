package floris.io;


public record NetworkParameters(float dt,
                                int simulationTime,
                                int neurons,
                                int inputNeurons,
                                int outputNeurons,
                                int inhibitoryNeurons,
                                String configFilePath) implements ImportedSynapseMatrix {
}
