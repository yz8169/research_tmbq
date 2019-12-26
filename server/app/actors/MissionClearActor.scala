package actors

import akka.actor.{Actor, ActorSystem, Props, Timers}
import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import models.Tables._
import org.joda.time.{DateTime, Days}
import tool.Pojo._
import tool._
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import implicits.Implicits._

/**
 * Created by Administrator on 2019/10/24
 */
@Singleton
class MissionClearActor @Inject()()(implicit val system: ActorSystem,
                                    implicit val materializer: Materializer,
                                    implicit val dao: MyDao

) extends Actor with Timers {

  timers.startPeriodicTimer("timer", "ask", 1 days)
  implicit val configDao = dao.configDao
  implicit val missionDao = dao.missionDao


  override def receive: Receive = {
    case "ask" =>
      missionDao.selectAll.map { missions =>
        val needCleanMissions = missions.filter { mission =>
          val startTime = mission.startTime
          val now = new DateTime()
          val time = Days.daysBetween(startTime, now)
          val saveDays = Tool.getSaveDays
          time.getDays >= saveDays
        }
        val ids = needCleanMissions.map(_.id)
        missionDao.deleteAll(ids).map { x =>
          ids.foreach { id =>
            val missionIdDir = Tool.getMissionIdDir(id)
            missionIdDir.deleteQuietly
          }
        }
      }

  }
}
