package uk.co.mglewis.cli

import uk.co.mglewis.core.{Dictionary, GameState}
import uk.co.mglewis.datamodel.Player.Human

object HumanVsComputer extends App {

  val game = new Game(
    dictionary = new Dictionary("resources/word_list.txt")
  )

  val startingState = GameState.generateStartState(
    playerName = "Matty",
    playerType = Human
  )

  val finishingState = game.playTurn(startingState)
  CommandLineUtils.printEndOfGameSummary(finishingState)

}
