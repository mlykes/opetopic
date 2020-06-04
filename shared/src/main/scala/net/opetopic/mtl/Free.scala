/**
  * Free.scala - The Free Monad
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.mtl

import upickle.default.{ReadWriter => RW, _}

sealed trait Free[F[+_], +A]
case class Ret[F[+_], +A](val a: A) extends Free[F, A]
case class Join[F[+_], +A](val fa: F[Free[F, A]]) extends Free[F, A]

// Serialization of the free monad.

trait SC[F[+_]] {
  def extend[A](rw: RW[A]): RW[F[A]]
}

object Free {
  implicit def freeRW[F[+_], A](implicit rwA: RW[A], scF: SC[F]): RW[Free[F, A]] =
    readwriter[ujson.Value].bimap[Free[F, A]](
      { case Ret(a) => { ujson.Obj("type" -> "ret", "val" -> writeJs(a)) }
        case Join(fa) => {
          implicit val faRW: RW[F[Free[F, A]]] = scF.extend(freeRW(rwA, scF))
          ujson.Obj("type" -> "join", "val" -> writeJs(fa))
        }},
      json => {
        json("type").str match {
          case "ret" => Ret(read[A](json("val")))
          case "join" => {
            implicit val faRW: RW[F[Free[F, A]]] = scF.extend(freeRW(rwA, scF))
            Join(read[F[Free[F, A]]](json("val")))
          }
        }
      }
    )
}



