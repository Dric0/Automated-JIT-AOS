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

import java.util.concurrent.ConcurrentHashMap;
import org.jikesrvm.adaptive.database.methodsamples.MethodCountData;
import static org.jikesrvm.adaptive.measurements.RuntimeMeasurements.installCBSMethodListener;
import org.jikesrvm.adaptive.measurements.listeners.MethodListener;
import org.jikesrvm.adaptive.measurements.listeners.YieldCounterListener;
import org.jikesrvm.adaptive.recompilation.CompilerDNA;
import org.jikesrvm.classloader.NormalMethod;
import org.jikesrvm.classloader.RVMMethod;
import org.jikesrvm.compilers.common.CompiledMethod;
import org.jikesrvm.compilers.opt.driver.CompilationPlan;
import static org.jikesrvm.runtime.Time.cycles;
import org.jikesrvm.scheduler.RVMThread;

/**
 * Event used by the basic recompilation organizer
 * to notify the controller that a method is hot.
 */
public final class HotMethodRecompilationEvent extends HotMethodEvent implements ControllerInputEvent {

  public GAIndividual gaIndividual;
  
  public void setIndividual(GAIndividual gai) {
    gaIndividual = gai;
  }
    
  //public int callTime = 0;
  
  /**
   * @param _cm the compiled method
   * @param _numSamples the number of samples attributed to the method
   */
  public HotMethodRecompilationEvent(CompiledMethod _cm, double _numSamples) {
    super(_cm, _numSamples);
  }

  /**
   * @param _cm the compiled method
   * @param _numSamples the number of samples attributed to the method
   */
  HotMethodRecompilationEvent(CompiledMethod _cm, int _numSamples) {
    this(_cm, (double) _numSamples);
  }

  @Override
  public String toString() {
    return "HotMethodRecompilationEvent: " + super.toString();
  }

  /**
   * {@inheritDoc}
   * In this case, the method simply passes the event to the recompilation strategy.
   */
  @Override
  public void process() {
    //System.out.println("DEBUG");
    //callTime++;
    ControllerPlan plan = Controller.recompilationStrategy.considerHotMethod(getCompiledMethod(), this);
    //System.out.println("Testing counter inside HotMethodRecompilationEvent. Method's Id: " + getCompiledMethod().method.getId() + "(CMID: " + getCompiledMethod().getId() + ") -> callTime: " + callTime);

    ControllerMemory.incrementNumMethodsConsidered();
    
    //GAHash map = GAHash.getInstance();
    //int methodId = getCompiledMethod().method.getId();
    //map.add(methodId, this.getNumSamples());
    
    //ConcurrentHashMap map = new ConcurrentHashMap<Integer, Double>();
    //map.putIfAbsent(getCompiledMethod().method.getId(), this.getNumSamples());
    
    //double sample = this.getNumSamples();
    
    //MethodCountData count = new MethodCountData();

    // If plan is still null we decided not to recompile.
    if (plan != null) {
      //map.add(methodId, this.getNumSamples(), gaIndividual);
      //NormalMethod method = plan.getCompPlan().getMethod();
      //CompilationPlan cp = plan.getCompPlan();
      //int compilerId = CompilerDNA.getCompilerConstant(cp.options.getOptLevel());
      //double compTime = CompilerDNA.estimateCompileTime(compilerId, method);
      //System.out.println("Estimated Compile Time of method before plan.execute() [" + method.getId() + "]: " + compTime);
      //System.out.println("HotMethodEvent.getNumSamples() before plan.execute(): " + this.getNumSamples());
      plan.execute();
      
      // Dric0 - Inserting values into hash.
      /*GAHash map = GAHash.getInstance();
      int methodId = getCompiledMethod().method.getId();
      double recentSample = this.getNumSamples();
          
      GATree tree = GATree.getInstance();
      if (!map.checkExistence(methodId)) {
        // No entries on the hash
        System.out.println("No entries on the hash.");
        GAIndividual individual = tree.getGARoot();
        map.add(methodId, recentSample, individual);     // This "ns" represents the value already executed.
      } else {
        // Already on the hash map.
        System.out.println("Already on the hash map.");
        GAWrapper tuple = map.getValues(methodId);
        //GAIndividual individual = map.getIndividual(methodId);
        //double previousSample = map.getSamples(methodId);
        GAIndividual individual = tuple.getIndividual();
        double previousSample = tuple.getSamples();
        individual.setFitness(previousSample);
        //tuple.getIndividual().setFitness(tuple.getSamples());
        map.add(methodId, recentSample, individual);
      }*/
      
      
      
      //System.out.println("Expected Speedup (Inside HotMethodRecompilationEvent.process()): " + plan.getExpectedSpeedup());
    
      //YieldCounterListener aux = new YieldCounterListener(20);
      //aux.report();
      //MethodListener mt = new MethodListener(20);
      //installCBSMethodListener(mt);
      //mt.update(this.getCMID(), compilerId, RVMThread.EPILOGUE);
      //System.out.println("MethodListener.getNumSamples(): " + mt.getNumSamples());
      
      System.out.println("Canonical method.getId(): " + getCompiledMethod().method.getId() + " -- Compiled method getId(): " + getCompiledMethod().getId());
      
      //System.out.println("HotMethodEvent.getNumSamples(): " + this.getNumSamples());
      //System.out.println("Estimated Compile Time of method [" + method.getId() + "]: " + compTime);
      
      //map.add(methodId, this.getNumSamples());
      //map.print();
      
      //System.out.println("Cycles: " + cycles());
      
      //MethodCountData count = new MethodCountData();
      //System.out.println("Current count for method [" + method.getId() + "]: " + count.getData(method.getId()));
      //System.out.println("Total number of samples for method [" + method.getId() + "]: " + count.getTotalNumberOfSamples());
      //count.getTotalNumberOfSamples();
    }
  }
}
