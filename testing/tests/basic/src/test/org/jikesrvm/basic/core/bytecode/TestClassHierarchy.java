/*
 * This file is part of Jikes RVM (http://jikesrvm.sourceforge.net).
 * The Jikes RVM project is distributed under the Common Public License (CPL).
 * A copy of the license is included in the distribution, and is also
 * available at http://www.opensource.org/licenses/cpl1.0.php
 *
 * (C) Copyright Peter Donald. 2007
 */
package test.org.jikesrvm.basic.core.bytecode;

import test.org.jikesrvm.basic.core.bytecode.data.SubClassInDifferentPackage;

/**
 * @author Peter Donald
 */
public class TestClassHierarchy {

  public interface Magic { void magic(); }

  static protected class A implements Magic {
    public void magic() { System.out.println("invoke magic A"); }
  }

  static protected class B extends A {
    public void magic() { System.out.println("invoke magic B"); }
  }

  static protected class C extends B {
    public void magic() { System.out.println("invoke magic C"); }
  }

  static protected class D extends A {
    public void magic() { System.out.println("invoke magic D"); }
  }

  protected class E extends D {
    public void magic() { System.out.println("invoke magic E"); }
  }

  protected class F extends A {
    public void magic() { System.out.println("invoke magic F"); }
  }

  protected class G extends F {
    public void magic() { System.out.println("invoke magic G"); }
  }

  protected class H implements Magic {
    public void magic() { System.out.println("invoke magic H"); }
  }

  protected class I extends H {
    public void magic() { System.out.println("invoke magic I"); }
  }

  protected class J extends I {
    public void magic() { System.out.println("invoke magic J"); }
  }

/*
In TestClassHierarchy:
    A
    B extends A
    C extends B
    D extends A
    E extends D
    F extends A
    G extends F
    H
    I extends H
    J extends I

In class SubClassInDifferentPackage extends TestClassHierarchy:

    P_B extends A
    P_C1 extends B
    P_C2 extends P_B
    P_D extends A
    P_E1 extends D
    P_E2 extends P_D
    P_F extends A
    P_G1 extends F
    P_G2 extends P_F
    P_H
    P_I1 extends H
    P_I2 extends P_H
    P_J1 extends I
    P_J2 extends P_I1
    P_J3

In class SubSubClass extends SubClassInDifferentPackage:
    O_C1 extends P_B
    O_C2 extends P_B
    O_E extends P_D
    O_G extends SubClassInDifferentPackage.P_F
    O_I1 extends H
    O_I2 extends P_H
    O_J1 extends P_I1
    O_J2 extends P_I2

*/

  static class SubSubClass extends test.org.jikesrvm.basic.core.bytecode.data.SubClassInDifferentPackage {

    static class O_C1 extends P_B {
      public void magic() { System.out.println("invoke magic O_C1"); }
    }

    class O_C2 extends P_B {
      public void magic() { System.out.println("invoke magic O_C2"); }
    }

    class O_E extends P_D {
      public void magic() { System.out.println("invoke magic O_E"); }
    }

    class O_G extends SubClassInDifferentPackage.P_F {
      public void magic() { System.out.println("invoke magic O_G"); }
    }

    class O_I1 extends H {
      public void magic() { System.out.println("invoke magic O_I1"); }
    }

    class O_I2 extends P_H {
      public void magic() { System.out.println("invoke magic O_I2"); }
    }

    class O_J1 extends P_I1 {
      public void magic() { System.out.println("invoke magic O_J1"); }
    }

    class O_J2 extends P_I2 {
      public void magic() { System.out.println("invoke magic O_J2"); }
    }

    private void runTests() {
      runTest("A", new A());
      runTest("B", new B());
      runTest("C", new C());
      runTest("D", new D());
      runTest("E", new E());
      runTest("F", new F());
      runTest("G", new G());
      runTest("H", new H());
      runTest("I", new I());
      runTest("J", new J());

      runTest("P_B", new P_B());
      runTest("P_C1", new P_C1());
      runTest("P_C2", new P_C2());
      runTest("P_D", new P_D());
      runTest("P_E1", new P_E1());
      runTest("P_E2", new P_E2());
      runTest("P_F", new P_F());
      runTest("P_G1", new P_G1());
      runTest("P_G2", new P_G2());
      runTest("P_H", new P_H());
      runTest("P_I1", new P_I1());
      runTest("P_I2", new P_I2());
      runTest("P_J1", new P_J1());
      runTest("P_J2", new P_J2());
      runTest("P_J3", new P_J3());

      runTest("O_C1", new O_C1());
      runTest("O_C2", new O_C2());
      runTest("O_E", new O_E());
      runTest("O_G", new O_G());
      runTest("O_I1", new O_I1());
      runTest("O_I2", new O_I2());
      runTest("O_J1", new O_J1());
      runTest("O_J2", new O_J2());
  }

  private static void runTest(final String name, final Magic x3) {
    System.out.println("Testing new " + name);
    testInstanceOf(x3);
    testCasts(x3);
    x3.magic();
  }

  @SuppressWarnings({"UnusedDeclaration"})
  private static void testCasts(final Object x) {
    System.out.print("Cast to A: ");
    try { final A o = (A) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to B: ");
    try { final B o = (B) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to C: ");
    try { final C o = (C) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to D: ");
    try { final D o = (D) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to E: ");
    try { final E o = (E) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to F: ");
    try { final F o = (F) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to G: ");
    try { final G o = (G) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to H: ");
    try { final H o = (H) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to I: ");
    try { final I o = (I) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to J: ");
    try { final J o = (J) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_B: ");
    try { final P_B o = (P_B) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_C1: ");
    try { final P_C1 o = (P_C1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_C2: ");
    try { final P_C2 o = (P_C2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_D: ");
    try { final P_D o = (P_D) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_E1: ");
    try { final SubClassInDifferentPackage.P_E1 o = (SubClassInDifferentPackage.P_E1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_E2: ");
    try { final SubClassInDifferentPackage.P_E2 o = (SubClassInDifferentPackage.P_E2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_F: ");
    try { final SubClassInDifferentPackage.P_F o = (SubClassInDifferentPackage.P_F) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_G1: ");
    try { final SubClassInDifferentPackage.P_G1 o = (SubClassInDifferentPackage.P_G1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_G2: ");
    try { final SubClassInDifferentPackage.P_G2 o = (SubClassInDifferentPackage.P_G2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_H: ");
    try { final SubClassInDifferentPackage.P_H o = (SubClassInDifferentPackage.P_H) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_I1: ");
    try { final SubClassInDifferentPackage.P_I1 o = (SubClassInDifferentPackage.P_I1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_I2: ");
    try { final SubClassInDifferentPackage.P_I2 o = (SubClassInDifferentPackage.P_I2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_J1: ");
    try { final SubClassInDifferentPackage.P_J1 o = (SubClassInDifferentPackage.P_J1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_J2: ");
    try { final SubClassInDifferentPackage.P_J2 o = (SubClassInDifferentPackage.P_J2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to P_J3: ");
    try { final SubClassInDifferentPackage.P_J3 o = (SubClassInDifferentPackage.P_J3) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_C1: ");
    try { final O_C1 o = (O_C1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_C2: ");
    try { final O_C2 o = (O_C2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_E: ");
    try { final O_E o = (O_E) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_G: ");
    try { final O_G o = (O_G) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_I1: ");
    try { final O_I1 o = (O_I1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_I2: ");
    try { final O_I2 o = (O_I2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_J1: ");
    try { final O_J1 o = (O_J1) x; success(); }
    catch (final ClassCastException cce) { failure(); }
    System.out.print("Cast to O_J2: ");
    try { final O_J2 o = (O_J2) x; success(); }
    catch (final ClassCastException cce) { failure(); }
  }

  private static void failure() {System.out.println("Failed");}

  private static void success() {System.out.println("Succeeded");}

  private static void testInstanceOf(final Object x) {
    io(A.class, (x instanceof A));
    io(B.class, (x instanceof B));
    io(C.class, (x instanceof C));
    io(D.class, (x instanceof D));
    io(E.class, (x instanceof E));
    io(F.class, (x instanceof F));
    io(G.class, (x instanceof G));
    io(H.class, (x instanceof H));
    io(I.class, (x instanceof I));
    io(J.class, (x instanceof J));
    io(P_B.class, (x instanceof P_B));
    io(P_C1.class, (x instanceof P_C1));
    io(P_C2.class, (x instanceof P_C2));
    io(P_D.class, (x instanceof P_D));
    io(SubClassInDifferentPackage.P_E1.class, (x instanceof SubClassInDifferentPackage.P_E1));
    io(SubClassInDifferentPackage.P_E2.class, (x instanceof SubClassInDifferentPackage.P_E2));
    io(SubClassInDifferentPackage.P_F.class, (x instanceof SubClassInDifferentPackage.P_F));
    io(SubClassInDifferentPackage.P_G1.class, (x instanceof SubClassInDifferentPackage.P_G1));
    io(SubClassInDifferentPackage.P_G2.class, (x instanceof SubClassInDifferentPackage.P_G2));
    io(SubClassInDifferentPackage.P_H.class, (x instanceof SubClassInDifferentPackage.P_H));
    io(SubClassInDifferentPackage.P_I1.class, (x instanceof SubClassInDifferentPackage.P_I1));
    io(SubClassInDifferentPackage.P_I2.class, (x instanceof SubClassInDifferentPackage.P_I2));
    io(SubClassInDifferentPackage.P_J1.class, (x instanceof SubClassInDifferentPackage.P_J1));
    io(SubClassInDifferentPackage.P_J2.class, (x instanceof SubClassInDifferentPackage.P_J2));
    io(SubClassInDifferentPackage.P_J3.class, (x instanceof SubClassInDifferentPackage.P_J3));
    io(O_C1.class, (x instanceof O_C1));
    io(O_C2.class, (x instanceof O_C2));
    io(O_E.class, (x instanceof O_E));
    io(O_G.class, (x instanceof O_G));
    io(O_I1.class, (x instanceof O_I1));
    io(O_I2.class, (x instanceof O_I2));
    io(O_J1.class, (x instanceof O_J1));
    io(O_J2.class, (x instanceof O_J2));
  }

  private static void io(final Class type, final boolean test) {
    System.out.println("instanceof " + type.getName() + " = " + test);
  }
  }
  public static void main(String args[]) {
    new SubSubClass().runTests();
  }
}