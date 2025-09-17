package floris.visualizer;

import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.SwingWrapper;

public class heatmap {
    public static void plot(double[][] matrix) {

    int rows = matrix.length;
    int cols = matrix[0].length;

    int[] xKeys = new int[cols];
    int[] yKeys = new int[rows];
    int[][] zValues = new int[rows][cols];

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            zValues[rows - 1 - i][j] = (int) matrix[i][j];
        }
    }

    HeatMapChart chart = new HeatMapChartBuilder()
            .title("Heatmap")
            .build();

    chart.addSeries("heat", xKeys, yKeys, zValues);

    new SwingWrapper<>(chart).displayChart();
}

}
