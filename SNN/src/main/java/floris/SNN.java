package floris;

import java.util.Arrays;



public class SNN {
    byte potentialThreshold = -50;
    byte restMembranePotential = -65;
    byte membraneResistance = 10; // Weerstand van membraan in mega Ohm.

    double initialMembranePotential = 60;
    double dv;
    double v;

    int membraneLeak = 10; // 10 ms
    int neurons = 5;

    double dt = 0.1;
    int simulationTime = 1;
    double simSteps = simulationTime / dt;

    int[][] spikeArray;

    double[][] test = new double[neurons][neurons];



    public static void main(String[] args) {
        SNN network = new SNN();

        int[] v = new int[10];
        Arrays.fill(v, 0);

        v[0] = 60;

        System.out.println(v[0]);




       // System.out.println(network.v);
        // Delta membraan voltage:
        //double dv = -(V-El)  * network.dt




        for (int i = 0; i < 20; i++) {
            System.out.println(i);

            network.dv = ((-(network.v - network.restMembranePotential) + network.membraneResistance))
            
        }

//            System.out.println(i);
         //   int V = (-network.initial_membrane_potential );

     //   }

     //  System.out.println(network.sim_steps);









    }

}
