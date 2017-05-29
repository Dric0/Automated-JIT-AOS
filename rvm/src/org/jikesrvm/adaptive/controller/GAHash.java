/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author dric0
 */
public class GAHash {
    private static final GAHash INSTANCE = new GAHash();
    
    private GAHash() {}
    
    public static GAHash getInstance() {
      return INSTANCE;
    }
    
    ConcurrentHashMap map;
    //ConcurrentHashMap map2;
    
    public void init() {
      //map = new ConcurrentHashMap<Integer, Double>();
      map = new ConcurrentHashMap<Integer, GAWrapper>();
    }
    
    /*public double checkExistence(int methodId) {
      GAWrapper tuple = (GAWrapper) map2.get(methodId);
      if (tuple != null) {
        return tuple.getIndividual().getFitness();
        //return tuple.getSamples();
        //return false;
      }
      return -1;
    }*/
    
    public boolean checkExistence(int methodId) {
      if (map.get(methodId) == null) {
        return false;
      }
      return true;
    }
    
    public GATreeNode getNode(int methodId) {
      System.out.println("Inside GAHash.getIndividual()");
      GAWrapper tuple = (GAWrapper) map.get(methodId);
      return tuple.getNode();
    }
    
    public GAWrapper getValues(int methodId) {
      return (GAWrapper) map.get(methodId);
    }
    
    public void add(int methodId, double numSamples, GATreeNode node) {
      if (map.get(methodId) == null) {
        System.out.println("Adding NEW key to hash map.");
        map.putIfAbsent(methodId, new GAWrapper(numSamples, node));
      } else {
        GAWrapper tuple = (GAWrapper) map.get(methodId);
        System.out.println("Key " + methodId + " already in the hash map. Replacing " + tuple.getSamples() + " with the new numSamples (" + numSamples + ").");
        map.replace(methodId, new GAWrapper(numSamples, node));
      }
      //map.putIfAbsent(methodId, numSamples);
    }
    
    /*public void add(int methodId, double numSamples) {
      if (map.get(methodId) == null) {
        System.out.println("Adding NEW key to hash map.");
        map.putIfAbsent(methodId, numSamples);
      } else {
        System.out.println("Key " + methodId + " already in the hash map. Replacing " + map.get(methodId) + " with the new numSamples (" + numSamples + ").");
        map.replace(methodId, numSamples);
      }
      //map.putIfAbsent(methodId, numSamples);
    }*/
    
    public void print() {
      System.out.println("Printing HashMap: ");
      for (Object key : map.keySet()) {
        System.out.println("\tKey: " + key + " - Value: " + map.get(key));
      }
    }
    
}
