/**
  * CardinalGallery.scala - A Gallery for displaying a Cardinal
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.ui

import net.opetopic.core._
import net.opetopic.mtl._

trait CardinalGallery[+F <: UIFramework]
    extends StableGallery[F] {

  import framework._
  import isNumeric._
  
  type PanelType <: CardinalPanel
  type CellType <: CardinalCell

  type NeutralCellType <: CellType with NeutralCell
  type PolarizedCellType <: CellType with PolarizedCell
  type PositiveCellType <: CellType with PositiveCell
  type NegativeCellType <: CellType with NegativeCell
  
  type AddressType = SCardAddr

  // The currently displayed cardinal
  def cardinal: SCardinal[NeutralCellType] 

  // The complex calculated from the current cardinal
  def complex: SComplex[CellType] = 
    Traverse[Suite].map(panels)(_.boxNesting)

  // Panel creation uses default labels
  def defaultLabel: LabelType
  
  //============================================================================================
  // CARDINAL PANELS
  //

  trait CardinalPanel extends StablePanel { thisPanel : PanelType => 

    def cardinalNesting: SCardNst[NeutralCellType]

    def positiveCell: PositiveCellType
    def negativeCell: NegativeCellType

    def boxNesting: SNesting[CellType] = 
      cardinalNesting.toNesting(
        positiveCell, 
        negativeCell
      )

    override def bounds: Bounds = {

      // Here are all the leaves
      val lvs = edgeNesting.spine(SDeriv(SLeaf)).getOrElse(SLeaf).toList

      // And here are all the boxes
      val bxs = boxNesting match {
        case SDot(_) => Nil // Shouldn't happen
        case SBox(_, cn) => cn.toList.map(_.baseValue).filter(_.isVisible)
      }

      val (boxMinX, boxMaxX, boxMinY, boxMaxY) =
        bxs match {
          case Nil => (zero,zero,zero,zero) // Error?
          case b::bs => {
            (bs foldLeft (b.x,b.x + b.width, b.y, b.y + b.height))({
              case ((curMinX, curMaxX, curMinY, curMaxY), cell) => {
                val nextMinX = isOrdered.min(curMinX, cell.x)
                val nextMaxX = isOrdered.max(curMaxX, cell.x + cell.width)
                val nextMinY = isOrdered.min(curMinY, cell.y)
                val nextMaxY = isOrdered.max(curMaxY, cell.y + cell.height)
                (nextMinX, nextMaxX, nextMinY, nextMaxY)
              }
            })
          }
        }

      // Now adjust if any of the leaves exceed the previous box calculations
      val (minX, maxX, minY) =
        (lvs foldLeft (boxMinX, boxMaxX, boxMinY))({
          case ((curMinX, curMaxX, curMinY), cell) => {
            val nextMinX = isOrdered.min(curMinX, cell.edgeStartX)
            val nextMaxX = isOrdered.max(curMaxX, cell.edgeStartX)
            val nextMinY = isOrdered.min(curMinY, cell.edgeStartY)
            (nextMinX, nextMaxX, nextMinY)
          }
        })

      Bounds(
        minX,
        minY,
        maxX - minX,
        (boxMaxY - minY) + (fromInt(4) * externalPadding)
      )

    }

  }

  // Panel Constructor
  def createPanel(dim: Int, cn: SCardNst[NeutralCellType], ed: Either[PanelType, SNesting[CellType]]): PanelType
  
  //============================================================================================
  // CARDINAL CELLS
  //

  trait CardinalCell extends GalleryCell { thisCell : CellType => 

    def cardinalAddress: SCardAddr
    def address = cardinalAddress.complexAddress

    def isExposed: Boolean =
      cardinalAddress.boxAddr == Nil

  }

  // Neutral Cell Constructor
  def createNeutralCell(dim: Int, ca: SCardAddr, initLabel: LabelType, isExternal: Boolean) : NeutralCellType

  trait NeutralCell extends CardinalCell { thisCell : CellType with NeutralCellType => }

  trait PolarizedCell extends CardinalCell { thisCell : CellType with PolarizedCellType =>
    override def isExposed = false
  }

  trait PositiveCell extends PolarizedCell { thisCell : CellType with PolarizedCellType with PositiveCellType => }
  trait NegativeCell extends PolarizedCell { thisCell : CellType with PolarizedCellType with NegativeCellType => }

  //============================================================================================
  // INITIALIZATION
  //

  def buildPanels(c: SCardinal[LabelType]): (Suite[PanelType], Int) = 
    c match {
      case ||(mt) => {

        val bn = MTreeOps(mt).traverseWithAddr[Id,SNesting[NeutralCellType]]({
          case (cn, ma) => cn.mapWithAddr({
            case (n,addr) => buildCells(0, ma, addr, n)
          })
        })

        // Hmmm.  You should find a way to not have to expose
        // a default value here.  It's a bit ugly....
        val inEdge = createNeutralCell(-1, SCardAddr(), defaultLabel, true)
        val outEdge = createNeutralCell(-1, SCardAddr(), defaultLabel, false)
        val en = SBox(outEdge, STree.obj(SDot(inEdge)))

        (||(createPanel(0, bn, Right(en))), 0)

      }
      case tl >> hd => {
        val (pt, d) = buildPanels(tl)

        val bn = MTreeOps(hd).traverseWithAddr[Id,SNesting[NeutralCellType]]({
          case (cn, ma) => cn.mapWithAddr({
            case (n,addr) => buildCells(d+1, ma, addr, n)
          })
        })

        (pt >> createPanel(d + 1, bn, Left(pt.head)), d + 1)
      }
    }

  def buildCells(d: Int, ma: MAddr, ca: SAddr, n: SNesting[LabelType]): SNesting[NeutralCellType] = 
    n.foldNestingWithAddr[SNesting[NeutralCellType]]()({
      case (optLabel, addr) => SDot(createNeutralCell(d, SCardAddr(ma, ca, addr), optLabel, true))
    })({
      case (optLabel, addr, cn) => SBox(createNeutralCell(d, SCardAddr(ma, ca, addr), optLabel, false), cn)
    })

}
