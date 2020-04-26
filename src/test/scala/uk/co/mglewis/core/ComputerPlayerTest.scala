package uk.co.mglewis.core

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.datamodel.{Letter, Pass, Play, Swap}

class ComputerPlayerTest extends AnyWordSpecLike with Matchers {

  "ComputerPlayerTest" should {
    val dictionary = new Dictionary("resources/test/word_list.txt")

    "pass the turn when there are no available moves" in {
      val action = ComputerPlayer.chooseAction(
        playerLetters = Letter.fromString("Z"),
        remainingLetters = Seq.empty,
        dictionary = dictionary
      )

      action should be (a[Pass])
    }

    "swap when there are no available words this turn, but there is a bonus next turn" in {
      val action = ComputerPlayer.chooseAction(
        playerLetters = Letter.fromString("SAUSAGT"),
        remainingLetters = Letter.fromString("EEEEEEE"),
        dictionary = dictionary
      )

      action should be (a[Swap])
      action.played should be (Letter.fromString("T"))
    }

    "go for the bonus when available" in {
      val action = ComputerPlayer.chooseAction(
        playerLetters = Letter.fromString("SAUSAGE"),
        remainingLetters = Letter.fromString("EEEEEEE"),
        dictionary = dictionary
      )

      val expectedAction = Play(
        played = Letter.fromString("SAUSAGE"),
        unused = Seq.empty
      )

      action should be (expectedAction)
    }

    "go for the swap if a big points haul is available next turn" in {
      val action = ComputerPlayer.chooseAction(
        playerLetters = Letter.fromString("BITAFFF"),
        remainingLetters = Letter.fromString("SUSAGEF"),
        dictionary = dictionary
      )

      action should be (a[Swap])
      action.played.sorted should be (Letter.fromString("BITFFF").sorted)
    }
  }

}
