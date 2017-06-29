/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.compilers.opt.util;

import java.util.Random;

/**
 *
 * @author dric0
 */
public class Randomizer {
    final private static Randomizer INSTANCE = new Randomizer();
    private Random random;

    //it is important to keep this, so no one can make multiple instances of this class
    private Randomizer() {
    }

    final static public Randomizer getInstance() {
      return INSTANCE;
    }
    final public int nextInt(int i) {
      return random.nextInt(i);
    }
    final public long nextLong() {
      return random.nextLong();
    }
    final public double nextDouble() {
      return random.nextDouble();
    }
    final public void setRandom(Random random) {
      this.random = random;
    }
    final public Random getRandom() {
      return this.random;
    }
}
