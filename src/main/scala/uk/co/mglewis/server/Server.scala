package uk.co.mglewis.server

import java.util.Properties

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter
import uk.co.mglewis.core.Dictionary

import scala.io.Source

object ServerMain extends Server

class Server extends HttpServer {

  private val properties = {
    val props = new Properties()
    props.load(Source.fromFile("resources/properties").bufferedReader)
    props
  }

  private val secretPath: String = properties.getProperty("TELEGRAM_SECRET_API_PATH")
  private val botApiKey = properties.getProperty("TELEGRAM_API_KEY")

  private val apiClient = new TelegramApiClient(botApiKey)
  private val dictionary = new Dictionary("resources/word_list.txt")

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add(new GameController(secretPath, apiClient, dictionary))
  }

}
