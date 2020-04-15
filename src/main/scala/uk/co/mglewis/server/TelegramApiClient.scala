package uk.co.mglewis.server

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
    val url = s"https://api.telegram.org/bot$botApiKey/sendMessage?chat_id=$chatId&text=$message"
    info(url)
    httpClient(Request(Method.Get, url))
  }

}
