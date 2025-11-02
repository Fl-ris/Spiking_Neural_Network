package floris.visualizer;

import floris.model.SNN;
import javax.swing.*;
import java.awt.*;

public class NetworkHeatmap {
    private JFrame frame;
    private JPanel panel;
    private int neurons, cols, rows;

    private boolean[] isInput;
    private boolean[] isOutput;
    private boolean[] isInhibitory;
    private boolean[] currentSpikes;
    private double[] currentVoltages;

    private enum NeuronType {
        INPUT(new Color(33, 225, 33), Color.GREEN),
        OUTPUT(Color.BLUE, Color.CYAN),
        INHIBITORY(new Color(255, 0, 0), Color.RED),
        EXCITATORY(Color.DARK_GRAY, Color.YELLOW);

        final Color restColor;
        final Color spikeColor;

        NeuronType(Color restColor, Color spikeColor) {
            this.restColor = restColor;
            this.spikeColor = spikeColor;
        }
    }

    public void initialize(SNN net) {
        this.neurons = net.simulationParameters.neurons;
        this.cols = (int) Math.ceil(Math.sqrt(neurons));
        this.rows = (neurons + cols - 1) / cols;

        this.isInput = net.synapseArray.isInput;
        this.isOutput = net.synapseArray.isOutput;
        this.isInhibitory = net.synapseArray.isInhibitory;

        SwingUtilities.invokeLater(this::createUI);
    }

    private void createUI() {
        panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintNeurons(g);
            }
        };
        panel.setBackground(Color.BLACK);

        frame = new JFrame("SNN");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setSize(900, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    // Bepaal cel grootte:
    private void paintNeurons(Graphics g) {
        if (currentSpikes == null || currentVoltages == null) return;

        int cellWidth = panel.getWidth() / cols;
        int cellHeight = panel.getHeight() / rows;

        for (int i = 0; i < neurons && i < currentSpikes.length; i++) {
            paintNeuron(g, i, cellWidth, cellHeight);
        }
    }

    private void paintNeuron(Graphics g, int index, int cellWidth, int cellHeight) {
        int x = (index % cols) * cellWidth;
        int y = (index / cols) * cellHeight;
        boolean isSpiking = currentSpikes[index];

        // Teken neuronen:
        NeuronType type = getNeuronType(index);
        g.setColor(isSpiking ? type.spikeColor : type.restColor);
        g.fillRect(x, y, cellWidth, cellHeight);

        // Maak grid lijnen:
        g.setColor(Color.BLACK);
        g.drawRect(x, y, cellWidth, cellHeight);

        // Weergeef de text:
        drawVoltageText(g, x, y, cellWidth, cellHeight, index, isSpiking);
    }

    private NeuronType getNeuronType(int index) {
        if (isInput[index]) return NeuronType.INPUT;
        if (isOutput[index]) return NeuronType.OUTPUT;
        if (isInhibitory[index]) return NeuronType.INHIBITORY;
        return NeuronType.EXCITATORY;
    }

    private void drawVoltageText(Graphics g, int x, int y, int width, int height,
                                 int neuronIndex, boolean isSpiking) {
        g.setColor(isSpiking ? Color.BLACK : Color.WHITE);

        String voltage = String.format("%.2f", currentVoltages[neuronIndex]);
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (width - fm.stringWidth(voltage)) / 2;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(voltage, textX, textY);
    }

    public void update(boolean[] spikes, double[] voltages) {
        currentSpikes = spikes;
        currentVoltages = voltages;
        SwingUtilities.invokeLater(panel::repaint);
    }

    public void addDelay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}