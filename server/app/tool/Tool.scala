package tool

import java.io.{File, FileOutputStream}
import java.nio.file.Files
import java.util.concurrent.ForkJoinPool

import dao.ModeDao
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import utils.Utils

import scala.jdk.CollectionConverters._
import implicits.Implicits._
import org.apache.commons.lang3.StringUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import tool.Pojo.{CommandData, IndexData}

import scala.collection.{SeqMap}
import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.parallel.CollectionConverters._

/**
 * Created by Administrator on 2019/12/5
 */
object Tool {

  val dbName = "research_tmbq_database"
  val windowsPath = s"D:\\${dbName}"
  val playPath = new File("../").getAbsolutePath
  val linuxPath = playPath + s"/${dbName}"
  val isWindows = {
    if (new File(windowsPath).exists()) true else false
  }
  val path = {
    if (new File(windowsPath).exists()) windowsPath else linuxPath
  }
  val windowsTestDir = new File("G:\\temp")
  val linuxTestDir = new File(playPath, "workspace")
  val testDir = if (windowsTestDir.exists()) windowsTestDir else linuxTestDir
  val exampleDir = new File(path, "example")
  val userDir = new File(path, "user")

  def getInfoByFile(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    val columnNames = lines.head.split("\t").drop(1)
    val array = lines.drop(1).map { line =>
      val columns = line.split("\t")
      val map = Map("geneId" -> columns(0))
      val otherMap = columnNames.zip(columns.drop(1)).map { case (columnName, data) =>
        (columnName -> data)
      }.toMap
      map ++ otherMap
    }
    (columnNames, array)
  }

  def generateMissionName = {
    (new DateTime).toString("yyyy_MM_dd_HH_mm_ss")
  }

  def createTempDirectory(prefix: String)(implicit modeDao: ModeDao) = {
    if (isTestMode) Tool.testDir else Files.createTempDirectory(prefix).toFile
  }

  def isTestMode(implicit modeDao: ModeDao) = {
    val mode = Utils.execFuture(modeDao.select)
    if (mode.test == "t") true else false
  }


  def deleteDirectory(direcotry: File)(implicit modeDao: ModeDao) = {
    if (!isTestMode) Utils.deleteDirectory(direcotry)
  }

  def getUserMissionDir = {
    new File(Tool.userDir, "mission")
  }

  def getMissionIdDir(missionId: Int) = {
    val missionDir = getUserMissionDir
    new File(missionDir, missionId.toString)
  }

  def getMissionWorkspaceDir(missionId: Int) = {
    val missionIdDir = getMissionIdDir(missionId)
    new File(missionIdDir, "workspace")
  }

  def getMissionResultDir(missionId: Int) = {
    val missionIdDir = getMissionIdDir(missionId)
    new File(missionIdDir, "result")
  }

  def getCompoundDatas(compoundConfigFile: File) = {
    val compoundLines = compoundConfigFile.xlsxLines()
    case class CompoundData(name: String, function: String, mz: Double, index: Int)
    val headers = compoundLines.head.split("\t").map(_.toLowerCase)
    val maps = compoundLines.drop(1).map { line =>
      val columns = line.split("\t")
      headers.zip(columns).toMap
    }
    val mzMap = maps.map { map =>
      val mzs = map("mass").split(">")
      val mz1 = if (mzs.size == 1) 0.0 else mzs(1).toDouble
      val function = map("function")
      ((function, mzs(0).toDouble), mz1)
    }.distinct.groupBy(_._1).mapValues(x => x.map(_._2).sorted)
    maps.map { map =>
      val name = map("compound")
      val function = map("function")
      val mzs = map("mass").split(">")
      val mz = mzs(0).toDouble
      val index = if (mzs.size == 1) {
        0
      } else {
        val mz1 = mzs(1).toDouble
        mzMap((function, mz)).indexOf(mz1)
      }
      CompoundData(name, function, mz, index)
    }
  }

  def getFtMap(file: File, functions: List[String]) = {
    val lines = FileUtils.readLines(file).asScala

    val acc = Vector[(String, String)]()
    val map = lines.foldLeft((acc, "")) { (info, line) =>
      if (line.startsWith("FUNCTION")) {
        (info._1, line)
      } else {
        val key = info._2
        (info._1 :+ (key, line), key)
      }
    }.accGroupMap

    val ftMap = map.withFilter(x => functions.contains(x._1)).map { case (key, lines) =>
      val acc = Vector[((Double, Double), Double)]()
      val map = lines.filterNot(StringUtils.isBlank(_)).filterNot(_.startsWith("Scan")).
        foldLeft((acc, 0.0)) { (info, line) =>
          if (line.startsWith("Retention Time")) {
            val timeKey = line.split("\t")(1).toDouble
            (info._1, timeKey)
          } else {
            val timeKey = info._2
            val columns = line.split("\t")
            val t = (columns(0).toDouble, timeKey)
            val value = columns(1).toDouble
            val curT = (t, value)
            (info._1 :+ curT, timeKey)
          }
        }.accGroupMap
      (key, map)
    }

    ftMap
  }

  def productDtaFiles(tmpDir: File, compoundConfigFile: File, dataDir: File, threadNum: Int) = {
    val dtaDir = new File(tmpDir, "dta")
    dtaDir.createDirectoryWhenNoExist
    val compounds = getCompoundDatas(compoundConfigFile)
    val functions = compounds.map(x => s"FUNCTION ${x.function}")
    val files = dataDir.listFiles()
    val finalThreadNum = threadNum
    val map = files.zipWithIndex.map { case (v, i) =>
      val j = (i % finalThreadNum) + 1
      (j, v)
    }.groupBy(_._1).mapValues(_.map(_._2))
    val f = map.map { case (i, files) =>
      Future {
        files.foreach { file =>
          val ftMap = getFtMap(file, functions)
          compounds.foreach { compound =>
            val function = s"FUNCTION ${compound.function}"
            val headers = (s"#SEC\tMZ\tINT")
            val trueMz = ftMap(function).map { case ((mz, time), value) =>
              mz
            }.toBuffer.distinct.sortBy { x: Double => (compound.mz - x).abs }.head
            val ftLines = ftMap(function).filter { case ((mz, time), value) =>
              mz == trueMz
            }.map { case ((mz, time), values) =>
              s"${time}\t${mz}\t${values(compound.index)}"
            }.toList
            val newLines = headers :: ftLines
            val dir = new File(dtaDir, compound.name)
            dir.createDirectoryWhenNoExist
            val prefix = file.namePrefix
            FileUtils.writeLines(new File(dir, s"${prefix}.dta"), newLines.asJava)
          }
        }
      }
    }.toBuffer.reduceLeft(_ zip _ map (x => ()))
    Utils.execFuture(f)
  }

  val rPath = {
    val rPath = "C:\\workspaceForIDEA\\tmbq_scala_js\\server\\rScripts"
    val linuxRPath = linuxPath + "/rScripts"
    if (new File(rPath).exists()) rPath else linuxRPath
  }

  def isFindPeak(tmpDir: File, isIndexs: Seq[IndexData], threadNum: Int) = {
    val isIndexPar = isIndexs.par.zipWithIndex
    isIndexPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    val commandsPar = isIndexPar.map { case (indexData, i) =>
      val fileName = s"is_${i}"
      val isDir = new File(tmpDir, fileName).createDirectoryWhenNoExist
      val compoundNameFile = new File(isDir, "compoundName.xlsx")
      val newLines = List("CompoundName", indexData.compoundName)
      newLines.toXlsxFile(compoundNameFile)
      val command =
        s"""
           |Rscript ${new File(Tool.rPath, "isFindPeak.R").unixPath} --ci ${compoundNameFile.getName} --co color.txt --io intensity.txt
           """.stripMargin
      CommandData(isDir, List(command))
    }
    commandsPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    commandsPar

  }

  def isMerge(tmpDir: File, isIndexs: Seq[IndexData]) = {
    val command = if (isIndexs.isEmpty) {
      ""
    } else {
      s"""
         |Rscript ${new File(Tool.rPath, "is_rt_merge.R").unixPath}
           """.stripMargin
    }
    CommandData(tmpDir, List(command))

  }

  def cFindPeak(tmpDir: File, indexDatas: Seq[IndexData], threadNum: Int) = {
    val cIndexs = indexDatas.filter(x => !x.index.startWithsIgnoreCase("is"))
    val cIndexPar = cIndexs.par.zipWithIndex
    cIndexPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    val commandsPar = cIndexPar.map { case (indexData, i) =>
      val fileName = s"c_${i}"
      val cDir = new File(tmpDir, fileName).createDirectoryWhenNoExist
      val compoundNameFile = new File(cDir, "compoundName.xlsx")
      val newLines = List("CompoundName", indexData.compoundName)
      newLines.toXlsxFile(compoundNameFile)
      val command =
        s"""
           |Rscript ${new File(Tool.rPath, "c_findPeak.R").unixPath} --ci ${compoundNameFile.getName} --co color.txt --io intensity.txt
           """.stripMargin
      CommandData(cDir, List(command))
    }
    commandsPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    commandsPar

  }

  def intensityMerge(tmpDir: File) = {
    val command =
      s"""
         |Rscript ${new File(Tool.rPath, "intensity_merge.R").unixPath}
           """.stripMargin
    CommandData(tmpDir, List(command))

  }

  def eachRegress(tmpDir: File, threadNum: Int) = {
    val dirs = tmpDir.listFiles().filter(_.isDirectory).
      filter(x => x.getName.startsWith("is_") || x.getName.startsWith("c_")).toList
    val commandsPar = dirs.map { dir =>
      val command =
        s"""
           |Rscript ${new File(Tool.rPath, "each_regress.R").unixPath} --ci compoundName.xlsx  --coi color.txt --ro regress.txt
                          """.stripMargin
      CommandData(dir, List(command))
    }.par

    commandsPar.threadNum(threadNum)

  }

  def allMerge(tmpDir: File) = {
    val command =
      s"""
         |Rscript ${new File(Tool.rPath, "all_merge.R").unixPath}
           """.stripMargin
    CommandData(tmpDir, List(command))
  }

  def dye(file: File, colorFile: File, outFile: File) = {
    val outputWorkbook = new XSSFWorkbook()
    val outputSheet = outputWorkbook.createSheet("Sheet1")
    val format = outputWorkbook.createDataFormat()
    val lines = FileUtils.readLines(file).asScala
    val colorLines = FileUtils.readLines(colorFile).asScala.map(_.split("\t"))
    val xlsxStyle = new XlsxStyle(outputWorkbook)
    val yellowStyle = xlsxStyle.yellowStyle
    val redStyle = xlsxStyle.redStyle
    val greenStyle = xlsxStyle.greenStyle
    for (i <- 0 until lines.size) {
      val outputEachRow = outputSheet.createRow(i)
      val line = lines(i)
      val columns = line.split("\t")
      for (j <- 0 until columns.size) {
        val cell = outputEachRow.createCell(j)
        cell.setCellValue(columns(j))
        val bat = colorLines(i)(1)
        if (i > 0 && j > 1 && bat.isDouble) {
          if (columns(j).isDouble) {
            cell.setCellValue(columns(j).toDouble)
          }
          colorLines(i)(j) match {
            case "yellow" =>
              cell.setCellStyle(yellowStyle)
            case "red" => cell.setCellStyle(redStyle)
            case "green" => cell.setCellStyle(greenStyle)
            case _ =>
          }
        }

      }
    }
    val fileOutputStream = new FileOutputStream(outFile)
    outputWorkbook.write(fileOutputStream)
    fileOutputStream.close()
    outputWorkbook.close()
  }

  val availCpu = Runtime.getRuntime.availableProcessors() - 1


}
