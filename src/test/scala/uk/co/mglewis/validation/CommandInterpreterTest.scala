package uk.co.mglewis.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.datamodel.{InvalidCommand, Letter, Pass, Play, Swap}

class CommandInterpreterTest extends AnyWordSpecLike with Matchers {

  val availableLetters = Letter.fromString("AAABCDE")


  "CommandInterpreterTest" should {
    "interpret a pass command" in {
      val result = CommandInterpreter.interpret(
        input = "-Pass",
        availableLetters = availableLetters
      )

      result should be (Pass(availableLetters))
    }

    "interpret a swap command" in {
      val result = CommandInterpreter.interpret(
        input = "-swap ABC",
        availableLetters = availableLetters
      )
      result should be (Swap(
        played = Letter.fromString("ABC"),
        unused = Letter.fromString("AADE")
      ))
    }

    "return a tetchy message if given an impossible swap command" in {
      val result = CommandInterpreter.interpret(
        input = "-SWAP ZZZ",
        availableLetters = availableLetters
      )
      result should be (a[InvalidCommand])
    }

    "interpret a play" in {
      val result = CommandInterpreter.interpret(
        input = "abcde",
        availableLetters = availableLetters
      )
      result should be (Play(
        played = Letter.fromString("abcde"),
        unused = Letter.fromString("aa")
      ))
    }

    "return a tetchy message if asked to play invalid characters" in {
      val result = CommandInterpreter.interpret(
        input = ":-)",
        availableLetters = availableLetters
      )
      result should be (a[InvalidCommand])
    }

    "return a tetchy message if asked to play letters that the player doesn't have" in {
      val result = CommandInterpreter.interpret(
        input = "Z",
        availableLetters = availableLetters
      )
      result should be (a[InvalidCommand])
    }
  }
}
