package uk.co.mglewis

import uk.co.mglewis.datamodel.{InvalidCommand, Letter, Player}

import scala.io.StdIn.readLine

object CommandLineUtils {

  def printTurnIntro(state: GameState): Unit = {
    import state._
    val summary =
      s"""
         |${activePlayer.name}: ${activePlayer.totalScore} pts
         |Letters: ${activePlayer.letters.map(_.char).mkString}
         |Upcoming: ${remainingLetters.take(Letter.maxLetters).map(_.char).mkString}
       """.stripMargin

    println(summary)
  }

  def printInvalidCommand(invalidCommand: InvalidCommand): Unit = {
    println(invalidCommand.message)
  }

  def printTurnResult(player: Player): Unit = {
    val lastAction = player
      .actions.lastOption
      .getOrElse(throw new RuntimeException(s"Player $player hasn't yet undertaken any actions"))
    val word = lastAction.action.played.map(_.char).mkString

    println(s"${lastAction.action.name} $word scored ${lastAction.points.value}")
  }

  def acceptPlayerInput: String = {
    print("> ")
    readLine().toUpperCase.trim
  }

  def printEndOfGameSummary(gameState: GameState): Unit = {
    import gameState._

    val (winner, loser) =
      if (activePlayer.totalScore >= opposingPlayer.totalScore) (activePlayer, opposingPlayer)
      else (opposingPlayer, activePlayer)

    s"""
       | ${winner.name} beat ${loser.name} ${winner.totalScore} to ${loser.totalScore}
     """.stripMargin
  }
}
