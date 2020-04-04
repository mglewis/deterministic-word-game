package uk.co.mglewis

import uk.co.mglewis.datamodel.{Command, InvalidCommand, Letter, Pass, Play, Player, Swap}
import uk.co.mglewis.validation.CommandInterpreter

import scala.io.{BufferedSource, Source}
import scala.util.Random
import scala.io.StdIn.readLine

object Main extends App {

  val file: BufferedSource = Source.fromFile("resources/word_list.txt")
  val reader = file.bufferedReader
  val dictionary = Stream.continually(reader.readLine()).takeWhile(_ != null).toSet
  reader.close()
  file.close()

  val startingState = generateStartState
  playTurn(startingState)

  def generateStartState: GameState = {
    val gameLetters = Random.shuffle(Letter.startingLetters)

    GameState(
      activePlayer = Player.create("Matt", gameLetters.slice(0, Letter.maxLetters)),
      opposingPlayer = Player.create("Katie", gameLetters.slice(Letter.maxLetters, Letter.maxLetters * 2)),
      remainingLetters = gameLetters.drop(Letter.maxLetters * 2)
    )
  }

  /**
    * Wishlist:
    * - Make swap actually swap tiles
    * - Support for wildcards
    * - Break out dictionary into separate class
    * - Turn into a webserver with a restful api
    */
  def playTurn(state: GameState): GameState = {
    printSummary(state)

    val playerInput = readLine().toUpperCase.trim

    val userCommand = CommandInterpreter.interpret(
      input = playerInput,
      availableLetters = state.activePlayer.letters
    )

    val newGameState = action(userCommand, state)

    if (newGameState.isGameComplete) newGameState else playTurn(newGameState)
  }

  private def printSummary(state: GameState): Unit = {
    import state._
    val summary =
      s"""
         | * ${activePlayer.name} [${activePlayer.totalScore}] - ${opposingPlayer.name} [${opposingPlayer.totalScore}]
         | Upcoming letters : ${upcomingLetters.mkString(", ")}
         | Letters remaining: ${remainingLetters.size}
         |
         | You have the following letters ${activePlayer.letters.mkString(", ")}
       """.stripMargin
    println(summary)
  }

  private def action(
    userCommand: Command,
    state: GameState
  ): GameState = {
    userCommand match {
      case invalidCommand: InvalidCommand =>
        println(invalidCommand.message)
        playTurn(state)
      case pass: Pass =>
        state.completeTurn(pointsScored = 0, action = pass)
      case swap: Swap =>
        state.completeTurn(pointsScored = 0, action = swap)
      case play: Play =>
        playWord(play, state)
    }
  }

  private def playWord(
    play: Play,
    state: GameState
  ): GameState = {
    if (dictionary.contains(play.word)) {
      val turnScore = calculateScore(play.played)
      println(s"Hurrah! ${play.word} is a valid word! You scored $turnScore!")
      state.completeTurn(turnScore, action = play)
    } else {
      println(s"Oh no! ${play.word} wasn't found in the dictionary. You scored 0")
      state.completeTurn(pointsScored = 0, action = play)
    }
  }

  private def calculateScore(word: Seq[Letter]): Int = {
    val bonus = if (word.length == 7) 50 else 0
    word.map(_.points).sum + bonus
  }
}


