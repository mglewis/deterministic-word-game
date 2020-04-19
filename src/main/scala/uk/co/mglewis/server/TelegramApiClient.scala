package uk.co.mglewis.server

import java.net.URLEncoder

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.inject.Logging
import com.twitter.util.Future

class TelegramApiClient(
  botApiKey: String
) extends Logging {

  private val httpClient = Http
    .client
    .withSessionQualifier.noFailureAccrual
    .withTls("api.telegram.org")
    .newService("api.telegram.org:443")

  def sendMessage(
    chatId: Int,
    message: String
  ): Future[Response] = {
    val encodedMessage = URLEncoder.encode(message, "UTF-8")
    val url = s"https://api.telegram.org/bot$botApiKey/sendMessage?chat_id=$chatId&text=$encodedMessage"
    info(s"target url: $url")
    httpClient(Request(Method.Get, url))
  }

}
