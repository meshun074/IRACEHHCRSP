package org.example.GA;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.example.GA.EvaluationFunctionUp.getIdOfObject;

public class Mutation2 implements Runnable {
    @Override
    public void run() {
        ga.getMutationChromosomes().add(mutate());
    }

    private final GeneticAlgorithm ga;
    private final Random rand;
    private final Chromosome original;
    private final InstancesClass data;
    private final Patient[] allPatients;
    public Mutation2(GeneticAlgorithm ga, Chromosome original, Random rand, InstancesClass data) {
        this.ga = ga;
        this.original = original;
        this.rand = rand;
        this.data = data;
        this.allPatients = data.getPatients();
    }
    private Chromosome mutate() {
        Chromosome mutated = new Chromosome(original.getGenes(), original.getFitness(), true);
        ArrayList<String>[]genes  = mutated.getGenes();

        int r1 = rand.nextInt(genes.length);
        int r2;
        do{
            r2 = rand.nextInt(genes.length);
        }while (genes[r1].isEmpty()&&genes[r2].isEmpty()||r1 == r2);
//        System.out.println(r1+" : "+r2+" Before: "+mutated);
        ArrayList<String> route1 = genes[r1];
        ArrayList<String> route2 = genes[r2];
        ArrayList<String> newRoute1 = new ArrayList<>();
        ArrayList<String> newRoute2 = new ArrayList<>();
        String[] mutable1 =newRoute(route1,r1,r2);
        String[] mutable2 =newRoute(route2,r2,r1);
        for(int i=0; i<mutable1.length; i++){
            if(mutable1[i]==null){
                newRoute1.add(route1.get(i));
            }
            if(i<mutable2.length&&mutable2[i]!=null){
                newRoute1.add(mutable2[i]);
            }
        }
        for (int i = mutable1.length; i<mutable2.length; i++){
            if(mutable2[i]!=null){
                newRoute1.add(mutable2[i]);
            }
        }
        for(int i=0; i<mutable2.length; i++){
            if(mutable2[i]==null){
                newRoute2.add(route2.get(i));
            }
            if(i<mutable1.length&&mutable1[i]!=null){
                newRoute2.add(mutable1[i]);
            }
        }
        for (int i = mutable2.length; i<mutable1.length; i++){
            if(mutable1[i]!=null){
                newRoute2.add(mutable1[i]);
            }
        }
        genes[r1] = newRoute1;
        genes[r2] = newRoute2;
        mutated.setGenes(genes);
//        System.out.println(r1+" : "+r2+" After: "+mutated);
        EvaluationFunctionUp.EvaluateFitness(mutated,data);
//        System.exit(1);
        return mutated;
    }
    private String[] newRoute(ArrayList<String> route1,  int r1, int r2) {
        String[] newRoute = new String[route1.size()];
        ArrayList<String>[]genes = original.getGenes();
        for(int i = 0; i < route1.size(); i++){
            String p = route1.get(i);
            Patient patient = allPatients[getIdOfObject(p)];
            if(patient.getRequired_caregivers().length>1){
                Set<Integer> allCaregivers = patient.getAllCaregiversForDoubleService();
                if(!allCaregivers.contains(r2)||genes[r2].contains(p))
                    continue;
                int otherIndex = getCaregiverIndex(r1, p,genes,allCaregivers);
                List<CaregiverPair> allCaregiverCombinations = patient.getAllPossibleCaregiverCombinations();
                for(CaregiverPair pair : allCaregiverCombinations){
                    if(pair.getFirst()==r2 && pair.getSecond()==otherIndex||pair.getFirst()==otherIndex && pair.getSecond()==r2){
                        newRoute[i]=p;
                        break;
                    }
                }
            }else {
                if(patient.getPossibleFirstCaregiver().contains(r2)){
                    newRoute[i]=p;
                }
            }
        }
        return newRoute;
    }
    private int getCaregiverIndex(int r,String p,ArrayList<String>[]genes, Set<Integer> allCaregivers){
        for(int i: allCaregivers){
            if(i==r)
                continue;
            if(genes[i].contains(p)){
                return i;
            }
        }
        return -1;
    }
}

