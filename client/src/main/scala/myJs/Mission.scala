package myJs

import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import myJs.Utils._
import myJs.myPkg._
import myJs.myPkg.Implicits._
import org.scalajs.dom.FormData
import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLFormElement, XMLHttpRequest}

import scala.scalajs.js.JSON
import scalajs.js.JSConverters._
import org.scalajs.dom._
import org.scalajs.jquery.jQuery
import org.scalajs.jquery.JQueryAjaxSettings
import myJs.myPkg.jquery.jquery._

@JSExportTopLevel("Mission")
object Mission {

  @JSExport("init")
  def init = {

    bootStrapValidator
    kindChangeByValue("waters")

  }

  @JSExport("kindChange")
  def kindChange(y: Element) = {
    val myValue = $(y).find(">option:selected").`val`().toString
    kindChangeByValue(myValue)
  }

  def kindChangeByValue(myValue: String) = {
    val url = g.jsRoutes.controllers.AppController.downloadExampleData().url.toString
    val filePrefix = myValue
    $("#dataFileA").attr("href", s"${url}?fileName=${filePrefix}_data.zip")
    $("#sampleFileA").attr("href", s"${url}?fileName=${filePrefix}_sample_config.xlsx")
    $("#compoundFileA").attr("href", s"${url}?fileName=${filePrefix}_compound_config.xlsx")
  }

  @JSExport("myRun")
  def myRun = {
    val bv = jQuery("#form").data("bootstrapValidator")
    bv.validate()
    val valid = bv.isValid().asInstanceOf[Boolean]
    if (valid) {
      val formData = new FormData(document.getElementById("form").asInstanceOf[HTMLFormElement])
      jQuery(":disabled").attr("disabled", false)
      val element = div(id := "content",
        span(id := "info", "Running",
          span(id := "progress", "。。。")), " ",
        img(src := "/assets/images/running2.gif", cls := "runningImage")
      ).render
      val layerOptions = LayerOptions.title(zhInfo).closeBtn(0).skin("layui-layer-molv").btn(js.Array())
      val index = layer.alert(element, layerOptions)
      val url = g.jsRoutes.controllers.MissionController.newMission().url.toString
      val xhr = new XMLHttpRequest
      xhr.open("post", url)
      xhr.upload.onprogress = progressHandlingFunction
      xhr.onreadystatechange = (e) => {
        if (xhr.readyState == XMLHttpRequest.DONE) {
          val data = xhr.response
          val rs = JSON.parse(data.toString).asInstanceOf[js.Dictionary[js.Any]]
          layer.close(index)
          val valid = rs("valid").asInstanceOf[Boolean]
          if (valid) {
            clearFile
            val base64Key = rs.myGet("key")
            val url = s"${g.jsRoutes.controllers.MissionController.resultBefore().url.toString}?key=${base64Key}"
            window.open(target = "_blank").location.href = url
          } else {
            g.swal("Error", rs.myGet("message"), "error")
          }
        }
      }
      xhr.send(formData)
    }
  }

  def clearFile = {
    g.$(":input[name='compoundConfigFile']").fileinput("clear")
    g.$(":input[name='sampleConfigFile']").fileinput("clear")
    g.$(":input[name='dataFile']").fileinput("clear")
    g.$("#form").bootstrapValidator("revalidateField", "compoundConfigFile")
    g.$("#form").bootstrapValidator("revalidateField", "sampleConfigFile")
    g.$("#form").bootstrapValidator("revalidateField", "dataFile")
  }

  def bootStrapValidator = {
    val maxNumber = Double.MaxValue
    val dict = js.Dictionary(
      "feedbackIcons" -> js.Dictionary(
        "valid" -> "glyphicon glyphicon-ok",
        "invalid" -> "glyphicon glyphicon-remove",
        "validating" -> "glyphicon glyphicon-refresh",
      ),
      "fields" -> js.Dictionary(
        "dataFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "Data file is required!"
            ),
            "file" -> js.Dictionary(
              "message" -> "The format of data file is invalid!",
              "extension" -> "zip",
            ),
          )
        ),
        "sampleConfigFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "Sample info config file is required!"
            ),
            "file" -> js.Dictionary(
              "message" -> "The format of sample info config file is invalid!",
              "extension" -> "xlsx",
            ),
          )
        ),
        "compoundConfigFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "Compound info config file is required!"
            ),
            "file" -> js.Dictionary(
              "message" -> "The format of compound info config file is invalid!",
              "extension" -> "xlsx",
            ),
          )
        ),

      )
    )
    g.$("#form").bootstrapValidator(dict)

  }


}
