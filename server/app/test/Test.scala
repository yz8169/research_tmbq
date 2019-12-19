package test


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.apache.commons.codec.binary.Base64

/**
 * Created by Administrator on 2019/12/17
 */
object Test {

  def main(args: Array[String]): Unit = {
    val base64=Base64.encodeBase64String("1159".getBytes)
    println(base64)

  }

}
