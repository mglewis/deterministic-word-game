package uk.co.mglewis

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.datamodel.Player.{ActionAndPoints, Computer, Human}
import uk.co.mglewis.datamodel.{Letter, Pass, Play, Player, Points, Swap}

class GameStateTest extends AnyWordSpecLike with Matchers {

  val remainingLetters = Letter.startingLetters.take(20)

  val wallace = Player.create(
    name = "Wallace",
    playerType = Human,
    letters = Letter.fromString("CHEESES")
  )

  val gromit = Player.create(
    name = "Gromit",
    playerType = Computer,
    letters = Letter.fromString("WALKIES")
  )

  "GameStateTest" should {
    "identify that the game should continue if everything looks normal" in {
      GameState(
        activePlayer = wallace,
        opposingPlayer = gromit,
        remainingLetters = remainingLetters
      ).isGameComplete should equal (false)
    }

    "identify that the game is over once a player has no letters left" in {
      GameState(
        activePlayer = wallace.copy(letters = Seq.empty),
        opposingPlayer = gromit,
        remainingLetters = remainingLetters
      ).isGameComplete should equal (true)

      GameState(
        activePlayer = wallace,
        opposingPlayer = gromit.copy(letters = Seq.empty),
        remainingLetters = remainingLetters
      ).isGameComplete should equal (true)
    }

    "identify that the game is over if both players chose to pass their last move" in {
      val pass = ActionAndPoints(Pass(Letter.fromString("CHEESES")), Points.zero)

      GameState(
        activePlayer = wallace.copy(actions = Seq(pass)),
        opposingPlayer = gromit.copy(actions = Seq(pass)),
        remainingLetters = remainingLetters
      ).isGameComplete should equal (true)
    }

    "update the state correctly after playing a word" in {
      val state = GameState(
        activePlayer = wallace,
        opposingPlayer = gromit,
        remainingLetters = Letter.fromString("AAAAAAA")
      )

      val newState = state.completeTurn(
        pointsScored = Points(10),
        action = Play(
          played = Letter.fromString("CHEESE"),
          unused = Letter.fromString("S")
        )
      )

      newState.remainingLetters should equal (Letter.fromString("A"))
      newState.activePlayer.name should equal (gromit.name)
      newState.opposingPlayer.name should equal (wallace.name)
      newState.opposingPlayer.letters should equal (Letter.fromString("SAAAAAA"))
    }

    "update the state correctly after swapping letters" in {
      val state = GameState(
        activePlayer = wallace,
        opposingPlayer = gromit,
        remainingLetters = Letter.fromString("AAAAAAA")
      )

      val newState = state.completeTurn(
        pointsScored = Points.zero,
        action = Swap(
          played = Letter.fromString("CHESS"),
          unused = Letter.fromString("EE")
        )
      )

      newState.remainingLetters should equal (Letter.fromString("CHESSAA"))
      newState.activePlayer.name should equal (gromit.name)
      newState.opposingPlayer.name should equal (wallace.name)
      newState.opposingPlayer.letters should equal (Letter.fromString("EEAAAAA"))
    }
  }
}
