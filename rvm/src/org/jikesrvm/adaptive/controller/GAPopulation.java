/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import org.jikesrvm.compilers.opt.util.Randomizer;

/**
 *
 * @author dric0
 */
public class GAPopulation implements Cloneable {
    private static final GAPopulation INSTANCE = new GAPopulation();
    
    public GAPopulation() {}
    
    public GAPopulation(int popSize, int optLevel, boolean root) {
      this.popSize = popSize;
      this.initPopulation(optLevel, root);
    }
    
    public GAPopulation(GAPopulation orig) {
      this.popSize = orig.getPopulationSize();
      this.copyPopulation(orig);
    }
    
    public static GAPopulation getInstance() {
      return INSTANCE;
    }

    private Random rand;
    
    private int popSize;
    
    GAIndividual[] individuals;
    
    public void setRandom(Random rand) {
      this.rand = rand;
    }
    
    public Random getRandom() {
      return this.rand;
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
    
    public GAIndividual getRandomIndividual() {
      Randomizer randUtil = Randomizer.getInstance();
      int INDEX = randUtil.nextInt(popSize);
      return individuals[INDEX];
    }
    
    public GAIndividual getRootIndividual() {
      return individuals[0];
    }
    
    public void replaceIndividual(int INDEX, GAIndividual newIndividual) {
      this.individuals[INDEX] = newIndividual;
    }
    
    public void initPopulation() {
      //System.out.println("Inside initPopulation");
      individuals = new GAIndividual[popSize];  
      Randomizer randUtil = Randomizer.getInstance();
      this.rand = randUtil.getRandom();
    
      for (int i = 0; i < popSize; i++) {
        int INDEX = rand.nextInt(16);
        //System.out.println("TESTE: " + INDEX);
        //System.out.println("Individuals[" + i + "]. <------------");
        individuals[i] = new GAIndividual();
        individuals[i].createIndividual();
        individuals[i].setProbability(popSize);
      }
    }
    
    public void initPopulation(int optLevel, boolean root) {
      //System.out.println("Inside initPopulation");
      individuals = new GAIndividual[popSize];  
    
      int i = 0;
      if (root) {
          individuals[i] = new GAIndividual();
          individuals[i].createRootIndividual(optLevel);
          individuals[i].setFitness(1);
          i = 1;
      }      
      
      for (; i < popSize; i++) {
        individuals[i] = new GAIndividual();
        individuals[i].createIndividual(optLevel);
        individuals[i].setProbability(popSize);
        individuals[i].setFitness(1);
      }
    }
    
    public final void copyPopulation(GAPopulation orig) {
      this.individuals = new GAIndividual[popSize];
      for (int i = 0; i < popSize; i++) {
        this.individuals[i] = new GAIndividual();
        this.individuals[i].copy(orig.individuals[i]);
      }
    }
    
    public void selectParents() {
      /*GAPopulation tournament = new GAPopulation();
      tournament.setPopulationSize(2);
      tournament.individuals = new GAIndividual[2];
      tournament.individuals[0] = new GAIndividual();
      int INDEX1 = rand.nextInt(popSize);
      tournament.individuals[0] = this.getIndividual(INDEX1);
      
      tournament.individuals[1] = new GAIndividual();
      int INDEX2 = rand.nextInt(popSize);
      tournament.individuals[1] = this.getIndividual(INDEX2);*/
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

    public static Object copy(Object orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }
    
}
