/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

import java.util.ArrayList;
import java.util.List;
import org.jikesrvm.compilers.opt.util.TreeNode;

/**
 *
 * @author dric0
 */
public class GATreeNode {
      
  /**
   *  The parent of this node
   */
  private GATreeNode parent = null;

  /**
   *  The first (leftmost) child
   */
  private GATreeNode leftChild = null;

  /**
   *  The next node on the child list that I am on
   */
  private GATreeNode rightSibling = null;
  
  private List<GATreeNode> children = new ArrayList<GATreeNode>();
  
  private GAPopulation population = new GAPopulation();
  
  private final GAIndividual DNA; 

  /**
   * Constructor
     * @param DNA
     * @param population
   */
  public GATreeNode(GAIndividual DNA, GAPopulation population) {
    this.DNA = DNA;
    this.population = population;
  }
  
  public GAIndividual getDNA() {
    return this.DNA;
  }
  
  public GAPopulation getPopulation() {
    return this.population;
  }
  
  public void setLeftChild(GATreeNode node) {
    this.leftChild = node;
  }
  
  public GATreeNode getParent() {
      return this.parent;
  }
  
  public void setParent(GATreeNode node) {
    this.parent = node;
  }
  
  public GATreeNode getLeftChild() {
    return this.leftChild;
  }
  
  public GATreeNode getRightSibling() {
    return this.rightSibling;
  }
  
  public void setRightSibling(GATreeNode node) {
    this.rightSibling = node;
  }
  
  public void setPopulation(GAPopulation population) {
    this.population = population;
  }
}
