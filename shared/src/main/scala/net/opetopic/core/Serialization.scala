/**
  * STree.scala - Serialization routines
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.core

import upickle.default.{ReadWriter => RW, macroRW}

trait Serialization {

  // I think with the current setup, this will serialize with
  // the complete nampespace of the case classes.  You may want
  // to customize the labels to avoid this kind of thing.

  implicit def sNodeRW[A](implicit rw: RW[A]): RW[SNode[A]] =
    macroRW
  
  implicit def sTreeRW[A](implicit rw: RW[A]): RW[STree[A]] =
    RW.merge(sNodeRW(rw), macroRW[SLeaf.type])

  implicit def sDotRW[A](implicit rw: RW[A]): RW[SDot[A]] =
    macroRW

  implicit def sBoxRW[A](implicit rw: RW[A]): RW[SBox[A]] =
    macroRW

  implicit def sNestingRW[A](implicit rw: RW[A]): RW[SNesting[A]] =
    RW.merge(sDotRW(rw), sBoxRW(rw))

  implicit def initRW[A](implicit rw: RW[A]): RW[||[A]] =
    macroRW

  implicit def extendRW[A](implicit rw: RW[A]): RW[>>[A]] =
    macroRW

  implicit def suiteRW[A](implicit rw: RW[A]): RW[Suite[A]] =
    RW.merge(initRW(rw), extendRW(rw))

}
