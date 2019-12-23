package tool

import java.io.File

import utils.Utils

import scala.xml.Utility
import scala.xml.pull.{EvElemEnd, EvElemStart, EvEntityRef, EvText}
import implicits.Implicits._

/**
 * Created by Administrator on 2019/8/8
 */
class HmdbXmlHandler(override val file: File) extends XmlHandler {

  val startTime = System.currentTimeMillis()

  case class HmdbInfo(accession: String, kind: String, value: String)

  def init = {
    //    println(xml.next())
    parse
  }

  def parse: Vector[HmdbInfo] = {
    def loop(currNode: List[String], beforeAccession: String, acc: Vector[HmdbInfo]): Vector[HmdbInfo] = {
      if (xml.hasNext) {
        xml.next match {
          case EvElemStart(_, label, _, _) =>
            val newNode = label :: currNode
            loop(newNode, beforeAccession, acc)
          case EvElemEnd(_, label) =>
            loop(currNode.tail, beforeAccession, acc)
          case EvText(text) =>
            val (curAccession, newAcc) = textDeal(text, currNode, beforeAccession, acc)
            val i = acc.size
            if (curAccession != beforeAccession && i % 500 == 0) {
              val accSize = acc.map(_.accession).distinct.size
              println(s"${accSize}\t${Utils.getTime(startTime)}")
            }
            loop(currNode, curAccession, newAcc)
          case EvEntityRef(e) =>
            val (curAccession, newAcc) = textDeal(Utility.Escapes.pairs(e).toString, currNode, beforeAccession, acc)
            loop(currNode, curAccession, newAcc)
          case _ => loop(currNode, beforeAccession, acc)
        }
      } else acc
    }

    val hmdbInfos = loop(List.empty, "", Vector[HmdbInfo]())
    close
    hmdbInfos.filter(x => x.accession.nonEmpty)
  }

  def textDeal(text: String, currNode: List[String], beforeAccession: String, acc: Vector[HmdbInfo]) = {

    currNode match {
      case List("accession", "metabolite", "hmdb") =>
        val accession = text.trim
        val newAccession = if (accession != beforeAccession) {
          accession
        } else beforeAccession
        (newAccession, acc)
      case List("name", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "name", text.trim.replaceLf))
      case List("iupac_name", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "iupac_name", text.trim.replaceLf))
      case List("traditional_iupac", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "traditional_iupac", text.trim.replaceLf))
      case List("synonym", "synonyms", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "synonyms", text.trim.replaceLf))
      case List("pubchem_compound_id", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "pubchem_compound_id", text.trim.replaceLf))
      case List("kegg_id", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "kegg_id", text.trim.replaceLf))
      case List("chemical_formula", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "chemical_formula", text.trim.replaceLf))
      case List("average_molecular_weight", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "average_molecular_weight", text.trim.replaceLf))
      case List("monisotopic_molecular_weight", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "monisotopic_molecular_weight", text.trim.replaceLf))
      case List("super_class", "taxonomy", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "super_class", text.trim.replaceLf))
      case List("name", "pathway", "pathways", "biological_properties", "metabolite", "hmdb") =>
        (beforeAccession, acc :+ HmdbInfo(beforeAccession, "pathways", text.trim.replaceLf))
      case _ => (beforeAccession, acc)
    }

  }


}
