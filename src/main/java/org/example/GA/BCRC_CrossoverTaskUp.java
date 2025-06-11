package org.example.GA;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.GA.EvaluationFunctionUp.*;
import static org.example.GA.GeneticAlgorithm.conflictCheck;

public class BCRC_CrossoverTaskUp implements Runnable {
    private final GeneticAlgorithm ga;
    private final boolean cross;
    private final int r;
    private final Chromosome p1, p2;
    private final InstancesClass data;
    private final Patient[] allPatients;
    private final double[][] distanceMatrix;
    private final Random rand;

    public BCRC_CrossoverTaskUp(GeneticAlgorithm ga,  Chromosome p1, Chromosome p2, int r, boolean cross, InstancesClass data) {
        this.ga = ga;
        this.cross = cross;
        this.p1 = p1;
        this.p2 = p2;
        this.r = r;
        this.data = data;
        rand = ThreadLocalRandom.current();
        this.allPatients = data.getPatients();
        this.distanceMatrix = data.getDistances();
    }

    public Chromosome Crossover() {
        if (!cross) {
            return p1;
        }
        Chromosome c2Temp;
        ArrayList[] p1Routes, c1Routes;
        ArrayList<String> route, route1, tempRoute1,
                tempRoute2;
        String patient;
        Patient p;
//        int patientLength = allPatients.length;
//        int size = 40;
        /*
        100 - 20 1757.859
        50 - 40
        50-20--1749.651
        */
        Set<String> selectRoute = new HashSet<>(p2.getGenes()[r]);

        p1Routes = p1.getGenes();
        c1Routes = new ArrayList[p1.getGenes().length];
        //removing patients of selected route from parent routes
        for (int i = 0; i < p1Routes.length; i++) {
            route = new ArrayList<>();
            for (int j = 0; j < p1Routes[i].size(); j++) {
                patient = (String) p1Routes[i].get(j);
                if (!selectRoute.contains(patient)) {
                    route.add(patient);
                }
            }
            c1Routes[i] = new ArrayList<>(route);
        }
        // inserting removed route.
        route1 = new ArrayList<>(selectRoute);
        //Collections.shuffle(route1, rand);
        String service1, service2;
        Set<Integer> caregivers1, caregivers2;
        MoveUp move1, bestMove;
        Set<String> listOfMoves;
        String moveSign1, moveSign2;
        ArrayList<Process> processes;
        Process process;
        c2Temp = new Chromosome(c1Routes, 0.0, true);
        EvaluateFitness(Collections.singletonList(c2Temp), data);

        for (int y = 0; y < route1.size(); y++) {
            String s = route1.get(y);
            bestMove = null;
            p = allPatients[getIdOfObject(s)];
            service1 = p.getRequired_caregivers()[0].getService();
            if (p.getRequired_caregivers().length > 1) {
                listOfMoves = new HashSet<>();
                service2 = p.getRequired_caregivers()[1].getService();
                caregivers1 = data.getQualifiedCaregiver(service1);
                caregivers2 = data.getQualifiedCaregiver(service2);
                boolean isSeq = p.getSynchronization().getType().equals("sequential");
                //set the hashset of the chromosome genes
                c2Temp.buildPatientRouteMap();

                for (int k : caregivers1) {
                    for (int l : caregivers2) {
                        if (k != l) {

                            for (int m = 0; m <= c1Routes[k].size(); m++) {
                                for (int n = 0; n <= c1Routes[l].size(); n++) {
                                    if (isSeq||noEvaluationConflicts(c1Routes[k], c1Routes[l], m, n)) {
                                        tempRoute1 = new ArrayList<>(c1Routes[k]);
                                        tempRoute2 = new ArrayList<>(c1Routes[l]);
                                        tempRoute1.add(m, s);
                                        tempRoute2.add(n, s);
                                        processes = new ArrayList<>();
                                        moveSign1 = tempRoute1 + " - " + tempRoute2;
                                        moveSign2 = tempRoute2 + " - " + tempRoute1;
                                        if (!listOfMoves.contains(moveSign1) && !listOfMoves.contains(moveSign2)) {
                                            process = new Process(tempRoute1, s, m, k);
                                            processes.add(process);
                                            process = new Process(tempRoute2, s, n, l);
                                            processes.add(process);
                                            move1 = new MoveUp(processes);
                                            bestMove = evaluateMoveUp(move1, bestMove, c2Temp);
                                            listOfMoves.add(moveSign1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                if (bestMove != null) {
                    ArrayList<Process> processesList = bestMove.getProcesses();
                    for (int z = 0; z < processesList.size(); z++) {
                        Process process1 = processesList.get(z);
                        c1Routes[process1.getRouteIndex()] = new ArrayList<>(process1.getRoute());
                    }
                    c2Temp = bestMove.getChromosome();
                }

            } else {
                caregivers1 = data.getQualifiedCaregiver(service1);
                //set the Map patient ot route of the chromosome genes
                c2Temp.buildPatientRouteMap();
                for (int j : caregivers1) {
                    for (int k = 0; k <= c1Routes[j].size(); k++) {
                        tempRoute1 = new ArrayList(c1Routes[j]);
                        tempRoute1.add(k, s);
                        processes = new ArrayList<>();
                        process = new Process(tempRoute1, s, k, j);
                        processes.add(process);
                        move1 = new MoveUp(processes);
                        bestMove = evaluateMoveUp(move1, bestMove, c2Temp);
                    }

                }

                if (bestMove != null) {
                    ArrayList<Process> processesList = bestMove.getProcesses();
                    for (int z = 0; z < processesList.size(); z++) {
                        Process process1 = processesList.get(z);
                        c1Routes[process1.getRouteIndex()] = new ArrayList<>(process1.getRoute());
                    }
                    c2Temp = bestMove.getChromosome();
                }
            }
        }
        return c2Temp;
        //return p1;
    }

    private MoveUp evaluateMoveUp(MoveUp m, MoveUp bestMove, Chromosome c) {
        Chromosome tempCh = new Chromosome(c.getGenes(), 0.0, true);
        int[] routeEndPoint = new int[c.getGenes().length];
        Arrays.fill(routeEndPoint, -1);
        ArrayList<Process> processesList = m.getProcesses();
        for (int z = 0; z < processesList.size(); z++) {
            Process process = processesList.get(z);
            routeEndPoint[process.getRouteIndex()] = process.getInsertPosition();
        }


        removeAffectedPatientsUp(m, c, routeEndPoint);
        int index;
        for (int i = 0; i < routeEndPoint.length; i++) {
            ArrayList<String> route;
            ArrayList<Double> currentTime;
            ArrayList<Double> travelCost;
            ArrayList<Double> tardiness;
            ArrayList<Double> maxTardiness;
            route = new ArrayList<>(c.getCaregiversRouteUp()[i].getRoute());
            currentTime = new ArrayList<>(c.getCaregiversRouteUp()[i].getCurrentTime());
            travelCost = new ArrayList<>(c.getCaregiversRouteUp()[i].getTravelCost());
            travelCost.removeLast();
            tardiness = new ArrayList<>(c.getCaregiversRouteUp()[i].getTardiness());
            maxTardiness = new ArrayList<>(c.getCaregiversRouteUp()[i].getMaxTardiness());
            if (routeEndPoint[i] != -1) {
                index = routeEndPoint[i] + 1;
                route.subList(index, route.size()).clear();
                travelCost.subList(index, travelCost.size()).clear();
                currentTime.subList(index, currentTime.size()).clear();
                tardiness.subList(index, tardiness.size()).clear();
                maxTardiness.subList(index, maxTardiness.size()).clear();
                tempCh.getCaregiversRouteUp()[i] = new ShiftUp(c.getCaregiversRouteUp()[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
            } else {
                tempCh.getCaregiversRouteUp()[i] = new ShiftUp(c.getCaregiversRouteUp()[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
            }
        }
        //changing routes with move routes
        for (int z = 0; z < processesList.size(); z++) {
            Process process = processesList.get(z);
            tempCh.getGenes()[process.getRouteIndex()] = process.getRoute();
        }

        double totalTravelCost = 0;
        double totalTardiness = 0;
        double highestTardiness = 0;
        for (ShiftUp s : tempCh.getCaregiversRouteUp()) {
            totalTravelCost += s.getTravelCost().getLast();
            totalTardiness += s.getTardiness().getLast();
            highestTardiness = Math.max(highestTardiness, s.getMaxTardiness().getLast());
        }

        tempCh.setTotalTravelCost(totalTravelCost);
        tempCh.setTotalTardiness(totalTardiness);
        tempCh.setHighestTardiness(highestTardiness);
        tempCh.setFitness(0.0);


        evaluateUp(tempCh, routeEndPoint, bestMove);
        if (bestMove == null || tempCh.getFitness() < bestMove.getFitness()||tempCh.getFitness() == bestMove.getFitness()&&rand.nextBoolean()) {
            m.setChromosome(tempCh);
            m.setFitness(tempCh.getFitness());
            return m;
        }
        return bestMove;
    }



    private void evaluateUp(Chromosome ch, int[] routeEndPoint, MoveUp bestMove) {
        ArrayList<String> route;
        ShiftUp[] routes = ch.getCaregiversRouteUp();
        ShiftUp caregiver1;
        int routeEnd;
        Set<String> track = new LinkedHashSet<>();
        for (int i = 0; i < routeEndPoint.length; i++) {
            route = new ArrayList<>(ch.getGenes()[i]);
            caregiver1 = routes[i];
            routeEnd = routeEndPoint[i];
            if (routeEnd != -1) {
                for (int j = routeEnd; j < route.size(); j++) {
                    String patient = route.get(j);
                    if (!caregiver1.getRoute().contains(patient)) {
                        if (!patientAssignment(ch, patient, caregiver1, routes, i, track)) {
                            ch.setFitness(Double.POSITIVE_INFINITY);
                            return;
                        }
                        UpdateCost(ch);
                        if (bestMove != null && ch.getFitness() > bestMove.getFitness()) {
                            return;
                        }
                        track.clear();
                    }
                }
            }
        }
        for (ShiftUp s : routes) {
            ch.updateTotalTravelCost(distanceMatrix[getIdOfObjectLocation(s.getRoute().getLast())][0]);
            s.updateTravelCost(distanceMatrix[getIdOfObjectLocation(s.getRoute().getLast())][0]);
        }
        UpdateCost(ch);
    }


    private static void UpdateCost(Chromosome ch) {
        ch.setFitness((1 / 3d * ch.getTotalTravelCost()) + (1 / 3d * ch.getTotalTardiness()) + (1 / 3d * ch.getHighestTardiness()));
    }

    private static int getIdOfObjectLocation(String s) {
        return Integer.parseInt(s.substring(1));
    }

    private void removeAffectedPatientsUp(MoveUp r, Chromosome c, int[] affectedRoutes) {
        int startPos;
        String patientId;
        Map<String, Set<Integer>> patientToRoutesMap = c.getPatientToRoutesMap();
        ArrayList<Process> processList = r.getProcesses();
        for (int z = 0; z < processList.size(); z++) {
            Process process = processList.get(z);
            ArrayList<String> currentRoute = c.getGenes()[process.getRouteIndex()];
            startPos = process.getInsertPosition();
            for (int i = startPos; i < currentRoute.size(); i++) {
                patientId = currentRoute.get(i);
                Patient p = allPatients[getIdOfObject(patientId)];
                if (p.getRequired_caregivers().length > 1) {
                    int routeIndex = getRouteIndexMethod(process.getRouteIndex(), patientToRoutesMap.get(p.getId()));
                    int patientIndex = c.getGenes()[routeIndex].indexOf(p.getId());
                    ArrayList<Process> processBuffer = new ArrayList<>();

                    if (affectedRoutes[routeIndex] == -1 || affectedRoutes[routeIndex] > patientIndex) {
                        affectedRoutes[routeIndex] = patientIndex;
                        processBuffer.add(new Process(new ArrayList<>(c.getGenes()[routeIndex]), p.getId(), patientIndex, routeIndex));
                        removeAffectedPatientsUp(new MoveUp(processBuffer), c, affectedRoutes);
                    }

                }
            }
        }
    }



    private int getRouteIndexMethod(int route1, Set<Integer> routes) {
        if (routes == null) return -1; // Patient not found
        for (int route : routes) {
            if (route != route1) {
                return route; // Return the first alternative route
            }
        }
        return -1; // No alternative route found
    }



    private boolean noEvaluationConflicts(ArrayList<String> c1Route, ArrayList<String> c2Route, int m, int n) {
        return conflictCheck(c1Route, c2Route, m, n);
    }

    @Override
    public void run() {
        ga.getCrossoverChromosomes().add(Crossover());
    }
}

