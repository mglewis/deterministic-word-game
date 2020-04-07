package uk.co.mglewis

import uk.co.mglewis.datamodel.Player.{Computer, Human}
import uk.co.mglewis.datamodel.{Command, InvalidCommand, Letter, Pass, Play, Player, Points, Swap, TurnEndingAction}
import uk.co.mglewis.validation.{AvailableLetterValidation, CommandInterpreter}

import scala.util.Random
import scala.io.StdIn.readLine

object Main extends App {

  val dictionary = new Dictionary("resources/word_list.txt")
  val startingState = generateStartState
  val finishingState = playTurn(startingState)
  // TODO: handle finishingState

  def generateStartState: GameState = {
    val gameLetters = Random.shuffle(Letter.startingLetters)
    val playerOneLetters = gameLetters.take(Letter.maxLetters)
    val playerTwoLetters = gameLetters.diff(playerOneLetters).take(Letter.maxLetters)
    val remainingLetters = gameLetters.diff(playerOneLetters ++ playerTwoLetters)

    GameState(
      activePlayer = Player.create("Matt", Computer, playerOneLetters),
      opposingPlayer = Player.create("Katie", Computer, playerTwoLetters),
      remainingLetters = remainingLetters
    )
  }

  case class ActionAndPoints(action: TurnEndingAction, points: Points)

  def allPossiblePlays(
    availableLetters: Seq[Letter]
  ): Seq[ActionAndPoints] = {
    dictionary.dictionaryOrderedByPoints.par.flatMap { entry =>
      val validationResult = AvailableLetterValidation.validate(entry.word, availableLetters)
      if (validationResult.isValid) {
        val play = Play(played = validationResult.usedLetters, unused = validationResult.unusedLetters)
        Some(ActionAndPoints(play, Points.calculate(validationResult.usedLetters)))
      } else None
    }.seq
  }

  def allPossibleSwaps(
    availableLetters: Seq[Letter]
  ): Set[ActionAndPoints] = {
    case class LetterWthIndex(letter: Letter, index: Int)

    // we use LetterWithIndex to prevent duplicate letters being removed when we determine the subsets
    val letterSet = availableLetters.zipWithIndex.map(LetterWthIndex.tupled).toSet
    val letterSubsets = letterSet.subsets.filter(_.nonEmpty)

    val allSwaps = for {
      subset <- letterSubsets
      played = subset.toSeq.map(_.letter)
      unused = availableLetters.diff(played)
      swap = Swap(played = played, unused = unused)
    } yield ActionAndPoints(swap, Points(0))

    allSwaps.toSet
  }

  def determineComputerAction(gameState: GameState): TurnEndingAction = {
    val availableLetters = gameState.activePlayer.letters

    val allPossibleActions = allPossiblePlays(availableLetters) ++ allPossibleSwaps(availableLetters)

    println(s"actions length ${allPossibleActions.length}")


    val allPossibleActionsWithOptimalFollowUpPlay = allPossibleActions.map { initialAction =>
      val newAvailableLetters = GameState.dealLettersToPlayer(
        initialAction.action,
        gameState.remainingLetters
      ).playerLetters

      val maybeBestFollowUpPlay = allPossiblePlays(newAvailableLetters).headOption
      val followUpPoints = maybeBestFollowUpPlay.map(_.points).getOrElse(Points.zero)

      ActionAndPoints(initialAction.action, initialAction.points + followUpPoints)
    }

    val maybeOptimalAction = allPossibleActionsWithOptimalFollowUpPlay.sortBy(_.points).headOption
    val action = maybeOptimalAction.map(_.action).getOrElse(Pass(availableLetters))

    println(s"Computer decided to ${action.name} - ${action.played.map(_.char).mkString}")
    action
  }

  /**
    * Computer Player Strategy
    *
    * # Things to maximise
    * - the number of points from this turn
    * - the maximum number of points that the opponent can score next turn
    * - the number of points from next turn
    * I guess this can be summarised by:
    * find max of: currentTurnPoints + nextTurnPoints - maxOpponentTurnPoints
    */
  def playTurn(state: GameState): GameState = {
    val command = if (state.activePlayer.playerType == Human) {
      printSummary(state)
      val playerInput = readLine().toUpperCase.trim

      CommandInterpreter.interpret(
        input = playerInput,
        availableLetters = state.activePlayer.letters
      )
    } else {
      determineComputerAction(state)
    }

    val newGameState = action(command, state)

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
      val paddedWord = play.word.mkString.padTo(Letter.maxLetters, ' ')
      println(s"Well done ${player.name}! \t $paddedWord scores you $turnScore points")
//      println(s"You have the following letters ${player.letters.mkString(", ")}\n")

      state.completeTurn(turnScore, action = play)
    } else {
      println(s"Oh no! ${play.word} wasn't found in the dictionary. You scored 0")
      state.completeTurn(pointsScored = Points.zero, action = play)
    }
  }
}


