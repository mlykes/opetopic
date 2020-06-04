/**
  * MutableCardinalGallery.scala - Mutation routines for a cardinal gallery
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.ui

import net.opetopic.core._
import net.opetopic.mtl._

trait MutableCardinalGallery[+F <: UIFramework]
    extends CardinalGallery[F] with SelectableGallery {

  import framework._
  import isNumeric._

  type CellType <: MutableCardinalCell
  type NeutralCellType <: CellType with MutableNeutralCell
  type PolarizedCellType <: CellType with MutablePolarizedCell
  type SelectionType <: MutableCardinalCell
  
  type PanelType <: MutableCardinalPanel

  var panels : Suite[PanelType] 

  //============================================================================================
  // GALLERY CONFIGURATION
  //

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

  var width: Size = fromInt(900)
  var height: Size = fromInt(300)
  var panelSpacing: Size = fromInt(2000)

  var layoutWidth: Bounds => Size = 
    (pb: Bounds) => width

  var layoutHeight: Bounds => Size = 
    (pb: Bounds) => height

  var layoutViewport: Bounds => Bounds = 
    (pb: Bounds) => pb

  var firstPanel: Option[Int] = None
  var lastPanel: Option[Int] = None

  var selectAfterExtrude: Boolean = true

  //============================================================================================
  // REFRESH ROUTINES
  //

  def renderAll: Unit

  // These should be combined somehow ...
  def refreshEdges: Unit = 
    panels.foreach(_.refreshEdges)

  def refreshAddresses: Unit = {
    panels.foreach(_.refreshAddresses)
  }

  //============================================================================================
  // MUTABLE CARDINAL PANELS
  //

  trait MutableCardinalPanel extends CardinalPanel { thisPanel : PanelType => 

    var cardinalNesting: SCardNst[NeutralCellType]
    
    def refreshAddresses: Unit = 
      cardinalNesting.foreachWithAddr(
        (box, addr) => { box.cardinalAddress = addr }
      )

  }

  //============================================================================================
  // MUTABLE CELLS
  //

  trait MutableCardinalCell
      extends CardinalCell
      with SelectableCell { thisCell : CellType with SelectionType =>

    var isExternal: Boolean

    def selectionAddress = cardinalAddress
    override def address = cardinalAddress.complexAddress

    var cardinalAddress: SCardAddr
    var label: LabelType

  }

  trait MutableNeutralCell
      extends MutableCardinalCell with NeutralCell {
    thisCell : CellType with NeutralCellType with SelectionType =>

    // Selection stuff
    def canSelect: Boolean = true
    var cardinalAddress: SCardAddr = SCardAddr()

    override def pathString: String = {
      if (isExposed) {
        var ps : String = "M " ++ edgeStartX.toString ++ " " ++ edgeStartY.toString ++ " "
        ps ++= "V " ++ edgeEndY.toString
        ps
      } else super.pathString
    }

    override def toString: String = 
      label.toString

  }

  trait MutablePolarizedCell
      extends MutableCardinalCell with PolarizedCell {
    thisCell : CellType with PolarizedCellType with SelectionType =>

    var cardinalAddress: SCardAddr = SCardAddr()
    def canSelect: Boolean = false

    // Polarized cells don't need updates on their labels
    def layoutLabel: Unit = ()
    labelNeedsLayout = false

  }

  //============================================================================================
  // MUTABILITY ROUTINES
  //

  def extendPanels(ps: Suite[PanelType]): Suite[PanelType] = {

    val ncn : MTree[STree[SNesting[NeutralCellType]]] = 
      Traverse[MTree].map(ps.head.cardinalNesting)(
        nst => nst.toTreeWith(_ =>
          SDot(createNeutralCell(ps.head.dim + 1, SCardAddr(), defaultLabel, true))
        )
      )

    val newPanel = createPanel(ps.head.dim + 1, MFix(ncn), Left(ps.head))

    ps >> newPanel

  }

  def extractSelection: Option[STree[SelectionType]] =
    selectionRoot.flatMap(root => {

      val addr = root.cardinalAddress

      for {
        zp <- seekToCanopy(root.cardinalAddress)
        cut <- zp.focus.takeWhile((n: SNesting[SelectionType]) => n.baseValue.isSelected)
        (et, es) = cut
      } yield et.map(_.baseValue)

    })

  def extrudeAtAddrWithMask[B](tgtVal: LabelType, fillVal: LabelType)(addr: SCardAddr, msk: STree[B]): Option[(SCardAddr, STree[B])] = {

    val dim = addr.dim

    val tgtCell = createNeutralCell(dim, SCardAddr(), tgtVal, false)
    val fillCell = createNeutralCell(dim + 1, SCardAddr(), fillVal, true)
    
    val extPanels : Suite[PanelType] =
      if (dim == panels.head.dim)
        extendPanels(panels)
      else panels

    val extCardinal : SCardinal[NeutralCellType] =
      Traverse[Suite].map(extPanels)(_.cardinalNesting)
    
    for {
      c <- extCardinal.extrudeWithMask(addr, tgtCell, fillCell)(msk)
    } yield {

      deselectAll

      extPanels.zipWithSuite(c).foreach({
        case (p, n) => p.cardinalNesting = n
      })

      panels = extPanels

      refreshEdges
      refreshAddresses
      
      renderAll

      if (selectAfterExtrude)
        tgtCell.selectAsRoot
      
      (addr, msk)

    }

  }
  
  def extrudeSelectionWith(tgtVal: LabelType, fillVal: LabelType): Option[(SCardAddr, STree[Int])] =
    selectionRoot match {
      case None => None
      case Some(root) => {

        if (root.isExposed) {

          val tgtCell = createNeutralCell(root.dim, SCardAddr(), tgtVal, false)
          val fillCell = createNeutralCell(root.dim + 1, SCardAddr(), fillVal, true)

          val extPanels : Suite[PanelType] =
            if (root.dim == panels.head.dim)
              extendPanels(panels)
            else panels

          val extCardinal : SCardinal[NeutralCellType] =
            Traverse[Suite].map(extPanels)(_.cardinalNesting)

          val extAddr = root.cardinalAddress

          for {
            pr <- extCardinal.extrude(extAddr, tgtCell, fillCell)(_.isSelected)
          } yield {

            val (c, msk) = pr

            deselectAll

            extPanels.zipWithSuite(c).foreach({
              case (p, n) => p.cardinalNesting = n
            })

            panels = extPanels

            refreshEdges
            refreshAddresses
            
            renderAll

            if (selectAfterExtrude)
              tgtCell.selectAsRoot

            (extAddr, msk)

          }

        } else None

      }
    }

  def extrudeSelection: Unit =
    extrudeSelectionWith(defaultLabel, defaultLabel)

  def loopAtAddrWith(tgtVal: LabelType, fillVal: LabelType)(addr: SCardAddr) : Option[SCardAddr] = {

    val dim = addr.dim
    val tgtCell = createNeutralCell(dim + 1, SCardAddr(), tgtVal, false)
    val fillCell = createNeutralCell(dim + 2, SCardAddr(), fillVal, true)

    val extPanels : Suite[PanelType] =
      if (dim == panels.head.dim)
        extendPanels(extendPanels(panels))
      else if (dim == panels.head.dim - 1)
        extendPanels(panels)
      else panels

    val extCardinal : SCardinal[NeutralCellType] =
      Traverse[Suite].map(extPanels)(_.cardinalNesting)

    for {
      c <- extCardinal.extrudeLoop(addr, tgtCell, fillCell)
    } yield {

      deselectAll

      extPanels.zipWithSuite(c).foreach({
        case (p, n) => p.cardinalNesting = n
      })

      panels = extPanels

      refreshEdges
      refreshAddresses

      renderAll

      // if (selectAfterExtrude)
      //   root.selectAsRoot

      addr

    }
  }

  def loopAtSelectionWith(tgtVal: LabelType, fillVal: LabelType) : Option[SCardAddr] = 
    selectionRoot match {
      case None => None
      case Some(root) => {

        if (root.isExposed) {

          val tgtCell = createNeutralCell(root.dim + 1, SCardAddr(), tgtVal, false)
          val fillCell = createNeutralCell(root.dim + 2, SCardAddr(), fillVal, true)

          val extPanels : Suite[PanelType] =
            if (root.dim == panels.head.dim)
              extendPanels(extendPanels(panels))
            else if (root.dim == panels.head.dim - 1)
              extendPanels(panels)
            else panels

          val extCardinal : SCardinal[NeutralCellType] =
            Traverse[Suite].map(extPanels)(_.cardinalNesting)

          val extAddr = root.cardinalAddress

          for {
            c <- extCardinal.extrudeLoop(extAddr, tgtCell, fillCell)
          } yield {

            deselectAll

            extPanels.zipWithSuite(c).foreach({
              case (p, n) => p.cardinalNesting = n
            })

            panels = extPanels

            refreshEdges
            refreshAddresses

            renderAll

            if (selectAfterExtrude)
              root.selectAsRoot

            extAddr

          }

        } else None

      }
    }

  def loopAtSelection : Unit = {
    loopAtSelectionWith(defaultLabel, defaultLabel)
  }

  def sproutAtAddrWith(srcVal: LabelType, fillVal: LabelType)(addr: SCardAddr): Option[SCardAddr] = {

    val dim = addr.dim

    val srcCell = createNeutralCell(dim, SCardAddr(), srcVal, true)
    val fillCell = createNeutralCell(dim + 1, SCardAddr(), fillVal, true)

    val extPanels : Suite[PanelType] =
      if (dim == panels.head.dim)
        extendPanels(panels)
      else panels

    val extCardinal : SCardinal[NeutralCellType] =
      Traverse[Suite].map(extPanels)(_.cardinalNesting)
    
    for {
      z <- cardinal.seekNesting(addr)
      c <- extCardinal.sprout(addr, srcCell, fillCell)
    } yield {

      deselectAll

      extPanels.zipWithSuite(c).foreach({
        case (p, n) => p.cardinalNesting = n
      })

      panels = extPanels
      z.focus.baseValue.isExternal = false

      refreshEdges
      refreshAddresses
      renderAll

      if (selectAfterExtrude)
        srcCell.selectAsRoot

      addr

    }


  }
  
  def sproutAtSelectionWith(srcVal: LabelType, fillVal: LabelType): Option[SCardAddr] = 
    selectionRoot match {
      case None => None
      case Some(root) => {
        if (root.isExternal) {

          val srcCell = createNeutralCell(root.dim, SCardAddr(), srcVal, true)
          val fillCell = createNeutralCell(root.dim + 1, SCardAddr(), fillVal, true)

          val extPanels : Suite[PanelType] =
            if (root.dim == panels.head.dim)
              extendPanels(panels)
            else panels

          val extCardinal : SCardinal[NeutralCellType] =
            Traverse[Suite].map(extPanels)(_.cardinalNesting)

          val extAddr = root.cardinalAddress

          for {
            c <- extCardinal.sprout(extAddr, srcCell, fillCell)
          } yield {

            deselectAll

            extPanels.zipWithSuite(c).foreach({
              case (p, n) => p.cardinalNesting = n
            })

            panels = extPanels
            root.isExternal = false

            refreshEdges
            refreshAddresses
            renderAll

            if (selectAfterExtrude)
              srcCell.selectAsRoot

            extAddr

          }

        } else None

      }
    }

  def sproutAtSelection: Unit = 
    sproutAtSelectionWith(defaultLabel, defaultLabel)

}
