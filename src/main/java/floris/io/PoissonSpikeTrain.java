package floris.io;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PoissonSpikeTrain {
    private static final Logger LOGGER = LogManager.getLogger();

        private final double[] firingRates; // Vuursnelheid in Hz voor elke neuron.
        private final int numInputNeurons;
        private final Random random = new Random();

        /**
         * Maak Poisson spike train generator:
         * @param imagePath
         * @param numInputNeurons
         * @param maxFiringRateHz
         */
        public PoissonSpikeTrain(String imagePath, int numInputNeurons, double maxFiringRateHz) {
            this.numInputNeurons = numInputNeurons;
            this.firingRates = new double[numInputNeurons];
            try {
                loadImageAndSetRates(imagePath, maxFiringRateHz);
            } catch (IOException e) {
                System.err.println("Error loading image: " + e.getMessage());
                // Zet alle vuursnelheden op 0 als het niet werkt.
                for (int i = 0; i < numInputNeurons; i++) {
                    firingRates[i] = 0;
                }
            }
        }

        private void loadImageAndSetRates(String imagePath, double maxFiringRateHz) throws IOException {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));

            int side = (int) Math.sqrt(numInputNeurons);
            if (side * side != numInputNeurons) {
                System.err.println("Warning: Number of input neurons is not a perfect square. Using largest possible square.");
            }

            // Resize afbeelding
            BufferedImage resizedImage = new BufferedImage(side, side, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, side, side, null);
            g.dispose();

            // Zet grayscale (0-255) om naar vuursneheid (0-maxFiringRateHz)
            for (int y = 0; y < side; y++) {
                for (int x = 0; x < side; x++) {
                    int grayValue = new Color(resizedImage.getRGB(x, y)).getRed();
                    double normalizedValue = grayValue / 255.0; // normaliseer.

                    int neuronIndex = y * side + x;
                    if (neuronIndex < numInputNeurons) {
                        firingRates[neuronIndex] = normalizedValue * maxFiringRateHz;

                    }
                }
            }
            LOGGER.info("Finished loading image.");
        }


        /**
         * Genereer een boolean array met spikes voor een enkele tijdsstap.
         * Waarschijnlijkheid van vuren: (firingRate * dt).
         * @param dt
         * @return A Spike array (boolean)
         */
        public boolean[] generateSpikes(double dt) {
            boolean[] spikes = new boolean[numInputNeurons];
            // Vuur snelheid in Hz:
            double dtInSeconds = dt / 1000.0;

            for (int i = 0; i < numInputNeurons; i++) {
                double spikeProbability = firingRates[i] * dtInSeconds;
                if (random.nextDouble() < spikeProbability) {
                    spikes[i] = true;
                }
            }
            return spikes;
        }
}


