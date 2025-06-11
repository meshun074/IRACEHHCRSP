package org.example.GA;

import java.util.ArrayList;

public class RouteInitializer {
    private ArrayList<Integer> alleles;
    private double solutionCost;
    private double totalTravelCost;
    private double totalTardiness;
    private double highestTardiness;
    private ShiftUp[] caregiversRoute;
    private ArrayList<Double> caregiversCost;

    public RouteInitializer(ArrayList<Integer> alleles, double solutionCost) {
        this.alleles = alleles;
        this.solutionCost = solutionCost;
        caregiversCost = new ArrayList<>();
        totalTravelCost = 0;
        totalTardiness = 0;
        highestTardiness = 0;
    }

    public ArrayList<Integer> getAlleles() {
        return alleles;
    }

    public void setAlleles(ArrayList<Integer> alleles) {
        this.alleles = alleles;
    }

    public double getSolutionCost() {
        return solutionCost;
    }

    public void setSolutionCost(double solutionCost) {
        this.solutionCost = solutionCost;
    }

    public ArrayList[] getCaregiversRoute() {
        ArrayList[] routes = new ArrayList[caregiversRoute.length];
        ArrayList<String> route;
        for (int i = 0; i < caregiversRoute.length; i++) {
            route = new ArrayList<>(caregiversRoute[i].getRoute());
            route.removeFirst();
            routes[i] = route;
        }
        return routes;
    }

    public void setCaregiversRoute(ShiftUp[] caregiversRoute) {
        this.caregiversRoute = caregiversRoute;
    }

    public ArrayList<Double> getCaregiversCost() {
        return caregiversCost;
    }

    public void setCaregiversCost(ArrayList<Double> caregiversCost) {
        this.caregiversCost = caregiversCost;
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
    public void updateTotalTardiness(double totalTardiness) {
        this.totalTardiness += totalTardiness;
    }

    public double getHighestTardiness() {
        return highestTardiness;
    }

    public void setHighestTardiness(double highestTardiness) {
        this.highestTardiness = highestTardiness;
    }

}


