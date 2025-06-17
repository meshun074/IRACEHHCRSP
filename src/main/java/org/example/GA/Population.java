package org.example.GA;

import org.example.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Population {
    public static ArrayList<Chromosome> initialize(int popSize, int chLength) {
        ArrayList<Integer> patients= new ArrayList<>();
        ArrayList<Integer> patientOrder;
        RouteInitializer ri;
        ArrayList<Chromosome> population = new ArrayList<>();
        for (int s = 1; s <= chLength; s++) {
            patients.add(s);
        }
        for (int i = 0; i < popSize; i++) {
            patientOrder = new ArrayList<>(patients);
            Collections.shuffle(patientOrder);
            ri = new RouteInitializer(patientOrder,0.0);
            AssignPatients.Assign(ri, Main.instance);
            population.add(new Chromosome(ri.getCaregiversRoute(),ri.getSolutionCost()));
            population.getLast().setTotalTravelCost(ri.getTotalTravelCost());
            population.getLast().setTotalTardiness(ri.getTotalTardiness());
            population.getLast().setHighestTardiness(ri.getHighestTardiness());
        }
//        System.out.println("population");
        return population;
    }
}

