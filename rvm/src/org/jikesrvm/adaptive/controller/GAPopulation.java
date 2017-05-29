/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

import java.util.Random;

/**
 *
 * @author dric0
 */
public class GAPopulation implements Cloneable {
    private static final GAPopulation INSTANCE = new GAPopulation();
    
    public GAPopulation() {}
    
    public static GAPopulation getInstance() {
      return INSTANCE;
    }

    private Random rand;
    
    private int popSize;
    
    GAIndividual[] individuals;
    
    public void setRandom(Random rand) {
      this.rand = rand;
    }
    
    public void setPopulationSize(int popSize) {
      this.popSize = popSize;
    }
    
    public int getPopulationSize() {
      return popSize;
    }
    
    public GAIndividual getIndividual(int index){
      return individuals[index];
    }
    
    public GAIndividual getFittest() {
      GAIndividual fittest = individuals[0];
      // Loop through individuals to find fittest
      for (int i = 0; i < popSize; i++) {
          //if (fittest.getFitness() <= getIndividual(i).getFitness()) {
              //fittest = getIndividual(i);
          //}
      }
      return fittest;
    }
    
    public GAIndividual getDefaultOptOptions() {
      //int INDEX = rand.nextInt(popSize);
      //return this.individuals[INDEX];
      GAIndividual defaultOptOptions = new GAIndividual();
      defaultOptOptions.initOptOption();
      return defaultOptOptions;
    }
    
    public void initPopulation() {
      System.out.println("Inside initPopulation");
      individuals = new GAIndividual[popSize];  
    
      for (int i = 0; i < popSize; i++) {
        int INDEX = rand.nextInt(16);
        //System.out.println("TESTE: " + INDEX);
        System.out.println("Individuals[" + i + "]. <------------");
        individuals[i] = new GAIndividual();
        individuals[i].createIndividual(INDEX);
        individuals[i].setProbability(popSize);
      }
    }
    
    public void selectParents() {
      GAPopulation tournament = new GAPopulation();
      tournament.setPopulationSize(2);
      tournament.individuals = new GAIndividual[2];
      tournament.individuals[0] = new GAIndividual();
      int INDEX1 = rand.nextInt(popSize);
      tournament.individuals[0] = this.getIndividual(INDEX1);
      
      tournament.individuals[1] = new GAIndividual();
      int INDEX2 = rand.nextInt(popSize);
      tournament.individuals[1] = this.getIndividual(INDEX2);
    }
    
    @Override
    public GAPopulation clone() throws CloneNotSupportedException {
      try {
        final GAPopulation result = (GAPopulation) super.clone();
        // copy fields that need to be copied here!
        result.individuals = individuals.clone();
        return result;
      } catch (final CloneNotSupportedException ex) {
          throw new AssertionError();
      }
    }

}
