/**
  * OpetopicApi.scala - Remote API
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.net

import upickle.default.{ReadWriter => RW, macroRW}

case class SaveSketchRequest(
  val name: String,
  val path: String,
  val description: String,
  val data: String
)

case class LoadSketchRequest(
  val id: String
)

case class DeleteSketchRequest(
  val id: String
)

case class RenderSketchRequest(
  val name: String,
  val data: String
)

case class SaveModuleRequest(
  val moduleId: Option[String],
  val name: String,
  val description: String,
  val data: String
)

case class LoadModuleRequest(
  val name: String,
  val uuid: String
)

case class DeleteModuleRequest(
  val id: String
)

// Not sure where this belongs ....
sealed trait SizingMethod
case class Sized(val width: Int, val height: Int) extends SizingMethod
case class FixedWidth(val width: Int) extends SizingMethod
case class FixedHeight(val height: Int) extends SizingMethod
case class Percentage(val pct: Double) extends SizingMethod

object Sized { implicit val sizedRW: RW[Sized] = macroRW }
object FixedWidth { implicit val fixedWidthRW: RW[FixedWidth] = macroRW }
object FixedHeight { implicit val fixedHeightRW: RW[FixedHeight] = macroRW }
object Percentage { implicit val percentageRW: RW[Percentage] = macroRW }

object SizingMethod {
  implicit val sizingMethodRW: RW[SizingMethod] =
    RW.merge(
      Sized.sizedRW,
      FixedWidth.fixedWidthRW,
      FixedHeight.fixedHeightRW,
      Percentage.percentageRW
    )
}
