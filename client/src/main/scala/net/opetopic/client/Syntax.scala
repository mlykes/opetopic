/**
  * Syntax.scala - Exporting to syntax
  * 
  * @author Eric Finster
  * @version 0.1 
  */


package net.opetopic.client

import net.opetopic.core._

object SyntaxGenerator {

  // A better idea would be that these return a string *and* a boolean
  // which indicates if the enclosing object should add parentheses

  def treeToString[A](tr: STree[A])(f: A => String): String =
    tr match {
      case SLeaf => "lf ()"
      case SNode(a,sh) =>
        val shStr = treeToString(sh)(treeToString(_)(f))
        "nd (" ++ f(a) ++ ") (" ++ shStr ++ ")"
    }

  def nestingToString[A](nst: SNesting[A])(f: A => String): String =
    nst match {
      case SDot(a) => "lf (" ++ f(a) ++ ")" 
      case SBox(a,cn) =>
        val cnStr = treeToString(cn)(nestingToString(_)(f))
        "nd (" ++ f(a) ++ ") (" ++ cnStr ++ ")" 
    }


  def complexToString[A](cmplx: SComplex[A])(f: A => String): String =
    cmplx match {
      case ||(n) => "|> " ++ nestingToString(n)(f)
      case tl >> hd =>
        complexToString(tl)(f) ++ "\n|> " ++
        nestingToString(hd)(f)
    }

}
