package uk.co.mglewis

import uk.co.mglewis.datamodel.{Letter, Pass, Player, Points, Swap, TurnEndingAction}

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
    val newLettersRequired = action.played.length
    val (newLettersForActivePlayer, newRemainingLetters) = remainingLetters.splitAt(newLettersRequired)

    val maybeSwappedLetters = action match {
      case swap: Swap => swap.played
      case _ => Seq.empty
    }

    val updatedActivePlayer = activePlayer.endOfTurnUpdate(
      pointsScored = pointsScored,
      newLetters = newLettersForActivePlayer,
      action = action
    )

    GameState(
      activePlayer = opposingPlayer,
      opposingPlayer = updatedActivePlayer,
      remainingLetters = maybeSwappedLetters ++ newRemainingLetters
    )
  }

  def isGameComplete: Boolean = {
    val noLettersLeft = activePlayer.letters.isEmpty || opposingPlayer.letters.isEmpty
    val bothPlayersPassed = (activePlayer.lastAction, opposingPlayer.lastAction) match {
      case (_: Pass, _: Pass) => true
      case _ => false
    }

    noLettersLeft || bothPlayersPassed
  }
}
