package uk.co.mglewis.server

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Version}
import com.twitter.finatra.http.Controller
import com.twitter.util.Await
import uk.co.mglewis.core.{Dictionary, GameState}
import uk.co.mglewis.datamodel.Player.Human

class GameController(
  secretPath: String,
  botApiKey: String
) extends Controller {

  private val httpClient = Http.client.newService("api.telegram.org")

  private val dictionary = new Dictionary("resources/word_list.txt")

  private var gameStates = Map.empty[User, GameState]

  get("/ping") { request: Request =>
    "Pong"
  }

  get(s"/$secretPath/ping") { request: Request =>
    "Secret pong."
  }

  post(s"/$secretPath/update") { request: Request =>
    val message = MessageSerializer.deserialize(request.contentString)
    val user = message.from

    require(!user.isBot, "Messages from bots are not supported")

    val messageRequest = if (!gameStates.contains(user)) {
      gameStates = gameStates + (user -> GameState.generateStartState(user.fullName, Human))

      Request(
        method = Method.Get,
        uri = s"https://api.telegram.org/bot$botApiKey/sendMessage?chat_id=${message.chat.id}&text=Hello!",
      )
    } else {
      Request(
        method = Method.Get,
        uri = s"/bot$botApiKey/sendMessage?chat_id=${message.chat.id}&text=Hello Again ${user.fullName}!",
      )
    }

    val x = httpClient(messageRequest)

    Await.result(x)
  }
}
