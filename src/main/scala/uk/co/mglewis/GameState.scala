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
    val letterAllocation = GameState.dealLettersToPlayer(action, remainingLetters)

    val maybeSwappedLetters = action match {
      case swap: Swap => swap.played
      case _ => Seq.empty
    }

    val updatedActivePlayer = activePlayer.endOfTurnUpdate(
      pointsScored = pointsScored,
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
    val bothPlayersPassed = (activePlayer.lastAction, opposingPlayer.lastAction) match {
      case (_: Pass, _: Pass) => true
      case _ => false
    }

    noLettersLeft || bothPlayersPassed
  }
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
}
