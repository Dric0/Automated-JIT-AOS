/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

import org.jikesrvm.compilers.opt.util.Tree;

/**
 *
 * @author dric0
 */
public class GATree {
  private static final GATree INSTANCE = new GATree();
    
  private GATree() {}
    
  public static GATree getInstance() {
    return INSTANCE;
  }
    
  private GATreeNode root;
  
  public final void setGARoot(GAIndividual DNA, GAPopulation pop) {
    //node.clear();  // make sure all pointers are pointing anywhere else
    root = new GATreeNode(DNA, pop);
  }
  
  public final GATreeNode getGARoot() {
    return root;
  }
  
  public void addChild(GAIndividual DNA, GAPopulation pop) {
    if (root.getLeftChild() == null) {
      GATreeNode son = new GATreeNode(DNA, pop);
      //son.setPopulation(pop);
      root.setLeftChild(son);
      son.setParent(root);
    }
  }
    
}
