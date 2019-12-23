package tool

import java.io.File

import scala.io.Source
import scala.xml.pull._

/**
 * Created by Administrator on 2019/8/8
 */

trait XmlHandler {

  val file: File

  lazy val src = Source.fromFile(file, "UTF-8")
  lazy val xml = {
    new XMLEventReader(src)
  }

  def close = {
    src.close()
  }

}
