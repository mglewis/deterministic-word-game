package uk.co.mglewis.core

import uk.co.mglewis.datamodel.Player.{Computer, Human, PlayerType}
import uk.co.mglewis.datamodel.{Letter, Pass, Player, Points, Swap, TurnEndingAction}

import scala.util.Random

case class GameState(
  activePlayer: Player,
  opposingPlayer: Player,
  remainingLetters: Seq[Letter]
) {
  def upcomingLetters: Seq[Letter] = remainingLetters.take(Letter.maxLetters)

  def completeTurn(
    pointsScored: Points,
    action: TurnEndingAction
  ): GameState = {
    val letterAllocation = GameState.dealLettersToPlayer(action, remainingLetters)

    val maybeSwappedLetters = action match {
      case swap: Swap => swap.played
      case _ => Seq.empty
    }

    val updatedActivePlayer = activePlayer.endOfTurnUpdate(
      points = pointsScored,
      newLetters = letterAllocation.playerLetters,
      action = action
    )

    GameState(
      activePlayer = opposingPlayer,
      opposingPlayer = updatedActivePlayer,
      remainingLetters = maybeSwappedLetters ++ letterAllocation.remainingLetters
    )
  }

  def isGameComplete: Boolean = {
    val noLettersLeft = activePlayer.letters.isEmpty || opposingPlayer.letters.isEmpty
    val bothPlayersPassed = isPass(activePlayer.lastAction) && isPass(opposingPlayer.lastAction)
    noLettersLeft || bothPlayersPassed
  }

  private def isPass(action: Option[TurnEndingAction]): Boolean = action.exists(_.isInstanceOf[Pass])
}

object GameState {
  case class LetterAllocation(
    playerLetters: Seq[Letter],
    remainingLetters: Seq[Letter]
  )

  def dealLettersToPlayer(
    action: TurnEndingAction,
    remainingLetters: Seq[Letter]
  ): LetterAllocation = {
    val (newLettersForPlayer, newRemainingLetters) = remainingLetters.splitAt(action.played.length)

    LetterAllocation(
      playerLetters = action.unused ++ newLettersForPlayer,
      remainingLetters = newRemainingLetters
    )
  }

  def generateStartState(
    playerId: Int,
    playerName: String,
    playerType: PlayerType
  ): GameState = {
    val gameLetters = Random.shuffle(Letter.startingLetters)
    val playerOneLetters = gameLetters.take(Letter.maxLetters)
    val playerTwoLetters = gameLetters.diff(playerOneLetters).take(Letter.maxLetters)
    val remainingLetters = gameLetters.diff(playerOneLetters ++ playerTwoLetters)

    GameState(
      activePlayer = Player.create(playerName, playerType, playerOneLetters),
      opposingPlayer = Player.create("Conan", Computer, playerTwoLetters),
      remainingLetters = remainingLetters
    )
  }

}
