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

import java.util.Random;
import org.jikesrvm.VM;
import org.jikesrvm.adaptive.recompilation.CompilerDNA;
import org.jikesrvm.adaptive.util.AOSLogging;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.opt.OptOptions;
import org.jikesrvm.compilers.opt.driver.CompilationPlan;
import org.jikesrvm.compilers.opt.driver.InstrumentationPlan;
import org.jikesrvm.compilers.opt.driver.OptimizationPlanElement;
import org.jikesrvm.compilers.opt.driver.OptimizationPlanner;
import org.jikesrvm.compilers.opt.runtimesupport.OptCompiledMethod;

/**
 * An abstract class providing the interface to the decision making
 * component of the controller.
 */
public abstract class RecompilationStrategy {

  //------  Interface -------

  ControllerPlan considerHotMethod(CompiledMethod cmpMethod, HotMethodEvent hme) {
    // Default behavior, do nothing.
    return null;
  }

  void considerHotCallEdge(CompiledMethod cmpMethod, AINewHotEdgeEvent event) {
    // Default behavior, do nothing.
  }

  // Functionality common to all recompilation strategies
  // (at least for now)

  /**
   *  Initialize the recompilation strategy.<p>
   *
   *  Note: This uses the command line options to set up the
   *  optimization plans, so this must be run after the command line
   *  options are available.
   */
  void init() {
    createOptimizationPlans();
  }

  /**
   * This helper method creates a ControllerPlan, which contains a
   * CompilationPlan, for the passed method using the passed optimization
   * level and instrumentation plan.
   * 
   * Dric0 - Method overloading to suit GA needs
   *
   * @param method the RVMMethod for the plan
   * @param optLevel the optimization level to use in the plan
   * @param instPlan the instrumentation plan to use
   * @param prevCMID the previous compiled method ID
   * @param expectedSpeedup  expected speedup from this recompilation
   * @param expectedCompilationTime expected time for compilation
   *  and execution of the new method
   * @param priority a measure of the oveall benefit we expect to see
   *                 by executing this plan.
   * @return the compilation plan to be used
   */
  ControllerPlan createControllerPlan(RVMMethod method, int optLevel, InstrumentationPlan instPlan, int prevCMID,
                                         double expectedSpeedup, double expectedCompilationTime, 
                                         double priority, HotMethodEvent hme) throws CloneNotSupportedException {

    // Construct the compilation plan (varies depending on strategy)
    //CompilationPlan compPlan = createCompilationPlan((NormalMethod) method, optLevel, instPlan);
    CompilationPlan compPlan = GAcreateCompilationPlan((NormalMethod) method, method.getId(), optLevel, instPlan, hme);
    System.out.println("Method's id: " + method.getId());
    
    // Create the controller plan
    return new ControllerPlan(compPlan,
                                 Controller.controllerClock,
                                 prevCMID,
                                 expectedSpeedup,
                                 expectedCompilationTime,
                                 priority);
  }
  
  /**
   * This helper method creates a ControllerPlan, which contains a
   * CompilationPlan, for the passed method using the passed optimization
   * level and instrumentation plan.
   *
   * @param method the RVMMethod for the plan
   * @param optLevel the optimization level to use in the plan
   * @param instPlan the instrumentation plan to use
   * @param prevCMID the previous compiled method ID
   * @param expectedSpeedup  expected speedup from this recompilation
   * @param expectedCompilationTime expected time for compilation
   *  and execution of the new method
   * @param priority a measure of the oveall benefit we expect to see
   *                 by executing this plan.
   * @return the compilation plan to be used
   */
  ControllerPlan createControllerPlan(RVMMethod method, int optLevel, InstrumentationPlan instPlan, int prevCMID,
                                         double expectedSpeedup, double expectedCompilationTime, double priority) {

    // Construct the compilation plan (varies depending on strategy)
    CompilationPlan compPlan = createCompilationPlan((NormalMethod) method, optLevel, instPlan);
    //CompilationPlan compPlan = GAcreateCompilationPlan((NormalMethod) method, method.getId(), optLevel, instPlan, hme);
    //System.out.println("Method's id: " + method.getId());
    
    // Create the controller plan
    return new ControllerPlan(compPlan,
                                 Controller.controllerClock,
                                 prevCMID,
                                 expectedSpeedup,
                                 expectedCompilationTime,
                                 priority);
  }

  //public int POPULATION_SIZE = 50;
  
  public CompilationPlan GAcreateCompilationPlan(NormalMethod method, int methodId, int optLevel,
                                                   InstrumentationPlan instPlan, HotMethodEvent hme) throws CloneNotSupportedException {
    System.out.println("GAcreateCompilationPlan() - Probably in here SPEA2 will run\nCalling GAcreatePopulation().");
    //GAcreatePopulation(POPULATION_SIZE);
    
    // Dric0 - Vou tentar gerar aqui (utilizando o SPEA2) novos valores para _options
    //GAPopulation population = GAPopulation.getInstance();
    //OptOptions[] _opt = population.getFittest().getGAOptions();
    
    GAHash map = GAHash.getInstance();
    double recentSample = hme.getNumSamples();
    
    GATree tree = GATree.getInstance();
    if (!map.checkExistence(methodId)) {
      // No entries on the hash.
      //System.out.println("No entries on the hash.");
      GAIndividual individual = tree.getGARoot().getDNA();
      map.add(methodId, recentSample, tree.getGARoot());     // This "recentSample" represents the value already executed.
    } else {
      // Already on the hash map.
      //System.out.println("Already on the hash map.");
      GAWrapper tuple = map.getValues(methodId);
      //GAIndividual individual = map.getIndividual(methodId);
      //double previousSample = map.getSamples(methodId);
      GAIndividual individual = tuple.getNode().getDNA();
      double previousSample = tuple.getSamples();
      individual.setFitness(previousSample);
      
      GAPopulation pop = new GAPopulation();
      
      
      //if (individual == tree.getGARoot().getDNA()) {
      if (tuple.getNode() == tree.getGARoot()) {
        // It is the root -> Breadth search for new node OR create new one in case none is found.
        
        if (tree.getGARoot().getLeftChild() == null) { // No childs - no need for search, just create new node and set leftChild.
        
        // To generate new node -> Select two individuals from pop (using its probability)
        //                      -> Crossover them and check if mutate or not
        //                      -> Replace one of the parents with the new node
        
        // Population we use at this new node:
        pop = tuple.getNode().getPopulation().clone(); // Population copied.
        
        // TODO - Select two individuals from the cloned pop.
        
        //tree.getGARoot().setLeftChild(tuple.getNode());
        tree.addChild(individual, pop); // TODO - Parameters not right. Need yet to generate new individual and pop.
        
        }
        
      } else { // The node is not the root.
        
      }
      
      //tuple.getIndividual().setFitness(tuple.getSamples());
      map.add(methodId, recentSample, tuple.getNode()); // TODO - Need to add poiting to new node.
    }
    
    
    /*System.out.println("Retrived map.");
    if (!map.checkExistence(methodId)) {
      System.out.println("No key inside map.");
    }*/
    GAWrapper tuple = map.getValues(methodId);
    System.out.println("Retrieving tuple from map. Samples: " + tuple.getSamples());
    GAIndividual individual = tuple.getNode().getDNA();
    OptOptions[] _opt = individual.getGAOptions();
    //map.add2(optLevel, optLevel, individual);
    
    //System.out.println("_options[optLevel]: " + _options[optLevel]);
    // Construct a plan from the basic pre-computed opt-levels
    //return new CompilationPlan(method, _optPlans[optLevel], null, _options[optLevel]);
    return new CompilationPlan(method, _optPlans[optLevel], null, _opt[optLevel]);
  }
  
  //protected Random rand = new Random();
  
  /*public int getRandomOpt() {
    //rand = new Random();
    // Only 16 boolean Opt selected so far.
    return rand.nextInt(16);
  }*/
  
  /*public void GAcreatePopulation(int populationSize) {
    System.out.println("Inside GAcreatePopulation");
    GAIndividual[] individuals;
    individuals = new GAIndividual[populationSize];
    
    OptOptions[] GAOptions = _options;
    
    for (int i = 0; i < POPULATION_SIZE; i++) {
      int INDEX = getRandomOpt();
      //System.out.println("TESTE: " + INDEX);
      System.out.println("Individuals[" + i + "]. <------------");
      individuals[i] = new GAIndividual();
      individuals[i].createIndividual(GAOptions, INDEX);
    }
    
    int maxOptLevel = getMaxOptLevel();
    for (int i = 0; i <= maxOptLevel; i++) {
      //System.out.println("GAOptions: " + GAOptions[i]);
    }
  }*/
  
  /**
   * Constructs a compilation plan that will compile the given method
   * with instrumentation.
   *
   * @param method The method to be compiled with instrumentation
   * @param optLevel The opt-level to recompile at
   * @param instPlan The instrumentation plan
   * @return a non-{@code null} compilation plan
   */
  public CompilationPlan createCompilationPlan(NormalMethod method, int optLevel,
                                                   InstrumentationPlan instPlan) {
    System.out.println("Inside RecompilationStrategy.java - createCompilationPlan() - Calling the GA version.");
    //GAcreateCompilationPlan(method, optLevel, instPlan);
    // Construct a plan from the basic pre-computed opt-levels
    return new CompilationPlan(method, _optPlans[optLevel], null, _options[optLevel]);
  }

  /**
   * Should we consider the hme for recompilation?
   *
   * @param hme the HotMethodEvent
   * @param plan the ControllerPlan for the compiled method (may be {@code null})
   * @return {@code true/false} value
   */
  boolean considerForRecompilation(HotMethodEvent hme, ControllerPlan plan) {
    RVMMethod method = hme.getMethod();
    if (plan == null) {
      // Our caller did not find a matching plan for this compiled method.
      // Therefore the code was not generated by the AOS recompilation subsystem.
      if (ControllerMemory.shouldConsiderForInitialRecompilation(method)) {
        // AOS has not already taken action to address the situation
        // (or it attempted to take action, and the attempt failed in a way
        //  that doesn't preclude trying again,
        //  for example the compilation queue could have been full).
        return true;
      } else {
        // AOS has already taken action to address the situation, and thus
        // we need to handle this as an old compiled version of a
        // method still being live on some thread's stack.
        transferSamplesToNewPlan(hme);
        return false;
      }
    } else {
      // A matching plan was found.
      if (plan.getStatus() == ControllerPlan.OUTDATED ||
          ControllerMemory.planWithStatus(method, ControllerPlan.IN_PROGRESS)) {
        // (a) The HotMethodEvent actually corresponds to an
        // old compiled version of the method
        // that is still live on some thread's stack or
        // (b) AOS has already initiated a plan that hasn't
        // completed yet to address the situation.
        // Therefore don't initiate a new recompilation action.
        transferSamplesToNewPlan(hme);
        return false;
      }
      // if AOS failed to successfully recompile this method before.
      // Don't try it again.
      return !ControllerMemory.planWithStatus(method, ControllerPlan.ABORTED_COMPILATION_ERROR);
    }
  }

  private void transferSamplesToNewPlan(HotMethodEvent hme) {
    AOSLogging.logger.oldVersionStillHot(hme);
    double oldNumSamples = Controller.methodSamples.getData(hme.getCMID());
    ControllerPlan activePlan = ControllerMemory.findLatestPlan(hme.getMethod());
    if (activePlan == null) return; // shouldn't happen.
    int newCMID = activePlan.getCMID();
    if (newCMID > 0) {
      // If we have a valid CMID then transfer the samples.
      // If the CMID isn't valid, it means the compilation hasn't completed yet and
      // the samples will be transfered by the compilation thread when it does (so we do nothing).
      Controller.methodSamples.reset(hme.getCMID());
      double expectedSpeedup = activePlan.getExpectedSpeedup();
      double newNumSamples = oldNumSamples / expectedSpeedup;
      Controller.methodSamples.augmentData(newCMID, newNumSamples);
    }
  }

  /**
   *  This method returns {@code true} if we've already tried to recompile the
   *  passed method.  It does not guarantee that the compilation was
   *  successful.
   *
   *  @param method the method of interest
   *  @return whether we've tried to recompile this method
   */
  boolean previousRecompilationAttempted(RVMMethod method) {
    return ControllerMemory.findLatestPlan(method) != null;
  }

  /**
   *  @param cmpMethod the compiled method whose previous compiler we want to know
   *  @return the constant for the previous compiler
   */
  int getPreviousCompiler(CompiledMethod cmpMethod) {
    switch (cmpMethod.getCompilerType()) {
      case CompiledMethod.TRAP:
      case CompiledMethod.JNI:
        return -1; // don't try to optimize these guys!
      case CompiledMethod.BASELINE: {
        // Prevent the adaptive system from recompiling certain classes
        // of baseline compiled methods.
        if (cmpMethod.getMethod().getDeclaringClass().hasDynamicBridgeAnnotation()) {
          // The opt compiler does not implement this calling convention.
          return -1;
        }
        if (cmpMethod.getMethod().getDeclaringClass().hasBridgeFromNativeAnnotation()) {
          // The opt compiler does not implement this calling convention.
          return -1;
        }
        if (cmpMethod.getMethod().hasNoOptCompileAnnotation()) {
          // Explict declaration that the method should not be opt compiled.
          return -1;
        }
        if (!cmpMethod.getMethod().isInterruptible()) {
          // A crude filter to identify the subset of core VM methods that
          // can't be recompiled because we require their code to be non-moving.
          // We really need to do a better job of this to avoid missing too many opportunities.
          // NOTE: it doesn't matter whether or not the GC is non-moving here,
          //       because recompiling effectively moves the code to a new location even if
          //       GC never moves it again!!!
          //      (C code may have a return address or other naked pointer into the old instruction array)
          return -1;
        }
        return 0;
      }
      case CompiledMethod.OPT:
        OptCompiledMethod optMeth = (OptCompiledMethod) cmpMethod;
        return CompilerDNA.getCompilerConstant(optMeth.getOptLevel());
      default:
        if (VM.VerifyAssertions) VM._assert(VM.NOT_REACHED, "Unknown Compiler");
        return -1;
    }
  }

  /**
   * @return is the maximum opt level that is valid according to this strategy
   */
  int getMaxOptLevel() {
    return Controller.options.DERIVED_MAX_OPT_LEVEL;
  }

  protected OptimizationPlanElement[][] _optPlans;
  protected OptOptions[] _options;

  /**
   * Creates the default set of &lt;optimization plan, options&gt; pairs.
   * Processes optimizing compiler command line options.
   */
  void createOptimizationPlans() {
    OptOptions options = new OptOptions();

    int maxOptLevel = getMaxOptLevel();
    _options = new OptOptions[maxOptLevel + 1];
    _optPlans = new OptimizationPlanElement[maxOptLevel + 1][];
    String[] optCompilerOptions = Controller.getOptCompilerOptions();
    for (int i = 0; i <= maxOptLevel; i++) {
      _options[i] = options.dup();
      _options[i].setOptLevel(i);               // set optimization level specific optimizations
      processCommandLineOptions(_options[i], i, maxOptLevel, optCompilerOptions);
      _optPlans[i] = OptimizationPlanner.createOptimizationPlan(_options[i]);
    }
  }

  /**
   * Process the command line arguments and pass the appropriate ones to the
   * Options
   * Called by sampling and counters recompilation strategy.
   *
   * @param options The options being constructed
   * @param optLevel The level of the options being constructed
   * @param maxOptLevel The maximum valid opt level
   * @param optCompilerOptions The list of command line options
   */
  public static void processCommandLineOptions(OptOptions options, int optLevel, int maxOptLevel,
                                               String[] optCompilerOptions) {

    String prefix = "opt" + optLevel + ":";
    for (String optCompilerOption : optCompilerOptions) {
      if (optCompilerOption.startsWith("opt:")) {
        String option = optCompilerOption.substring(4);
        if (!options.processAsOption("-X:recomp:", option)) {
          VM.sysWrite("vm: Unrecognized optimizing compiler command line argument: \"" +
                      option +
                      "\" passed in as " +
                      optCompilerOption +
                      "\n");
        }
      } else if (optCompilerOption.startsWith(prefix)) {
        String option = optCompilerOption.substring(5);
        if (!options.processAsOption("-X:recomp:" + prefix, option)) {
          VM.sysWrite("vm: Unrecognized optimizing compiler command line argument: \"" +
                      option +
                      "\" passed in as " +
                      optCompilerOption +
                      "\n");
        }
      }
    }
    // TODO: check for optimization levels that are invalid; that is,
    // greater than optLevelMax.
    //
    for (String optCompilerOption1 : optCompilerOptions) {
      if (!optCompilerOption1.startsWith("opt")) {
        // This should never be the case!
        continue;
      }
      if (!optCompilerOption1.startsWith("opt:")) {
        // must specify optimization level!
        int endPoint = optCompilerOption1.indexOf(':');
        if (endPoint == -1) {
          VM.sysWrite("vm: Unrecognized optimization level in optimizing compiler command line argument: \"" +
                      optCompilerOption1 +
                      "\"\n");
        }
        String optLevelS;
        try {
          optLevelS = optCompilerOption1.substring(3, endPoint);
        } catch (IndexOutOfBoundsException e) {
          VM.sysWriteln("vm internal error: trying to find opt level has thrown indexOutOfBoundsException");
          e.printStackTrace();
          continue;
        }
        try {
          Integer optLevelI = Integer.valueOf(optLevelS);
          int cmdOptLevel = optLevelI;
          if (cmdOptLevel > maxOptLevel) {
            VM.sysWrite("vm: Invalid optimization level in optimizing compiler command line argument: \"" +
                        optCompilerOption1 +
                        "\"\n" +
                        "  Specified optimization level " +
                        cmdOptLevel +
                        " must be less than " +
                        maxOptLevel +
                        "\n");
          }
        } catch (NumberFormatException e) {
          VM.sysWrite("vm: Unrecognized optimization level in optimizing compiler command line argument: \"" +
                      optCompilerOption1 +
                      "\"\n");
        }
      }
    }
  }
}




