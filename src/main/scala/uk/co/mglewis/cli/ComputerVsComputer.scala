package uk.co.mglewis.cli

import uk.co.mglewis.core.{Dictionary, GameState}
import uk.co.mglewis.datamodel.Player.Computer

object ComputerVsComputer extends App {

  val game = new Game(
    dictionary = new Dictionary("resources/word_list.txt")
  )

  val startingState = GameState.generateStartState(
    playerName = "Matty",
    playerType = Computer
  )

  val finishingState = game.playTurn(startingState)
  CommandLineUtils.printEndOfGameSummary(finishingState)

}
