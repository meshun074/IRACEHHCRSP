package org.example.GA;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.example.GA.EvaluationFunctionUp.*;
import static org.example.GA.GeneticAlgorithm.conflictCheck;

public class BCRC_CrossoverTaskSwap implements Runnable {
    private final GeneticAlgorithm ga;
    private final long identity;
    private final boolean cross;
    private final int r;
    private final Chromosome p1, p2;
    private final InstancesClass data;
    private final Patient[] allPatients;
    private final double[][] distanceMatrix;
    private final Random rand;

    public BCRC_CrossoverTaskSwap(GeneticAlgorithm ga, long identity,  Chromosome p1, Chromosome p2, int r, boolean cross, InstancesClass data) {
        this.ga = ga;
        this.identity = identity;
        this.cross = cross;
        this.p1 = p1;
        this.p2 = p2;
        this.r = r;
        this.data = data;
        rand = ThreadLocalRandom.current();
        this.allPatients = data.getPatients();
        this.distanceMatrix = data.getDistances();
    }

    private Chromosome Crossover1() {
        if (!cross) {
            return p1;
        }
        Chromosome c2Temp = p1;
        Random rand = ThreadLocalRandom.current();
        ArrayList[] p1Routes, c1Routes;
        ArrayList<String> route, route1, tempRoute1,
                tempRoute2;
        String patient;
        Patient p;

        Set<String> selectRoute = new HashSet<>(p2.getGenes()[r]);
        c1Routes = new ArrayList[c2Temp.getGenes().length];
        route1 = new ArrayList<>(selectRoute);
        Collections.shuffle(route1, rand);

        for (int y = 0; y < route1.size(); y++) {
            String s = route1.get(y);
            p1Routes = c2Temp.getGenes();

            //removing patients of selected route from parent routes
            for (int i = 0; i < p1Routes.length; i++) {
                route = new ArrayList<>();
                for (int j = 0; j < p1Routes[i].size(); j++) {
                    patient = (String) p1Routes[i].get(j);
                    if (!s.equals(patient)) {
                        route.add(patient);
                    }
                }
                c1Routes[i] = new ArrayList<>(route);
            }
            // inserting removed route.

            String service1, service2;
            Set<Integer> caregivers1, caregivers2;
            MoveUp move1, bestMove;
            Set<String> listOfMoves;
            String moveSign1, moveSign2;
            ArrayList<Process> processes;
            Process process;
            c2Temp = new Chromosome(c1Routes, 0.0, true);
            EvaluateFitness(Collections.singletonList(c2Temp), data);
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
                                    if (isSeq || noEvaluationConflicts(c1Routes[k], c1Routes[l], m, n)) {
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
                                            bestMove = evaluateMoveUp(move1, bestMove, c2Temp, false);
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
                        bestMove = evaluateMoveUp(move1, bestMove, c2Temp, false);
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

    public Chromosome Crossover() {
        if (!cross) {
            return p1;
        }
        Chromosome c2Temp;
        Random rand = ThreadLocalRandom.current();
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
//        Set<String> selectRoute = new HashSet<>(size);

//        int sp;
//        while (selectRoute.size() < size) {
//            sp = rand.nextInt(patientLength);
//            selectRoute.add(allPatients[sp].getId());
//        }
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
        Collections.shuffle(route1, rand);
        String service1, service2;
        Set<Integer> caregivers1, caregivers2;
        MoveUp move1, bestMove;
        Set<String> listOfMoves;
        String moveSign1, moveSign2;
        ArrayList<Process> processes;
        Process process;
        c2Temp = new Chromosome(c1Routes, 0.0, true);
        EvaluateFitness(Collections.singletonList(c2Temp), data);

        boolean isInvalid;
        for (int y = 0; y < route1.size(); y++) {
            isInvalid = c2Temp.getFitness() == Double.POSITIVE_INFINITY;
//            System.out.println("Beginning of Loop "+c2Temp.getFitness()+ " Chromsome "+c2Temp);
//            System.out.println(isInvalid);
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
                                    if (isSeq || noEvaluationConflicts(c1Routes[k], c1Routes[l], m, n)) {
                                        tempRoute1 = new ArrayList<>(c1Routes[k]);
                                        tempRoute2 = new ArrayList<>(c1Routes[l]);
                                        tempRoute1.add(m, s);
                                        tempRoute2.add(n, s);
//                                        System.out.println(c1Routes[k]+" Temp route1 "+tempRoute1);
//                                        System.out.println(c1Routes[l]+" Temp route2 "+tempRoute2);
                                        processes = new ArrayList<>();
                                        moveSign1 = tempRoute1 + " - " + tempRoute2;
                                        moveSign2 = tempRoute2 + " - " + tempRoute1;
                                        if (!listOfMoves.contains(moveSign1) && !listOfMoves.contains(moveSign2)) {
                                            process = new Process(tempRoute1, s, m, k);
                                            processes.add(process);
                                            process = new Process(tempRoute2, s, n, l);
                                            processes.add(process);
                                            move1 = new MoveUp(processes);
                                            bestMove = evaluateMoveUp(move1, bestMove, c2Temp, isInvalid);
                                            listOfMoves.add(moveSign1);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                if (bestMove != null) {
                    isInvalid = bestMove.getChromosome().getFitness() == Double.POSITIVE_INFINITY;
                    bestMove = swap(bestMove, isInvalid);
                    ArrayList<Process> processesList = bestMove.getProcesses();
                    for (int z = 0; z < processesList.size(); z++) {
                        Process process1 = processesList.get(z);
                        c1Routes[process1.getRouteIndex()] = new ArrayList<>(process1.getRoute());
//                        System.out.println("best in cross Route "+process1.getRouteIndex()+" "+process1.getRoute());
                    }
//                    System.out.println("New routes c1Routes");
//                    for(ArrayList list : c1Routes) {
//                        System.out.println(list);
//                    }
//                    System.out.println(bestMove.getChromosome());
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
//                        System.out.println(c1Routes[j]+" Temp route1 "+tempRoute1);
                        processes = new ArrayList<>();
                        process = new Process(tempRoute1, s, k, j);
                        processes.add(process);
                        move1 = new MoveUp(processes);
                        bestMove = evaluateMoveUp(move1, bestMove, c2Temp, isInvalid);
                    }

                }

                if (bestMove != null) {
                    isInvalid = bestMove.getChromosome().getFitness() == Double.POSITIVE_INFINITY;
                    bestMove = swap(bestMove, isInvalid);
                    ArrayList<Process> processesList = bestMove.getProcesses();
                    for (int z = 0; z < processesList.size(); z++) {
                        Process process1 = processesList.get(z);
                        c1Routes[process1.getRouteIndex()] = new ArrayList<>(process1.getRoute());
//                        System.out.println("best in cross Route "+process1.getRouteIndex()+" "+process1.getRoute());
                    }

//                    System.out.println("New routes c1Routes");
//                    for(ArrayList list : c1Routes) {
//                        System.out.println(list);
//                    }
//                    System.out.println(bestMove.getChromosome());
                    c2Temp = bestMove.getChromosome();
                }
            }
        }
//        System.exit(1);
        return c2Temp;
        //return p1;
    }

    private MoveUp swap(MoveUp bestMove, boolean isInvalid) {
//        System.out.println("Before Fitness: " + bestMove.getChromosome().getFitness()+" Chromosome: " + bestMove.getChromosome());
        Chromosome c1 = bestMove.getChromosome();
        c1.buildPatientRouteMap();
        Process process1 = bestMove.getProcesses().get(0);
//        System.out.println("Patient: " + process1.getPatient() + "\t" + process1.getInsertPosition());
        if (bestMove.getProcesses().size() > 1) {
            Process process2 = bestMove.getProcesses().get(1);
//            System.out.println("Patient: " + process2.getPatient() + "\t" + process2.getInsertPosition());
            ArrayList<String> route1 = new ArrayList<>(process1.getRoute());
            ArrayList<String> route2 = new ArrayList<>(process2.getRoute());
            String p1 = process1.getPatient();
            String p2 = process2.getPatient();
            int position1 = process1.getInsertPosition();
            int position2 = process2.getInsertPosition();
            int routeIndex1 = process1.getRouteIndex();
            int routeIndex2 = process2.getRouteIndex();
            for (int z = 0; z < route1.size(); z++) {
                if (Math.abs(z - position1) > 1) {
                    String p3 = route1.get(z);
                    route1.set(position1, p3);
                    route1.set(z, p1);
//                    System.out.println("Route1: " + route1);
//                    System.out.println("Route2: " + route2);
                    int insertionPosition1 = Math.min(position1, z);
                    ArrayList<Process> processes = new ArrayList<>();
                    Process process = new Process(route1, p1, insertionPosition1, routeIndex1);
                    processes.add(process);
                    processes.add(bestMove.getProcesses().get(1));
                    MoveUp move = new MoveUp(processes);
                    bestMove = evaluateMoveUp(move, bestMove, c1, isInvalid);
//                    System.out.println("BestMove after evaluation: " + bestMove.getChromosome());
                    route1.set(position1, p1);
                    route1.set(z, p3);
//                    System.out.println("BestMove after route reset: " + bestMove.getChromosome());
                }
            }
            for (int l = 0; l < route2.size(); l++) {
                if (Math.abs(l - position2) > 1) {
                    String p4 = route2.get(l);
                    route2.set(position2, p4);
                    route2.set(l, p2);
//                    System.out.println("Route1: " + route1);
//                    System.out.println("Route2: " + route2);
                    int insertionPosition2 = Math.min(position2, l);
                    ArrayList<Process> processes = new ArrayList<>();
                    processes.add(bestMove.getProcesses().getFirst());
                    Process process = new Process(route2, p2, insertionPosition2, routeIndex2);
                    processes.add(process);
                    MoveUp move = new MoveUp(processes);
                    bestMove = evaluateMoveUp(move, bestMove, c1, isInvalid);
//                    System.out.println("BestMove after evaluation: " + bestMove.getChromosome());
                    route2.set(position2, p2);
                    route2.set(l, p4);
//                    System.out.println("BestMove after route reset: " + bestMove.getChromosome());
                }
            }
            for (int z = 0; z < route1.size(); z++) {
                if (Math.abs(z - position1) > 1) {
                    for (int l = 0; l < route2.size(); l++) {
                        if (Math.abs(l - position2) > 1) {
                            String p3 = route1.get(z);
                            String p4 = route2.get(l);
                            route1.set(position1, p3);
                            route1.set(z, p1);
                            route2.set(position2, p4);
                            route2.set(l, p2);
//                            System.out.println("Route1: " + route1);
//                            System.out.println("Route2: " + route2);
                            int insertionPosition1 = Math.min(position1, z);
                            int insertionPosition2 = Math.min(position2, l);
                            ArrayList<Process> processes = new ArrayList<>();
                            Process process = new Process(route1, p1, insertionPosition1, routeIndex1);
                            processes.add(process);
                            process = new Process(route2, p2, insertionPosition2, routeIndex2);
                            processes.add(process);
                            MoveUp move = new MoveUp(processes);
                            bestMove = evaluateMoveUp(move, bestMove, c1, isInvalid);
//                            System.out.println("BestMove after evaluation: " + bestMove.getChromosome());
                            route1.set(position1, p1);
                            route1.set(z, p3);
                            route2.set(position2, p2);
                            route2.set(l, p4);
//                            System.out.println("BestMove after route reset: " + bestMove.getChromosome());
                        }
                    }
                }
            }
        } else {
            ArrayList<String> route = new ArrayList<>(process1.getRoute());
            int position = process1.getInsertPosition();
            String p1 = process1.getPatient();
            int routeIndex = process1.getRouteIndex();
            for (int i = 0; i < route.size(); i++) {
                if (Math.abs(i - position) > 1) {
                    String p2 = route.get(i);
                    route.set(position, p2);
                    route.set(i, p1);
//                    System.out.println("Route1: " + route);
                    int insertPosition = Math.min(position, i);
                    ArrayList<Process> processes = new ArrayList<>();
                    Process process = new Process(route, p1, insertPosition, routeIndex);
                    processes.add(process);
                    MoveUp move1 = new MoveUp(processes);
                    bestMove = evaluateMoveUp(move1, bestMove, c1, isInvalid);
//                    System.out.println("BestMove after evaluation: " + bestMove.getChromosome());
                    route.set(position, p1);
                    route.set(i, p2);
//                    System.out.println("BestMove after route reset: " + bestMove.getChromosome());
                }
            }
        }
//        System.out.println("yes");
//        System.out.println("After Fitness: " + bestMove.getChromosome().getFitness());
//        System.out.println(bestMove.getChromosome());
//        EvaluationFunctionUp.EvaluateFitness(bestMove.getChromosome(), data);
//        System.out.println("Confirm: " + bestMove.getChromosome().getFitness());
        return bestMove;
    }

    //    private MoveUp evaluateMoveUp1(MoveUp m, MoveUp bestMove, Chromosome c, Patient[] allPatients, double [][] distanceMatrix) {
//        Chromosome tempCh = new Chromosome(c.getGenes(), 0.0, true);
//        int[] routeEndPoint = new int[c.getGenes().length];
//        Arrays.fill(routeEndPoint, -1);
//        Map<Integer, Integer> affectedRoutes = new HashMap<>();
//        for (Process process : m.getProcesses()) {
//            affectedRoutes.put(process.getRouteIndex(), process.getInsertPosition());
//            routeEndPoint[process.getRouteIndex()] = process.getInsertPosition();
//        }
//
//
//        removeAffectedPatientsUp1(m, c, affectedRoutes, allPatients);
//        for (Map.Entry<Integer, Integer> entry : affectedRoutes.entrySet()) {
//            routeEndPoint[entry.getKey()] = entry.getValue();
//            //System.out.println(entry.getKey() + " yaya " + routeEndPoint[entry.getKey()]);
//        }
//        int index;
//        for (int i = 0; i < routeEndPoint.length; i++) {
//            ArrayList<String> route;
//            ArrayList<Double> currentTime;
//            ArrayList<Double> travelCost;
//            ArrayList<Double> tardiness;
//            ArrayList<Double> maxTardiness;
//            route = new ArrayList<>(c.getCaregiversRouteUp()[i].getRoute());
//            currentTime = new ArrayList<>(c.getCaregiversRouteUp()[i].getCurrentTime());
//            travelCost = new ArrayList<>(c.getCaregiversRouteUp()[i].getTravelCost());
//            travelCost.removeLast();
//            tardiness = new ArrayList<>(c.getCaregiversRouteUp()[i].getTardiness());
//            maxTardiness = new ArrayList<>(c.getCaregiversRouteUp()[i].getMaxTardiness());
//            if (routeEndPoint[i] != -1) {
//                index = routeEndPoint[i] + 1;
//                route.subList(index, route.size()).clear();
//                travelCost.subList(index, travelCost.size()).clear();
//                currentTime.subList(index, currentTime.size()).clear();
//                tardiness.subList(index, tardiness.size()).clear();
//                maxTardiness.subList(index, maxTardiness.size()).clear();
//                tempCh.getCaregiversRouteUp()[i] = new ShiftUp(c.getCaregiversRouteUp()[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
//            } else {
//                tempCh.getCaregiversRouteUp()[i] = new ShiftUp(c.getCaregiversRouteUp()[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
//            }
//        }
//        //changing routes with move routes
//        for (Process process : m.getProcesses()) {
//            tempCh.getGenes()[process.getRouteIndex()] = process.getRoute();
//        }
//
//
//
//        double totalTravelCost = 0;
//        double totalTardiness = 0;
//        double highestTardiness = 0;
//        for (ShiftUp s : tempCh.getCaregiversRouteUp()) {
//            totalTravelCost += s.getTravelCost().getLast();
//            totalTardiness += s.getTardiness().getLast();
//            highestTardiness = Math.max(highestTardiness, s.getMaxTardiness().getLast());
//        }
//
//        tempCh.setTotalTravelCost(totalTravelCost);
//        tempCh.setTotalTardiness(totalTardiness);
//        tempCh.setHighestTardiness(highestTardiness);
//        tempCh.setFitness(0.0);
//
//
//        evaluateUp(tempCh, routeEndPoint, bestMove, distanceMatrix);
//        if (bestMove == null || tempCh.getFitness() < bestMove.getFitness()) {
//            m.setChromosome(tempCh);
//            m.setFitness(tempCh.getFitness());
//            return m;
//        }
//        return bestMove;
//    }
    private MoveUp evaluateMoveUp(MoveUp m, MoveUp bestMove, Chromosome c, boolean isInvalid) {
//        if(bestMove != null)
//            System.out.println("Best Move In: " + bestMove.getChromosome().getFitness());
        Chromosome tempCh = new Chromosome(c.getGenes(), 0.0, true);
        int[] routeEndPoint = new int[c.getGenes().length];
        Arrays.fill(routeEndPoint, -1);
        ArrayList<Process> processesList = m.getProcesses();

        if(isInvalid){
            for (int z = 0; z < processesList.size(); z++) {
                Process process = processesList.get(z);
                tempCh.getGenes()[process.getRouteIndex()] = process.getRoute();
            }
            EvaluationFunctionUp.EvaluateFitness(tempCh, data);
            if (bestMove == null || tempCh.getFitness() < bestMove.getFitness() || tempCh.getFitness() == bestMove.getFitness() && rand.nextBoolean()) {
//                System.out.println("Improved Move from invalid: " + tempCh.getFitness());
//                System.out.println("Chromosome: " + tempCh);
                for (int z = 0; z < processesList.size(); z++) {
                    Process process = processesList.get(z);
                    process.setRoute(process.getRoute());
                    tempCh.getGenes()[process.getRouteIndex()] = process.getRoute();
//                    System.out.println("Route with improvement: "+process.getRoute());
                }
                m.setChromosome(tempCh);
                m.setFitness(tempCh.getFitness());
                return m;
            }
            return bestMove;
        }

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
        if (bestMove == null || tempCh.getFitness() < bestMove.getFitness() || tempCh.getFitness() == bestMove.getFitness() && rand.nextBoolean()) {
            for (int z = 0; z < processesList.size(); z++) {
                Process process = processesList.get(z);
                process.setRoute(process.getRoute());
                tempCh.getGenes()[process.getRouteIndex()] = process.getRoute();
            }
            m.setChromosome(tempCh);
            m.setFitness(tempCh.getFitness());
//            System.out.println("Improved Move: " + tempCh+" and "+m.getChromosome());
//            System.out.println("Best Move out improved: " + m.getChromosome().getFitness());
            return m;
        }
//        System.out.println("Best Move out: " + bestMove.getChromosome().getFitness());
        return bestMove;
    }

//    private Move evaluateMove(Move m, Move bestMove, Chromosome c) {
//        Chromosome tempCh = new Chromosome(c.getGenes(), 0.0, true);
//        int[] routeEndPoint = new int[c.getGenes().length];
//        Arrays.fill(routeEndPoint, -1);
//        Map<Integer, Integer> affectedRoutes = new HashMap<>();
//        affectedRoutes.put(m.getRouteIndex1(), m.getInsertPosition1());
//        routeEndPoint[m.getRouteIndex1()] = m.getInsertPosition1();
//        if (m.getRouteIndex2() != -1) {
//            routeEndPoint[m.getRouteIndex2()] = m.getInsertPosition2();
//            affectedRoutes.put(m.getRouteIndex2(), m.getInsertPosition2());
//        }
//        removeAffectedPatients(m, c, affectedRoutes);
//        for (Map.Entry<Integer, Integer> entry : affectedRoutes.entrySet()) {
//            routeEndPoint[entry.getKey()] = entry.getValue();
//            //System.out.println(entry.getKey() + " yaya " + routeEndPoint[entry.getKey()]);
//        }
//        int index;
//        for (int i = 0; i < routeEndPoint.length; i++) {
//            ArrayList<String> route;
//            ArrayList<Double> currentTime;
//            ArrayList<Double> travelCost;
//            ArrayList<Double> tardiness;
//            ArrayList<Double> maxTardiness;
//            route = new ArrayList<>(c.getCaregiversRouteUp()[i].getRoute());
//            currentTime = new ArrayList<>(c.getCaregiversRouteUp()[i].getCurrentTime());
//            travelCost = new ArrayList<>(c.getCaregiversRouteUp()[i].getTravelCost());
//            travelCost.removeLast();
//            tardiness = new ArrayList<>(c.getCaregiversRouteUp()[i].getTardiness());
//            maxTardiness = new ArrayList<>(c.getCaregiversRouteUp()[i].getMaxTardiness());
//            if (routeEndPoint[i] != -1) {
//                index = routeEndPoint[i] + 1;
//                route.subList(index, route.size()).clear();
//                travelCost.subList(index, travelCost.size()).clear();
//                currentTime.subList(index, currentTime.size()).clear();
//                tardiness.subList(index, tardiness.size()).clear();
//                maxTardiness.subList(index, maxTardiness.size()).clear();
//                tempCh.getCaregiversRouteUp()[i] = new ShiftUp(c.getCaregiversRouteUp()[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
//            } else {
//                tempCh.getCaregiversRouteUp()[i] = new ShiftUp(c.getCaregiversRouteUp()[i].getCaregiver(), route, currentTime, travelCost, tardiness, maxTardiness);
//            }


    private void evaluateUp(Chromosome ch, int[] routeEndPoint, MoveUp bestMove) {
        ArrayList<String> route;
        ShiftUp[] routes = ch.getCaregiversRouteUp();
        ShiftUp caregiver1;
        int routeEnd;
        Set<String> track = new LinkedHashSet<>();
        Map<String, List<Integer>> sycTrack = new HashMap<>();
        int simCounter = 0;
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
                        sycTrack.clear();
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

//    private void evaluate(Chromosome ch, int[] routeEndPoint, Move bestMove) {
//        ArrayList<String> route;
//        ShiftUp[] routes = ch.getCaregiversRouteUp();
//        ShiftUp caregiver1;
//        Set<String> track = new HashSet<>();
//        for (int i = 0; i < routeEndPoint.length; i++) {
//            route = new ArrayList<>(ch.getGenes()[i]);
//            caregiver1 = routes[i];
//            if (routeEndPoint[i] != -1) {
//                for (int j = routeEndPoint[i]; j < route.size(); j++) {
//                    String patient = route.get(j);
//                    if (!caregiver1.getRoute().contains(patient)) {
//                        if (!patientAssignment(ch, patient, caregiver1, routes, i, track)) {
//                            ch.setFitness(Double.POSITIVE_INFINITY);
//                            return;
//                        }
//                        UpdateCost(ch);
//                        if (bestMove != null && ch.getFitness() > bestMove.getFitness()) {
//                            return;
//                        }
//                        track.clear();
//                    }
//                }
//            }
//        }
//        for (ShiftUp s : routes) {
//            ch.updateTotalTravelCost(distanceMatrix[getIdOfObjectLocation(s.getRoute().getLast())][0]);
//            s.updateTravelCost(distanceMatrix[getIdOfObjectLocation(s.getRoute().getLast())][0]);
//        }
//        UpdateCost(ch);
//    }

    private static void UpdateCost(Chromosome ch) {
        ch.setFitness((1 / 3d * ch.getTotalTravelCost()) + (1 / 3d * ch.getTotalTardiness()) + (1 / 3d * ch.getHighestTardiness()));
    }

    private static int getIdOfObjectLocation(String s) {
        return Integer.parseInt(s.substring(1));
    }

    //    private void removeAffectedPatientsUp1(MoveUp r, Chromosome c, Map<Integer, Integer> affectedRoutes, Patient[] allPatients) {
//        int startPos;
//        String patientId;
//        Map<String, Set<Integer>> patientToRoutesMap = c.getPatientToRoutesMap();
//        for (Process process : r.getProcesses()) {
//            ArrayList<String> currentRoute = c.getGenes()[process.getRouteIndex()];
//            startPos = process.getInsertPosition();
//            for (int i = startPos; i < currentRoute.size(); i++) {
//                patientId = currentRoute.get(i);
//                Patient p = allPatients[getIdOfObject(patientId)];
//                if (p.getRequired_caregivers().length > 1) {
//                    int routeIndex = getRouteIndexMethod(process.getRouteIndex(),  patientToRoutesMap.get(p.getId()));
//                    int patientIndex = c.getGenes()[routeIndex].indexOf(p.getId());
//                    ArrayList<Process> processBuffer = new ArrayList<>();
//
//                    if (!affectedRoutes.containsKey(routeIndex) || affectedRoutes.get(routeIndex) > patientIndex) {
//                        affectedRoutes.put(routeIndex, patientIndex);
//                        processBuffer.add(new Process(new ArrayList<>(c.getGenes()[routeIndex]), p.getId(), patientIndex, routeIndex));
//                        removeAffectedPatientsUp1(new MoveUp(processBuffer), c, affectedRoutes, allPatients);
//                    }
//
//                }
//            }
//        }
//    }
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

//    private ArrayList<Integer> getQualifiedCaregiver(String service) {
//        ArrayList<Integer> caregivers = new ArrayList<>();
//        Set<String> abilities;
//        for (Caregiver c : data.getCaregivers()) {
//            abilities = new HashSet<>(c.getAbilities());
//            if (abilities.contains(service)) {
//                caregivers.add(c.getCacheId());
//            }
//        }
//        return caregivers;
//    }

    private boolean noEvaluationConflicts(ArrayList<String> c1Route, ArrayList<String> c2Route, int m, int n) {
        return conflictCheck(c1Route, c2Route, m, n);
    }

    @Override
    public void run() {
        ga.getCrossoverChromosomes().add(Crossover());
    }
}

