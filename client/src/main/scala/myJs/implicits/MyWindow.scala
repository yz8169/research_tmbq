package myJs.implicits

import org.scalajs.dom
import org.scalajs.dom.window

/**
 * Created by Administrator on 2019/12/17
 */
trait MyWindowTool {

  implicit class MyWindow(window: dom.Window) {

    def openNewWindow(url: String) = {
      window.open(target = "_blank").location.href = url
    }

    def redirect(url: String) = {
      window.location.href = url
    }

  }

}
