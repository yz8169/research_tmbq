package myJs

import myJs.Utils._
import myJs.implicits.Implicits._
import myJs.myPkg.jquery.jquery._
import myJs.myPkg.jquery.{JQueryAjaxSettings, JQueryXHR}
import org.scalajs.dom._
import org.scalajs.jquery.jQuery
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation._

@JSExportTopLevel("Manual")
object Manual {

  @JSExport("init")
  def init = {
    refreshArgs

  }

  @JSExport("refreshArgs")
  def refreshArgs = {
    val url = g.jsRoutes.controllers.AppController.getAllArgs().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(url).contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      val rs = data.asInstanceOf[js.Dictionary[js.Any]]
      val columnNames = rs("columnNames").asInstanceOf[js.Array[String]]
      val columns: js.Array[js.Dictionary[Any]] = columnNames.map { columnName =>
        js.Dictionary("field" -> columnName, "title" -> columnName, "sortable" -> true)
      }
      val dict = js.Dictionary("data" -> rs("array"), "columns" -> columns)
      g.$("#table").bootstrapTable(dict)
    }
    $.ajax(ajaxSettings)
  }

}
