package uk.co.mglewis.server

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.Controller
import uk.co.mglewis.core.{ComputerPlayer, Dictionary, GameState}
import uk.co.mglewis.datamodel.{InvalidCommand, Pass, Play, Player, Points, Swap, TurnEndingAction}
import uk.co.mglewis.datamodel.Player.Human
import uk.co.mglewis.server.TelegramMessages.{ComputerReplies, Instructions, PlayerReplies}
import uk.co.mglewis.validation.CommandInterpreter

class GameController(
  secretPath: String,
  apiClient: TelegramApiClient,
  dictionary: Dictionary
) extends Controller {

  private var gameStates = Map.empty[User, GameState]

  private var hallOfFame = Set.empty[User]

  get("/ping") { request: Request =>
    "Pong"
  }

  get("/stats") { request: Request =>
    s"""
       |There are ${gameStates.size} games in progress
       |
       |Current active players are ${gameStates.keySet.map(_.name).mkString("\n")}
       |
       |The following players have reached the hall of fame ${hallOfFame.map(_.name).mkString("\n")}
     """.stripMargin
  }

  get(s"/$secretPath/ping") { request: Request =>
    "Secret pong."
  }

  post(s"/$secretPath/update") { request: Request =>
    info(request.contentString)

    val message = MessageSerializer.deserialize(request.contentString)
    val user = message.from

    require(!user.isBot, "Messages from bots are not supported")

    val maybeGameState = gameStates.get(user)

    val replies = maybeGameState match {
      case None if !isStartCommand(message.text) => Seq(Instructions.introduction(user.name))
      case None if isStartCommand(message.text) => Seq(startNewGame(user))
      case Some(state) => continueGame(message, state)
    }

    replies.foreach { reply =>
      apiClient.sendMessage(message.chat.id, reply).map(_ => Response(Status.Ok))
    }
  }

  private def isStartCommand(message: String): Boolean = message.toUpperCase == "START"

  private def startNewGame(user: User): String = {
    val initialGameState = GameState.generateStartState(user.id, user.name, Human)
    gameStates = gameStates + (user -> initialGameState)
    Instructions.gameStart(initialGameState)
  }

  private def continueGame(message: Message, state: GameState): Seq[String] = {
    val command = CommandInterpreter.interpret(message.text, state.activePlayer.letters)

    command match {
      case _: InvalidCommand =>
        Seq(PlayerReplies.helpfulMessage(state))
      case pass: Pass =>
        val stateAfterPlayerAction = state.completeTurn(pointsScored = Points.zero, action = pass)
        val endOfPlayerTurnMessage = PlayerReplies.turnPassed(stateAfterPlayerAction)

        val computerAction = ComputerPlayer.chooseAction(
          playerLetters = state.activePlayer.letters,
          remainingLetters = state.remainingLetters,
          dictionary = dictionary
        )
        val stateAfterComputerAction = action(computerAction, stateAfterPlayerAction)
        val endOfComputerTurnMessage = ComputerReplies.computerAction(stateAfterComputerAction)
        val startOfNewPlayerTurnMessage = PlayerReplies.startOfTurn(state.activePlayer)

        gameStates = gameStates + (message.from -> stateAfterComputerAction)
        Seq(endOfPlayerTurnMessage, endOfComputerTurnMessage, startOfNewPlayerTurnMessage)
      case swap: Swap =>
        val stateAfterPlayerAction = state.completeTurn(pointsScored = Points.zero, action = swap)
        val endOfPlayerTurnMessage = PlayerReplies.lettersSwapped(stateAfterPlayerAction.opposingPlayer, swap)

        val computerAction = ComputerPlayer.chooseAction(
          playerLetters = state.activePlayer.letters,
          remainingLetters = state.remainingLetters,
          dictionary = dictionary
        )
        val stateAfterComputerAction = action(computerAction, stateAfterPlayerAction)
        val endOfComputerTurnMessage = ComputerReplies.computerAction(stateAfterComputerAction)
        val startOfNewPlayerTurnMessage = PlayerReplies.startOfTurn(state.activePlayer)

        gameStates = gameStates + (message.from -> stateAfterComputerAction)
        Seq(endOfPlayerTurnMessage, endOfComputerTurnMessage, startOfNewPlayerTurnMessage)
      case play: Play =>
        val (stateAfterPlayerAction, endOfPlayerTurnMessage) = if (dictionary.contains(play.word)) {
          val turnScore = Points.calculate(play.played)
          val newState = state.completeTurn(turnScore, action = play)
          (newState, PlayerReplies.validWord(play.word, turnScore))
        } else {
          val newState = state.completeTurn(pointsScored = Points.zero, action = play)
          (newState, PlayerReplies.validWord(play.word, Points.zero))
        }

        val computerAction = ComputerPlayer.chooseAction(
          playerLetters = stateAfterPlayerAction.activePlayer.letters,
          remainingLetters = stateAfterPlayerAction.remainingLetters,
          dictionary = dictionary
        )
        val stateAfterComputerAction = action(computerAction, stateAfterPlayerAction)

        val endOfComputerTurnMessage = ComputerReplies.computerAction(stateAfterComputerAction)
        val startOfNewPlayerTurnMessage = PlayerReplies.startOfTurn(stateAfterComputerAction.activePlayer)
        gameStates = gameStates + (message.from -> stateAfterComputerAction)
        Seq(endOfPlayerTurnMessage, endOfComputerTurnMessage, startOfNewPlayerTurnMessage)
    }
  }

  // copied from CLI version, remove in cleanup
  private def action(
    action: TurnEndingAction,
    state: GameState
  ): GameState = {
    action match {
      case pass: Pass =>
        state.completeTurn(pointsScored = Points.zero, action = pass)
      case swap: Swap =>
        state.completeTurn(pointsScored = Points.zero, action = swap)
      case play: Play =>
        playWord(state.activePlayer, play, state)
    }
  }

  // copied from CLI version, remove in cleanup
  private def playWord(
    player: Player,
    play: Play,
    state: GameState
  ): GameState = {
    if (dictionary.contains(play.word)) {
      val turnScore = Points.calculate(play.played)
      state.completeTurn(turnScore, action = play)
    } else {
      state.completeTurn(pointsScored = Points.zero, action = play)
    }
  }

}
