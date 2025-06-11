package org.example.GA;

import org.example.Data.InstancesClass;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class GeneticAlgorithm {
    private final int popSize;
    private final int gen;
    private final long identity;
    private final int LSRate;
    private final int TSRate;
    private final String selectTechnique;
    private final String crossType;
    private final String mutType;
    private final int numOfEliteSearch;
    private final int elitismSize;
    private final List<Integer> eliteRandomList;
    private final double crossRate;
    private final double mutRate;
    private final InstancesClass data;
    private Chromosome bestChromosome;
    private List<Chromosome> nextPopulation;
    private List<Chromosome> tempPopulation;
    private List<Chromosome> tempMutPopulation;
    private List<Chromosome> newPopulation;
    private List<Chromosome> crossoverChromosomes;
    private List<Chromosome> mutationChromosomes;
    private final double[] popProbabilities;
    private Map<Integer, Chromosome> LSChromosomes;
    private int terminator =0;
    private final int patientLength;
    private final Random rand;


    public GeneticAlgorithm(long identity, Configuration config, int gen, InstancesClass data) {
        this.identity = identity;
        rand = new Random(identity);
        this.numOfEliteSearch = config.getNumberOfElites();
        this.LSRate = config.getLSRate();
        this.TSRate = config.getTSRate();
        this.popSize = config.getPopulationSize();
        this.gen = gen;
        selectTechnique = config.getSelectionMethod();
        mutType = config.getMutationMethod();
        crossType = config.getCrossoverMethod();
        double elitismRate = config.getElitismRate();
        this.elitismSize = (int)(elitismRate *popSize);
        this.eliteRandomList = new ArrayList<>(elitismSize);
        for (int i = 0; i < elitismSize; i++) {
            eliteRandomList.add(i);
        }
        this.crossRate = config.getCrossRate();
        mutRate = config.getMutRate();
        this.data = data;
        popProbabilities = new double[popSize];
        patientLength = data.getPatients().length;
    }

    public Chromosome start() {
        bestChromosome = null;
        //initialize and evaluate fitness of chromosome
        newPopulation = Population.initialize(popSize, patientLength,identity);

        //Sort population
        sortPopulation(newPopulation);
        //printing output
        performanceUpdate(newPopulation, 0);
        for (int i = 1; i <= gen; i++) {
//            BCRC_CrossoverTaskSwap bs = new BCRC_CrossoverTaskSwap(this,identity,mutRate,newPopulation.get(0), newPopulation.get(1), 0, true,data);
//            Chromosome ches = bs.Crossover();
//            System.out.println("Crossover chromosome swap: "+ ches.toString() + ches.getFitness());
//            System.exit(1);
            maintainElitism();
            //select appropriate crossover;
            crossoverSelection();
            mutationSelection();
            updatePopulation1();
            //Local search();
            if (LSRate!=0 && i % LSRate == 0)
                LocalSearch(i);
            performanceUpdate(newPopulation, i);
            if(patientLength<=100){
                if(terminator == patientLength/2) break;
            }else {
                if (terminator == 50) break;
            }
        }
        return bestChromosome;
    }

    public List<Chromosome> getCrossoverChromosomes() {
        return crossoverChromosomes;
    }
    public List<Chromosome> getMutationChromosomes(){
        return mutationChromosomes;
    }

    private void updatePopulation1() {
        newPopulation.clear();
        newPopulation.addAll(nextPopulation);
        newPopulation.addAll(tempMutPopulation);
        //Collections.shuffle(tempPopulation);
        sortPopulation(tempPopulation);
        for(Chromosome c : tempPopulation){
            if(newPopulation.size()<popSize){
                newPopulation.add(c);
            }else break;
        }
    }

    private void crossoverSelection(){
        tempPopulation = new ArrayList<>();
        if(crossType.equals("BS"))
            bestCostRouteCrossoverWithSwap();
        else
            bestCostRouteCrossover();
    }

    private void bestCostRouteCrossover() {
        Chromosome p1, p2;
        int r1, r2;
        int count;
        boolean cross;
        int index =0;
//        ExecutorService service = Executors.newFixedThreadPool(1);
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        crossoverChromosomes = Collections.synchronizedList(new ArrayList<>());
        List<Callable<Void>> crossoverTasks = new ArrayList<>();
        if(selectTechnique.equals("RW"))
            rouletteWheelSetup();
        while (index < popSize) {
            p1 = newPopulation.get(selectionTechnique(rand));
            count = 0;
            do {
                p2 = newPopulation.get(selectionTechnique(rand));
                count++;
            }
            while (count < 10 && p2.toString().equals(p1.toString()));
            do {
                r1 = rand.nextInt(p2.getGenes().length);
                r2 = rand.nextInt(p1.getGenes().length);
            }while (p1.getGenes()[r2].isEmpty() || p2.getGenes()[r1].isEmpty());

            Chromosome finalP1 = p1;
            Chromosome finalP2 = p2;
            int finalR1 = r1;
            int finalR2 = r2;

            cross = index < popSize * crossRate;
            boolean finalCross = cross;
            crossoverTasks.add(() -> {
                new BCRC_CrossoverTaskUp(this,finalP1, finalP2, finalR1, finalCross,data).run();
                return null;
            });
            index++;


            if (index < popSize) {
                cross = index < popSize * crossRate;
                boolean finalCross1 = cross;
                crossoverTasks.add(() -> {
                    new BCRC_CrossoverTaskUp(this,finalP2, finalP1, finalR2, finalCross1,data).run();
                    return null;
                });
                index++;
            }
        }
        invokeThreads(service, crossoverTasks);
    }

    private void invokeThreads(ExecutorService service, List<Callable<Void>> crossoverTasks) {
        try {
            service.invokeAll(crossoverTasks);
            List<Chromosome> xChromosomes = crossoverChromosomes;
            synchronized (xChromosomes){
                tempPopulation.addAll(xChromosomes);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }finally {
            service.shutdown();
        }
    }

    private void bestCostRouteCrossoverWithSwap() {
        Chromosome p1, p2;
        int r1, r2;
        int count;
        boolean cross;
        int index =0;
//        ExecutorService service = Executors.newFixedThreadPool(1);
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        crossoverChromosomes = Collections.synchronizedList(new ArrayList<>());
        List<Callable<Void>> crossoverTasks = new ArrayList<>();
        if(selectTechnique.equals("RW"))
            rouletteWheelSetup();
        while (index < popSize) {
            p1 = newPopulation.get(selectionTechnique(rand));
            count = 0;
            do {
                p2 = newPopulation.get(selectionTechnique(rand));
                count++;
            }
            while (count < 10 && p2.toString().equals(p1.toString()));
            do {
                r1 = rand.nextInt(p2.getGenes().length);
                r2 = rand.nextInt(p1.getGenes().length);
            }while (p1.getGenes()[r2].isEmpty() || p2.getGenes()[r1].isEmpty());

            Chromosome finalP1 = p1;
            Chromosome finalP2 = p2;
            int finalR1 = r1;
            int finalR2 = r2;

            cross = index < popSize * crossRate;
            boolean finalCross = cross;
            crossoverTasks.add(() -> {
                new BCRC_CrossoverTaskSwap(this,identity,finalP1, finalP2, finalR1, finalCross,data).run();
                return null;
            });
            index++;


            if (index < popSize) {
                cross = index < popSize * crossRate;
                boolean finalCross1 = cross;
                crossoverTasks.add(() -> {
                    new BCRC_CrossoverTaskSwap(this,identity,finalP2, finalP1, finalR2, finalCross1,data).run();
                    return null;
                });
                index++;
            }
        }
        invokeThreads(service, crossoverTasks);
    }

    private int selectionTechnique(Random rand) {
        if (selectTechnique.equals("RW")){
            return rouletteWheelSelection();
        }else if (selectTechnique.equals("TS")){
            return tournamentSelection(TSRate);
        }else
            return rand.nextInt(popSize);
    }

    private int tournamentSelection(int k) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            list.add(rand.nextInt(popSize));
        }
        if (Math.random() < 0.8) {
            list.sort(Comparator.comparingInt(Integer::intValue));
            return list.getFirst();
        }
        return list.get(rand.nextInt(list.size()));
    }
    private void rouletteWheelSetup(){
        double total = 0.0;
        double lambda = 1e-6;
        for (int i = 0; i < newPopulation.size(); i++){
            popProbabilities[i] = 1 / (newPopulation.get(i).getFitness()+lambda);
            total+=popProbabilities[i];
        }
        for(int i = 0; i < popProbabilities.length; i++){
            popProbabilities[i] = (popProbabilities[i] / total);
        }
    }
    private int rouletteWheelSelection(){
        double rand = Math.random();
        double cumulativeFitness = 0.0;
        for(int i = 0; i < newPopulation.size(); i++){
            cumulativeFitness +=popProbabilities[i];
            if(rand<=cumulativeFitness)
                return i;
        }
        return (int)(rand*popSize);
    }

    public void mutationSelection(){
        if(mutType.equals("R"))
            mutation1();
        else
            mutation2();
    }

    private void mutation1() {
        int mutNum = (int) (popSize * mutRate);
        tempMutPopulation = new ArrayList<>(mutNum);
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        mutationChromosomes = Collections.synchronizedList(new ArrayList<>());
        List<Callable<Void>> mutationTasks = new ArrayList<>();
        if(selectTechnique.equals("RW"))
            rouletteWheelSetup();
        for(int i = 0; i <mutNum; i++) {
            Chromosome p = newPopulation.get(selectionTechnique(rand));
            mutationTasks.add(() -> {
                new Mutation1(this, p, rand, data).run();
                return null;
            });
        }
        invokeMutationThreads(service, mutationTasks);
    }

    private void mutation2() {
        int mutNum = (int) (popSize * mutRate);
        tempMutPopulation = new ArrayList<>(mutNum);
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        mutationChromosomes = Collections.synchronizedList(new ArrayList<>());
        List<Callable<Void>> mutationTasks = new ArrayList<>();
        if(selectTechnique.equals("RW"))
            rouletteWheelSetup();
        for(int i = 0; i <mutNum; i++) {
            Chromosome p = newPopulation.get(selectionTechnique(rand));
            mutationTasks.add(() -> {
                new Mutation2(this, p, rand, data).run();
                return null;
            });
        }
        invokeMutationThreads(service, mutationTasks);
    }


    private void invokeMutationThreads(ExecutorService service, List<Callable<Void>> mutationTasks) {
        try {
            service.invokeAll(mutationTasks);
            List<Chromosome> xChromosomes = mutationChromosomes;
            synchronized (xChromosomes){
                tempMutPopulation.addAll(xChromosomes);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }finally {
            service.shutdown();
        }
    }

    public Map<Integer, Chromosome> getLSChromosomes() {
        return LSChromosomes;
    }


    private void LocalSearch(int generation) {
        Chromosome ch;

        try (ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            //ExecutorService service = Executors.newFixedThreadPool(1);
            HashMap<Integer, Chromosome> newMap = new HashMap<>();
            LSChromosomes = Collections.synchronizedMap(newMap);
            List<Callable<Void>> LSTasks = new ArrayList<>();
            Collections.shuffle(eliteRandomList);
            sortPopulation(newPopulation);
            for (int i = 0; i < Math.min(numOfEliteSearch,eliteRandomList.size()); i++) {
                int r = eliteRandomList.get(i);
                ch = newPopulation.get(r);
                Chromosome finalCh = ch;
                LSTasks.add(() -> {
                    new LocalSearchThreadUp(this, finalCh, identity, r, generation, data).run();
                    return null;
                });
            }
            try {
                service.invokeAll(LSTasks);
                Map<Integer, Chromosome> sChromosomes = LSChromosomes;
                synchronized (sChromosomes) {
                    for (Map.Entry<Integer, Chromosome> entry : sChromosomes.entrySet()) {
                        if(newPopulation.get(entry.getKey()).getFitness() == entry.getValue().getFitness())
                            newPopulation.set(entry.getKey(), entry.getValue());
                        else {
                            newPopulation.remove(newPopulation.size() - 1);
                            newPopulation.add(elitismSize, entry.getValue());
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                service.shutdown();
            }
        }
    }


    public static void swapPatients(ArrayList<String> base, int b, int g) {
        String keyB = base.get(b);
        base.set(b, base.get(g));
        base.set(g, keyB);
    }


    public static boolean conflictCheck(ArrayList<String> c1Route, ArrayList<String> c2Route, int m, int n) {
        int index1;
        int index2;
        Set<String> route2 = new HashSet<>(c2Route);
        for (int i = 0; i < c1Route.size(); i++) {
            if (route2.contains(c1Route.get(i))) {
                index1 = c1Route.indexOf(c1Route.get(i));
                index2 = c2Route.indexOf(c1Route.get(i));
                if (m <= index1 && n > index2 || m > index1 && n <= index2) {
                    return false;
                }
            }
        }
        return true;
    }


    private void maintainElitism() {
        nextPopulation = new ArrayList<>();
        sortPopulation(newPopulation);
        for (int i = 0; i < elitismSize; i++) {
            nextPopulation.add(newPopulation.get(i));
        }
    }

    private void sortPopulation(List<Chromosome> population) {
        population.sort(Comparator.comparingDouble(Chromosome::getFitness));
    }

    private void performanceUpdate(List<Chromosome> population, int iterations) {
        sortPopulation(population);
        if(iterations>0&&bestChromosome.getFitness()==population.getFirst().getFitness()){
            terminator++;
        }else {
            terminator=0;
        }
        bestChromosome = population.getFirst();
    }
}

