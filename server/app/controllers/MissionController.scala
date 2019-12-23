package controllers

import java.io.File
import java.net.URLEncoder

import actors.MissionManageActor
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import command.CommandExecutor
import dao.{MissionDao, ModeDao}
import javax.inject.Inject
import mission.MissionUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import org.joda.time.DateTime
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.streams.ActorFlow
import tool.{FileTool, FormTool, Tool, WebTool}

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
import scala.language.postfixOps


/**
 * Created by yz on 2018/9/18
 */
class MissionController @Inject()(cc: ControllerComponents, formTool: FormTool,
                                 )(implicit val system: ActorSystem,
                                   implicit val missionDao: MissionDao,
                                   implicit val modeDao: ModeDao,
                                   implicit val materializer: Materializer) extends AbstractController(cc) {

  val missionManageActor = system.actorOf(
    Props(new MissionManageActor())
  )
  missionManageActor ! "ask"

  def newMission = Action.async(parse.multipartFormData) { implicit request =>
    val threadNum = 2
    val missionName = Tool.generateMissionName
    val data = formTool.missionForm.bindFromRequest().get
    val tmpDir = Tool.createTempDirectory("tmpDir")
    val myTmpDir = Tool.getDataDir(tmpDir)
    val myMessage = FileTool.fileCheck(myTmpDir)
    if (myMessage.valid) {
      val row = MissionRow(0, s"${missionName}", data.kind, new DateTime(), None, "preparing", threadNum)
      missionDao.insert(row).flatMap(_ => missionDao.selectByMissionName(row.missionName)).flatMap { mission =>
        val outDir = Tool.getUserMissionDir
        val missionDir = MissionUtils.getMissionDir(mission.id, outDir)
        val (workspaceDir, resultDir) = (missionDir.workspaceDir, missionDir.resultDir)
        FileUtils.copyDirectory(myTmpDir.tmpDir, workspaceDir)
        Tool.deleteDirectory(myTmpDir.tmpDir)
        val newMission = mission.copy(state = "wait")
        missionDao.update(newMission).map { x =>
          val json = Json.obj("missionId" -> newMission.id, "missionName" -> newMission.missionName)
          val base64Key = Json.stringify(json).base64Str
          Ok(Json.obj("valid" -> true, "key" -> base64Key))
        }
      }
    } else {
      Tool.deleteDirectory(myTmpDir.tmpDir)
      Future.successful(Ok(Json.obj("valid" -> myMessage.valid, "message" -> myMessage.message)))
    }


  }

  def resultBefore = Action { implicit request =>
    val data = formTool.keyForm.bindFromRequest().get
    Ok(views.html.result(data.key))
  }

  def updateMissionSocket = WebSocket.accept[JsValue, JsValue] {
    implicit request =>
      case class MissionAction(missionId: Int, action: String)
      ActorFlow.actorRef(out => Props(new Actor {
        override def receive: Receive = {
          case msg: JsValue if (msg \ "key").asOpt[String].nonEmpty =>
            val key = (msg \ "key").as[String]
            val missionId = Tool.getMissionIdByKey(key)
            system.scheduler.scheduleOnce(0 seconds, self, MissionAction(missionId, "update"))
          case MissionAction(missionId, action) =>
            missionDao.selectByMissionId(missionId).map {
              mission =>
                out ! Json.obj("state" -> mission.state)
                if (!List("success", "error").contains(mission.state)) {
                  system.scheduler.scheduleOnce(3 seconds, self, MissionAction(missionId, "update"))
                }
            }
          case _ =>
            self ! PoisonPill
        }

        override def postStop(): Unit = {
          self ! PoisonPill
        }
      }))

  }

  def downloadResult = Action.async {
    implicit request =>
      val data = formTool.keyForm.bindFromRequest().get
      val missionId = Tool.getMissionIdByKey(data.key)
      missionDao.selectByMissionId(missionId).map {
        mission =>
          val missionIdDir = Tool.getMissionIdDir(missionId)
          val resultDir = new File(missionIdDir, "result")
          val resultFile = new File(missionIdDir, s"result.zip")
          if (!resultFile.exists()) ZipUtil.pack(resultDir, resultFile)
          Ok.sendFile(resultFile).withHeaders(
            //            CACHE_CONTROL -> "max-age=3600",
            CONTENT_DISPOSITION -> WebTool.getContentDisposition(s"${mission.missionName}_result.zip"),
            CONTENT_TYPE -> "application/x-download"
          )
      }
  }

  def downloadLog = Action.async {
    implicit request =>
      val data = formTool.keyForm.bindFromRequest().get
      val missionId = Tool.getMissionIdByKey(data.key)
      missionDao.selectByMissionId(missionId).map {
        mission =>
          val missionIdDir = Tool.getMissionIdDir(missionId)
          val logFile = new File(missionIdDir, s"log.txt")
          Ok.sendFile(logFile).withHeaders(
            //            CACHE_CONTROL -> "max-age=3600",
            CONTENT_DISPOSITION -> WebTool.getContentDisposition(s"${mission.missionName}_log.txt"),
            CONTENT_TYPE -> "application/x-download"
          )
      }
  }

  def getMissionState = Action.async { implicit request =>
    val data = formTool.keyForm.bindFromRequest().get
    val missionId = Tool.getMissionIdByKey(data.key)
    missionDao.selectByMissionId(missionId).map { mission =>
      Ok(Json.obj("state" -> mission.state))
    }
  }


}
