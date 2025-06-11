package org.example.GA;

import java.util.*;

public class Chromosome {
    private int caregivers;
    private double fitness;
    private double totalTravelCost;
    private double totalTardiness;
    private double highestTardiness;
    private ArrayList[] genes;

    private ShiftUp[] caregiversRouteUp;
    private final Map<String, Set<Integer>> patientToRoutesMap = new HashMap<>();
    public Chromosome(int caregivers) {
        this.caregivers = caregivers;
        fitness = 0;
        genes = new ArrayList[caregivers];
        for (int i = 0; i < caregivers; i++) {
            genes[i] = new ArrayList();
        }
        caregiversRouteUp = new ShiftUp[caregivers];
    }

    public Chromosome(ArrayList[] genes, double fitness) {
        this.genes = genes;
        this.caregivers = genes.length;
        this.fitness = fitness;
        caregiversRouteUp = new ShiftUp[caregivers];
    }
    public Chromosome(ArrayList[] genes, double fitness, boolean newChromosome) {
        this.genes = new ArrayList[genes.length];
        for (int i = 0; i < genes.length; i++) {
            this.genes[i] = new ArrayList(genes[i]);
        }
        this.caregivers = genes.length;
        this.fitness = fitness;
        caregiversRouteUp = new ShiftUp[caregivers];
        this.totalTravelCost = 0;
        this.totalTardiness = 0;
        this.highestTardiness = 0;
    }


    public int getCaregivers() {
        return caregivers;
    }




    // Call this once when `genes` is initialized or updated
    public void buildPatientRouteMap() {
        patientToRoutesMap.clear();
        Set<String> patients;
        for (int i = 0; i < genes.length; i++) {
            patients = new HashSet<>(genes[i]);
            for (String patient : patients) {
                patientToRoutesMap.computeIfAbsent(patient, k -> new HashSet<>()).add(i);
            }
        }
    }

    public  Map<String, Set<Integer>> getPatientToRoutesMap(){
        return patientToRoutesMap;
    }

    public Set<Integer> getPatientRoutes(String patient) {
        return patientToRoutesMap.get(patient);
    }

    public void setCaregivers(int caregivers) {
        this.caregivers = caregivers;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public ArrayList[] getGenes() {
        return genes;
    }

    public void setGenes(ArrayList[] genes) {
        this.genes = genes;
    }

    public void setCaregiversRouteUp(ShiftUp[] caregiversRouteUp) {
        this.caregiversRouteUp = caregiversRouteUp;
    }
    public ShiftUp[] getCaregiversRouteUp() {
        return caregiversRouteUp;
    }

    public double getTotalTravelCost() {
        return totalTravelCost;
    }

    public void setTotalTravelCost(double totalTravelCost) {
        this.totalTravelCost = totalTravelCost;
    }
    public void updateTotalTravelCost(double totalTravelCost) {
        this.totalTravelCost += totalTravelCost;
    }

    public double getTotalTardiness() {
        return totalTardiness;
    }

    public void setTotalTardiness(double totalTardiness) {
        this.totalTardiness = totalTardiness;
    }

    public double getHighestTardiness() {
        return highestTardiness;
    }

    public void setHighestTardiness(double highestTardiness) {
        this.highestTardiness = highestTardiness;
    }
    public void updateTotalTardiness(double totalTardiness) {
        this.totalTardiness += totalTardiness;
    }

    public void showSolution(int index) {
        System.out.print("\n Best Solution : "+index+"\n");
        for (int i =0; i< genes.length; i++) {
            ArrayList<String> route = genes[i];
            ShiftUp Caregiver = caregiversRouteUp[i];
            System.out.println(Caregiver.getCaregiver().getId() +" - "+ route);
            System.out.println("Travel Cost to patients\n"+Caregiver.getTravelCost());
            System.out.println("Service completed time at patients\n"+Caregiver.getCurrentTime());
            System.out.println("Route total tardiness: "+Caregiver.getTardiness().getLast()+" Route Highest tardiness: "+Caregiver.getMaxTardiness().getLast());
            System.out.println();
        }

    }
    public String toString() {
        StringBuilder genesStrings= new StringBuilder();
        for(ArrayList c :genes){
            genesStrings.append(c.toString());
        }
        return genesStrings.toString();
    }
}

