package floris.model;


public interface ImportedSynapseMatrix {
    float dt();
    int simulationTime();
    int neurons();
    int inputNeurons();
    int outputNeurons();
    int inhibitoryNeurons();
    boolean enableSTDP();
    boolean enableLateralInhibition();
    String configFilePath();
    String imagePath();
    double maxFiringRateHz();
}
