package net.opetopic.controllers

import javax.inject._
import play.api.mvc._

import org.webjars.play.WebJarsUtil

@Singleton
class Application @Inject()(cc: ControllerComponents)(
  implicit webJarsUtil: WebJarsUtil
) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index())
  }

}
