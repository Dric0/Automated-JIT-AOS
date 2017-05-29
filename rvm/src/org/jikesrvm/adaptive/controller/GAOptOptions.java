/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jikesrvm.adaptive.controller;

import org.jikesrvm.compilers.opt.OptOptions;

/**
 *
 * @author dric0
 */
public class GAOptOptions {
    
    int getMaxOptLevel() {
      return Controller.options.DERIVED_MAX_OPT_LEVEL;
    }
    
    /*public boolean mutateBoolean(OptOptions[] options, int INDEX, int maxOptLevel) {
      
      //int maxOptLevel = getMaxOptLevel();
        
      if (INDEX == 0) {
          //System.out.println("Inside mutateBoolean - GAOptOptions.java\nFREQ_FOCUS_EFFORT is the one at INDEX.");
          options[maxOptLevel].FREQ_FOCUS_EFFORT = !options[maxOptLevel].FREQ_FOCUS_EFFORT;
          return true;
      } 
      if (INDEX == 1)
          return !options[maxOptLevel].READS_KILL;
      if (INDEX == 2)
          return !options[maxOptLevel].FIELD_ANALYSIS;
      if (INDEX == 3)
          return !options[maxOptLevel].INLINE;
      if (INDEX == 4)
          return !options[maxOptLevel].INLINE_GUARDED;
      if (INDEX == 5)
          return !options[maxOptLevel].INLINE_GUARDED_INTERFACES;
      if (INDEX == 6)
          return !options[maxOptLevel].INLINE_PREEX;
      if (INDEX == 7)
          return !options[maxOptLevel].SIMPLIFY_INTEGER_OPS;
      if (INDEX == 8)
          return !options[maxOptLevel].SIMPLIFY_LONG_OPS;
      if (INDEX == 9)
          return !options[maxOptLevel].SIMPLIFY_FLOAT_OPS;
      if (INDEX == 10)
          return !options[maxOptLevel].SIMPLIFY_DOUBLE_OPS;
      if (INDEX == 11)
          return !options[maxOptLevel].SIMPLIFY_REF_OPS;
      if (INDEX == 12)
          return !options[maxOptLevel].SIMPLIFY_TIB_OPS;
      if (INDEX == 13)
          return !options[maxOptLevel].SIMPLIFY_FIELD_OPS;
      if (INDEX == 14)
          return !options[maxOptLevel].SIMPLIFY_CHASE_FINAL_FIELDS;
      if (INDEX == 15)
          return !options[maxOptLevel].LOCAL_CONSTANT_PROP;
        
      // None of the above tests matched, so this wasn't an option
      return false;
    }*/
    
}
