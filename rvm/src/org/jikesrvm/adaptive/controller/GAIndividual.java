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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.jikesrvm.compilers.opt.OptOptions;
import org.jikesrvm.compilers.opt.util.Randomizer;
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
    
    public boolean FIELD_ANALYSIS;
    public boolean INLINE;
    public boolean INLINE_GUARDED;
    public boolean INLINE_GUARDED_INTERFACES;
    public boolean INLINE_PREEX;
    public boolean LOCAL_CONSTANT_PROP;
    public boolean LOCAL_COPY_PROP;
    public boolean LOCAL_CSE;
    public boolean REORDER_CODE;
    public boolean H2L_INLINE_NEW;
    public boolean REGALLOC_COALESCE_MOVES;
    public boolean REGALLOC_COALESCE_SPILLS;
    public boolean CONTROL_STATIC_SPLITTING;
    public boolean ESCAPE_SCALAR_REPLACE_AGGREGATES;
    public boolean ESCAPE_MONITOR_REMOVAL;
    public boolean REORDER_CODE_PH;
    public boolean H2L_INLINE_WRITE_BARRIER;
    public boolean H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
    public boolean OSR_GUARDED_INLINING;
    public boolean OSR_INLINE_POLICY;
    public boolean L2M_HANDLER_LIVENESS;

    int prevCompiler;
    
    private final int maxSamples = Controller.options.METHOD_SAMPLE_SIZE * RVMThread.availableProcessors;
    
    private double fitness;
    
    public double sample;
    
    private double probability;

    int maxOptLevel = getMaxOptLevel();
    
    public OptOptions[] GAOptions; //= new OptOptions[maxOptLevel + 1];
    
    public List optionList = new ArrayList<String>();
    
    public double getFitness() {
      /*if (fitness == 0) {
        fitness = maxSamples;
      }*/
      return fitness;
    }
    
    public void setFitness(double numSamples) {
      fitness = numSamples/maxSamples;
      System.out.println("\tSetting new fitness: " + fitness);
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
    
    public void createIndividual() {
      //System.out.println("Inside GAIndividuals");
      
      // Tentar fazer igual no createOptimizationPlans() do RecompilationStrategy.java :/
      //GAOptions = options.clone();
      initOptOption();
      
      //GAOptOptions GAOpt;
      //GAOpt = new GAOptOptions();
      
      //int maxOptLevel = getMaxOptLevel();
      
      //this.GAOptions[maxOptLevel].setOptLevel(maxOptLevel);
      
      //if (INDEX == 0) System.out.println("SAIU O 0 -------------------------------------------------------------------------------------------------------------------------------------------");
      //System.out.println("INDEX: " + INDEX);
      //System.out.println("Value of FREQ_FOCUS_EFFORT before mutateBoolean: " + GAOptions[maxOptLevel].FREQ_FOCUS_EFFORT);
      Randomizer rand = Randomizer.getInstance();
      int INDEX = rand.nextInt(20);
      mutateBoolean(INDEX, maxOptLevel);
      //System.out.println("Value of FREQ_FOCUS_EFFORT after mutateBoolean: " + GAOptions[maxOptLevel].FREQ_FOCUS_EFFORT);
      
      //String GAOptions;
      //GAOptions = Arrays.toString(options);
      //System.out.println("\tTesting String version of optOptions: " + GAOptions);
    }
    
    public void createIndividual(int optLevel) {
      initOptOption(optLevel);

      mutateBoolean(maxOptLevel);
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
    
    public void initOptOption(int level) {
      if (level >= 0)
        FIELD_ANALYSIS = true;
     else
        FIELD_ANALYSIS = false;
     if (level >= 0)
        INLINE = true;
     else
        INLINE = false;
     if (level >= 0)
        INLINE_GUARDED = true;
     else
        INLINE_GUARDED = false;
     if (level >= 0)
        INLINE_GUARDED_INTERFACES = true;
     else
        INLINE_GUARDED_INTERFACES = false;
     if (level >= 0)
        INLINE_PREEX = true;
     else
        INLINE_PREEX = false;
     if (level >= 0)
        LOCAL_CONSTANT_PROP = true;
     else
        LOCAL_CONSTANT_PROP = false;
     if (level >= 0)
        LOCAL_COPY_PROP = true;
     else
        LOCAL_COPY_PROP = false;
     if (level >= 0)
        LOCAL_CSE = true;
     else
        LOCAL_CSE = false;
     if (level >= 1)
        CONTROL_STATIC_SPLITTING = true;
     else
        CONTROL_STATIC_SPLITTING = false;
     if (level >= 1)
        ESCAPE_SCALAR_REPLACE_AGGREGATES = true;
     else
        ESCAPE_SCALAR_REPLACE_AGGREGATES = false;
     if (level >= 1)
        ESCAPE_MONITOR_REMOVAL = true;
     else
        ESCAPE_MONITOR_REMOVAL = false;
     if (level >= 0)
        REORDER_CODE = true;
     else
        REORDER_CODE = false;
     if (level >= 1)
        REORDER_CODE_PH = true;
     else
        REORDER_CODE_PH = false;
     if (level >= 0)
        H2L_INLINE_NEW = true;
     else
        H2L_INLINE_NEW = false;
     if (level >= 1)
        H2L_INLINE_WRITE_BARRIER = true;
     else
        H2L_INLINE_WRITE_BARRIER = false;
     if (level >= 1)
        H2L_INLINE_PRIMITIVE_WRITE_BARRIER = true;
     else
        H2L_INLINE_PRIMITIVE_WRITE_BARRIER = false;
     if (level >= 2)
        L2M_HANDLER_LIVENESS = true;
     else
        L2M_HANDLER_LIVENESS = false;
     if (level >= 0)
        REGALLOC_COALESCE_MOVES = true;
     else
        REGALLOC_COALESCE_MOVES = false;
     if (level >= 0)
        REGALLOC_COALESCE_SPILLS = true;
     else
        REGALLOC_COALESCE_SPILLS = false;
     if (level >= 1)
        OSR_GUARDED_INLINING = true;
     else
        OSR_GUARDED_INLINING = false;
     if (level >= 1)
        OSR_INLINE_POLICY = true;
     else
        OSR_INLINE_POLICY = false;
    
    }
    
    public OptOptions[] getGAOptions() {
      return this.GAOptions;
    }
    
    public boolean mutateBoolean(int INDEX, int optLevel) {
      
      //int maxOptLevel = getMaxOptLevel();
      
      if (optLevel >= 0) {
        if (INDEX == 0) {
            //System.out.println("Inside mutateBoolean - GAOptOptions.java\nFREQ_FOCUS_EFFORT is the one at INDEX.");
            GAOptions[0].FIELD_ANALYSIS = !GAOptions[0].FIELD_ANALYSIS;
            return true;
        }
        if (INDEX == 1) {
            GAOptions[0].INLINE = !GAOptions[0].INLINE;
            return true;
        }
        if (INDEX == 2) {
            GAOptions[0].INLINE_GUARDED = !GAOptions[0].INLINE_GUARDED;
            return true;
        }
        if (INDEX == 3) {
            GAOptions[0].INLINE_GUARDED_INTERFACES = !GAOptions[0].INLINE_GUARDED_INTERFACES;
            return true;
        }
        if (INDEX == 4) {
            GAOptions[0].INLINE_PREEX = !GAOptions[0].INLINE_PREEX;
            return true;
        }
        if (INDEX == 5) {
            GAOptions[0].LOCAL_CONSTANT_PROP = !GAOptions[0].LOCAL_CONSTANT_PROP;
            return true;
        }
        if (INDEX == 6) {
            GAOptions[0].LOCAL_COPY_PROP = !GAOptions[0].LOCAL_COPY_PROP;
            return true;
        }
        if (INDEX == 7) {
            GAOptions[0].LOCAL_CSE = !GAOptions[0].LOCAL_CSE;
            return true;
        }
        if (INDEX == 8) {
            GAOptions[0].REORDER_CODE = !GAOptions[0].REORDER_CODE;
            return true;
        }
        if (INDEX == 9) {
            GAOptions[0].H2L_INLINE_NEW = !GAOptions[0].H2L_INLINE_NEW;
            return true;
        }
        if (INDEX == 10) {
            GAOptions[0].REGALLOC_COALESCE_MOVES = !GAOptions[0].REGALLOC_COALESCE_MOVES;
            return true;
        }
        if (INDEX == 11) {
            GAOptions[0].REGALLOC_COALESCE_SPILLS = !GAOptions[0].REGALLOC_COALESCE_SPILLS;
            return true;
        }
      } else if (optLevel >= 1) {
        if (INDEX == 12) {
            GAOptions[1].CONTROL_STATIC_SPLITTING = !GAOptions[1].CONTROL_STATIC_SPLITTING;
            return true;
        }
        if (INDEX == 13) {
            GAOptions[1].ESCAPE_SCALAR_REPLACE_AGGREGATES = !GAOptions[1].ESCAPE_SCALAR_REPLACE_AGGREGATES;
            return true;
        }
        if (INDEX == 14) {
            GAOptions[1].ESCAPE_MONITOR_REMOVAL = !GAOptions[1].ESCAPE_MONITOR_REMOVAL;
            return true;
        }
        if (INDEX == 15) {
            GAOptions[1].REORDER_CODE_PH = !GAOptions[1].REORDER_CODE_PH;
            return true;
        }
        if (INDEX == 16) {
            GAOptions[1].H2L_INLINE_WRITE_BARRIER = !GAOptions[1].H2L_INLINE_WRITE_BARRIER;
            return true;
        }
        if (INDEX == 17) {
            GAOptions[1].H2L_INLINE_PRIMITIVE_WRITE_BARRIER = !GAOptions[1].H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
            return true;
        }
        if (INDEX == 18) {
            GAOptions[1].OSR_GUARDED_INLINING = !GAOptions[1].OSR_GUARDED_INLINING;
            return true;
        }
        if (INDEX == 19) {
            GAOptions[1].OSR_INLINE_POLICY = !GAOptions[1].OSR_INLINE_POLICY;
            return true;
        }
      } else if (optLevel >= 2) {
        if (INDEX == 20) {
            GAOptions[2].L2M_HANDLER_LIVENESS = !GAOptions[2].L2M_HANDLER_LIVENESS;
            return true;
        }
      }     
      return false;
    }
    
    public boolean mutateBoolean(int optLevel) {
        
      Randomizer rand = Randomizer.getInstance();
      int INDEX;
      if (optLevel >= 0)
          INDEX = rand.nextInt(12);
      else if (optLevel >= 1)
          INDEX = rand.nextInt(20);
      else 
          INDEX = rand.nextInt(21);
      
      if (optLevel >= 0) {
        if (INDEX == 0) {
            //System.out.println("Inside mutateBoolean - GAOptOptions.java\nFREQ_FOCUS_EFFORT is the one at INDEX.");
            FIELD_ANALYSIS = !FIELD_ANALYSIS;
            return true;
        }
        if (INDEX == 1) {
            INLINE = !INLINE;
            return true;
        }
        if (INDEX == 2) {
            INLINE_GUARDED = !INLINE_GUARDED;
            return true;
        }
        if (INDEX == 3) {
            INLINE_GUARDED_INTERFACES = !INLINE_GUARDED_INTERFACES;
            return true;
        }
        if (INDEX == 4) {
            INLINE_PREEX = !INLINE_PREEX;
            return true;
        }
        if (INDEX == 5) {
            LOCAL_CONSTANT_PROP = !LOCAL_CONSTANT_PROP;
            return true;
        }
        if (INDEX == 6) {
            LOCAL_COPY_PROP = !LOCAL_COPY_PROP;
            return true;
        }
        if (INDEX == 7) {
            LOCAL_CSE = !LOCAL_CSE;
            return true;
        }
        if (INDEX == 8) {
            REORDER_CODE = !REORDER_CODE;
            return true;
        }
        if (INDEX == 9) {
            H2L_INLINE_NEW = !H2L_INLINE_NEW;
            return true;
        }
        if (INDEX == 10) {
            REGALLOC_COALESCE_MOVES = !REGALLOC_COALESCE_MOVES;
            return true;
        }
        if (INDEX == 11) {
            REGALLOC_COALESCE_SPILLS = !REGALLOC_COALESCE_SPILLS;
            return true;
        }
      } else if (optLevel >= 1) {
        if (INDEX == 12) {
            CONTROL_STATIC_SPLITTING = !CONTROL_STATIC_SPLITTING;
            return true;
        }
        if (INDEX == 13) {
            ESCAPE_SCALAR_REPLACE_AGGREGATES = !ESCAPE_SCALAR_REPLACE_AGGREGATES;
            return true;
        }
        if (INDEX == 14) {
            ESCAPE_MONITOR_REMOVAL = !ESCAPE_MONITOR_REMOVAL;
            return true;
        }
        if (INDEX == 15) {
            REORDER_CODE_PH = !REORDER_CODE_PH;
            return true;
        }
        if (INDEX == 16) {
            H2L_INLINE_WRITE_BARRIER = !H2L_INLINE_WRITE_BARRIER;
            return true;
        }
        if (INDEX == 17) {
            H2L_INLINE_PRIMITIVE_WRITE_BARRIER = !H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
            return true;
        }
        if (INDEX == 18) {
            OSR_GUARDED_INLINING = !OSR_GUARDED_INLINING;
            return true;
        }
        if (INDEX == 19) {
            OSR_INLINE_POLICY = !OSR_INLINE_POLICY;
            return true;
        }
      } else if (optLevel >= 2) {
        if (INDEX == 20) {
            L2M_HANDLER_LIVENESS = !L2M_HANDLER_LIVENESS;
            return true;
        }
      }
      
      return false;
    }
    
    public void copy(GAIndividual orig) {
        FIELD_ANALYSIS = orig.FIELD_ANALYSIS;
        INLINE = orig.INLINE;
        INLINE_GUARDED = orig.INLINE_GUARDED;
        INLINE_GUARDED_INTERFACES = orig.INLINE_GUARDED_INTERFACES;
        INLINE_PREEX = orig.INLINE_PREEX;
        LOCAL_CONSTANT_PROP = orig.LOCAL_CONSTANT_PROP;
        LOCAL_COPY_PROP = orig.LOCAL_COPY_PROP;
        LOCAL_CSE = orig.LOCAL_CSE;
        REORDER_CODE = orig.REORDER_CODE;
        H2L_INLINE_NEW = orig.H2L_INLINE_NEW;
        REGALLOC_COALESCE_MOVES = orig.REGALLOC_COALESCE_MOVES;
        REGALLOC_COALESCE_SPILLS = orig.REGALLOC_COALESCE_SPILLS;
        CONTROL_STATIC_SPLITTING = orig.CONTROL_STATIC_SPLITTING;
        ESCAPE_SCALAR_REPLACE_AGGREGATES = orig.ESCAPE_SCALAR_REPLACE_AGGREGATES;
        ESCAPE_MONITOR_REMOVAL = orig.ESCAPE_MONITOR_REMOVAL;
        REORDER_CODE_PH = orig.REORDER_CODE_PH;
        H2L_INLINE_WRITE_BARRIER = orig.H2L_INLINE_WRITE_BARRIER;
        H2L_INLINE_PRIMITIVE_WRITE_BARRIER = orig.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
        OSR_GUARDED_INLINING = orig.OSR_GUARDED_INLINING;
        OSR_INLINE_POLICY = orig.OSR_INLINE_POLICY;
        L2M_HANDLER_LIVENESS = orig.L2M_HANDLER_LIVENESS;
    }
    
}
