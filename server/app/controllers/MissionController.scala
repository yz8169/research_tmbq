package controllers

import java.io.File
import java.net.URLEncoder

import actors.MissionManageActor
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import command.CommandExecutor
import dao.MissionDao
import javax.inject.Inject
import mission.MissionUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}

import org.joda.time.DateTime
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.streams.ActorFlow
import tool.{FormTool, Tool, WebTool}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.collection.parallel.ForkJoinTaskSupport
//import scala.concurrent.forkjoin.ForkJoinPool
import java.util.concurrent.ForkJoinPool
import models.Tables._
import utils.Utils
import implicits.Implicits._
import tool.Pojo.IndexData


/**
 * Created by yz on 2018/9/18
 */
class MissionController @Inject()(cc: ControllerComponents, formTool: FormTool,
                                 )(implicit val system: ActorSystem,
                                   implicit val missionDao: MissionDao,
                                   implicit val materializer: Materializer) extends AbstractController(cc) {

  val missionManageActor = system.actorOf(
    Props(new MissionManageActor())
  )
  missionManageActor ! "ask"

  def newMission = Action.async(parse.multipartFormData) { implicit request =>
    val threadNum = 2
    val missionName = Tool.generateMissionName
    val row = MissionRow(0, s"${missionName}", new DateTime(), None, "preparing", threadNum)
    missionDao.insert(row).flatMap(_ => missionDao.selectByMissionName(row.missionName)).flatMap { mission =>
      val outDir = Tool.getUserMissionDir
      val missionDir = MissionUtils.getMissionDir(mission.id, outDir)
      val (workspaceDir, resultDir) = (missionDir.workspaceDir, missionDir.resultDir)
      val file = new File(workspaceDir, "data.zip")
      WebTool.fileMove("dataFile", file)
      val sampleConfigExcelFile = new File(workspaceDir, "sample_config.xlsx")
      WebTool.fileMove("sampleConfigFile", sampleConfigExcelFile)
      val compoundConfigFile = new File(workspaceDir, "compound_config.xlsx")
      WebTool.fileMove("compoundConfigFile", compoundConfigFile)
      val newMission = mission.copy(state = "wait")
      missionDao.update(newMission).map { x =>
        Ok(Json.obj("valid" -> true))
      }
    }

  }


}
