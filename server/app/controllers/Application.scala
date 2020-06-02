package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf._

import org.webjars.play.WebJarsUtil

import forms.RenderSketchForm._

import net.opetopic.core._
import net.opetopic.ui._

@Singleton
class Application @Inject()(
  messagesAction: MessagesActionBuilder,
  addToken: CSRFAddToken, checkToken: CSRFCheck,
  cc: ControllerComponents
)(
  implicit webJarsUtil: WebJarsUtil
) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index())
  }

  def studio = addToken {
    messagesAction { implicit request =>
      Ok(views.html.studio(renderForm))
    }
  }

  def multiedit = Action {
    Ok(views.html.multiedit())
  }

  def renderSketch = checkToken {
    Action { implicit request =>

      renderForm.bindFromRequest.fold(
        form => BadRequest("Bad render reqeust"),
        data => {

          import upickle.default._
          import ScalatagsTextFramework.Bounds

          val cmplx = read[SComplex[Option[SimpleMarker]]](data.renderData)
          val staticGallery = new SimpleStaticGallery(ScalatagsTextFramework)(cmplx)

          val maxWidth = 725
          val maxHeight = 260

          val fct = 0.02
          staticGallery.layoutWidth = (b: Bounds) => { val fw = (b.width * fct).toInt ; if (fw > maxWidth) maxWidth else fw }
          staticGallery.layoutHeight = (b: Bounds) => { val fh = (b.height * fct).toInt ; if (fh > maxHeight) maxHeight else fh }

          val xmlHeader: String = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
          Ok(xmlHeader + "\n" + staticGallery.element.toString).
            as("image/svg.xml").
            withHeaders(
              CONTENT_DISPOSITION -> ("attachment; filename=" ++ data.fileName)
            )

        }
      )
    }
  }
    
}


