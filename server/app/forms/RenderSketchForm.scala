/**
  * RenderSketchForm.scala - Request form for rendering sketches
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package forms

import play.api.data.Form
import play.api.data.Forms._

object RenderSketchForm {

  val renderForm = Form(
    mapping(
      "fileName" -> nonEmptyText,
      "renderType" -> nonEmptyText,
      "renderData" -> nonEmptyText,
      "sizingMethod" -> nonEmptyText
    )(RenderDesc.apply)(RenderDesc.unapply)
  )

  case class RenderDesc(
    fileName: String,
    renderType: String, 
    renderData: String,
    sizingMethod: String
  )

}
