package myJs

import myJs.Utils._
import myJs.myPkg._
import org.scalajs.dom.{FormData, document}
import org.scalajs.dom.raw.{HTMLFormElement, XMLHttpRequest}
import org.scalajs.jquery.{JQueryAjaxSettings, jQuery}
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation._
import org.scalajs.dom._
import implicits.Implicits._
import myJs.myPkg.jquery.{JQueryAjaxSettings, JQueryXHR}
import myJs.myPkg.jquery.jquery._

@JSExportTopLevel("Result")
object Result {

  @JSExport("init")
  def init(missionId: Int) = {
    updateMissionSocket(missionId)

  }

  def updateMissionSocket(missionId: Int) = {
    val url = g.jsRoutes.controllers.MissionController.updateMissionSocket().url.toString
    val wsUri = s"ws://${window.location.host}${url}"
    webSocket(wsUri, missionId)
  }

  def dealMessage(data: js.Dictionary[String], missionId: Int) = {
    if (data("state") == "success") {
      jQuery("#info").html("Task is completed!")
      val url = s"${g.jsRoutes.controllers.MissionController.downloadResult().url.toString}?missionId=${missionId}"
      window.openNewWindow(url)
    } else if (List("running", "wait").contains(data("state"))) {
      val element = div()(
        span()("Task is Running"),
        " ",
        img(src := "/assets/images/running2.gif", cls := "runningImg")()
      ).render
      jQuery("#info").html(element)
    } else if (List("error").contains(data("state"))) {
      jQuery("#info").html("Task is failed!")
      val url = s"${g.jsRoutes.controllers.MissionController.downloadLog().url.toString}?missionId=${missionId}"
      window.openNewWindow(url)
    }
  }

  def webSocket(wsUri: String, missionId: Int) = {
    val websocket = new WebSocket(wsUri)
    websocket.onopen = (evt) =>
      websocket.send(JSON.stringify(js.Dictionary("missionId" -> missionId)))
    websocket.onclose = (evt) =>
      println(s"ERROR:${evt.code},${evt.reason},${evt.wasClean}")
    websocket.onmessage = (evt) => {
      val message = evt.data
      val data = JSON.parse(message.toString).asInstanceOf[js.Dictionary[String]]
      dealMessage(data, missionId)
    }
    websocket.onerror = (evt) => {
      updateByHand(missionId)
      println(s"ERROR:${evt.toString}")
    }
  }

  def updateByHand(missionId: Int) = {
    js.timers.setInterval(3000) {
      refreshMission(missionId)
    }
  }

  @JSExport("refreshMission")
  def refreshMission(missionId: Int, f: () => Any = () => ()) = {
    val url = g.jsRoutes.controllers.MissionController.getMissionState().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?missionId=${missionId}").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      val rs = data.asInstanceOf[js.Dictionary[String]]
      dealMessage(rs, missionId)
      f()
    }
    $.ajax(ajaxSettings)

  }


}
