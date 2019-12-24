package controllers

import dao.{ConfigDao, MissionDao}
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.ExecutionContext.Implicits.global
import tool.Tool

/**
 * Created by Administrator on 2019/7/2
 */
class TestController @Inject()(cc: ControllerComponents)(missionDao:MissionDao)(implicit val configDao:ConfigDao) extends AbstractController(cc) {

  def test = Action.async { implicit request =>
    missionDao.selectByMissionId(1173).map{misison=>
      Tool.sendMail(misison)
      Ok(Json.toJson("success!"))
    }


  }



}
