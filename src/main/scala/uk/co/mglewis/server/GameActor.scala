package uk.co.mglewis.server

import akka.actor.SupervisorStrategy.{Escalate, Stop}
import akka.actor.{Actor, ActorInitializationException, ActorKilledException, ActorLogging, DeathPactException, OneForOneStrategy, Props}
import com.twitter.util.Future
import uk.co.mglewis.core.{ComputerPlayer, Dictionary, GameState}
import uk.co.mglewis.datamodel.{InvalidCommand, Pass, Play, Points, Swap, TurnEndingAction}
import uk.co.mglewis.datamodel.Player.Human
import uk.co.mglewis.server.GameActor.Messages.UserMessage
import uk.co.mglewis.server.Messages.{ComputerReplies, Gifs, Instructions, PlayerReplies}
import uk.co.mglewis.validation.CommandInterpreter

class GameActor(
  var state: GameState,
  chatId: Int,
  userName: String,
  dictionary: Dictionary,
  apiClient: TelegramApiClient
) extends Actor with ActorLogging {

  override val supervisorStrategy = OneForOneStrategy() {
    case _: ActorInitializationException => Escalate
    case _: ActorKilledException => Stop
    case _: DeathPactException => Stop
    case _: Exception => Escalate
  }

  def awaitingUserInput: Receive = {
    case UserMessage(text) if text == "/start" =>
      state = GameState.generateStartState(userName, Human)
      apiClient.sendMessage(chatId, Instructions.introduction(userName))
      apiClient.sendMessage(chatId, Instructions.gameStart(state))
    case UserMessage(text) =>
      val command = CommandInterpreter.interpret(text, state.activePlayer.letters)

      command match {
        case _: InvalidCommand =>
          apiClient.sendMessage(chatId, PlayerReplies.helpfulMessage(state))
        case pass: Pass =>
          state = state.completeTurn(pointsScored = Points.zero, action = pass)
          apiClient.sendMessage(chatId, PlayerReplies.turnPassed(state))
          startComputerTurn()
        case swap: Swap =>
          state = state.completeTurn(pointsScored = Points.zero, action = swap)
          apiClient.sendMessage(chatId, PlayerReplies.lettersSwapped(state.opposingPlayer, swap))
          startComputerTurn()
        case play: Play =>
          if (dictionary.contains(play.word)) {
            val points = Points.calculate(play.played)
            state = state.completeTurn(pointsScored = points, action = play)
            apiClient.sendMessage(chatId, PlayerReplies.validWord(play.word, points))
          } else {
            state = state.completeTurn(pointsScored = Points.zero, action = play)
            apiClient.sendMessage(chatId, PlayerReplies.invalidWord(play.word))
          }
          startComputerTurn()
      }

      if (state.isGameComplete) {
        apiClient.sendAnimation(chatId, Gifs.swingingSword)
        apiClient.sendMessage(chatId, Instructions.gameEnd(state))
        context.become(awaitingUserInput)
      }
  }

  def awaitingComputerCommand: Receive = {
    case UserMessage(_) =>
      apiClient.sendMessage(chatId, ComputerReplies.notYourTurn)
    case a: TurnEndingAction =>
      a match {
        case pass: Pass =>
          state = state.completeTurn(pointsScored = Points.zero, action = pass)
        case swap: Swap =>
          state = state.completeTurn(pointsScored = Points.zero, action = swap)
        case play: Play =>
          val points = Points.calculate(play.played)
          state = state.completeTurn(pointsScored = points, action = play)
      }
      apiClient.sendMessage(chatId, ComputerReplies.computerAction(state))
      if (state.isGameComplete) {
        apiClient.sendAnimation(chatId, Gifs.swingingSword)
        apiClient.sendMessage(chatId, Instructions.gameEnd(state))
      } else {
        apiClient.sendMessage(chatId, PlayerReplies.startOfTurn(state))
      }

      context.become(awaitingUserInput)
  }

  def receive: Receive = {
    case any =>
      context.become(awaitingUserInput)
      self ! any
  }

  private def startComputerTurn(): Unit = {
    context.become(awaitingComputerCommand)
    Future {
      ComputerPlayer.chooseAction(
        playerLetters = state.activePlayer.letters,
        remainingLetters = state.remainingLetters,
        dictionary = dictionary
      )
    }.map {
      self ! _
    }
  }
}

object GameActor {
  object Messages {
    case class UserMessage(text: String)
  }

  def props(
    user: User,
    chatId: Int,
    dictionary: Dictionary,
    apiClient: TelegramApiClient
  ): Props = {
    require(!user.isBot, "Messages from bots are not supported")
    val startingState = GameState.generateStartState(user.name, Human)
    Props(new GameActor(startingState, chatId, user.name, dictionary, apiClient))
  }
}
