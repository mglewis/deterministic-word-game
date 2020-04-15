package uk.co.mglewis.server

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response, Status, Version}
import com.twitter.finatra.http.Controller
import com.twitter.util.Await
import uk.co.mglewis.core.{Dictionary, GameState}
import uk.co.mglewis.datamodel.Player.Human

class GameController(
  secretPath: String,
  apiClient: TelegramApiClient
) extends Controller {

  //private val dictionary = new Dictionary("resources/word_list.txt")

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

    val startCommandReceived = message.text.toUpperCase == "START"

    require(!user.isBot, "Messages from bots are not supported")

    val reply = if (gameStates.get(user).isEmpty && !startCommandReceived) {
      intro(user.firstName)
    } else if (gameStates.get(user).isEmpty && startCommandReceived) {
      val initialGameState = GameState.generateStartState(user.firstName, Human)
      gameStates = gameStates + (user -> initialGameState)
      gameStart(initialGameState)
    } else {
      "Oh no, I can't do that yet"
    }

    val response = Await.result(apiClient.sendMessage(message.chat.id, reply))
    info(response)
    info(response.contentString)
    Response(Status.Ok)
  }

  private def intro(userName: String): String =
    s"Hi $userName\n\nDo you wish to challenge me? I am a tough opponent to beat.\n\nJust message me with the text START so battle may commence!"

  private def gameStart(gameState: GameState): String = {
    val playerLetters = gameState.activePlayer.letters.mkString(",")
    val conanLetters = gameState.opposingPlayer.letters.mkString(",")
    val upcoming️Letters = gameState.remainingLetters.take(7)

    s"""
       |So it begins. As a benevolent barbarian I will let you move first.
       |
       |You have been dealt the following letters: $playerLetters
       |I have been dealt the following letters: $conanLetters
       |
       |The next 7 available letters are $upcoming️Letters
       |
       |If you want to swap your letters send me a message in the format "-SWAP ${playerLetters.take(3)}
       |If you can't think of any words just type -PASS
       """.stripMargin
  }

}
