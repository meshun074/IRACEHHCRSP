package org.example.GA;

import org.example.Data.InstancesClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.example.GA.EvaluationFunctionUp.EvaluateFitness;

public class Mutation1 implements Runnable {
    private final GeneticAlgorithm ga;
    private final Random rand;
    private final Chromosome original;
    private final InstancesClass data;
    public Mutation1(GeneticAlgorithm ga, Chromosome original, Random rand, InstancesClass data) {
        this.ga = ga;
        this.original = original;
        this.rand = rand;
        this.data = data;
    }
    private Chromosome mutation() {
        // Create mutated chromosome (shallow copy first)
        Chromosome mutated = new Chromosome(original.getGenes(), original.getFitness(), true);

        ArrayList<String>[] genes = mutated.getGenes();
        int selectedRoute=0;
        ArrayList<String> route=null;
        int end=3;
        do {
            end--;
            if(route!=null){
                genes[selectedRoute] = route;
            }
            // Select random route to mutate
            selectedRoute = rand.nextInt(genes.length);
            route = genes[selectedRoute];
            int routeSize = route.size();

            // Only mutate if route has at least 2 elements
            if (routeSize > 1) {
                // Create working copy of the route
                ArrayList<String> newRoute = new ArrayList<>(route);

                // Apply mutation based on route size
                if (routeSize == 2) {
                    // Simple swap for size 2
                    Collections.reverse(newRoute);
                } else {
                    int index = rand.nextInt(routeSize);

                    if (routeSize - index == 1) {
                        // Handle end of route cases
                        swap(newRoute, index, rand.nextBoolean() ? index - 1 : index - 2);
                    } else if (routeSize - index == 2) {
                        swap(newRoute, index, index + 1);
                    } else {
                        swap(newRoute, index, rand.nextBoolean() ? index + 1 : index + 2);
                    }
                }

                // Update the route (only if changed)
                genes[selectedRoute] = newRoute;
            }

            // Evaluate fitness
            EvaluateFitness(Collections.singletonList(mutated), data);
        }
        while (mutated.getFitness() == Double.POSITIVE_INFINITY&&end>0);

        // Return original if invalid solution
        return mutated.getFitness() == Double.POSITIVE_INFINITY ? original : mutated;
    }

    // Helper method for swapping elements
    private static void swap(ArrayList<String> list, int i, int j) {
        GeneticAlgorithm.swapPatients(list, i, j);
    }
    @Override
    public void run() {
        ga.getMutationChromosomes().add(mutation());
    }
}

