package uk.co.mglewis.server

import java.net.URLEncoder

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.inject.Logging
import com.twitter.util.{Await, Future}

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
  ): Response = {
    val encodedMessage = URLEncoder.encode(message, "UTF-8")
    val url = s"https://api.telegram.org/bot$botApiKey/sendMessage?chat_id=$chatId&text=$encodedMessage"
    Await.result(httpClient(Request(Method.Get, url)))
  }

  def sendAnimation(
    chatId: Int,
    gifUrl: String
  ): Response = {
    val encodedUrl = URLEncoder.encode(gifUrl, "UTF-8")
    val url = s"https://api.telegram.org/bot$botApiKey/sendAnimation?chat_id=$chatId&animation=$encodedUrl"
    Await.result(httpClient(Request(Method.Get, url)))
  }

}
