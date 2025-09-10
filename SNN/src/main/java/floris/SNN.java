package floris;


public class SNN {

    int potential_threshold = 60;
    int rest_membrane_potential = 50;
    double initial_membrane_potential = 0.9;
    int membrane_leak = 5; // 5 ms
    int neurons = 5;

    double dt = 0.1;

    int simulation_time = 1;
    double sim_steps = simulation_time * dt;

    int[][] spike_array



    double[][] test = new double[neurons][neurons];


    public static void main(String[] args) {
        double dt = 0.1;

        int simulation_time = 1000;
        double sim_steps = simulation_time * dt;

        System.out.println(sim_steps);



    }

}
