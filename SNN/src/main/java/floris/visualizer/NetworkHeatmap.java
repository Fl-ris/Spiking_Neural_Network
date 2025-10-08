package floris.visualizer;

import floris.model.SNN;

import javax.swing.*;
import java.awt.*;

public class NetworkHeatmap {
    private JFrame frame;
    private JPanel panel;
    private int neurons, cols, rows;

    public void initialize(SNN net) {
        this.neurons = net.neurons;
        this.cols = (int) Math.ceil(Math.sqrt(neurons));
        this.rows = (neurons + cols - 1) / cols;

        SwingUtilities.invokeLater(() -> {
            panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    paintGrid(g);
                }
            };
            panel.setBackground(Color.BLACK);

            frame = new JFrame("SNN");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.setSize(900, 900);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private boolean[] currentSpikes;

    private void paintGrid(Graphics g) {
        if (currentSpikes == null) return;

        int w = panel.getWidth() / cols;
        int h = panel.getHeight() / rows;

        for (int i = 0; i < neurons && i < currentSpikes.length; i++) {
            g.setColor(currentSpikes[i] ? Color.RED : Color.BLACK);
            g.fillRect((i % cols) * w, (i / cols) * h, w, h);
        }
    }

    public void update(boolean[] spikes) {
        if (panel == null) return;
        currentSpikes = spikes;
        SwingUtilities.invokeLater(() -> panel.repaint());
    }

    public void addDelay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}