/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package org.jikesrvm.adaptive.controller;

//import java.util.Arrays;
//import org.jikesrvm.adaptive.recompilation.CompilerDNA;
//import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.opt.OptOptions;
import org.jikesrvm.scheduler.RVMThread;
//import org.jikesrvm.compilers.opt.GAOptOptions;
//import java.util.Random;
//import static org.jikesrvm.compilers.opt.GAOptOptions.*;
//import static org.jikesrvm.compilers.opt.GAOptOptions.*;
//import static org.jikesrvm.compilers.opt.GAOptOptions.mutateBooleans;
//import static org.jikesrvm.compilers.opt.GAOptOptions.mutateBoolean;

/**
 *
 * @author dric0
 */
public class GAIndividual {

    int prevCompiler;
    
    private final int maxSamples = Controller.options.METHOD_SAMPLE_SIZE * RVMThread.availableProcessors;
    
    private double fitness;
    
    private double probability;

    int maxOptLevel = getMaxOptLevel();
    
    private OptOptions[] GAOptions; //= new OptOptions[maxOptLevel + 1];
    
    public double getFitness() {
      /*if (fitness == 0) {
        fitness = maxSamples;
      }*/
      return fitness;
    }
    
    public void setFitness(double numSamples) {
      fitness = numSamples/maxSamples;
    }
    
    public GAIndividual() {//CompiledMethod cmpMethod) {
      //this.prevCompiler = getPreviousCompiler(cmpMethod);
    }
    
    /*int getCompiler() {
      CompilerDNA.getCompilerConstant(level);
      return thisChoiceCompiler;
    }*/
    
    int getMaxOptLevel() {
      return Controller.options.DERIVED_MAX_OPT_LEVEL;
    }
    
    public void setProbability(int popSize) {
      probability = 1/popSize;
    }
    
    public void createIndividual(int INDEX) {
      System.out.println("Inside GAIndividuals");
      
      // Tentar fazer igual no createOptimizationPlans() do RecompilationStrategy.java :/
      //GAOptions = options.clone();
      initOptOption();
      
      //GAOptOptions GAOpt;
      //GAOpt = new GAOptOptions();
      
      //int maxOptLevel = getMaxOptLevel();
      
      //this.GAOptions[maxOptLevel].setOptLevel(maxOptLevel);
      
      if (INDEX == 0) System.out.println("SAIU O 0 -------------------------------------------------------------------------------------------------------------------------------------------");
      System.out.println("INDEX: " + INDEX);
      System.out.println("Value of FREQ_FOCUS_EFFORT before mutateBoolean: " + GAOptions[maxOptLevel].FREQ_FOCUS_EFFORT);
      mutateBoolean(INDEX);
      System.out.println("Value of FREQ_FOCUS_EFFORT after mutateBoolean: " + GAOptions[maxOptLevel].FREQ_FOCUS_EFFORT);
      
      //String GAOptions;
      //GAOptions = Arrays.toString(options);
      //System.out.println("\tTesting String version of optOptions: " + GAOptions);
    }
    
    public void initOptOption() {
      OptOptions options = new OptOptions();

      //int maxOptLevel = getMaxOptLevel();
      GAOptions = new OptOptions[maxOptLevel + 1];
      //String[] optCompilerOptions = Controller.getOptCompilerOptions();
      for (int i = 0; i <= maxOptLevel; i++) {
        GAOptions[i] = options.dup();
        GAOptions[i].setOptLevel(i);               // set optimization level specific optimizations
      //processCommandLineOptions(_options[i], i, maxOptLevel, optCompilerOptions);
      }
    }
    
    public OptOptions[] getGAOptions() {
      return this.GAOptions;
    }
    
    public boolean mutateBoolean(int INDEX) {
      
      //int maxOptLevel = getMaxOptLevel();
        
      if (INDEX == 0) {
          //System.out.println("Inside mutateBoolean - GAOptOptions.java\nFREQ_FOCUS_EFFORT is the one at INDEX.");
          GAOptions[maxOptLevel].FREQ_FOCUS_EFFORT = !GAOptions[maxOptLevel].FREQ_FOCUS_EFFORT;
          return true;
      } 
      if (INDEX == 1) {
          GAOptions[maxOptLevel].READS_KILL = !GAOptions[maxOptLevel].READS_KILL;
          return true;
      }
      if (INDEX == 2) {
          GAOptions[maxOptLevel].FIELD_ANALYSIS = !GAOptions[maxOptLevel].FIELD_ANALYSIS;
          return true;
      }
      if (INDEX == 3) {
          GAOptions[maxOptLevel].INLINE = !GAOptions[maxOptLevel].INLINE;
          return true;
      }
      if (INDEX == 4) {
          GAOptions[maxOptLevel].INLINE_GUARDED = !GAOptions[maxOptLevel].INLINE_GUARDED;
          return true;
      }
      if (INDEX == 5) {
          GAOptions[maxOptLevel].INLINE_GUARDED_INTERFACES = !GAOptions[maxOptLevel].INLINE_GUARDED_INTERFACES;
          return true;
      }
      if (INDEX == 6) {
          GAOptions[maxOptLevel].INLINE_PREEX = !GAOptions[maxOptLevel].INLINE_PREEX;
          return true;
      }
      if (INDEX == 7) {
          GAOptions[maxOptLevel].SIMPLIFY_INTEGER_OPS = !GAOptions[maxOptLevel].SIMPLIFY_INTEGER_OPS;
          return true;
      }
      if (INDEX == 8) {
          GAOptions[maxOptLevel].SIMPLIFY_LONG_OPS = !GAOptions[maxOptLevel].SIMPLIFY_LONG_OPS;
          return true;
      }
      if (INDEX == 9) {
          GAOptions[maxOptLevel].SIMPLIFY_FLOAT_OPS = !GAOptions[maxOptLevel].SIMPLIFY_FLOAT_OPS;
          return true;
      }
      if (INDEX == 10) {
          GAOptions[maxOptLevel].SIMPLIFY_DOUBLE_OPS = !GAOptions[maxOptLevel].SIMPLIFY_DOUBLE_OPS;
          return true;
      }
      if (INDEX == 11) {
          GAOptions[maxOptLevel].SIMPLIFY_REF_OPS = !GAOptions[maxOptLevel].SIMPLIFY_REF_OPS;
          return true;
      }
      if (INDEX == 12) {
          GAOptions[maxOptLevel].SIMPLIFY_TIB_OPS = !GAOptions[maxOptLevel].SIMPLIFY_TIB_OPS;
          return true;
      }
      if (INDEX == 13) {
          GAOptions[maxOptLevel].SIMPLIFY_FIELD_OPS = !GAOptions[maxOptLevel].SIMPLIFY_FIELD_OPS;
          return true;
      }
      if (INDEX == 14) {
          GAOptions[maxOptLevel].SIMPLIFY_CHASE_FINAL_FIELDS = !GAOptions[maxOptLevel].SIMPLIFY_CHASE_FINAL_FIELDS;
          return true;
      }
      if (INDEX == 15) {
          GAOptions[maxOptLevel].LOCAL_CONSTANT_PROP = !GAOptions[maxOptLevel].LOCAL_CONSTANT_PROP;
          return true;
      }
        
      // None of the above tests matched, so this wasn't an option
      return false;
    }
}
