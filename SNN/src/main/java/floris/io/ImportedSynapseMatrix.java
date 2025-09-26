package floris.io;


public interface ImportedSynapseMatrix {
    float dt();
    int simulationTime();
    int neurons();
    int inputNeurons();
    int outputNeurons();
    int inhibitoryNeurons();
}
