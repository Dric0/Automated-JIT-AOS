/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

import java.util.Enumeration;

/**
 *
 * @author dric0
 */
public class GATree {
  private final boolean DEBUG = false;
    
  private static final GATree INSTANCE0 = new GATree(0);
  private static final GATree INSTANCE1 = new GATree(1);
  private static final GATree INSTANCE2 = new GATree(2);
    
  private GATree(int optLevel) {
    this.optLevel = optLevel;
  }
    
  public static GATree getInstance0() {
    return INSTANCE0;
  }
  
  public static GATree getInstance1() {
    return INSTANCE1;
  }
  
  public static GATree getInstance2() {
    return INSTANCE2;
  }
  
  private final int optLevel;
  
  public final int getOptLevel() {
    return this.optLevel;
  }
  
  private GATreeNode root;
   
  public final void setGARoot(GAIndividual DNA, GAPopulation pop) {
    //node.clear();  // make sure all pointers are pointing anywhere else
    root = new GATreeNode(DNA, pop);
  }
  
  public final GATreeNode getGARoot() {
    return root;
  }
  
  synchronized public GATreeNode addChild(GAIndividual DNA, GAPopulation pop, GATreeNode currentNode, boolean rollback) {
    if (DEBUG) System.out.println("\t\t\t\t\tInside addChild()");
    GATreeNode son = new GATreeNode(DNA, pop);
    GATreeNode current = currentNode;
        
    if (rollback) {
        // Need to rollback to the parent.
        if (DEBUG) System.out.println("\t\t\t\t\tNeed to rollback to the parent.");
        current = currentNode.getParent();
    }
        
    if (DEBUG) System.out.println("\t\t\t\t\tChecking if current node has a child.");
    if (current.getLeftChild() == null) {
        // There is no childs. Setting the new child as the left son.
        if (DEBUG) System.out.println("\t\t\t\t\tThere is no childs. Setting the new child as the left son.");
        current.setLeftChild(son);
        son.setParent(currentNode);

    } else {
        // There is/are childs on the current node. Place new one as a brother.
        if (DEBUG) System.out.println("\t\t\t\t\tThere is/are childs on the current node. Place new one as a brother.");
        GATreeNode left = current.getLeftChild();
        while(true) {
            //GATreeNode left = currentNode.getLeftChild();
            if (left.getRightSibling() == null) {
                // No right brother. Setting new node as right sibling.
                if (DEBUG) System.out.println("\t\t\t\t\t\tNo right brother. Setting new node as right sibling.");
                left.setRightSibling(son);
                son.setParent(current);
                break;
            } else {
                // There is a right brother. Pointing to it now.
                if (DEBUG) System.out.println("\t\t\t\t\t\tThere is a right brother. Pointing to it now and search for the next brother.");
                left = left.getRightSibling();
                //continue;
            }
        }
    }
    
    return son;
      
    /*if (root.getLeftChild() == null) {
      // There is no childs. Setting the new child as the left son.
      GATreeNode son = new GATreeNode(DNA, pop);
      //son.setPopulation(pop);
      root.setLeftChild(son);
      son.setParent(root);
      return son;
    } else {
      // There is/are childs.
      return null;
    }*/
  }
  
  public void print() {
    System.out.println("Printing Tree:\nRoot:\n\tDNA's fitness: " + root.getDNA().getFitness());
    if (root.getLeftChild() != null) {
      System.out.println("\t\tRoot's childs:\n\t\t\tDNA's fitness: " + root.getLeftChild().getDNA().getFitness());
      System.out.println("\t\t\tDoes this child has a brother?");
      if (root.getLeftChild().getRightSibling() == null) {
        System.out.println("\t\t\t\tNo brother.");
      }
    }
  }
  
  public void print2() {
      System.out.print("node: " + root);
      GATreeNode left = root;
      GATreeNode right = left.getRightSibling();
      while (true) {
          if (right != null) {
              System.out.print(" ----> " + right);
              right = right.getRightSibling();
          } else {
              if (left.getLeftChild() != null) {
                  left = left.getLeftChild();
                  System.out.print("\n");
                  System.out.print(" ----> " + left);
                  if (left.getRightSibling() != null) {
                      right = left.getRightSibling();
                  }
                  
              }
          }
          
          if (left.getLeftChild() == null && right == null) {
              break;
          }
      }
  }
  
  private StringBuilder DFS(StringBuilder sb, GATreeNode node, int depth) {
    // indent appropriate spaces and print node
    for (int i = 0; i < 2 * depth; i++) {
      sb.append(' ');
    }
    sb.append(node).append('\n');

    Enumeration<GATreeNode> childEnum = (Enumeration<GATreeNode>) node.getLeftChild();
    while (childEnum.hasMoreElements()) {
      GATreeNode child = childEnum.nextElement();
      DFS(sb, child, depth + 1);
    }
    return sb;
  }
    
}
