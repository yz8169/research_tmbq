package utils

import java.io.{File, FileInputStream, FileOutputStream}
import java.lang.reflect.Field
import java.nio.file.Files
import java.text.SimpleDateFormat

import javax.imageio.ImageIO
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.batik.transcoder.{TranscoderInput, TranscoderOutput}
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang3.StringUtils
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.poi.ss.usermodel.{Cell, DateUtil}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime
import tool.Pojo.{DiffMethodData, MyConfig}
//import org.apache.commons.math3.stat.StatUtils
//import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
//import org.saddle.io._
//import CsvImplicits._
//import javax.imageio.ImageIO
import org.apache.commons.codec.binary.Base64
//import org.apache.pdfbox.pdmodel.PDDocument
//import org.apache.pdfbox.rendering.PDFRenderer

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.math.log10
import implicits.Implicits._


object Utils {

  def isWindows = {
    System.getProperty("os.name") match {
      case x if x.contains("Windows") => true
      case _ => false
    }
  }

  val Rscript = {
    "Rscript"
  }

  def deleteDirectory(direcotry: File) = {
    try {
      FileUtils.deleteDirectory(direcotry)
    } catch {
      case _ =>
    }
  }

  def getTime(startTime: Long) = {
    val endTime = System.currentTimeMillis()
    (endTime - startTime) / 1000.0
  }

  def spentTime(f: () => Unit) = {
    val startTime = System.currentTimeMillis()
    f()
    val time = getTime(startTime)
    println(time)
  }

  def execFuture[T](f: Future[T]): T = {
    Await.result(f, Duration.Inf)
  }

  def getValue[T](kind: T, noneMessage: String = "暂无"): String = {
    kind match {
      case x if x.isInstanceOf[DateTime] => val time = x.asInstanceOf[DateTime]
        time.toString("yyyy-MM-dd HH:mm:ss")
      case x if x.isInstanceOf[Option[T]] => val option = x.asInstanceOf[Option[T]]
        if (option.isDefined) getValue(option.get, noneMessage) else noneMessage
      case x if x.isInstanceOf[Seq[T]] => val list = x.asInstanceOf[Seq[T]]
        list.mkString(";")
      case _ => kind.toString
    }
  }

  def getValues[T](kind: T, noneMessage: String = "暂无", field: Field): List[String] = {
    kind match {
      case x if x.isInstanceOf[DateTime] => val time = x.asInstanceOf[DateTime]
        val line = List(field.getName, time.toString("yyyy-MM-dd HH:mm:ss")).mkString(",")
        List(line)
      case x if x.isInstanceOf[Option[T]] => val option = x.asInstanceOf[Option[T]]
        if (option.isDefined) getValues(option.get, noneMessage, field) else {
          List[String]()
        }
      case x if x.isInstanceOf[Seq[T]] => val list = x.asInstanceOf[Seq[T]]
        val line = List(field.getName, list.mkString(";")).mkString(",")
        List(line)
      case x if x.isInstanceOf[MyConfig] => val myConfig = x.asInstanceOf[MyConfig]
        myConfig.getClass.getDeclaredFields.toList.map { x: Field =>
          x.setAccessible(true)
          val kind = x.get(myConfig)
          val value = getValue(kind, "")
          List(s"${field.getName}.${x.getName}", value).mkString(",")
        }.filter(_.split(",").size >= 2)
      case _ =>
        val line = List(field.getName, kind.toString).mkString(",")
        List(line)
    }
  }


  def getArrayByTs[T](x: Seq[T]) = {
    x.map { y =>
      y.getClass.getDeclaredFields.toBuffer.map { x: Field =>
        x.setAccessible(true)
        val kind = x.get(y)
        val value = getValue(kind)
        (x.getName, value)
      }.init.toMap
    }
  }

  def getLinesByT[T](y: T)(implicit sep: String = ",") = {
    y.getClass.getDeclaredFields.toList.flatMap { x: Field =>
      x.setAccessible(true)
      val kind = x.get(y)
      getValues(kind, "", x)
    }.notEmptyLines
  }

  def pdf2png(tmpDir: File, fileName: String) = {
    val pdfFile = new File(tmpDir, fileName)
    val outFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".png"
    val outFile = new File(tmpDir, outFileName)
    val document = PDDocument.load(pdfFile)
    val renderer = new PDFRenderer(document)
    ImageIO.write(renderer.renderImage(0, 3), "png", outFile)
    document.close()
  }


  def svg2png(svgFile: File, pngFile: File) = {
    val input = new TranscoderInput(svgFile.toURI.toString)
    val outStream = new FileOutputStream(pngFile)
    val output = new TranscoderOutput(outStream)
    val t = new PNGTranscoder()
    t.transcode(input, output)
    outStream.flush()
    outStream.close()
  }

  def createAndReturnDir(dir: File, subDirName: String) = {
    new File(dir, subDirName).createDirectoryWhenNoExist
  }


}
