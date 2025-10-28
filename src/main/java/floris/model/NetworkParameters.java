package floris.model;


public record NetworkParameters(float dt,
                                int simulationTime,
                                int neurons,
                                int inputNeurons,
                                int outputNeurons,
                                int inhibitoryNeurons,
                                boolean enableSTDP,
                                boolean enableLateralInhibition,
                                String configFilePath) implements ImportedSynapseMatrix {
}
