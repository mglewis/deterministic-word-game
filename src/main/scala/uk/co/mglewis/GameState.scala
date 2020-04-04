package uk.co.mglewis

import uk.co.mglewis.datamodel.{Letter, Pass, Player, TurnEndingAction}

case class GameState(
  activePlayer: Player,
  opposingPlayer: Player,
  remainingLetters: Seq[Letter]
) {
  def upcomingLetters: Seq[Letter] = remainingLetters.take(7)

  def completeTurn(
    pointsScored: Int,
    unusedLettersFromThisTurn: Seq[Letter],
    action: TurnEndingAction
  ): GameState = {
    val newLettersRequired = Math.abs(unusedLettersFromThisTurn.length - 7)
    val (newLettersForActivePlayer, leftoverLetters) = remainingLetters.splitAt(newLettersRequired)

    val updatedActivePlayer = Player(
      activePlayer.name,
      activePlayer.totalScore + pointsScored,
      letters = unusedLettersFromThisTurn ++ newLettersForActivePlayer,
      lastAction = action
    )

    GameState(
      activePlayer = opposingPlayer,
      opposingPlayer = updatedActivePlayer,
      remainingLetters = leftoverLetters
    )
  }

  def isGameComplete: Boolean = {
    val noLettersLeft = activePlayer.letters.isEmpty || opposingPlayer.letters.isEmpty
    val bothPlayersPassed = activePlayer.lastAction == Pass && opposingPlayer.lastAction == Pass
    noLettersLeft || bothPlayersPassed
  }
}
