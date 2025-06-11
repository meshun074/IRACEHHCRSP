package org.example.GA;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;
import org.example.Data.Required_Caregiver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EvaluationFunctionUp {
    private static InstancesClass dataset;

    public static void EvaluateFitness(List<Chromosome> population, InstancesClass data) {
        dataset = data;
        for (Chromosome chromosome : population) {
            Evaluate(chromosome);
        }
    }
    public static void EvaluateFitness(Chromosome chromosome, InstancesClass data) {
        dataset = data;
        Evaluate(chromosome);

    }

    private static void Evaluate(Chromosome ch) {
        // Testing
//        ch.showSolution(78);
//        System.out.println("Fitness: yieeee" );

        ShiftUp[] routes = new ShiftUp[ch.getCaregivers()];
        //initializing caregivers shift.
        initializeRoutes(routes);
        ch.setCaregiversRouteUp(routes);
        ch.setHighestTardiness(0);
        ch.setTotalTardiness(0);
        ch.setTotalTravelCost(0);
        ch.setFitness(Double.POSITIVE_INFINITY);
        ArrayList<String> route;
        ShiftUp caregiver1;
        Set<String> track = new HashSet<>();
        for (int i = 0; i < routes.length; i++) {
            route = new ArrayList<>(ch.getGenes()[i]);
            caregiver1 = routes[i];
            for (String patient : route) {
                if (!caregiver1.getRoute().contains(patient)) {
                    if (!patientAssignment(ch, patient, caregiver1, routes, i, track)) {
                        ch.setFitness(Double.POSITIVE_INFINITY);
                        return;
                    }
                    track.clear();
                }
            }
        }
        for (ShiftUp s : routes) {
            ch.updateTotalTravelCost(dataset.getDistances()[getIdOfObjectLocation(s.getRoute().getLast())][0]);
            s.updateTravelCost(dataset.getDistances()[getIdOfObjectLocation(s.getRoute().getLast())][0]);
        }
        UpdateCost(ch);
//        ch.showSolution(89);
//        int c=0;
//        for(ShiftUp s : ch.getCaregiversRouteUp()) {
//            System.out.println("Caregiver "+ c);
//            s.showInfo();
//            c++;
//        }
//        System.exit(1);
    }

    private static void UpdateCost(Chromosome ch) {
        ch.setFitness((1 / 3d * ch.getTotalTravelCost()) + (1 / 3d * ch.getTotalTardiness()) + (1 / 3d * ch.getHighestTardiness()));
    }

    public static boolean patientAssignment(Chromosome ch, String patient, ShiftUp caregiver1, ShiftUp[] routes, int i, Set<String> track) {
        double maxTardiness;
        double travelCost;
        double tardiness1, tardiness2;
        Patient[] allPatients = dataset.getPatients();
        double[][] distancesMatrix = dataset.getDistances();
        int index;
        ShiftUp caregiver2;
        Patient p = allPatients[getIdOfObject(patient)];
        double[] timeWindow = p.getTime_window();
        String lastLocation =caregiver1.getRoute().getLast();
        int currentLocation1 = getIdOfObjectLocation(lastLocation);
        int nextLocation = getIdOfObjectLocation(p.getId());
        double arrivalTime1 = caregiver1.getCurrentTime().getLast() + dataset.getDistances()[currentLocation1][nextLocation];
        double startTime1 = Math.max(arrivalTime1, timeWindow[0]);
        if (p.getRequired_caregivers().length > 1) {
            if (track.contains(patient)) {
                //
                return false;
            }
            track.add(patient);
            index = findSecondCaregiver(p, i, routes, ch, track);
            if (index > dataset.getCaregivers().length - 1) {
                // Testing
                return false;
            }
            caregiver2 = routes[index];
            //check for the first and second route based on assignment
            //find position of service 1 and 2
            Required_Caregiver[] requiredCaregiverServices = p.getRequired_caregivers();
            String service1 = requiredCaregiverServices[0].getService();
            String service2 = requiredCaregiverServices[1].getService();
            Set<Integer> service1RoutesList = dataset.getQualifiedCaregiver(service1);
            Set<Integer> service2RoutesList = dataset.getQualifiedCaregiver(service2);

            int caregiver2Id = caregiver2.getCaregiver().getCacheId();
            int caregiver1Id = caregiver1.getCaregiver().getCacheId();

            boolean condition1 = service1RoutesList.contains(caregiver2Id) && !service2RoutesList.contains(caregiver2Id);
            boolean condition2 = service2RoutesList.contains(caregiver1Id) && !service1RoutesList.contains(caregiver1Id);
            boolean condition3 = service1RoutesList.contains(caregiver2Id) && !service1RoutesList.contains(caregiver1Id);

            if (condition1 || condition2 || condition3) {
                ShiftUp temp = caregiver1;
                caregiver1 = caregiver2;
                caregiver2 = temp;

                //Recalculate since you have made a swap
                lastLocation = caregiver1.getRoute().getLast();
                currentLocation1 = getIdOfObjectLocation(lastLocation);
                arrivalTime1 = caregiver1.getCurrentTime().getLast() + distancesMatrix[currentLocation1][nextLocation];
                startTime1 = Math.max(arrivalTime1, timeWindow[0]);
            }


            int currentLocation2 = getIdOfObjectLocation(caregiver2.getRoute().getLast());
            double arrivalTime2 = caregiver2.getCurrentTime().getLast() + distancesMatrix[currentLocation2][nextLocation];
            double startTime2 = Math.max(arrivalTime2, timeWindow[0]);

            if (p.getSynchronization().getType().equals("sequential")) {
                double[] syncDistances = p.getSynchronization().getDistance();
                startTime2 = Math.max(startTime2, startTime1 + syncDistances[0]);
                if (startTime2 - startTime1 > syncDistances[1]) {
                    startTime1 = startTime2 - syncDistances[1];
                }
                tardiness1 = Math.max(0, startTime1 - timeWindow[1]);
                tardiness2 = Math.max(0, startTime2 - timeWindow[1]);
                maxTardiness = Math.max(tardiness1, tardiness2);
                ch.updateTotalTardiness(tardiness1 + tardiness2);
                ch.setHighestTardiness(Math.max(maxTardiness, ch.getHighestTardiness()));
                caregiver1.setCurrentTime(startTime1 + requiredCaregiverServices[0].getDuration());
                caregiver1.updateTardiness(tardiness1);
                caregiver2.setCurrentTime(startTime2 + requiredCaregiverServices[1].getDuration());
                caregiver2.updateTardiness(tardiness2);
            } else {
                double startTime = Math.max(startTime1, startTime2);
                tardiness1 = Math.max(0, startTime - timeWindow[1]);
                tardiness2 = tardiness1;
                ch.setHighestTardiness(Math.max(tardiness1, ch.getHighestTardiness()));
                caregiver1.setCurrentTime(startTime + requiredCaregiverServices[0].getDuration());
                caregiver1.updateTardiness(tardiness1);
                caregiver2.setCurrentTime(startTime + requiredCaregiverServices[1].getDuration());
                caregiver2.updateTardiness(tardiness2);
                ch.updateTotalTardiness(tardiness1 + tardiness2);
            }
            travelCost = distancesMatrix[currentLocation1][nextLocation] + dataset.getDistances()[currentLocation2][nextLocation];
            ch.updateTotalTravelCost(travelCost);
            caregiver1.updateRoute(p.getId());
            caregiver1.updateTravelCost(distancesMatrix[currentLocation1][nextLocation]);
            caregiver2.updateRoute(p.getId());
            caregiver2.updateTravelCost(distancesMatrix[currentLocation2][nextLocation]);
        } else {
            tardiness1 = Math.max(0, startTime1 - timeWindow[1]);
            ch.setHighestTardiness(Math.max(tardiness1, ch.getHighestTardiness()));
            ch.updateTotalTardiness(tardiness1);
            travelCost = distancesMatrix[currentLocation1][nextLocation];
            ch.updateTotalTravelCost(travelCost);
            caregiver1.setCurrentTime(startTime1 + p.getRequired_caregivers()[0].getDuration());
            caregiver1.updateRoute(p.getId());
            caregiver1.updateTravelCost(travelCost);
            caregiver1.updateTardiness(tardiness1);
        }
        return true;
    }

    private static int findSecondCaregiver(Patient p, int route1, ShiftUp[] routes, Chromosome ch, Set<String> track) {
        ArrayList<String> route = null;
        ArrayList[] genes = ch.getGenes();
        int routeIndex = 0;
        int patientPositionInRoute;
        for (int i = 0; i < genes.length; i++) {
            if (i != route1&&genes[i].contains(p.getId())) {
                route = new ArrayList<>(genes[i]);
                routeIndex = i;
                break;
            }
        }
        if (route == null) {
            return Integer.MAX_VALUE;
        }
        ShiftUp caregiver = routes[routeIndex];
        patientPositionInRoute = route.indexOf(p.getId());
        int i = caregiver.getRoute().size() - 1;
        while (caregiver.getRoute().size() - 1 != patientPositionInRoute && i < route.size()) {
            String patient = route.get(i);
            if (!patientAssignment(ch, patient, caregiver, routes, routeIndex, track))
                return Integer.MAX_VALUE;
            i++;
        }
        return routeIndex;
    }

    private static void initializeRoutes(ShiftUp[] routes) {
        for (int s = 0; s < routes.length; s++)
            //Initialize the shift of the caregivers
            routes[s] = new ShiftUp(dataset.getCaregivers()[s], new ArrayList<>() {{
                add("d0");
            }}, 0.0);
    }

    //    static int getIdOfObject(String s) {
//        return Integer.parseInt(s.substring(1)) - 1;
//    }
    static int getIdOfObject(String s) {
        // Skip first char and parse the rest
        int id = 0;
        for (int i = 1; i < s.length(); i++) {
            id = id * 10 + (s.charAt(i) - '0');
        }
        return id - 1;
    }

    //gets the index of a location of a patient or depot
    private static int getIdOfObjectLocation(String s) {
        return Integer.parseInt(s.substring(1));
    }

}


