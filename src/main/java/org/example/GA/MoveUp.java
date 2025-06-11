package org.example.GA;

import java.util.ArrayList;

public class MoveUp {
    private Chromosome chromosome;
    private double fitness;
    private final ArrayList<Process> processes;

    public MoveUp(ArrayList<Process> processes ) {
        this.processes = processes;
        this.fitness = 0;
    }

    public ArrayList<Process> getProcesses() {
        return processes;
    }

    public Chromosome getChromosome() {
        return chromosome;
    }

    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

}

