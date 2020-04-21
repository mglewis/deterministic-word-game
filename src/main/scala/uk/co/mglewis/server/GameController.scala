package uk.co.mglewis.server

import akka.actor.{ActorRef, ActorSystem}
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import uk.co.mglewis.core.{ComputerPlayer, Dictionary, GameState}
import uk.co.mglewis.datamodel.{InvalidCommand, Pass, Play, Player, Points, Swap, TurnEndingAction}
import uk.co.mglewis.server.GameActor.Messages.UserMessage
import uk.co.mglewis.server.TelegramMessages.{ComputerReplies, PlayerReplies}
import uk.co.mglewis.validation.CommandInterpreter

class GameController(
  secretPath: String,
  apiClient: TelegramApiClient,
  dictionary: Dictionary
) extends Controller {

  private var games = Map.empty[User, ActorRef]

  val actorSystem: ActorSystem = ActorSystem(
    "game-actor-system"
  )

  get("/ping") { request: Request =>
    "Pong"
  }

  get("/stats") { request: Request =>
    s"""
       |There are ${games.size} games in progress
       |
       |Current active players are ${games.keySet.map(_.name).mkString("\n")}
     """.stripMargin
  }

  get(s"/$secretPath/ping") { request: Request =>
    "Secret pong."
  }

  post(s"/$secretPath/update") { request: Request =>
    info(request.contentString)

    val message = MessageSerializer.deserialize(request.contentString)
    val user = message.from

    val gameActor = games.getOrElse(
      user,
      actorSystem.actorOf(GameActor.props(user, message.chat.id, dictionary, apiClient))
    )

    games = games + (user -> gameActor)

    gameActor ! UserMessage(message.text)
  }
}
