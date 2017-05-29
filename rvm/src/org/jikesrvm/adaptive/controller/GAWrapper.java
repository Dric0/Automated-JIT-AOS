/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

/**
 *
 * @author dric0
 */
public class GAWrapper {

    public GAWrapper(double numSamples, GATreeNode node) {
      this.numSamples = numSamples;
      this.node = node;
      //individual.setFitness(numSamples);
    }
       
    private final double numSamples;
    
    private final GATreeNode node;
    
    public double getSamples() {
      return numSamples;
    }
    
    public GATreeNode getNode() {
      return this.node;
    }
}
