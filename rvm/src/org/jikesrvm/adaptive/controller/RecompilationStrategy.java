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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jikesrvm.compilers.opt.util.Randomizer;

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
                                         double priority, HotMethodEvent hme) {

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
                                                   InstrumentationPlan instPlan, HotMethodEvent hme) {
    System.out.println("GAcreateCompilationPlan() - The optimization level to use in the plan: " + optLevel);
    
    System.out.println("Initializing tree from opt level " + optLevel);
    
    
    
    GAHash map = GAHash.getInstance();
    double recentSample = hme.getNumSamples();
    
    GATree tree = null;
    //GATree tree1 = null;
    
    // Caso o level de opt mude para o method -> Verificar o level que foi executado pela ultima vez (currentOptLevel)
    //                                        -> Verificar se houve ganho ou nao de desempenho
    //                                              -> Se melhorou, utilizar a populacao atual para criar um novo no na NOVA arvore
    //                                              -> Se piorou, utilizar a populacao do pai
    
    if (optLevel == 0) {
        tree = GATree.getInstance0();
        System.out.println("The tree being used is the opt0.");
        if (tree.getGARoot() != null) System.out.println("\tThe opt0 tree has its root.");
    } else if (optLevel == 1) {
        tree = GATree.getInstance1();
        System.out.println("The tree being used is the opt1.");
        if (tree.getGARoot() != null) System.out.println("\tThe opt1 tree has its root.");
    } else if (optLevel == 2) {
        tree = GATree.getInstance2();
        System.out.println("The tree being used is the opt2.");
        if (tree.getGARoot() != null) System.out.println("\tThe opt2 tree has its root.");
    }
    
    // Checking the previous optLevel.
    System.out.println("---> Checking if it's the first entry of this method.");
    //if (method.previousOptLevel != -1 && method.previousOptLevel != optLevel) { // Se previousOptLevel for -1 -> nao foi inicializado ainda.
                                                                                // Se previousOptLevel for diferente de optLevel -> mudou o level.
    if (method.previousOptLevel != -1) {
        System.out.println("---> The previousOptLevel is different than -1 -> It is NOT the first entry for this method.");
        
        System.out.println("* Setting sample from the individual at the treeNode with recentSample(" + recentSample + ") *");
        method.treeNode.getDNA().sample = recentSample;
        
        System.out.println("---> Checking if the optLevel changed.");
        if (method.previousOptLevel != optLevel) {
        
            int previousOptLevel = method.previousOptLevel;
            System.out.println("\tThe optLevel DID changed. Current: " + optLevel + ", Previous: " + previousOptLevel);

            System.out.println("\tChecking the performance. Previous sample from old opt level: " + method.sampleRecorded/*getSamplesRecorded(previousOptLevel)*/ + ", recent sample: " + recentSample);
            // TODO - Deixar um unico samplesRecorded - acho que nao tem necessidade de um pra cada level.
            //if (recentSample > method.getSamplesRecorded(previousOptLevel)) {
            if (recentSample > method.sampleRecorded) {
                System.out.println("\t\tPerformance is worse than before -> Need to use the parent's node (in case there is one) population");

                //if (method.getTreeNode(previousOptLevel).getParent() == null) {
                if (method.treeNode.getParent() == null) {
                    System.out.println("\t\t\tThere is no parent (it is the root) -> Getting the root of the actual opt level tree.");
                    System.out.println("\t\t\tCurrent tree node for this method: " + method.getTreeNode(previousOptLevel) + "/" + method.treeNode);
                    method.setTreeNode(optLevel, tree.getGARoot());
                    method.treeNode = tree.getGARoot();
                    System.out.println("\t\t\tTree root (it will be used as new node): " + tree.getGARoot());
                    System.out.println("\t\t\tNew tree node for this method: " + method.getTreeNode(optLevel) + "/" + method.treeNode);
                } else {
                    System.out.println("\t\t\tThere IS a parent for the current node -> Need to rollback.");
                    System.out.println("\t\t\tThe opt level changed so we need to rollback to the parent and use him to create new node in the new tree.");
                    
                    //System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                    //method.treeNode = method.treeNode.getParent();
                    //System.out.println("\t\t\tRollback - New tree node for this method (the parent): " + method.treeNode);
                    
                    System.out.println("\t\t\tCreating new node from the parent.");
                    
                    // TODO - Create new node. check
                    GATreeNode newNode = generateNode(tree, method, optLevel, true);
                    System.out.println("\t\tMaking method point to created node.");
                    System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                    method.treeNode = newNode;
                    System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);
                    
                }
            } else {
                System.out.println("\t\tPerformance is better than before -> Search for childs or create new one.");
                System.out.println("\t\tAs the opt level changed, we will use this node to create a new one and put in the NEW tree.");

                System.out.println("\t\tGenerating new node (Using the new optLevel already)");
                // TODO - Create new node. check
                GATreeNode newNode = generateNode(tree, method, optLevel, false);
                System.out.println("\t\tMaking method point to created node.");
                System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                method.treeNode = newNode;
                System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);

            }
        } else {
            System.out.println("\tThe optLevel did <NOT> change. Current: " + optLevel);
            
            System.out.println("\tChecking the performance. Previous sample from old opt level: " + method.sampleRecorded + ", recent sample: " + recentSample);
            if (recentSample > method.sampleRecorded) {
                System.out.println("\t\tPerformance is worse than before -> Need to rollback if it's not the root, if is the root need to search child OR create new node.");
                
                if (method.treeNode.getParent() == null) {
                    System.out.println("\t\t\tThere is no parent (it is the root) -> Need to breadth search its childs OR create new child node.");
                    if (method.treeNode.getLeftChild() == null) {
                        System.out.println("\t\t\t\tThere is no child node to search -> Creating new one.");
                        
                        // TODO - Create new node. check
                        GATreeNode newNode = generateNode(tree, method, optLevel, false);
                        System.out.println("\t\tMaking method point to created node.");
                        System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                        method.treeNode = newNode;
                        System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);
                        
                    } else {
                        System.out.println("\t\t\t\tThere IS child node to search -> Setting method to the child.");
                        System.out.println("\t\t\tOld tree node for this method: " + method.treeNode + ". Tree node: " + tree.getGARoot());
                        method.treeNode = method.treeNode.getLeftChild();
                        System.out.println("\t\t\tNew tree node for this method: " + method.treeNode + ". Tree node: " + tree.getGARoot());
                    }
                } else {
                    System.out.println("\t\t\tThere IS a parent (current node is NOT the root) -> Need to rollback to the parent and search another path OR create new node.");
                    
                    System.out.println("\t\t\tChecking if the parent has more childs.");
                    if (method.treeNode.getRightSibling() == null) {
                        System.out.println("\t\t\t\tThere are NO brother for this node -> Need to create new node.");
                        //method.treeNode = method.treeNode.getParent();
                        
                        // TODO - Create new node. check
                        GATreeNode newNode = generateNode(tree, method, optLevel, true);
                        System.out.println("\t\tMaking method point to created node.");
                        System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                        method.treeNode = newNode;
                        System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);
                        
                    } else {
                        System.out.println("\t\t\t\tThere is a brother for this node -> Pointing method to it.");
                        System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                        method.treeNode = method.treeNode.getRightSibling();
                        System.out.println("\t\t\tNew tree node for this method (the right brother): " + method.treeNode);
                    }
                }
                
            } else {
                System.out.println("\t\tPerformance is better than before -> Need to go to child node (if there is one) OR create new node.");
                
                if (method.treeNode.getLeftChild() == null) {
                    System.out.println("\t\t\tThere is no child node -> Need to create new one.");
                    
                    // TODO - Create new node. check
                    GATreeNode newNode = generateNode(tree, method, optLevel, false);
                    System.out.println("\t\tMaking method point to created node.");
                    System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                    method.treeNode = newNode;
                    System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);
                    
                } else {
                    System.out.println("\t\t\tThere IS a child node -> Need to set is as the new one.");
                    
                    System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                    method.treeNode = method.treeNode.getLeftChild();
                    System.out.println("\t\t\tNew tree node for this method (left-most child): " + method.treeNode);
                }
                
            }
             
        }
        
        System.out.println("Retrieving Samples from variable in RVMMethod. Samples: " + method.sampleRecorded);
        
        GAIndividual DNA = method.treeNode.getDNA();
        
        System.out.println("Creating new optOptions[] to receive the DNA.");
        OptOptions options = new OptOptions();
        int maxOptLevel = getMaxOptLevel();
        OptOptions[] _opt = new OptOptions[maxOptLevel + 1];
        String[] optCompilerOptions = Controller.getOptCompilerOptions();
        for (int i = 0; i <= maxOptLevel; i++) {
          _opt[i] = options.dup();
          _opt[i].setOptLevel(i);               // set optimization level specific optimizations
          processCommandLineOptions(_opt[i], i, maxOptLevel, optCompilerOptions);
        }
        System.out.println("New optOptions[] created -> Cloning DNA parameters into it.");
        
        cloneOptOptions(_opt, DNA, optLevel);
        System.out.println("Cloning completed -> Returning to Jikes the new optOptions(_opt)\n\n");

        return new CompilationPlan(method, _optPlans[optLevel], null, _opt[optLevel]);
        
    //} else if (method.getTreeNode(optLevel) == null) {
    } else if (method.treeNode == null) {
        System.out.println("No tree node set at this method (first entry). Need to make it point to the root of the current opt level(" + optLevel + ").");
        
        System.out.println("\tTree root -> " + tree.getGARoot());
        method.setTreeNode(optLevel, tree.getGARoot());
        method.treeNode = tree.getGARoot();
        System.out.println("\tSetting tree node -> " + method.getTreeNode(optLevel));
        System.out.println("\tSetting the first record of samples for this method - " + recentSample + " samples.");
        method.setSamplesRecorded(optLevel, recentSample);
        method.sampleRecorded = recentSample;
        
        System.out.println("\tSetting the current opt level used at this method.");
        method.previousOptLevel = optLevel;
        
        System.out.println("\tReturning the standard compilation plan for this method.\n\n");
        return new CompilationPlan(method, _optPlans[optLevel], null, _options[optLevel]);
    }
    
    
    
    
    //----------------------------------------------OLD
    
    return new CompilationPlan(method, _optPlans[optLevel], null, _options[optLevel]);
  }
  
  public GATreeNode generateNode(GATree tree, NormalMethod method, int optLevel, boolean rollback) {
    System.out.println("~~~/~~~ Inside new generateNode()");
      
    // Coping population into new one.
    System.out.println("~~~/~~~ Creating pop and coping population from current node at method.");
    GAPopulation orig = method.treeNode.getPopulation();
    System.out.println("~~~/~~~ Checking if rollback is needed.");
    if (rollback) {
        System.out.println("~~~/~~~/~~~ Rollback is needed -> Coping population from the father.");
        orig = method.treeNode.getParent().getPopulation();
    }
    GAPopulation clonedPop = new GAPopulation(orig);
    System.out.println("~~~/~~~ Copy population created.");
      
    // Selecting two individuals to crossover.
    System.out.println("~~~/~~~ Selecting two individuals from the cloned pop using tournament.");
    tournamentResult result = binaryTournament(clonedPop);
    GAIndividual firstIndividual = result.getIndividual();
    int INDEX1 = result.getFirstIndex();
    System.out.println("~~~/~~~ First individual selected from binary tournament. It has the INDEX: " + INDEX1 + " from the cloned pop.");
    result = binaryTournament(clonedPop);
    GAIndividual secondIndividual = result.getIndividual();
    int INDEX2 = result.getFirstIndex();
    System.out.println("~~~/~~~ Second individual selected from binary tournament. It has the INDEX: " + INDEX2 + " from the cloned pop.");
    
    // Crossover
    System.out.println("~~~/~~~ Performing crossover on the two individuals.");
    GAIndividual newIndividual = crossover(clonedPop, firstIndividual, secondIndividual);
    System.out.println("~~~/~~~ Replacing one of the parents with the new individual generanted from crossover.");
    clonedPop.replaceIndividual(INDEX2, newIndividual);
    
    // Mutating newIndividual (or not, depdends on mutation rate).
    System.out.println("~~~/~~~ Mutating newIndividual (or not, depdends on mutation rate).");
    System.out.println("~~~/~~~ *Using the optLevel from the new tree (IF the opt level changed) <- VERIFY");
    mutate(newIndividual, optLevel);
    
    // Adding new node to the tree.
    System.out.println("~~~/~~~ Adding new node in the tree and returning it.");
    GATreeNode newNode = tree.addChild(newIndividual, clonedPop, method.treeNode, rollback);
    System.out.println("~~~/~~~ Node added to the tree. Tree level: " + optLevel);
    System.out.println("~~~/~~~ Returning new node.");
    return newNode;
  }
  
  public void generateNode(GATree tree, NormalMethod method, int methodId, int optLevel, double recentSample, boolean rollback) {
    //GATree tree = GATree.getInstance();
    GAHash map = GAHash.getInstance();
    
    System.out.println("\t\t\tInside generateNode()");
    
    
    System.out.println("\t\t\tCreating pop and coping population from current node at method.");
    int popSize = method.treeNode.getPopulation().getPopulationSize();
    //GAPopulation pop = new GAPopulation();
    GAPopulation orig = method.treeNode.getPopulation();
    GAPopulation pop = new GAPopulation(orig);  // Creating new pop as copy of the original
    //pop.setPopulationSize(50);
    //pop.individuals = new GAIndividual[50];
    System.out.println("\t\t\tCopy population created.");
    
    // Population we use at this new node:
    //System.out.println("\tCoping population: ");
    
    //System.out.println("\tTest: " + method.getTreeNode(method.previousOptLevel).getPopulation().individuals);
    
    /*GAIndividual orig = method.treeNode.getDNA();
    for (int i = 0; i < 50; i++) {
        pop.individuals[i] = new GAIndividual();
        //pop.individuals[i].initOptOption();
        pop.individuals[i].initOptOption(optLevel);
        //cloneOptOptions(method.getTreeNode(method.previousOptLevel).getPopulation().individuals[i], pop.individuals[i]);
        pop.individuals[i].copy(orig);
    }*/
    //System.out.println("\tPopulation copied.");

    // Select two individuals from the cloned pop using tournament.
    System.out.println("\tSelecting two individuals from the cloned pop using tournament.");
    //GAIndividual firstIndividual = binaryTournament(pop);
    //GAIndividual secondIndividual = binaryTournament(pop);
    tournamentResult result = binaryTournament(pop);
    GAIndividual firstIndividual = result.getIndividual();
    int INDEX1 = result.getFirstIndex();
    System.out.println("\tFirst individual selected from binary tournament. It has the INDEX: " + INDEX1 + " from the cloned pop.");

    result = binaryTournament(pop);
    GAIndividual secondIndividual = result.getIndividual();
    int INDEX2 = result.getFirstIndex();
    System.out.println("\tSecond individual selected from binary tournament. It has the INDEX: " + INDEX2 + " from the cloned pop.");

    // TODO - Perform crossover and mutation.
    System.out.println("\tPerforming crossover on the two individuals.");
    GAIndividual newIndividual = crossover(pop, firstIndividual, secondIndividual);
    System.out.println("\tReplacing one of the parents with the new individual generanted from crossover.");
    pop.replaceIndividual(INDEX2, newIndividual);
    // Mutating newIndividual (or not, depdends on mutation rate).
    System.out.println("\tMutating newIndividual (or not, depdends on mutation rate).");
    mutate(newIndividual, optLevel);

    //tree.getGARoot().setLeftChild(tuple.getNode());
    System.out.println("\tAdding new node in the tree and Making current method point to new node.");
    //GATreeNode aux = method.getTreeNode(optLevel);

    GATreeNode auxNew = tree.addChild(newIndividual, pop, method.getTreeNode(optLevel), rollback);
    method.setTreeNode(optLevel, auxNew);
    //method.treeNode0 = tree.addChild(newIndividual, pop, aux, rollback); // TODO - Parameters not right. Need yet to generate new individual and pop.
    
    map.add(methodId, recentSample, method.getTreeNode(optLevel));
    System.out.println("\tPriting part of the tree: ");
    tree.print();
  }
  
  public void cloneOptOptions(OptOptions[] _opt, GAIndividual DNA, int optLevel) {
      
    _opt[optLevel].FIELD_ANALYSIS = DNA.FIELD_ANALYSIS;
    _opt[optLevel].INLINE = DNA.INLINE;
    _opt[optLevel].INLINE_GUARDED = DNA.INLINE_GUARDED;
    _opt[optLevel].INLINE_GUARDED_INTERFACES = DNA.INLINE_GUARDED_INTERFACES;
    _opt[optLevel].INLINE_PREEX = DNA.INLINE_PREEX;
    _opt[optLevel].LOCAL_CONSTANT_PROP = DNA.LOCAL_CONSTANT_PROP;
    _opt[optLevel].LOCAL_COPY_PROP = DNA.LOCAL_COPY_PROP;
    _opt[optLevel].LOCAL_CSE = DNA.LOCAL_CSE;
    _opt[optLevel].REORDER_CODE = DNA.REORDER_CODE;
    _opt[optLevel].H2L_INLINE_NEW = DNA.H2L_INLINE_NEW;
    _opt[optLevel].REGALLOC_COALESCE_MOVES = DNA.REGALLOC_COALESCE_MOVES;
    _opt[optLevel].REGALLOC_COALESCE_SPILLS = DNA.REGALLOC_COALESCE_SPILLS;
    _opt[optLevel].CONTROL_STATIC_SPLITTING = DNA.CONTROL_STATIC_SPLITTING;
    _opt[optLevel].ESCAPE_SCALAR_REPLACE_AGGREGATES = DNA.ESCAPE_SCALAR_REPLACE_AGGREGATES;
    _opt[optLevel].ESCAPE_MONITOR_REMOVAL = DNA.ESCAPE_MONITOR_REMOVAL;
    _opt[optLevel].REORDER_CODE_PH = DNA.REORDER_CODE_PH;
    _opt[optLevel].H2L_INLINE_WRITE_BARRIER = DNA.H2L_INLINE_WRITE_BARRIER;
    _opt[optLevel].H2L_INLINE_PRIMITIVE_WRITE_BARRIER = DNA.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
    _opt[optLevel].OSR_GUARDED_INLINING = DNA.OSR_GUARDED_INLINING;
    _opt[optLevel].OSR_INLINE_POLICY = DNA.OSR_INLINE_POLICY;
    _opt[optLevel].L2M_HANDLER_LIVENESS = DNA.L2M_HANDLER_LIVENESS;
    
  }
  
  public final float MUTATION_RATE = 10;
  
  public void mutate(GAIndividual individual, int optLevel) {
    // One parameter from the DNA will be changed per mutate() call.
    Randomizer randUtil = Randomizer.getInstance();
    Random rand = randUtil.getRandom();
      
    int coin = rand.nextInt(100);
    if (coin < MUTATION_RATE) {
        System.out.println("~~~/~~~/~~~ Performing mutation");
        individual.mutateBoolean(optLevel);
    }
  }
  
  /*public void mutate(GAIndividual individual, int optLevel) {
    // One parameter from the DNA will be changed per mutate() call.
    Randomizer randUtil = Randomizer.getInstance();
    Random rand = randUtil.getRandom();
    
    int coin = rand.nextInt(100);
    if (coin < 10) {
      // Performing mutation.
      int INDEX = rand.nextInt(16);
      System.out.println("\t\tPerforming mutation.\t\tThe option with INDEX: " + INDEX + " was selected.");
      System.out.println("\t\tPerforming mutation using the new mutateBoolean method.");
      //individual.mutateBoolean(INDEX, optLevel);
      individual.mutateBoolean(optLevel);
    }
  }*/
  
  public tournamentResult binaryTournament(GAPopulation pop) {
    Randomizer randUtil = Randomizer.getInstance();
    
    int popSize = pop.getPopulationSize();
    int INDEX1 = randUtil.nextInt(popSize);
    GAIndividual firstContender = pop.getIndividual(INDEX1);
    int INDEX2 = randUtil.nextInt(popSize);
    GAIndividual secondContender = pop.getIndividual(INDEX2);
    System.out.println("~~~/~~~/~~~ First contender sample's: " + firstContender.sample + ", Second contender sample's: " + secondContender.sample);
    if (firstContender.sample < secondContender.sample) {
      return new tournamentResult(firstContender, INDEX1);
    } else {
      return new tournamentResult(secondContender, INDEX2);
    }
  }
  
  public final class tournamentResult {
    private final GAIndividual individual;
    private final int first;
    //private final int second;

    public tournamentResult(GAIndividual individual, int first) {
        this.individual = individual;
        this.first = first;
        //this.second = second;
    }

    public GAIndividual getIndividual() {
        return individual;
    }

    public int getFirstIndex() {
        return first;
    }
    
    /*public int getSecondIndex() {
        return second;
    }*/
  }
  
  public GAIndividual crossover(GAPopulation pop, GAIndividual firstIndividual, GAIndividual secondIndividual) {
    Random rand = pop.getRandom();
    Randomizer randUtil = Randomizer.getInstance();
    rand = randUtil.getRandom();
    
    GAIndividual newIndividual = new GAIndividual();
    newIndividual.initOptOption();
    //int optLevel = getMaxOptLevel();
    //OptOptions[] newSolOpt = newIndividual.getGAOptions();
    
    int coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.FIELD_ANALYSIS = firstIndividual.FIELD_ANALYSIS;
    } else {
      newIndividual.FIELD_ANALYSIS = secondIndividual.FIELD_ANALYSIS;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.INLINE = firstIndividual.INLINE;
    } else {
      newIndividual.INLINE = secondIndividual.INLINE;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.INLINE_GUARDED = firstIndividual.INLINE_GUARDED;
    } else {
      newIndividual.INLINE_GUARDED = secondIndividual.INLINE_GUARDED;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.INLINE_GUARDED_INTERFACES = firstIndividual.INLINE_GUARDED_INTERFACES;
    } else {
      newIndividual.INLINE_GUARDED_INTERFACES = secondIndividual.INLINE_GUARDED_INTERFACES;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.INLINE_PREEX = firstIndividual.INLINE_PREEX;
    } else {
      newIndividual.INLINE_PREEX = secondIndividual.INLINE_PREEX;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.LOCAL_CONSTANT_PROP = firstIndividual.LOCAL_CONSTANT_PROP;
    } else {
      newIndividual.LOCAL_CONSTANT_PROP = secondIndividual.LOCAL_CONSTANT_PROP;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.LOCAL_COPY_PROP = firstIndividual.LOCAL_COPY_PROP;
    } else {
      newIndividual.LOCAL_COPY_PROP = secondIndividual.LOCAL_COPY_PROP;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.LOCAL_CSE = firstIndividual.LOCAL_CSE;
    } else {
      newIndividual.LOCAL_CSE = secondIndividual.LOCAL_CSE;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.REORDER_CODE = firstIndividual.REORDER_CODE;
    } else {
      newIndividual.REORDER_CODE = secondIndividual.REORDER_CODE;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.H2L_INLINE_NEW = firstIndividual.H2L_INLINE_NEW;
    } else {
      newIndividual.H2L_INLINE_NEW = secondIndividual.H2L_INLINE_NEW;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.REGALLOC_COALESCE_MOVES = firstIndividual.REGALLOC_COALESCE_MOVES;
    } else {
      newIndividual.REGALLOC_COALESCE_MOVES = secondIndividual.REGALLOC_COALESCE_MOVES;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.REGALLOC_COALESCE_SPILLS = firstIndividual.REGALLOC_COALESCE_SPILLS;
    } else {
      newIndividual.REGALLOC_COALESCE_SPILLS = secondIndividual.REGALLOC_COALESCE_SPILLS;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.CONTROL_STATIC_SPLITTING = firstIndividual.CONTROL_STATIC_SPLITTING;
    } else {
      newIndividual.CONTROL_STATIC_SPLITTING = secondIndividual.CONTROL_STATIC_SPLITTING;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES = firstIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES;
    } else {
      newIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES = secondIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.ESCAPE_MONITOR_REMOVAL = firstIndividual.ESCAPE_MONITOR_REMOVAL;
    } else {
      newIndividual.ESCAPE_MONITOR_REMOVAL = secondIndividual.ESCAPE_MONITOR_REMOVAL;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.REORDER_CODE_PH = firstIndividual.REORDER_CODE_PH;
    } else {
      newIndividual.REORDER_CODE_PH = secondIndividual.REORDER_CODE_PH;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.H2L_INLINE_WRITE_BARRIER = firstIndividual.H2L_INLINE_WRITE_BARRIER;
    } else {
      newIndividual.H2L_INLINE_WRITE_BARRIER = secondIndividual.H2L_INLINE_WRITE_BARRIER;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER = firstIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
    } else {
      newIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER = secondIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.OSR_GUARDED_INLINING = firstIndividual.OSR_GUARDED_INLINING;
    } else {
      newIndividual.OSR_GUARDED_INLINING = secondIndividual.OSR_GUARDED_INLINING;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.OSR_INLINE_POLICY = firstIndividual.OSR_INLINE_POLICY;
    } else {
      newIndividual.OSR_INLINE_POLICY = secondIndividual.OSR_INLINE_POLICY;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual.L2M_HANDLER_LIVENESS = firstIndividual.L2M_HANDLER_LIVENESS;
    } else {
      newIndividual.L2M_HANDLER_LIVENESS = secondIndividual.L2M_HANDLER_LIVENESS;
    }
    
    return newIndividual;
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
    //System.out.println("Inside RecompilationStrategy.java - createCompilationPlan() - Calling the GA version.");
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




