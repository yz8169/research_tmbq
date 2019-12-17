package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter

/**
 * Created by Administrator on 2019/7/2
 */
class AppController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def toIndex = Action { implicit request =>
    Ok(views.html.index())
  }


  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        controllers.routes.javascript.MissionController.newMission,
        controllers.routes.javascript.MissionController.resultBefore,
        controllers.routes.javascript.MissionController.updateMissionSocket,
        controllers.routes.javascript.MissionController.downloadResult,
        controllers.routes.javascript.MissionController.downloadLog,
        controllers.routes.javascript.MissionController.getMissionState,

      )
    ).as("text/javascript")

  }


}
