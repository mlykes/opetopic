/**
  * ComplexGallery.scala - A Trait for a gallery containing a complex
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.ui

import net.opetopic.core._
import net.opetopic.mtl._

trait ComplexGallery[F <: UIFramework] { thisGallery: StableGallery[F] => 

  type PanelType <: ComplexPanel

  trait ComplexPanel { thisPanel : PanelType => refreshEdges }

  def createCell(lbl: LabelType, dim: Int, addr: SAddr, isExternal: Boolean): CellType
  def createPanel(bn: SNesting[CellType], en: Either[PanelType, SNesting[CellType]]): PanelType

  def buildCells(dim: Int, n: SNesting[LabelType]): SNesting[CellType] =
    n.foldNestingWithAddr[SNesting[BoxType]]()({
      case (a, addr) => SDot(createCell(a, dim, addr, true))
    })({
      case (a, addr, cn) => SBox(createCell(a, dim, addr, false), cn)
    })

  def buildPanels(c: SComplex[LabelType]) : Suite[PanelType] =
    c match {
      case ||(n) => {

        val objNesting = buildCells(0, n)
        val inEdge = createCell(n.baseValue, -1, Nil, true)
        val outEdge = createCell(n.baseValue, -1, Nil, false)
        val panel = createPanel(objNesting, Right(SBox(outEdge, STree.obj(SDot(inEdge)))))

        ||(panel)

      }
      case tl >> hd => {

        val tailPanels = buildPanels(tl)
        val prevPanel = tailPanels.head

        val panelCells = buildCells(prevPanel.dim + 1, hd)
        val panel = createPanel(panelCells, Left(prevPanel))

        tailPanels >> panel

      }
    }


}
