/**
  * MultiEdit.scala - A multitopic editor implementation
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.client

import scala.scalajs.{js => sjs}
import scala.scalajs.js.timers._
import scala.scalajs.js.annotation._
import sjs.Dynamic.{literal => lit}
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.jquery._

import ui._
import JsDomFramework._
import JQuerySemanticUI._

import scalatags.JsDom.all._

import net.opetopic.core._
import net.opetopic.ui._
import net.opetopic.mtl._

@JSExportTopLevel("MultiEdit")
object MultiEdit {

  val me = new MultiEditor[SimpleMarker, JsDomFramework.type](JsDomFramework)

  val innerControlPane = new CardinalEditorPane(me.innerControlEditor)
  val outerControlPane = new CardinalEditorPane(me.outerControlEditor)

  val multiEditPane = new CardinalEditorPane(me.dblEditor)

  val topPane =
    new FixedBottomPane(
      multiEditPane,
      PlainComponent(
        div(cls := "ui inverted menu", style := "margin-top: 0; border-radius: 0;")(
          div(cls := "right menu")(
            div(cls := "item")(
              div(cls := "ui action input")(
                input(id := "download-input", `type` := "text", placeholder := "Filename ...", onchange := { () => onDownloadSketch }),
                button(cls := "ui button", onclick := { () => onDownloadSketch })("Download")
              )
            )
          )
        ).render)
    )

  val controlPanes =
    new HorizontalSplitPane(
      innerControlPane,
      outerControlPane
    )

  val content =
    new VerticalSplitPane(
      topPane,
      // multiEditPane,
      controlPanes
    )

  def handleResize: Unit = {

    val uiWidth = jQuery("#editor-div").width.toInt
    val uiHeight = jQuery(".content").first.height.toInt

    content.setWidth(uiWidth)
    content.setHeight(uiHeight)

  }

  def onDownloadSketch: Unit = {

      println("In download sketch ...")

      import upickle.default._
      import net.opetopic.net._

      val renderData: String = write[MultiCard[Option[SimpleMarker]]](me.multiCard)
      val sizingMethod : String = write(Percentage(0.05))

      jQuery("#sketch-file").value(jQuery("#download-input").value.asInstanceOf[String])
      jQuery("#render-type").value("MultiCardinal")
      jQuery("#render-data").value(renderData)
      jQuery("#sizing-mthd").value(sizingMethod)
      jQuery("#render-request-form").submit()

    }


  @JSExport
  def initialize(): Unit = {

    jQuery("#editor-div").append(content.uiElement)

    jQuery(dom.window).on("resize", () => { handleResize })
    setTimeout(100){
      jQuery(dom.window).trigger("resize")

      // Make dividers active
      controlPanes.initialize
      content.initialize

      // Setup event handling in the editors
      innerControlPane.initialize
      outerControlPane.initialize

      // No keypress handlers for main multi-editor
      // me.renderAllInner
      multiEditPane.editor.renderAll
    }

  }

}

