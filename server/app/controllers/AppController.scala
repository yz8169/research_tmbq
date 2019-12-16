package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter

/**
  * Created by Administrator on 2019/7/2
  */
class AppController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def toIndex=Action{implicit request=>
//    println("in")
    Ok(views.html.index())
  }


  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(

      )
    ).as("text/javascript")

  }


}
