package uk.co.mglewis.cli

import uk.co.mglewis.core.{ComputerPlayer, Dictionary, GameState}
import uk.co.mglewis.datamodel.Player.{Computer, Human}
import uk.co.mglewis.datamodel.{Command, InvalidCommand, Pass, Play, Player, Points, Swap}
import uk.co.mglewis.validation.CommandInterpreter

class Game(
  private val dictionary: Dictionary
) {

  def playTurn(
    state: GameState,
    maybePreviousInvalidCommand: Option[InvalidCommand] = None
  ): GameState = {
    if (maybePreviousInvalidCommand.isEmpty) CommandLineUtils.printTurnIntro(state)
    else maybePreviousInvalidCommand.foreach(CommandLineUtils.printInvalidCommand)

    val command = state.activePlayer.playerType match {
      case Human =>
        val playerInput = CommandLineUtils.acceptPlayerInput
        CommandInterpreter.interpret(playerInput, state.activePlayer.letters)
      case Computer =>
        ComputerPlayer.chooseAction(
          playerLetters = state.activePlayer.letters,
          remainingLetters = state.remainingLetters,
          dictionary = dictionary
        )
    }

    val newGameState = action(command, state)
    CommandLineUtils.printTurnResult(newGameState.opposingPlayer)

    if (newGameState.isGameComplete) newGameState else playTurn(newGameState)
  }

  private def action(
    userCommand: Command,
    state: GameState
  ): GameState = {
    userCommand match {
      case invalidCommand: InvalidCommand =>
        playTurn(state, Some(invalidCommand))
      case pass: Pass =>
        state.completeTurn(pointsScored = Points.zero, action = pass)
      case swap: Swap =>
        state.completeTurn(pointsScored = Points.zero, action = swap)
      case play: Play =>
        playWord(state.activePlayer, play, state)
    }
  }

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
