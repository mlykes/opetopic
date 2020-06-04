/**
  * StaticMultiCardinalGallery.scala - A static gallery for rendering multi-cardinals
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.ui

import net.opetopic.core._
import net.opetopic.mtl._

class StaticMultiCardinalGallery[A, F <: UIFramework](val frmwk: F)(val mc: MultiCard[A])(implicit rn: Renderable[A, F]) {

  type FT = frmwk.type

  import frmwk._
  import isNumeric._

  implicit val aRenderable: Renderable[A, FT] =
    new Renderable[A, FT] {
      def render(fm: FT)(a : A): fm.CellRendering =
        rn.render(fm)(a)
    }

  implicit def multCardRenderable: Renderable[MultiCard[A], FT] =
    new Renderable[MultiCard[A], FT] {
      def render(fm: FT)(mcard: MultiCard[A]): fm.CellRendering = {
        renderMultiCard(mcard)
      }
    }

  def renderMultiCard(mc: MultiCard[A]): CellRendering =
    mc match {
      case Ret(a) => aRenderable.render(frmwk)(a)
      case Join(mm) => {
        CellRendering(new LevelGallery(mm.map(Some(_))).boundedElement)
      }
    }

  def element: Element = {

    val be = renderMultiCard(mc).boundedElement

    val bnds = be.bounds
    val w = bnds.width
    val h = bnds.height

    viewport(w, h, bnds, be.element)

  }

  //
  //  Renderable instances
  //

  object LevelGallery {
    implicit def levelGalleryRenderable[B](implicit rb: Renderable[B, FT]): Renderable[LevelGallery[B], FT] =
      new Renderable[LevelGallery[B], FT] {
        def render(ft: FT)(lg: LevelGallery[B]): ft.CellRendering = {
          CellRendering(lg.boundedElement)
        }
      }
  }

  //
  //  Level Gallery - A gallery instance for each depth 
  //

  class LevelGallery[B](card: SCardinal[Option[B]])(implicit brn: Renderable[B, FT])
      extends StaticStableGallery(frmwk) with CardinalGallery[FT]  {

    type LabelType = Option[B]
    type PanelType = LevelPanel
    type CellType = LevelCell
    type NeutralCellType = LevelNeutralCell
    type PolarizedCellType = LevelPolarizedCell
    type PositiveCellType = LevelPositiveCell
    type NegativeCellType = LevelNegativeCell

    //
    //  Visual Options
    //

    var internalPadding : Size = fromInt(400)
    var externalPadding : Size = fromInt(600)
    var decorationPadding : Size = fromInt(300)
    var leafWidth : Size = fromInt(200)
    var strokeWidth : Size = fromInt(100)
    var cornerRadius : Size = fromInt(200)

    //
    //  Gallery Options
    //

    var panelSpacing: Size = fromInt(2000)

    var layoutWidth: Bounds => Size = 
      (pb: Bounds) => pb.width

    var layoutHeight: Bounds => Size = 
      (pb: Bounds) => pb.height

    var layoutViewport: Bounds => Bounds = 
      (pb: Bounds) => pb
    
    var firstPanel: Option[Int] = None
    var lastPanel: Option[Int] = None

    val renderer : Renderable[Polarity[Option[B]], FT] =
      Renderable[Polarity[Option[B]], FT]

    // The currently displayed cardinal
    def cardinal: SCardinal[NeutralCellType] = 
      Traverse[Suite].map(panels)(_.cardinalNesting)

    // Panel creation uses default labels
    def defaultLabel: LabelType = None

    /// Our panels
    def panels: Suite[PanelType] = 
      buildPanels(card)._1

    // Panel Constructor
    def createPanel(dim: Int, cn: SCardNst[NeutralCellType], ed: Either[PanelType, SNesting[CellType]]): PanelType =
      new LevelPanel(dim, cn, ed)

    // Neutral Cell Constructor
    def createNeutralCell(dim: Int, ca: SCardAddr, initLabel: LabelType, isExternal: Boolean) : NeutralCellType =
      new LevelNeutralCell(dim, ca, initLabel, isExternal)

    // We want top level elements to be groups, not viewports, as
    // they are going to be nested as labels ....

    override def element: Element =
      boundedElement.element

    override def boundedElement: BoundedElement = {
      val (bnds, els) = panelElementsAndBounds
      BoundedElement(group(els.toList: _*), bnds)
    }

    class LevelPanel(
      val dim: Int,
      val cardinalNesting: SCardNst[LevelNeutralCell],
      val edgeData: Either[LevelPanel, SNesting[LevelCell]]
    ) extends StaticPanel with CardinalPanel {

      val positiveCell: PositiveCellType = new LevelPositiveCell(dim)
      val negativeCell: NegativeCellType = new LevelNegativeCell(dim)

      // We have to initialize the edge connections
      // before any layout happens ...
      refreshEdges

    }

    abstract class LevelCell extends StaticCell with CardinalCell

    class LevelNeutralCell(
      val dim: Int,
      val cardinalAddress: SCardAddr, 
      val label: Option[B],
      var isExternal: Boolean
    ) extends LevelCell with NeutralCell {

      def layoutLabel: Unit = ()
      val cellRendering: CellRendering =
        renderer.render(framework)(Neutral(label))

      // Gotta fix this guy.  This is why some of the paths
      // are missing ....
      override def pathString: String = {
        if (isExposed) {
          var ps : String = "M " ++ edgeStartX.toString ++ " " ++ edgeStartY.toString ++ " "
          ps ++= "V " ++ edgeEndY.toString
          ps
        } else super.pathString
      }

    }

    abstract class LevelPolarizedCell extends LevelCell with PolarizedCell {
      override def isVisible = false
    }

    class LevelPositiveCell(val dim: Int) extends LevelPolarizedCell with PositiveCell {

      val label: Option[B] = None
      val isExternal: Boolean = false
      val cellRendering: CellRendering =
        renderer.render(framework)(Positive())
      val cardinalAddress: SCardAddr = SCardAddr()
      def layoutLabel: Unit = ()

      override def toString = "+"

    }

    class LevelNegativeCell(val dim: Int) extends LevelPolarizedCell with NegativeCell {

      val label: Option[B] = None
      val isExternal: Boolean = true

      val cellRendering: CellRendering = 
        renderer.render(framework)(Negative())
      val cardinalAddress: SCardAddr = SCardAddr()
      def layoutLabel: Unit = ()

      override def toString = "-"

    }


  }

}
