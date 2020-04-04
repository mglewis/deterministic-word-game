package uk.co.mglewis

import uk.co.mglewis.datamodel.{Letter, Pass, Player, Swap, TurnEndingAction}

case class GameState(
  activePlayer: Player,
  opposingPlayer: Player,
  remainingLetters: Seq[Letter]
) {
  def upcomingLetters: Seq[Letter] = remainingLetters.take(7)

  def completeTurn(
    pointsScored: Int,
    action: TurnEndingAction
  ): GameState = {
    val newLettersRequired = action.played.length
    val (newLettersForActivePlayer, newRemainingLetters) = remainingLetters.splitAt(newLettersRequired)

    val maybeSwappedLetters = action match {
      case swap: Swap => swap.played
      case _ => Seq.empty
    }

    val updatedActivePlayer = Player(
      activePlayer.name,
      activePlayer.totalScore + pointsScored,
      letters = action.unused ++ newLettersForActivePlayer,
      lastAction = action
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
