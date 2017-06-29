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

    public GAWrapper(double numSamples, GATreeNode node, int optLevel) {
      this.numSamples = numSamples;
      this.node = node;
      this.optLevel = optLevel;
    }
       
    private final double numSamples;
    
    private final GATreeNode node;
    
    private final int optLevel;
    
    public double getSamples() {
      return this.numSamples;
    }
    
    public GATreeNode getNode() {
      return this.node;
    }
    
    public int getOptLevel() {
        return this.optLevel;
    }
}
