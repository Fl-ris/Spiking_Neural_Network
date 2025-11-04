package floris.config;


import floris.model.ImportedSynapseMatrix;

public record NetworkParameters(float dt,
                                int simulationTime,
                                int neurons,
                                int inputNeurons,
                                int outputNeurons,
                                int inhibitoryNeurons,
                                boolean enableSTDP,
                                boolean enableLateralInhibition,
                                String configFilePath,
                                String imagePath,
                                double maxFiringRateHz,
                                boolean writeSpikeOutputCsv,
                                double inhibitoryStrength,
                                double excitatoryStrength,
                                double lambda,
                                double refractoryPeriod,
                                String outputDirectory,
                                boolean enableHeatmap) implements ImportedSynapseMatrix {
}
