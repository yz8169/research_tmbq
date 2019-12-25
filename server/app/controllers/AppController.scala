package controllers

import java.io.File

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter
import tool.{FormTool, Tool}
import utils.Utils
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Administrator on 2019/7/2
 */
class AppController @Inject()(cc: ControllerComponents,formTool:FormTool) extends AbstractController(cc) {

  def toIndex = Action { implicit request =>
    Ok(views.html.index())
  }

  def toManual = Action { implicit request =>
    Ok(views.html.manual())
  }

  def getAllArgs = Action { implicit request =>
    val file = new File(Tool.dataDir, "args.txt")
    val (columnNames, array) = Utils.getInfoByFile(file)
    val json = Json.obj("columnNames" -> columnNames, "array" -> array)
    Ok(json)
  }

  def downloadExampleData = Action {
    implicit request =>
      val data = formTool.fileNameForm.bindFromRequest().get
      val exampleDir = Tool.exampleDir
      val resultFile = new File(exampleDir, data.fileName)
      Ok.sendFile(resultFile).withHeaders(
        CONTENT_DISPOSITION -> s"attachment; filename=${
          resultFile.getName
        }",
        CONTENT_TYPE -> "application/x-download"
      )
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

        controllers.routes.javascript.AppController.getAllArgs,
        controllers.routes.javascript.AppController.downloadExampleData,

      )
    ).as("text/javascript")

  }


}
