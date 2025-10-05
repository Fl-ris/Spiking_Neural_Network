package floris.visualizer;

import model.SNN;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class NetworkHeatmap {
    private JFrame frame;
    private View view;
    private boolean init;

    static class View extends JPanel {
        final int n, cols, rows;
        final BufferedImage img;
        final int[] pix;
        final int[] neuron2pix;

        View(int neurons) {
            setBackground(Color.black);

            this.n = neurons;
            this.cols = (int) Math.ceil(Math.sqrt(n));
            this.rows = (n + cols - 1) / cols;

            this.img = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_ARGB);
            this.pix = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

            this.neuron2pix = new int[n];
            Arrays.fill(neuron2pix, -1);
            for (int i = 0; i < n; i++) {
                int y = i / cols;
                int x = i % cols;
                int pixIdx = y * cols + x;
                if (pixIdx >= 0 && pixIdx < pix.length) {
                    neuron2pix[i] = pixIdx;
                }
            }

            Arrays.fill(pix, 0xFF000000);
        }

        void updateSpikes(boolean[] spikes) {
            Arrays.fill(pix, 0xFF000000);

            if (spikes != null) {
                int len = Math.min(spikes.length, n);
                for (int i = 0; i < len; i++) {
                    if (spikes[i]) {
                        int p = neuron2pix[i];
                        if (p >= 0) {
                            pix[p] = 0xFFFF0000;
                        }
                    }
                }
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public void initialize(SNN net) {
        if (init) return;

        SwingUtilities.invokeLater(() -> {
            view = new View(net.neurons);

            frame = new JFrame("SNN Activity");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(view);
            frame.setSize(900, 900);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        init = true;
    }

    public void update(boolean[] spikes) {
        if (!init) return;

        boolean[] spikesCopy;
        if (spikes != null) {
            spikesCopy = Arrays.copyOf(spikes, spikes.length);
        } else {
            spikesCopy = null;
        }

        SwingUtilities.invokeLater(() -> {
            if (view != null) {
                view.updateSpikes(spikesCopy);
            }
        });
    }

    public void addDelay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}