package uk.co.mglewis.server

import java.util.Properties

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

import scala.io.Source

object ServerMain extends Server

class Server extends HttpServer {

  val properties: Properties = {
    val props = new Properties()
    props.load(Source.fromFile("resources/properties").bufferedReader)
    props
  }

  val secretPath = properties.getProperty("TELEGRAM_SECRET_API_PATH")
  val botApiKey = properties.getProperty("TELEGRAM_API_KEY")

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add(new GameController(secretPath))
  }


}
