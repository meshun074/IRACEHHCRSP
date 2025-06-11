package org.example.GA;

import org.example.Data.Caregiver;
import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.ArrayList;

public class AssignPatients {
    private static InstancesClass data;
    //Evaluate routeInitializer
    public static void Assign(RouteInitializer ch, InstancesClass instance) {
        data = instance;
        ch.setSolutionCost(greedyCheapestInsertionHeuristics(ch));
    }

    private static double greedyCheapestInsertionHeuristics(RouteInitializer ch) {
        boolean convHull = !(ch.getAlleles().get(ch.getAlleles().size() - 2) < 0.5);
        boolean wLoadHeuristic = !(ch.getAlleles().getLast() < 0.5);
        ShiftUp[] caregivers = new ShiftUp[data.getCaregivers().length];
        //initializing caregivers shift.
        for (int s = 0; s< caregivers.length; s++)
            //Initialize the shift of the caregivers
            caregivers[s] = new ShiftUp(data.getCaregivers()[s], new ArrayList<>(){{add("d0");}},0.0);
        ch.setCaregiversRoute(caregivers);
        ch.setHighestTardiness(0);
        ch.setTotalTardiness(0);
        ch.setTotalTravelCost(0);
        ch.setSolutionCost(0);
        ArrayList<Caregiver> qualifiedC1;
        ArrayList<Caregiver> qualifiedC2;
        String c1;
        String c2;
        double currentCost;
        double newCost;
        for(int i = 0; i<data.getPatients().length; i++){
            currentCost =Double.POSITIVE_INFINITY;
            c1 = "";
            c2 = "";
            //gets patient
            Patient p = null;
            try {
                p = data.getPatients()[(ch.getAlleles().get(i) - 1)];
            }catch (ArrayIndexOutOfBoundsException e){
                System.out.println(ch.getAlleles());
                System.out.println(ch.getAlleles().size());
                System.out.println(ch.getAlleles().get(i));
                System.out.println(i);
                System.out.println(e.getMessage());
                System.exit(1);
            }
            //Gets qualified caregivers for patient requested service
            qualifiedC1 = getQualifiedCaregiver(p.getRequired_caregivers()[0].getService());
            //checks for double service
            if(p.getRequired_caregivers().length>1) {
                qualifiedC2 = getQualifiedCaregiver(p.getRequired_caregivers()[1].getService());
                for (Caregiver caregiver1 : qualifiedC1) {
                    for (Caregiver caregiver2 : qualifiedC2) {
                        if(!caregiver1.getId().equals(caregiver2.getId())){
                            newCost = findInsertionCost(ch, caregivers[getIdOfObject(caregiver1.getId())], caregivers[getIdOfObject(caregiver2.getId())], p, convHull);
                            if (wLoadHeuristic)
                                newCost += caregivers[getIdOfObject(caregiver1.getId())].getLoad() +
                                        caregivers[getIdOfObject(caregiver2.getId())].getLoad();
                            if (newCost < currentCost || newCost == currentCost && ch.getAlleles().get(ch.getAlleles().size() - 2) > 0.5) {
                                c1 = caregiver1.getId();
                                c2 = caregiver2.getId();
                                currentCost = newCost;
                            }
                        }
                    }
                }

            }
            else {
                //single service
                for (Caregiver caregiver : qualifiedC1) {
                    newCost = findInsertionCost(ch,caregivers[getIdOfObject(caregiver.getId())],  p, convHull);
                    if(wLoadHeuristic)
                        newCost+=caregivers[getIdOfObject(caregiver.getId())].getLoad();
                    if(newCost<currentCost||newCost==currentCost && ch.getAlleles().get(ch.getAlleles().size() - 2) >0.5) {
                        c1 = caregiver.getId();
                        currentCost = newCost;
                    }

                }
            }
            //update route and cost of caregivers shift based on the best insertion
            UpdateRoutes(ch, caregivers,c1,c2,p,convHull);
            UpdateCost(ch);
            caregivers[getIdOfObject(c1)].updateLoad(p.getRequired_caregivers()[0].getDuration());
            if(!c2.isEmpty())
                caregivers[getIdOfObject(c2)].updateLoad(p.getRequired_caregivers()[1].getDuration());
        }
        //add distance from last visited patient of the caregiver to the depot
        if(!convHull)
        {
            for (ShiftUp s: caregivers)
                ch.updateTotalTravelCost(data.getDistances()[getIdOfObjectLocation(s.getRoute().getLast())][0]);
        }
        //update and return the cost of the function
        UpdateCost(ch);
        return ch.getSolutionCost();
    }
    //gets qualifiedCaregiver
    private static ArrayList<Caregiver> getQualifiedCaregiver(String service){
        ArrayList<Caregiver> caregivers = new ArrayList<>();
        for (Caregiver c: data.getCaregivers()){
            if(c.getAbilities().contains(service)){
                caregivers.add(c);
            }
        }
        return caregivers;
    }

    private static void UpdateRoutes(RouteInitializer ch, ShiftUp[] c, String c1, String c2, Patient p, boolean convHull){
        ShiftUp caregiver1 = c[getIdOfObject(c1)];
        int currentLocation1 = getIdOfObjectLocation(caregiver1.getRoute().getLast());
        int nextLocation = getIdOfObjectLocation(p.getId());
        double arrivalTime1 = caregiver1.getCurrentTime().getLast()+data.getDistances()[currentLocation1][nextLocation];
        double startTime1  = Math.max(arrivalTime1, p.getTime_window()[0]);


        if(p.getRequired_caregivers().length>1) {
            ShiftUp caregiver2 = c[getIdOfObject(c2)];
            int currentLocation2 = getIdOfObjectLocation(caregiver2.getRoute().getLast());
            double arrivalTime2 = caregiver2.getCurrentTime().getLast()+data.getDistances()[currentLocation2][nextLocation];
            double startTime2  = Math.max(arrivalTime2, p.getTime_window()[0]);
            double tardiness;
            double maxTardiness;

            if(p.getSynchronization().getType().equals("simultaneous")){
                double startTime = Math.max(startTime1,startTime2);
                tardiness = 2 * Math.max(0, startTime-p.getTime_window()[1]);
                //System.out.println(p.getId()+ " + "+tardiness+" + "+ startTime+" + "+p.getTime_window()[1]);
                ch.setHighestTardiness(Math.max(tardiness/2, ch.getHighestTardiness()));
                ch.updateTotalTardiness(tardiness);
                caregiver1.setCurrentTime(startTime+p.getRequired_caregivers()[0].getDuration());
                caregiver2.setCurrentTime(startTime+p.getRequired_caregivers()[1].getDuration());
            }else {
                startTime2 = Math.max(startTime2, startTime1+p.getSynchronization().getDistance()[0]);
                if(startTime2 -startTime1>p.getSynchronization().getDistance()[1])
                    startTime1 = startTime2 - p.getSynchronization().getDistance()[1];
                ch.updateTotalTardiness(Math.max(0,startTime1-p.getTime_window()[1]) + Math.max(0, startTime2 - p.getTime_window()[1]));
                maxTardiness = Math.max(Math.max(0,startTime1-p.getTime_window()[1]), Math.max(0, startTime2 - p.getTime_window()[1]));
                ch.setHighestTardiness(Math.max(maxTardiness,ch.getHighestTardiness()));
                caregiver1.setCurrentTime(startTime1+p.getRequired_caregivers()[0].getDuration());
                caregiver2.setCurrentTime(startTime2+p.getRequired_caregivers()[1].getDuration());
            }
            double travelCost = data.getDistances()[currentLocation1][nextLocation] + data.getDistances()[currentLocation2][nextLocation];
            if(convHull){
                travelCost += (data.getDistances()[nextLocation][0]-data.getDistances()[currentLocation1][0]);
                travelCost += (data.getDistances()[nextLocation][0]-data.getDistances()[currentLocation2][0]);
            }
            ch.updateTotalTravelCost(travelCost);
            caregiver1.updateRoute(p.getId());
            caregiver2.updateRoute(p.getId());
        }
        else {
            double tardiness = Math.max(0, startTime1 - p.getTime_window()[1]);
            ch.setHighestTardiness(Math.max(tardiness, ch.getHighestTardiness()));
            ch.updateTotalTardiness(tardiness);
            double travelCost = data.getDistances()[currentLocation1][nextLocation];
            //System.out.println(p.getId()+ " + "+tardiness+" + "+ startTime1+" + "+p.getTime_window()[1]);
            if(convHull)
                travelCost +=(data.getDistances()[nextLocation][0]-data.getDistances()[currentLocation1][0]);
            //System.out.println(p.getId()+ " + before "+ch.getTotalTravelCost());
            ch.updateTotalTravelCost(travelCost);
            //System.out.println(p.getId()+ " + "+travelCost);
            caregiver1.setCurrentTime(startTime1+p.getRequired_caregivers()[0].getDuration());
            caregiver1.updateRoute(p.getId());
        }
    }
    private static void UpdateCost(RouteInitializer ch){
        ch.setSolutionCost((1/3d*ch.getTotalTravelCost())+(1/3d*ch.getTotalTardiness())+(1/3d*ch.getHighestTardiness()));
    }
    private static int getIdOfObject(String s){
        return Integer.parseInt(s.substring(1))-1;
    }
    //gets the index of a location of a patient or depot
    private static int getIdOfObjectLocation(String s){
        return Integer.parseInt(s.substring(1));
    }
    //total cost insertion
    //single
    private static double findInsertionCost(RouteInitializer ch, ShiftUp caregiverShift, Patient p, boolean convHull){
        int currentLocation = getIdOfObjectLocation(caregiverShift.getRoute().getLast());
        int nextLocation = getIdOfObjectLocation(p.getId());
        double arrivalTime = caregiverShift.getCurrentTime().getLast()+data.getDistances()[currentLocation][nextLocation];
        double startTime  = Math.max(arrivalTime, p.getTime_window()[0]);
        double tardiness = Math.max(0, startTime - p.getTime_window()[1]);
        double maxTardiness = Math.max(tardiness, ch.getHighestTardiness());
        tardiness += ch.getTotalTardiness();
        double travelCost = data.getDistances()[currentLocation][nextLocation] + ch.getTotalTravelCost();
        if(convHull)
            travelCost +=(data.getDistances()[nextLocation][0]-data.getDistances()[currentLocation][0]);

        return (1/3d*travelCost)+(1/3d*tardiness)+(1/3d*maxTardiness);
    }
    //double
    private static double findInsertionCost(RouteInitializer ch, ShiftUp c1, ShiftUp c2, Patient p, boolean convHull){
        int c1CurrentLocation = getIdOfObjectLocation(c1.getRoute().getLast());
        int c2CurrentLocation = getIdOfObjectLocation(c2.getRoute().getLast());
        int nextLocation = getIdOfObjectLocation(p.getId());
        double startTime1 = Math.max(p.getTime_window()[0], c1.getCurrentTime().getLast()+data.getDistances()[c1CurrentLocation][nextLocation]);
        double startTime2 = Math.max(p.getTime_window()[0], c2.getCurrentTime().getLast()+data.getDistances()[c2CurrentLocation][nextLocation]);
        double travelCost = data.getDistances()[c1CurrentLocation][nextLocation] + data.getDistances()[c2CurrentLocation][nextLocation];
        double tardiness;
        double maxTardiness;
        if(convHull){
            travelCost += (data.getDistances()[nextLocation][0]-data.getDistances()[c1CurrentLocation][0]);
            travelCost += (data.getDistances()[nextLocation][0]-data.getDistances()[c2CurrentLocation][0]);
        }
        if(p.getSynchronization().getType().equals("simultaneous")){
            double startTime = Math.max(startTime1,startTime2);
            tardiness = 2 * Math.max(0, startTime-p.getTime_window()[1]);
            maxTardiness = Math.max(tardiness/2, ch.getHighestTardiness());
            tardiness += ch.getTotalTardiness();
        }else {
            startTime2 = Math.max(startTime2, startTime1+p.getSynchronization().getDistance()[0]);
            if(startTime2 -startTime1>p.getSynchronization().getDistance()[1])
                startTime1 = startTime2 - p.getSynchronization().getDistance()[1];
            tardiness = Math.max(0,startTime1-p.getTime_window()[1])+Math.max(0, startTime2 - p.getTime_window()[1]);
            maxTardiness = Math.max(Math.max(0,startTime1-p.getTime_window()[1]), Math.max(0, startTime2 - p.getTime_window()[1]));
            maxTardiness = Math.max(maxTardiness,ch.getHighestTardiness());
        }
        travelCost += ch.getTotalTravelCost();
        return (1/3d*travelCost)+(1/3d*tardiness)+(1/3d*maxTardiness);
    }
}


