package uk.co.mglewis.validation

import uk.co.mglewis.datamodel.{Command, InvalidCommand, Letter, Pass, Play, Swap}

object CommandInterpreter {

  private val pass = "-PASS"
  private val swap = "-SWAP"

  def interpret(
    input: String,
    availableLetters: Seq[Letter]
  ): Command = {
    val cleanedInput = input.toUpperCase.trim

    if (cleanedInput == pass) {
      Pass(availableLetters)
    } else if (cleanedInput.startsWith(swap)) {
      interpretSwap(cleanedInput, availableLetters)
    } else {
      interpretPlay(cleanedInput, availableLetters)
    }
  }

  private def interpretSwap(
    input: String,
    availableLetters: Seq[Letter]
  ): Command = {
    val rawInput = input.diff(swap).trim
    val lettersToSwap = Letter.fromString(rawInput)
    val result = AvailableLetterValidation.validate(lettersToSwap, availableLetters)

    if (result.isValid) {
      Swap(played = lettersToSwap, unused = result.unusedLetters)
    } else {
      val invalidLetters = result.invalidLetters.mkString(",")
      InvalidCommand(s"Swap failed as the following letter selections were invalid: $invalidLetters")
    }
  }

  private def interpretPlay(
    input: String,
    availableLetters: Seq[Letter]
  ): Command = {
    if (Letter.validString(input)) {
      val letters = Letter.fromString(input)
      checkPlayUsesAvailableLetters(
        usedLetters = letters,
        availableLetters = availableLetters
      )
    } else {
      InvalidCommand(s"Unfortunately '$input' was not recognised as valid input")
    }
  }

  private def checkPlayUsesAvailableLetters(
    usedLetters: Seq[Letter],
    availableLetters: Seq[Letter]
  ): Command = {
    val result = AvailableLetterValidation.validate(
      usedLetters = usedLetters,
      availableLetters = availableLetters
    )

    if (result.isValid) {
      Play(
        played = usedLetters,
        unused = result.unusedLetters
      )
    } else {
      val invalidLetters = result.invalidLetters.mkString(",")
      InvalidCommand(s"Word rejected because the following unavailable letters were used $invalidLetters")
    }
  }
}
