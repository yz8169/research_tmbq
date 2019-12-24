package test


import java.io.{BufferedWriter, File, FileWriter}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.apache.commons.codec.binary.Base64
import tool.{HmdbXmlHandler, Tool}
import implicits.Implicits._
import models.Tables.MissionRow
import org.joda.time.DateTime

/**
 * Created by Administrator on 2019/12/17
 */
object Test {

  def main(args: Array[String]): Unit = {

    //    val parent = new File("D:\\hmdb")
    //    val file = new File(parent, "hmdb_metabolites.xml")
    //    val hmdbInfos = (new HmdbXmlHandler(file)).init
    //    val outFile = new File(parent, "hmdb_info.txt")
    //    val bw = new BufferedWriter(new FileWriter(outFile), 16384)
    //    val headers = List("accession", "name", "iupac_name", "traditional_iupac", "chemical_formula", "average_molecular_weight",
    //      "monisotopic_molecular_weight", "super_class", "kegg_id", "pubchem_compound_id", "synonyms", "pathways")
    //    bw.write(headers.mkString("\t") + "\n")
    //
    //    hmdbInfos.orderGroupBy(_.accession).foreach { case (access, info) =>
    //      val columns = access :: headers.drop(1).map { kind =>
    //        info.filter(_.kind == kind).map(_.value).mkString(" || ")
    //      }
    //      val line = columns.mkString("\t")
    //      bw.write(line + "\n")
    //    }
    //    bw.close()


  }

}
