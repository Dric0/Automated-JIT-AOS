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

import java.util.Arrays;
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
    if (DEBUG) System.out.println("Method's id: " + method.getId());
    //if (DEBUG) System.out.println("Compiled Method's id: " + method.getCurrentCompiledMethod().getId());
    //if (DEBUG) System.out.println("Previous Compiled Method's id: " + method.getCurrentCompiledMethod().getId());
    
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
  
  double futureMethodTime(HotMethodEvent hme) {
    double numSamples = hme.getNumSamples();
    double timePerSample = VM.interruptQuantum;
    if (!VM.UseEpilogueYieldPoints) {
      // NOTE: we take two samples per timer interrupt, so we have to
      // adjust here (otherwise we'd give the method twice as much time
      // as it actually deserves).
      timePerSample /= 2.0;
    }
    if (Controller.options.mlCBS()) {
      // multiple method samples per timer interrupt. Divide accordingly.
      timePerSample /= VM.CBSMethodSamplesPerTick;
    }
    double timeInMethodSoFar = numSamples * timePerSample;
    return timeInMethodSoFar;
  }

  private final boolean DEBUG = false;
  
  public CompilationPlan GAcreateCompilationPlan(NormalMethod method, int methodId, int optLevel,
                                                   InstrumentationPlan instPlan, HotMethodEvent hme) {
    long startTime = System.nanoTime();  
      
    if (DEBUG) System.out.println("GAcreateCompilationPlan() - The optimization level to use in the plan: " + optLevel);
    
    if (DEBUG) System.out.println("Initializing tree from opt level " + optLevel);
    
    if (DEBUG) System.out.println("Compilation time for this method: " + hme.getCompiledMethod().getCompilationTime());
    if (DEBUG) System.out.println("Number of instructions for this method: " + hme.getCompiledMethod().numberOfInstructions());
    double compilationRate = hme.getCompiledMethod().numberOfInstructions()/hme.getCompiledMethod().getCompilationTime();
    if (DEBUG) System.out.println("Compilation Rate: " + compilationRate);
    
    //methodId = method.getCurrentCompiledMethod().getId();
    
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
        if (DEBUG) System.out.println("The tree being used is the opt0.");
        //if (tree.getGARoot() != null) System.out.println("\tThe opt0 tree has its root.");
    } else if (optLevel == 1) {
        tree = GATree.getInstance1();
        if (DEBUG) System.out.println("The tree being used is the opt1.");
        //if (tree.getGARoot() != null) System.out.println("\tThe opt1 tree has its root.");
    } else if (optLevel == 2) {
        tree = GATree.getInstance2();
        if (DEBUG) System.out.println("The tree being used is the opt2.");
        //if (tree.getGARoot() != null) System.out.println("\tThe opt2 tree has its root.");
    }
    
    // Checking the previous optLevel.
    if (DEBUG) System.out.println("---> Checking if it's the first entry of this method.");
    //if (method.previousOptLevel != -1 && method.previousOptLevel != optLevel) { // Se previousOptLevel for -1 -> nao foi inicializado ainda.
                                                                                // Se previousOptLevel for diferente de optLevel -> mudou o level.
    if (recentSample > 1) {
        if (DEBUG) System.out.println("* We only consider methods for searching with sampling > 1 (" + recentSample + ") *");

        if (DEBUG) System.out.println("Checking if the methodId already is on the map.");
        if (map.checkExistence(methodId)) {
            if (DEBUG) System.out.println("-> methodId already on the map (NOT the first entry for this method).");
        //if (method.previousOptLevel != -1) {
            //if (DEBUG) System.out.println("---> The previousOptLevel is different than -1 -> It is NOT the first entry for this method.");
            
            /*if (DEBUG) System.out.println("Setting the compilation rate to the individual(DNA) used: " + compilationRate);
            method.treeNode.getDNA().setCompilationRate(compilationRate);
            double speedUp = method.initialFitness/recentSample; 
            if (DEBUG) System.out.println("Setting the speedUp (sample based) - baseline samples(" + method.initialFitness + ") / aos samples(" + recentSample + "): " + speedUp);
            method.treeNode.getDNA().setSpeedUp(speedUp);
            */
            for (int i = 0; i < map.getNode(methodId).getPopulation().getPopulationSize(); i++) {
                if (map.getNode(methodId).getDNA() == map.getNode(methodId).getPopulation().individuals[i]) {
                //if (method.treeNode.getDNA() == method.treeNode.getPopulation().individuals[i]) {
                    if (DEBUG) System.out.println("********** The current DNA node is index " + i + " from the node's population.");

                }
            }
            System.out.println("Inside recompilation strategy -> Method's id from hme: " + hme.getMethod().getId());
            System.out.println("Method's id from 'method': " + methodId);
            method.currentTime++;
            
            /*
            double estimateTime = futureMethodTime(hme);
            if (DEBUG) System.out.println("Estimate time for this method: " + estimateTime);
            
            // Calculating fitness for every individual for this method's node.
            if (DEBUG) System.out.println("-> Calculating and setting fitness for every individual for this method's node.");
            if (DEBUG) System.out.println("-> Building the distance matrix.");
            double [][] distance = distanceMatrix(method.treeNode.getPopulation());
            if (DEBUG) System.out.println("-> Calculating the strenght of every individual.");
            double [] strength = calculateStrength(method.treeNode.getPopulation());
            if (DEBUG) System.out.println("-> Calculating the raw fitness of every individual.");
            double[] rawFitness = calculateRawFitness(method.treeNode.getPopulation(), strength);
            if (DEBUG) System.out.println("-> Setting fitness for every individual (raw fitness + kDistance).");
            settingFitnessWithDistance(distance, rawFitness , method.treeNode.getPopulation());
            */
            
            double speedUp = method.sampleRecorded/recentSample; 
            if (DEBUG) System.out.println("Setting the speedUp (sample based) - baseline samples(" + method.sampleRecorded + ") / aos samples(" + recentSample + "): " + speedUp);
            if (DEBUG) System.out.println("Setting the speedUp as fitness - old fitness: " + method.treeNode.getDNA().getFitness());
            method.treeNode.getDNA().setFitness(speedUp);
            if (DEBUG) System.out.println("New fitness: " + method.treeNode.getDNA().getFitness());
            
            if (DEBUG) System.out.println("* Setting sample from the individual at the treeNode with recentSample(" + recentSample + ") *");
            method.treeNode.getDNA().sample = recentSample;

            if (DEBUG) System.out.println("---> Checking if the optLevel changed.");
            if (map.getOptLevel(methodId) != optLevel) {
            //if (method.previousOptLevel != optLevel) {

                int previousOptLevel = method.previousOptLevel;
                if (DEBUG) System.out.println("\tThe optLevel DID changed. Current: " + optLevel + ", Previous: " + previousOptLevel);

                if (DEBUG) System.out.println("\tChecking the performance. Previous sample from old opt level: " + method.sampleRecorded/*getSamplesRecorded(previousOptLevel)*/ + ", recent sample: " + recentSample);
                if (recentSample > map.getPreviousSample(methodId)) {
                //if (recentSample > method.sampleRecorded) {
                    if (DEBUG) System.out.println("\t\tPerformance is worse than before -> Need to use the parent's node (in case there is one) population");

                    if (map.getNode(methodId).getParent() == null) {
                    //if (method.treeNode.getParent() == null) {
                        if (DEBUG) System.out.println("\t\t\tThere is no parent (it is the root) -> Getting the root of the actual opt level tree.");
                        if (DEBUG) System.out.println("\t\t\tCurrent tree node for this method: " + method.getTreeNode(previousOptLevel) + "/" + method.treeNode);
                        double previousSample = map.getPreviousSample(methodId);
                        map.add(methodId, previousSample, tree.getGARoot(), optLevel);
                        method.treeNode = tree.getGARoot();
                        if (DEBUG) System.out.println("\t\t\tTree root (it will be used as new node): " + tree.getGARoot());
                        if (DEBUG) System.out.println("\t\t\tNew tree node for this method: " + method.getTreeNode(optLevel) + "/" + method.treeNode);

                        /*if (tree.getGARoot().getLeftChild() != null) {
                            if (DEBUG) System.out.println("\t\t\tThe root node from new tree has childs -> Set to one of them.");
                            method.treeNode = tree.getGARoot().getLeftChild();
                            if (DEBUG) System.out.println("\t\t\tNew tree node for this method: " + method.treeNode);
                        } else {
                            if (DEBUG) System.out.println("\t\t\tThe root node from new tree has NO childs -> Create new one from new root.");
                            GATreeNode newNode = generateNode(tree, method, optLevel, false);
                            if (DEBUG) System.out.println("\t\t\tMaking method point to created node.");
                            if (DEBUG) System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                            method.treeNode = newNode;
                            if (DEBUG) System.out.println("\t\t\tNew tree node for this method (recently created): " + method.treeNode);
                        }*/

                        if (DEBUG) System.out.println("Retrieving Samples from variable in RVMMethod - Discarding the recentSample as we just rollback. Samples: " + method.sampleRecorded);

                    } else {
                        if (DEBUG) System.out.println("\t\t\tThere IS a parent for the current node -> Need to rollback.");
                        if (DEBUG) System.out.println("\t\t\tThe opt level changed so we need to rollback to the parent and use him to create new node in the new tree.");

                        /*if (method.treeNode.getParent().getDNA() == method.treeNode.getPopulation().individuals[0]) {
                            if (DEBUG) System.out.println("\t\t\tThe parent is the root from the old level tree -> Setting to the new tree's root");
                            method.treeNode = tree.getGARoot();
                            if (tree.getGARoot().getLeftChild() != null) {
                                if (DEBUG) System.out.println("\t\t\tThere is/are childs from the new tree's node -> Setting this method to left-most child.");
                                method.treeNode = method.treeNode.getLeftChild();
                            } else {
                                if (DEBUG) System.out.println("\t\t\tThere are NO childs from the new tree's node. Creating new one.");
                                GATreeNode newNode = generateNode(tree, method, optLevel, false);
                                if (DEBUG) System.out.println("\t\t\tMaking method point to created node.");
                                if (DEBUG) System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                                method.treeNode = newNode;
                                if (DEBUG) System.out.println("\t\t\tNew tree node for this method (recently created): " + method.treeNode);
                            }
                        } else {
                            if (DEBUG) System.out.println("\t\t\tThe parent is NOT the root from the old level tree -> Rollback to the parent to create new node and insert in the new tree.");
                            if (DEBUG) System.out.println("\t\t\tCreating new node from the parent.");
                            GATreeNode newNode = generateNode(tree, method, optLevel, true);
                            if (DEBUG) System.out.println("\t\t\tMaking method point to created node.");
                            if (DEBUG) System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                            method.treeNode = newNode;
                            if (DEBUG) System.out.println("\t\t\tNew tree node for this method (recently created): " + method.treeNode);
                        }*/

                        if (DEBUG) System.out.println("\t\t\tCreating new node from the parent.");

                        // TODO - Create new node. check
                        GATreeNode newNode = generateNode(tree, method, optLevel, true);
                        if (DEBUG) System.out.println("\t\tMaking method point to created node.");
                        if (DEBUG) System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                        double previousSample = map.getPreviousSample(methodId);
                        map.add(methodId, previousSample, newNode, optLevel);
                        method.treeNode = newNode;
                        if (DEBUG) System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);
                        
                        if (DEBUG) System.out.println("Retrieving Samples from variable in RVMMethod - Discarding the recentSample as we just rollback. Samples: " + method.sampleRecorded);

                    }
                } else {
                    if (DEBUG) System.out.println("\t\tPerformance is better than before -> Search for childs or create new one.");
                    if (DEBUG) System.out.println("\t\tAs the opt level changed, we will use this node to create a new one and put in the NEW tree.");

                    if (DEBUG) System.out.println("\t\t\t-> Current DNA's speedUp: " + method.treeNode.getDNA().getSpeedUp() + ", CompRate: " + method.treeNode.getDNA().getCompilationRate());
                    
                    if (DEBUG) System.out.println("\t\tGenerating new node (Using the new optLevel already)");
                    // TODO - Create new node. check
                    GATreeNode newNode = generateNode(tree, method, optLevel, false);
                    if (DEBUG) System.out.println("\t\tMaking method point to created node.");
                    if (DEBUG) System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                    map.add(methodId, recentSample, newNode, optLevel);
                    method.treeNode = newNode;
                    if (DEBUG) System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);

                    if (DEBUG) System.out.println("\t\tSetting the recentSample into the method variable. Sample recorded: " + method.sampleRecorded + ", Recent sample: " + recentSample);
                    method.sampleRecorded = recentSample;
                    if (DEBUG) System.out.println("\t\tRetrieving Samples from variable in RVMMethod. Samples: " + method.sampleRecorded);
                }
            } else {
                if (DEBUG) System.out.println("\tThe optLevel did <NOT> change. Current: " + optLevel);

                if (DEBUG) System.out.println("\tChecking the performance. Previous sample from old opt level: " + method.sampleRecorded + ", recent sample: " + recentSample);
                if (recentSample > map.getPreviousSample(methodId)) {
                //if (recentSample > method.sampleRecorded) {
                    if (DEBUG) System.out.println("\t\tPerformance is worse than before -> Need to rollback if it's not the root, if is the root need to search child OR create new node.");

                    if (map.getNode(methodId).getParent() == null) {
                    //if (method.treeNode.getParent() == null) {
                        if (DEBUG) System.out.println("\t\t\tThere is no parent (it is the root) -> Need to breadth search its childs OR create new child node.");
                        if (map.getNode(methodId).getLeftChild() == null) {
                        //if (method.treeNode.getLeftChild() == null) {
                            if (DEBUG) System.out.println("\t\t\t\tThere is no child node to search -> Creating new one.");

                            // TODO - Create new node. check
                            GATreeNode newNode = generateNode(tree, method, optLevel, false);
                            if (DEBUG) System.out.println("\t\tMaking method point to created node.");
                            if (DEBUG) System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                            double previousSample = map.getPreviousSample(methodId);
                            map.add(methodId, previousSample, newNode, optLevel);
                            method.treeNode = newNode;
                            if (DEBUG) System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);

                        } else {
                            if (DEBUG) System.out.println("\t\t\t\tThere IS child node to search -> Setting method to the child.");
                            if (DEBUG) System.out.println("\t\t\tOld tree node for this method: " + method.treeNode + ". Tree node: " + tree.getGARoot());
                            double previousSample = map.getPreviousSample(methodId);
                            map.add(methodId, previousSample, tree.getGARoot().getLeftChild(), optLevel);
                            method.treeNode = method.treeNode.getLeftChild();
                            if (DEBUG) System.out.println("\t\t\tNew tree node for this method: " + method.treeNode + ". Tree node: " + tree.getGARoot());
                        }
                        
                        if (DEBUG) System.out.println("Retrieving Samples from variable in RVMMethod - Discarding the recentSample as we just rollback. Samples: " + method.sampleRecorded);
                    
                    } else {
                        if (DEBUG) System.out.println("\t\t\tThere IS a parent (current node is NOT the root) -> Need to rollback to the parent and search another path OR create new node.");

                        if (DEBUG) System.out.println("\t\t\t-> Current DNA's speedUp: " + method.treeNode.getDNA().getSpeedUp() + ", CompRate: " + method.treeNode.getDNA().getCompilationRate());
                        
                        if (DEBUG) System.out.println("\t\t\tChecking if the parent has more childs.");
                        if (map.getNode(methodId).getRightSibling() == null) {
                        //if (method.treeNode.getRightSibling() == null) {
                            if (DEBUG) System.out.println("\t\t\t\tThere are NO brother for this node -> Need to create new node.");
                            //method.treeNode = method.treeNode.getParent();

                            // TODO - Create new node. check
                            GATreeNode newNode = generateNode(tree, method, optLevel, true);
                            if (DEBUG) System.out.println("\t\tMaking method point to created node.");
                            if (DEBUG) System.out.println("\t\tOld tree node for this method: " + method.treeNode);
                            double previousSample = map.getPreviousSample(methodId);
                            map.add(methodId, previousSample, newNode, optLevel);
                            method.treeNode = newNode;
                            if (DEBUG) System.out.println("\t\tNew tree node for this method (recently created): " + method.treeNode);
                            
                            if (DEBUG) System.out.println("Retrieving Samples from variable in RVMMethod - Discarding the recentSample as we just rollback. Samples: " + method.sampleRecorded);

                        } else {
                            if (DEBUG) System.out.println("\t\t\t\tThere is a brother for this node -> Pointing method to it.");
                            if (DEBUG) System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                            double previousSample = map.getPreviousSample(methodId);
                            GATreeNode rightSibling = map.getNode(methodId).getRightSibling();
                            map.add(methodId, previousSample, rightSibling, optLevel);
                            method.treeNode = method.treeNode.getRightSibling();
                            if (DEBUG) System.out.println("\t\t\tNew tree node for this method (the right brother): " + method.treeNode);
                            
                            if (DEBUG) System.out.println("Retrieving Samples from variable in RVMMethod - Discarding the recentSample as we just rollback. Samples: " + method.sampleRecorded);
                        }
                    }

                } else {
                    if (DEBUG) System.out.println("\t\tPerformance is better than before -> Need to go to child node (if there is one) OR create new node.");

                    if (map.getNode(methodId).getLeftChild() == null) {
                    //if (method.treeNode.getLeftChild() == null) {
                        if (DEBUG) System.out.println("\t\t\tThere is no child node -> Need to create new one.");

                        // TODO - Create new node. check
                        GATreeNode newNode = generateNode(tree, method, optLevel, false);
                        if (DEBUG) System.out.println("\t\t\tMaking method point to created node.");
                        if (DEBUG) System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                        map.add(methodId, recentSample, newNode, optLevel);
                        method.treeNode = newNode;
                        if (DEBUG) System.out.println("\t\t\tNew tree node for this method (recently created): " + method.treeNode);
                        
                        if (DEBUG) System.out.println("\t\t\tSetting the recentSample into the method variable. Sample recorded: " + method.sampleRecorded + ", Recent sample: " + recentSample);
                        method.sampleRecorded = recentSample;
                        if (DEBUG) System.out.println("\t\t\tRetrieving Samples from variable in RVMMethod. Samples: " + method.sampleRecorded);

                    } else {
                        if (DEBUG) System.out.println("\t\t\tThere IS a child node -> Need to set is as the new one.");

                        if (DEBUG) System.out.println("\t\t\tOld tree node for this method: " + method.treeNode);
                        GATreeNode leftChild = map.getNode(methodId).getLeftChild();
                        map.add(methodId, recentSample, leftChild, optLevel);
                        method.treeNode = method.treeNode.getLeftChild();
                        if (DEBUG) System.out.println("\t\t\tNew tree node for this method (left-most child): " + method.treeNode);
                        
                        if (DEBUG) System.out.println("\t\t\tSetting the recentSample into the method variable. Sample recorded: " + method.sampleRecorded + ", Recent sample: " + recentSample);
                        method.sampleRecorded = recentSample;
                        if (DEBUG) System.out.println("\t\t\tRetrieving Samples from variable in RVMMethod. Samples: " + method.sampleRecorded);
                    }

                }

            }

            // FIX-ME: If rollback happened -> The recentSample should be discarded because we will use the father's
            /*if (DEBUG) System.out.println("Setting the recentSample into the method variable. Sample recorded: " + method.sampleRecorded + ", Recent sample: " + recentSample);
            method.sampleRecorded = recentSample;
            if (DEBUG) System.out.println("Retrieving Samples from variable in RVMMethod. Samples: " + method.sampleRecorded);
            */
            
            if (DEBUG) System.out.println("\tSetting the current opt level used at this method.");
            method.previousOptLevel = optLevel;
            if (map.getOptLevel(methodId) == 2) {
                optLevel = 1;
            }
            
            GAIndividual DNA = map.getNode(methodId).getDNA();
            //GAIndividual DNA = method.treeNode.getDNA();
            
            OptOptions[] _opt = compilationPlanResult();
            if (DEBUG) System.out.println("Starting cloning the optOptions to return the options produced by GA.");
            cloneOptOptions(_opt, DNA, map.getOptLevel(methodId));
            if (DEBUG) System.out.println("Cloning completed -> Returning to Jikes the new optOptions(_opt)\n\n");
            
            long stopTime = System.nanoTime();
            map.elapsedTime += stopTime - startTime;
            //long elapsedTime = stopTime - startTime;
            //System.out.println(map.elapsedTime);

            return new CompilationPlan(method, _optPlans[optLevel], null, _opt[optLevel]);

        //} else if (method.getTreeNode(optLevel) == null) {
        } else if (method.treeNode == null) {
            if (DEBUG) System.out.println("No tree node set at this method (first entry). Need to make it point to the root of the current opt level(" + optLevel + ").");

            if (DEBUG) System.out.println("\tSaving the 1st sample collected (from base compiler).");
            method.initialFitness = recentSample;
            
            if (DEBUG) System.out.println("\tNEW: Adding methodId to the map.");
            if (DEBUG) System.out.println("\tTree root -> " + tree.getGARoot());
            method.treeNode = tree.getGARoot();
            map.add(methodId, recentSample, tree.getGARoot(), optLevel);
            if (DEBUG) System.out.println("\tSetting tree node -> " + method.treeNode);
            if (DEBUG) System.out.println("\tSetting the first record of samples for this method - " + recentSample + " samples.");
            method.sampleRecorded = recentSample;

            if (DEBUG) System.out.println("\tSetting the current opt level used at this method.");
            method.previousOptLevel = optLevel;
            
            method.currentTime++;
            
            /*if (DEBUG) System.out.println("Setting the compilation rate to the individual(DNA) used: " + compilationRate);
            method.treeNode.getDNA().setCompilationRate(compilationRate);
            double speedUp = 1; 
            if (DEBUG) System.out.println("Setting the speedUp as 1 (the baseline value).");
            method.treeNode.getDNA().setSpeedUp(speedUp);*/
            
            // Calculating fitness for every individual for this method's node.
            /*if (DEBUG) System.out.println("-> Calculating and setting fitness for every individual for this method's node.");
            if (DEBUG) System.out.println("-> Building the distance matrix.");
            double [][] distance = distanceMatrix(method.treeNode.getPopulation());
            if (DEBUG) System.out.println("-> Calculating the strenght of every individual.");
            double [] strength = calculateStrength(method.treeNode.getPopulation());
            if (DEBUG) System.out.println("-> Calculating the raw fitness of every individual.");
            double[] rawFitness = calculateRawFitness(method.treeNode.getPopulation(), strength);
            if (DEBUG) System.out.println("-> Setting fitness for every individual (raw fitness + kDistance).");
            settingFitnessWithDistance(distance, rawFitness , method.treeNode.getPopulation());
            */
            
            
            GAIndividual DNA = map.getNode(methodId).getDNA();
            //GAIndividual DNA = method.treeNode.getDNA();
            
            OptOptions[] _opt = compilationPlanResult();
            if (DEBUG) System.out.println("Starting cloning the optOptions to return the options produced by GA.");
            cloneOptOptions(_opt, DNA, optLevel);
            if (DEBUG) System.out.println("Cloning completed -> Returning to Jikes the new optOptions(_opt) - The DNA used is in the root.\n\n");

            long stopTime = System.nanoTime();
            map.elapsedTime += stopTime - startTime;
            //long elapsedTime = stopTime - startTime;
            //VM.sysWriteln(map.elapsedTime);
            
            return new CompilationPlan(method, _optPlans[optLevel], null, _opt[optLevel]);    
            
            //if (DEBUG) System.out.println("\tReturning the standard compilation plan for this method.\n\n");
            //return new CompilationPlan(method, _optPlans[optLevel], null, _options[optLevel]);
        }
    }
    if (DEBUG) System.out.println("\tAs the recentSample(" + recentSample + ") is < 1: Returning the standard compilation plan for this method.\n\n");

    //----------------------------------------------OLD
    
    long stopTime = System.nanoTime();
    map.elapsedTime += stopTime - startTime;
    //long elapsedTime = stopTime - startTime;
    //System.out.println(map.elapsedTime);
    
    return new CompilationPlan(method, _optPlans[optLevel], null, _options[optLevel]);
  }
  
  public OptOptions[] compilationPlanResult() {
    if (DEBUG) System.out.println("Creating new optOptions[] to receive the DNA.");
    OptOptions options = new OptOptions();
    int maxOptLevel = getMaxOptLevel();
    OptOptions[] _opt = new OptOptions[maxOptLevel + 1];
    String[] optCompilerOptions = Controller.getOptCompilerOptions();
    for (int i = 0; i <= maxOptLevel; i++) {
        _opt[i] = options.dup();
        _opt[i].setOptLevel(i);               // set optimization level specific optimizations
        processCommandLineOptions(_opt[i], i, maxOptLevel, optCompilerOptions);
    }
    if (DEBUG) System.out.println("New optOptions[] created -> Returning it to clone.");
    return _opt;
  }
  
  public GATreeNode generateNode(GATree tree, NormalMethod method, int optLevel, boolean rollback) {
    if (DEBUG) System.out.println("~~~/~~~ Inside new generateNode()");
      
    GAHash map = GAHash.getInstance();
    
    // Coping population into new one.
    if (DEBUG) System.out.println("~~~/~~~ Creating pop and coping population from current node at method.");
    GAPopulation orig = map.getNode(method.getId()).getPopulation();
    //GAPopulation orig = method.treeNode.getPopulation();
    if (DEBUG) System.out.println("~~~/~~~ Checking if rollback is needed.");
    if (rollback) {
        if (DEBUG) System.out.println("~~~/~~~/~~~ Rollback is needed -> Coping population from the father.");
        orig = map.getNode(method.getId()).getParent().getPopulation();
        //orig = method.treeNode.getParent().getPopulation();
    }
    GAPopulation clonedPop = new GAPopulation(orig);
    if (DEBUG) System.out.println("~~~/~~~ Copy population created.");
      
    // Selecting two individuals to crossover.
    if (DEBUG) System.out.println("~~~/~~~ Selecting two individuals from the cloned pop using tournament.");
    if (DEBUG) System.out.println("~~~/~~~ Selecting one individual from the cloned pop using tournament, the other one is the current DNA.");
    tournamentResult result = binaryTournament(clonedPop);
    //GAIndividual firstIndividual = result.getIndividual();
    GAIndividual firstIndividual = new GAIndividual();
    firstIndividual.copy(result.getIndividual());
    int INDEX1 = result.getIndex();
    if (DEBUG) System.out.println("~~~/~~~ First individual selected from binary tournament. It has the INDEX: " + INDEX1 + " from the cloned pop.");
    //result = binaryTournament(clonedPop);
    //GAIndividual secondIndividual = result.getIndividual();
    GAIndividual secondIndividual = new GAIndividual();
    secondIndividual.copy(map.getNode(method.getId()).getDNA());
    //secondIndividual.copy(result.getIndividual());
    //int INDEX2 = result.getIndex();
    if (DEBUG) System.out.println("~~~/~~~ Second individual is the current DNA from node. Fitness: " + secondIndividual.getFitness());
    //if (DEBUG) System.out.println("~~~/~~~ Second individual selected from binary tournament. It has the INDEX: " + INDEX2 + " from the cloned pop.");
    
    // Crossover
    if (DEBUG) System.out.println("~~~/~~~ Performing crossover on the two individuals.");
    crossoverResult bothIndividuals = crossover(clonedPop, firstIndividual, secondIndividual);
    GAIndividual newIndividual1 = bothIndividuals.getFirstIndividual();
    GAIndividual newIndividual2 = bothIndividuals.getSecondIndividual();
    
    //newIndividual1.setCompilationRate(0);
    //newIndividual1.setSpeedUp(0);
    
    if (DEBUG) System.out.println("~~~/~~~ Replacing both the parents with the new individuals generanted from crossover.");
    clonedPop.replaceIndividual(INDEX1, newIndividual1);
    int INDEX2 = 0;
    for (int i = 0; i < map.getNode(method.getId()).getPopulation().getPopulationSize(); i++) {
        if (map.getNode(method.getId()).getDNA() == map.getNode(method.getId()).getPopulation().individuals[i]) {
            if (DEBUG) System.out.println("********** The current DNA node is index " + i + " from the node's population.");
            INDEX2 = i;
        }
    }
    clonedPop.replaceIndividual(INDEX2, newIndividual2);
    
    // Mutating newIndividual (or not, depdends on mutation rate).
    if (DEBUG) System.out.println("~~~/~~~ Mutating both newIndividual (or not, depdends on mutation rate).");
    if (DEBUG) System.out.println("~~~/~~~ *Using the optLevel from the new tree (IF the opt level changed) <- VERIFY");
    mutate(newIndividual1, optLevel);
    mutate(newIndividual2, optLevel);
    
    //if (DEBUG) System.out.println("~~~/~~~ Selecting one individual (based on its fitness) to be the DNA on the new node.");
    if (DEBUG) System.out.println("~~~/~~~ New DNA of new node is one of the 2 recently created from crossover.");
    GAIndividual newDNA;
    int newINDEX;
    if (newIndividual1.getFitness() > newIndividual2.getFitness()) {
        if (DEBUG) System.out.println("~~~/~~~ Individual[" + INDEX1 + "] selected (fitness: " + newIndividual1.getFitness() + ").");
        newDNA = newIndividual1;
        newINDEX = INDEX1;
    } else {
        if (DEBUG) System.out.println("~~~/~~~ Individual[" + INDEX2 + "] selected (fitness: " + newIndividual2.getFitness() + ").");
        newDNA = newIndividual2;
        newINDEX = INDEX2;
    }
    //tournamentResult newResult = rouletteWheelSelection(clonedPop);
    //GAIndividual newDNA = newResult.getIndividual();
    //int newINDEX = newResult.getIndex();
    
    // Adding new node to the tree.
    if (DEBUG) System.out.println("~~~/~~~ Adding new node in the tree and returning it.");
    //if (DEBUG) System.out.println("~~~/~~~ Getting the first from the two new individuals to set as the node's DNA.");
    //GATreeNode newNode = tree.addChild(newIndividual1, clonedPop, method.treeNode, rollback);
    if (DEBUG) System.out.println("~~~/~~~ Individual selected from Wheel: " + newINDEX);
    GATreeNode newNode = tree.addChild(newDNA, clonedPop, method.treeNode, rollback);
    if (DEBUG) System.out.println("~~~/~~~ Node added to the tree. Tree level: " + optLevel);
    if (DEBUG) System.out.println("~~~/~~~ Returning new node.");
    return newNode;
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
    _opt[optLevel].CONTROL_TURN_WHILES_INTO_UNTILS = DNA.CONTROL_TURN_WHILES_INTO_UNTILS;
    _opt[optLevel].LOCAL_EXPRESSION_FOLDING = DNA.LOCAL_EXPRESSION_FOLDING;
    _opt[optLevel].SSA = DNA.SSA;
    _opt[optLevel].SSA_EXPRESSION_FOLDING = DNA.SSA_EXPRESSION_FOLDING;
    _opt[optLevel].SSA_REDUNDANT_BRANCH_ELIMINATION = DNA.SSA_REDUNDANT_BRANCH_ELIMINATION;
    _opt[optLevel].SSA_LOAD_ELIMINATION = DNA.SSA_LOAD_ELIMINATION;
    
  }
  
  public final float MUTATION_RATE = 10;
  
  public void mutate(GAIndividual individual, int optLevel) {
    // One parameter from the DNA will be changed per mutate() call.
    Randomizer randUtil = Randomizer.getInstance();
    Random rand = randUtil.getRandom();
      
    int coin = rand.nextInt(100);
    if (coin < MUTATION_RATE) {
        if (DEBUG) System.out.println("~~~/~~~/~~~ Performing mutation");
        individual.mutateBoolean(optLevel);
    }
  }
  
  public tournamentResult rouletteWheelSelection(GAPopulation pop) {
    double [] cumulativeFitness = new double[pop.getPopulationSize()];
    cumulativeFitness[0] = pop.individuals[0].getFitness();
    if (DEBUG) System.out.println("Cumulative fitness[0]: " + cumulativeFitness[0]);
    
    for (int i = 1; i < pop.getPopulationSize(); i++) {
        double fitness = pop.individuals[i].getFitness();
        
        cumulativeFitness[i] = cumulativeFitness[i-1] + fitness;
        if (DEBUG) System.out.println("Cumulative fitness[" + i + "]: " + cumulativeFitness[i]);
    }
    
    Randomizer rand = Randomizer.getInstance();
    double randomFitness = rand.nextDouble() * cumulativeFitness[cumulativeFitness.length - 1];
    if (DEBUG) System.out.println("Random fitness generated: " + randomFitness);
    int INDEX = Arrays.binarySearch(cumulativeFitness, randomFitness);
    if (INDEX < 0) {
        INDEX = Math.abs(INDEX + 1);
    }
    if (DEBUG) System.out.println("Individual INDEX selected: " + INDEX);
    
    return new tournamentResult(pop.individuals[INDEX], INDEX);
  }
  
  public tournamentResult binaryTournament(GAPopulation pop) {
    tournamentResult result;
    result = rouletteWheelSelection(pop);
    GAIndividual firstContender = result.getIndividual();
    int INDEX1 = result.getIndex();
    //firstContender = pop.getIndividual(INDEX1);
    //int INDEX2 = randUtil.nextInt(popSize);
    result = rouletteWheelSelection(pop);
    GAIndividual secondContender = result.getIndividual();
    int INDEX2 = result.getIndex();
    //secondContender = pop.getIndividual(INDEX2);
    if (DEBUG) System.out.println("~~~/~~~/~~~ First contender fitness's: " + firstContender.getFitness() + ", Second contender fitness's: " + secondContender.getFitness());
    if (firstContender.getFitness() < secondContender.getFitness()) {
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

    public int getIndex() {
        return first;
    }
    
    /*public int getSecondIndex() {
        return second;
    }*/
  }
  
  public crossoverResult crossover(GAPopulation pop, GAIndividual firstIndividual, GAIndividual secondIndividual) {
    //Random rand = pop.getRandom();
    Randomizer rand = Randomizer.getInstance();
    //rand = randUtil.getRandom();
    
    GAIndividual newIndividual1 = new GAIndividual();
    newIndividual1.initOptOption();
    newIndividual1.setSpeedUp(firstIndividual.getSpeedUp());
    newIndividual1.setCompilationRate(firstIndividual.getCompilationRate());
    newIndividual1.setFitness(firstIndividual.getFitness());
    GAIndividual newIndividual2 = new GAIndividual();
    newIndividual2.initOptOption();
    newIndividual2.setSpeedUp(secondIndividual.getSpeedUp());
    newIndividual2.setCompilationRate(secondIndividual.getCompilationRate());
    newIndividual2.setFitness(secondIndividual.getFitness());
    
    int coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.FIELD_ANALYSIS = firstIndividual.FIELD_ANALYSIS;
      newIndividual2.FIELD_ANALYSIS = secondIndividual.FIELD_ANALYSIS;
    } else {
      newIndividual1.FIELD_ANALYSIS = secondIndividual.FIELD_ANALYSIS;
      newIndividual2.FIELD_ANALYSIS = firstIndividual.FIELD_ANALYSIS;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.INLINE = firstIndividual.INLINE;
      newIndividual2.INLINE = secondIndividual.INLINE;
    } else {
      newIndividual1.INLINE = secondIndividual.INLINE;
      newIndividual2.INLINE = firstIndividual.INLINE;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.INLINE_GUARDED = firstIndividual.INLINE_GUARDED;
      newIndividual2.INLINE_GUARDED = secondIndividual.INLINE_GUARDED;
    } else {
      newIndividual1.INLINE_GUARDED = secondIndividual.INLINE_GUARDED;
      newIndividual2.INLINE_GUARDED = firstIndividual.INLINE_GUARDED;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.INLINE_GUARDED_INTERFACES = firstIndividual.INLINE_GUARDED_INTERFACES;
      newIndividual2.INLINE_GUARDED_INTERFACES = secondIndividual.INLINE_GUARDED_INTERFACES;
    } else {
      newIndividual1.INLINE_GUARDED_INTERFACES = secondIndividual.INLINE_GUARDED_INTERFACES;
      newIndividual2.INLINE_GUARDED_INTERFACES = firstIndividual.INLINE_GUARDED_INTERFACES;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.INLINE_PREEX = firstIndividual.INLINE_PREEX;
      newIndividual2.INLINE_PREEX = secondIndividual.INLINE_PREEX;
    } else {
      newIndividual1.INLINE_PREEX = secondIndividual.INLINE_PREEX;
      newIndividual2.INLINE_PREEX = firstIndividual.INLINE_PREEX;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.LOCAL_CONSTANT_PROP = firstIndividual.LOCAL_CONSTANT_PROP;
      newIndividual2.LOCAL_CONSTANT_PROP = secondIndividual.LOCAL_CONSTANT_PROP;
    } else {
      newIndividual1.LOCAL_CONSTANT_PROP = secondIndividual.LOCAL_CONSTANT_PROP;
      newIndividual2.LOCAL_CONSTANT_PROP = firstIndividual.LOCAL_CONSTANT_PROP;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.LOCAL_COPY_PROP = firstIndividual.LOCAL_COPY_PROP;
      newIndividual2.LOCAL_COPY_PROP = secondIndividual.LOCAL_COPY_PROP;
    } else {
      newIndividual1.LOCAL_COPY_PROP = secondIndividual.LOCAL_COPY_PROP;
      newIndividual2.LOCAL_COPY_PROP = firstIndividual.LOCAL_COPY_PROP;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.LOCAL_CSE = firstIndividual.LOCAL_CSE;
      newIndividual2.LOCAL_CSE = secondIndividual.LOCAL_CSE;
    } else {
      newIndividual1.LOCAL_CSE = secondIndividual.LOCAL_CSE;
      newIndividual2.LOCAL_CSE = firstIndividual.LOCAL_CSE;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.REORDER_CODE = firstIndividual.REORDER_CODE;
      newIndividual2.REORDER_CODE = secondIndividual.REORDER_CODE;
    } else {
      newIndividual1.REORDER_CODE = secondIndividual.REORDER_CODE;
      newIndividual2.REORDER_CODE = firstIndividual.REORDER_CODE;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.H2L_INLINE_NEW = firstIndividual.H2L_INLINE_NEW;
      newIndividual2.H2L_INLINE_NEW = secondIndividual.H2L_INLINE_NEW;
    } else {
      newIndividual1.H2L_INLINE_NEW = secondIndividual.H2L_INLINE_NEW;
      newIndividual2.H2L_INLINE_NEW = firstIndividual.H2L_INLINE_NEW;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.REGALLOC_COALESCE_MOVES = firstIndividual.REGALLOC_COALESCE_MOVES;
      newIndividual2.REGALLOC_COALESCE_MOVES = secondIndividual.REGALLOC_COALESCE_MOVES;
    } else {
      newIndividual1.REGALLOC_COALESCE_MOVES = secondIndividual.REGALLOC_COALESCE_MOVES;
      newIndividual2.REGALLOC_COALESCE_MOVES = firstIndividual.REGALLOC_COALESCE_MOVES;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.REGALLOC_COALESCE_SPILLS = firstIndividual.REGALLOC_COALESCE_SPILLS;
      newIndividual2.REGALLOC_COALESCE_SPILLS = secondIndividual.REGALLOC_COALESCE_SPILLS;
    } else {
      newIndividual1.REGALLOC_COALESCE_SPILLS = secondIndividual.REGALLOC_COALESCE_SPILLS;
      newIndividual2.REGALLOC_COALESCE_SPILLS = firstIndividual.REGALLOC_COALESCE_SPILLS;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.CONTROL_STATIC_SPLITTING = firstIndividual.CONTROL_STATIC_SPLITTING;
      newIndividual2.CONTROL_STATIC_SPLITTING = secondIndividual.CONTROL_STATIC_SPLITTING;
    } else {
      newIndividual1.CONTROL_STATIC_SPLITTING = secondIndividual.CONTROL_STATIC_SPLITTING;
      newIndividual2.CONTROL_STATIC_SPLITTING = firstIndividual.CONTROL_STATIC_SPLITTING;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.ESCAPE_SCALAR_REPLACE_AGGREGATES = firstIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES;
      newIndividual2.ESCAPE_SCALAR_REPLACE_AGGREGATES = secondIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES;
    } else {
      newIndividual1.ESCAPE_SCALAR_REPLACE_AGGREGATES = secondIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES;
      newIndividual2.ESCAPE_SCALAR_REPLACE_AGGREGATES = firstIndividual.ESCAPE_SCALAR_REPLACE_AGGREGATES;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.ESCAPE_MONITOR_REMOVAL = firstIndividual.ESCAPE_MONITOR_REMOVAL;
      newIndividual2.ESCAPE_MONITOR_REMOVAL = secondIndividual.ESCAPE_MONITOR_REMOVAL;
    } else {
      newIndividual1.ESCAPE_MONITOR_REMOVAL = secondIndividual.ESCAPE_MONITOR_REMOVAL;
      newIndividual2.ESCAPE_MONITOR_REMOVAL = firstIndividual.ESCAPE_MONITOR_REMOVAL;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.REORDER_CODE_PH = firstIndividual.REORDER_CODE_PH;
      newIndividual2.REORDER_CODE_PH = secondIndividual.REORDER_CODE_PH;
    } else {
      newIndividual1.REORDER_CODE_PH = secondIndividual.REORDER_CODE_PH;
      newIndividual2.REORDER_CODE_PH = firstIndividual.REORDER_CODE_PH;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.H2L_INLINE_WRITE_BARRIER = firstIndividual.H2L_INLINE_WRITE_BARRIER;
      newIndividual2.H2L_INLINE_WRITE_BARRIER = secondIndividual.H2L_INLINE_WRITE_BARRIER;
    } else {
      newIndividual1.H2L_INLINE_WRITE_BARRIER = secondIndividual.H2L_INLINE_WRITE_BARRIER;
      newIndividual2.H2L_INLINE_WRITE_BARRIER = firstIndividual.H2L_INLINE_WRITE_BARRIER;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.H2L_INLINE_PRIMITIVE_WRITE_BARRIER = firstIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
      newIndividual2.H2L_INLINE_PRIMITIVE_WRITE_BARRIER = secondIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
    } else {
      newIndividual1.H2L_INLINE_PRIMITIVE_WRITE_BARRIER = secondIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
      newIndividual2.H2L_INLINE_PRIMITIVE_WRITE_BARRIER = firstIndividual.H2L_INLINE_PRIMITIVE_WRITE_BARRIER;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.OSR_GUARDED_INLINING = firstIndividual.OSR_GUARDED_INLINING;
      newIndividual2.OSR_GUARDED_INLINING = secondIndividual.OSR_GUARDED_INLINING;
    } else {
      newIndividual1.OSR_GUARDED_INLINING = secondIndividual.OSR_GUARDED_INLINING;
      newIndividual2.OSR_GUARDED_INLINING = firstIndividual.OSR_GUARDED_INLINING;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.OSR_INLINE_POLICY = firstIndividual.OSR_INLINE_POLICY;
      newIndividual2.OSR_INLINE_POLICY = secondIndividual.OSR_INLINE_POLICY;
    } else {
      newIndividual1.OSR_INLINE_POLICY = secondIndividual.OSR_INLINE_POLICY;
      newIndividual2.OSR_INLINE_POLICY = firstIndividual.OSR_INLINE_POLICY;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.L2M_HANDLER_LIVENESS = firstIndividual.L2M_HANDLER_LIVENESS;
      newIndividual2.L2M_HANDLER_LIVENESS = secondIndividual.L2M_HANDLER_LIVENESS;
    } else {
      newIndividual1.L2M_HANDLER_LIVENESS = secondIndividual.L2M_HANDLER_LIVENESS;
      newIndividual2.L2M_HANDLER_LIVENESS = firstIndividual.L2M_HANDLER_LIVENESS;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.CONTROL_TURN_WHILES_INTO_UNTILS = firstIndividual.CONTROL_TURN_WHILES_INTO_UNTILS;
      newIndividual2.CONTROL_TURN_WHILES_INTO_UNTILS = secondIndividual.CONTROL_TURN_WHILES_INTO_UNTILS;
    } else {
      newIndividual1.CONTROL_TURN_WHILES_INTO_UNTILS = secondIndividual.CONTROL_TURN_WHILES_INTO_UNTILS;
      newIndividual2.CONTROL_TURN_WHILES_INTO_UNTILS = firstIndividual.CONTROL_TURN_WHILES_INTO_UNTILS;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.LOCAL_EXPRESSION_FOLDING = firstIndividual.LOCAL_EXPRESSION_FOLDING;
      newIndividual2.LOCAL_EXPRESSION_FOLDING = secondIndividual.LOCAL_EXPRESSION_FOLDING;
    } else {
      newIndividual1.LOCAL_EXPRESSION_FOLDING = secondIndividual.LOCAL_EXPRESSION_FOLDING;
      newIndividual2.LOCAL_EXPRESSION_FOLDING = firstIndividual.LOCAL_EXPRESSION_FOLDING;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.SSA = firstIndividual.SSA;
      newIndividual2.SSA = secondIndividual.SSA;
    } else {
      newIndividual1.SSA = secondIndividual.SSA;
      newIndividual2.SSA = firstIndividual.SSA;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.SSA_EXPRESSION_FOLDING = firstIndividual.SSA_EXPRESSION_FOLDING;
      newIndividual2.SSA_EXPRESSION_FOLDING = secondIndividual.SSA_EXPRESSION_FOLDING;
    } else {
      newIndividual1.SSA_EXPRESSION_FOLDING = secondIndividual.SSA_EXPRESSION_FOLDING;
      newIndividual2.SSA_EXPRESSION_FOLDING = firstIndividual.SSA_EXPRESSION_FOLDING;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.SSA_REDUNDANT_BRANCH_ELIMINATION = firstIndividual.SSA_REDUNDANT_BRANCH_ELIMINATION;
      newIndividual2.SSA_REDUNDANT_BRANCH_ELIMINATION = secondIndividual.SSA_REDUNDANT_BRANCH_ELIMINATION;
    } else {
      newIndividual1.SSA_REDUNDANT_BRANCH_ELIMINATION = secondIndividual.SSA_REDUNDANT_BRANCH_ELIMINATION;
      newIndividual2.SSA_REDUNDANT_BRANCH_ELIMINATION = firstIndividual.SSA_REDUNDANT_BRANCH_ELIMINATION;
    }
    coin = rand.nextInt(100);
    if (coin < 50) {
      newIndividual1.SSA_LOAD_ELIMINATION = firstIndividual.SSA_LOAD_ELIMINATION;
      newIndividual2.SSA_LOAD_ELIMINATION = secondIndividual.SSA_LOAD_ELIMINATION;
    } else {
      newIndividual1.SSA_LOAD_ELIMINATION = secondIndividual.SSA_LOAD_ELIMINATION;
      newIndividual2.SSA_LOAD_ELIMINATION = firstIndividual.SSA_LOAD_ELIMINATION;
    }
    
    //return newIndividual1;
    return new crossoverResult(newIndividual1, newIndividual2);
  }
  
  public final class crossoverResult {
    private final GAIndividual first;
    private final GAIndividual second;

    public crossoverResult(GAIndividual firstIndividual, GAIndividual secondIndividual) {
        this.first = firstIndividual;
        this.second = secondIndividual;
    }

    public GAIndividual getFirstIndividual() {
        return first;
    }

    public GAIndividual getSecondIndividual() {
        return second;
    }
  }
  
  public double [][] distanceMatrix(GAPopulation pop) {
    GAIndividual individualI, individualJ;
      
    // The matrix of distances
    double [][] distance = new double [pop.getPopulationSize()][pop.getPopulationSize()];
    
    // Calculating the distances
    for (int i = 0; i < pop.getPopulationSize(); i++) {
        distance[i][i] = 0;
        individualI = pop.individuals[i];
        for (int j = i + 1; j < pop.getPopulationSize(); j++) {
            individualJ = pop.individuals[j];
            distance[i][j] = distanceBetweenIndividuals(individualI, individualJ);
            distance[j][i] = distance[i][j];
            if (DEBUG) System.out.println("Distance between individual[" + i + "] and individual[" + j + "]: " + distance[i][j]);
        }
    }
    
    return distance;
  }
  
  public double distanceBetweenIndividuals(GAIndividual individualI, GAIndividual individualJ) {
    double diff;
    double distance = 0;
      
    diff = individualI.getSpeedUp() - individualJ.getSpeedUp();
    distance += Math.pow(diff, 2);
    
    diff = individualI.getCompilationRate() - individualJ.getCompilationRate();
    distance += Math.pow(diff, 2);
    
    if (DEBUG) System.out.println("Distance between individualI and individualJ: " + Math.sqrt(distance));
    
    return Math.sqrt(distance);
  }
  
  public void settingFitnessWithDistance(double[][] distance, double[] rawFitness ,GAPopulation pop) {
    int k = 1;
    double kDistance;
    for (int i = 0; i < distance.length; i++) {
        Arrays.sort(distance[i]);
        kDistance = 1.0 / (distance[i][k] + 2.0); // Calcule de D(i) distance
        double fit = rawFitness[i] + kDistance;
        if (DEBUG) System.out.println("Individual[" + i + "]'s kDistance: " + kDistance + ". Raw fitness: " + rawFitness[i] + ". Final fitness: " + fit);
        pop.individuals[i].setFitness(rawFitness[i] + kDistance);
    }
    
  }
  
  public int compareDominance(GAIndividual firstIndividual, GAIndividual secondIndividual) {
    int result;
    if (firstIndividual.getCompilationRate() < secondIndividual.getCompilationRate() &&
            firstIndividual.getSpeedUp() > secondIndividual.getSpeedUp()) {
        if (DEBUG) System.out.println("First individual dominates second individual.");
          result = -1;
    } else if (firstIndividual.getCompilationRate() > secondIndividual.getCompilationRate() &&
            firstIndividual.getSpeedUp() < secondIndividual.getSpeedUp()) {
        if (DEBUG) System.out.println("First individual dominates second individual.");
        result = 1;
    } else
        result = 0;
    return result;
  }
  
  public double[] calculateStrength(GAPopulation pop) {
    double [] strength    = new double[pop.getPopulationSize()];
      
    for (int i = 0; i < pop.getPopulationSize(); i++) {
        for (int j = 0; j < pop.getPopulationSize(); j++) {
            if (compareDominance(pop.individuals[i], pop.individuals[j]) == -1 ) {
                // Individual i dominates individual j
                if (DEBUG) System.out.println("Individual[" + i + "] dominates individual[" + j + "].");
                strength[i] += 1;
            }
        }
    }
    
    return strength;
  }
  
  public double[] calculateRawFitness(GAPopulation pop, double[] strength) {
    double [] rawFitness = new double[pop.getPopulationSize()];
      
    for (int i = 0; i < pop.getPopulationSize(); i++) {
        for (int j = 0; j < pop.getPopulationSize(); j++) {
            if (compareDominance(pop.individuals[i], pop.individuals[j]) == 1 ) {
                // Individual j dominates i -> Using j's strength to measure the rawFitness of i
                if (DEBUG) System.out.println("Individual[" + j + "] dominates individual[" + i + "]. Using " + j + "'s strength to measure the rawFitness of " + i + ".");
                rawFitness[i] += strength[j];
            }
        }
    }
    
    return rawFitness;
  }
  
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




