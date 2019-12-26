package tool

import java.security.Security
import java.util.Properties

import javax.mail.{Message, Session}
import javax.mail.internet.{InternetAddress, MimeMessage, MimeUtility}

import javax.mail.{Message, Session}
import javax.mail.internet.{InternetAddress, MimeMessage, MimeUtility}
import implicits.Implicits._

/**
 * Created by Administrator on 2019/12/23
 */
object EmailTool {

  case class Sender(nick: String, host: String, email: String, password: String)

  case class Info(subject: String, content: String)


  def sendEmailBySsl(sender: Sender, info: Info, inbox: String) = {
    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider())
    val SSL_FACTORY = "javax.net.ssl.SSLSocketFactory"
    props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY)
    props.setProperty("mail.smtp.socketFactory.fallback", "false")
    props.setProperty("mail.smtp.port", "465")
    props.setProperty("mail.smtp.socketFactory.port", "465")
    val mailSession = Session.getDefaultInstance(props)
    val transport = mailSession.getTransport("smtp")
    val message = new MimeMessage(mailSession)
    val nick = MimeUtility.encodeText(sender.nick)
    message.setSubject(info.subject)
    message.setFrom(new InternetAddress(s"${nick}<${sender.email}>"))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(inbox))
    message.setContent(info.content, "text/html;charset=utf-8")
    transport.connect(sender.host, sender.email, sender.password)
    transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
    transport.close()
  }

  def send163EmailBySsl(sender: Sender, info: Info, inbox: String) = {
    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider())
    val SSL_FACTORY = "javax.net.ssl.SSLSocketFactory"
    props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY)
    props.setProperty("mail.smtp.socketFactory.fallback", "false")
    props.setProperty("mail.smtp.port", "465")
    props.setProperty("mail.smtp.socketFactory.port", "465")
    val mailSession = Session.getDefaultInstance(props)
    val transport = mailSession.getTransport("smtp")
    val message = new MimeMessage(mailSession)
    val nick = MimeUtility.encodeText(sender.nick)
    message.setSubject(info.subject)
    message.setFrom(new InternetAddress(s"${nick}<${sender.email}>"))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(inbox))
    message.setContent(info.content, "text/html;charset=utf-8")
    transport.connect(sender.host, sender.email, sender.password)
    transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
    transport.close()
  }


}
