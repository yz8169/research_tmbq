package myJs

import myJs.myPkg.{BootstrapTableJQueryImplicits, Layer, LayerOptions}
import scalatags.Text.all._
import org.scalajs.dom.raw.ProgressEvent

import scala.scalajs.js

import scala.math.BigDecimal.RoundingMode
import org.scalajs.jquery.jQuery
import org.scalajs.jquery.JQuery


/**
  * Created by yz on 2019/2/26
  */
object Utils {

  implicit class MyJson(val json: js.Dictionary[js.Any]) {

    def myGet(key: String) = json.getOrElse(key, "NA").toString

    def myGetInt(key: String) = json(key).toString.toInt

  }

  implicit class MyJsArray(val array: js.Array[js.Dictionary[js.Any]]) {
    val emptyDic: js.Dictionary[js.Any] = js.Dictionary()

    def myHead = array.headOption.getOrElse(emptyDic)

  }

  implicit class BootstrapTableJQuery(val jq: JQuery) extends AnyVal {

  }

  val g = js.Dynamic.global
  val layer = g.layer.asInstanceOf[Layer]
  val zhInfo="Info"

  val layerOptions = LayerOptions.title(zhInfo).closeBtn(0).skin("layui-layer-molv").btn(js.Array())

  val element = div()(
    span()("Running..."),
    " ",
    img(src := "/assets/images/running2.gif",
      width := 30,
      height := 20
    )()
  ).render

  val dataToggle=attr("data-toggle")
  val dataContent=attr("data-content")
  val dataContainer=attr("data-container")
  val dataPlacement=attr("data-placement")
  val dataHtml=attr("data-html")
  val dataAnimation=attr("data-animation")
  val dataTrigger=attr("data-trigger")

  def progressHandlingFunction = {
    (e: ProgressEvent) => {
      if (e.lengthComputable) {
        val percent = e.loaded / e.total * 100
        val newPercent = BigDecimal(percent).setScale(2, RoundingMode.HALF_UP)
        jQuery("#progress").html(s"(${newPercent}%)")
        if (percent >= 100) {
          jQuery("#info").text("正在运行")
          jQuery("#progress").html("")
        }
      }
    }

  }





}
