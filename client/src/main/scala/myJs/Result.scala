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
import scala.concurrent.ExecutionContext.Implicits.global
import js.timers


import scala.concurrent.Promise

@JSExportTopLevel("Result")
object Result {

  @JSExport("init")
  def init(key: String) = {

    $.ajaxSetup(JQueryAjaxSettings.cache(false))

    updateMission(key)
  }

  def updateMission(key: String) = {
    updateMissionSocket(key)
  }


  def updateMissionSocket(key: String) = {
    val url = g.jsRoutes.controllers.MissionController.updateMissionSocket().url.toString
    val wsUri = s"ws://${window.location.host}${url}"
    webSocket(wsUri, key)
  }

  def dealMessage(data: js.Dictionary[String], key: String) = {
    if (data("state") == "success") {
      jQuery("#info").html("Task is completed!")
      val url = s"${g.jsRoutes.controllers.MissionController.downloadResult().url.toString}?key=${key}"
      window.redirect(url)
    } else if (List("running", "wait").contains(data("state"))) {
      val element = div()(
        span()("Task is Running"),
        " ",
        img(src := "/assets/images/running2.gif", cls := "runningImg")(),
        br,
        span()("If you have filled your email,you can close the window,we will send a message after the task is finished!"),
      ).render
      jQuery("#info").html(element)
    } else if (List("error").contains(data("state"))) {
      jQuery("#info").html("Task is failed!")
      val url = s"${g.jsRoutes.controllers.MissionController.downloadLog().url.toString}?key=${key}"
      window.redirect(url)
    } else if (List("deleted").contains(data("state"))) {
      jQuery("#info").html("Task has been cleaned up!")
    }
    val isContinue = List("running", "wait").contains(data("state"))
    isContinue
  }

  def webSocket(wsUri: String, key: String) = {
    val websocket = new WebSocket(wsUri)
    websocket.onopen = (evt) =>
      websocket.send(JSON.stringify(js.Dictionary("key" -> key)))
    websocket.onclose = (evt) =>
      println(s"ERROR:${evt.code},${evt.reason},${evt.wasClean}")
    websocket.onmessage = (evt) => {
      val message = evt.data
      val data = JSON.parse(message.toString).asInstanceOf[js.Dictionary[String]]
      dealMessage(data, key)
    }
    websocket.onerror = (evt) => {
      updateByHand(key)
      println(s"ERROR:${evt.toString}")
    }
  }

  def updateByHand(key: String) = {
    refreshMission(key)
  }

  @JSExport("refreshMission")
  def refreshMission(key: String): JQueryXHR = {
    val url = g.jsRoutes.controllers.MissionController.getMissionState().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?key=${key}").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      val rs = data.asInstanceOf[js.Dictionary[String]]
      val isContinue = dealMessage(rs, key)
      if (isContinue) {
        timers.setTimeout(3000) {
          refreshMission(key)
        }
      }
    }
    $.ajax(ajaxSettings)

  }


}
