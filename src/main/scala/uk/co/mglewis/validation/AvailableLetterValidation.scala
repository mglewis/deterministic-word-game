package uk.co.mglewis.validation

import uk.co.mglewis.datamodel.Letter

object AvailableLetterValidation {

  case class ValidationResult(
    usedLetters: Seq[Letter],
    unusedLetters: Seq[Letter],
    invalidLetters: Set[Letter]
  ) {
    def isValid: Boolean = invalidLetters.isEmpty
  }

  def validate(
    usedLetters: Seq[Letter],
    availableLetters: Seq[Letter]
  ): ValidationResult = {
    import Letter.blank

    val startingLetterValidation = ValidationResult(
      usedLetters = Seq.empty,
      unusedLetters = availableLetters,
      invalidLetters = Set.empty
    )

    usedLetters.foldLeft(startingLetterValidation) { (validationState, letter) =>
      if (validationState.unusedLetters.contains(letter)) {
        ValidationResult(
          usedLetters = validationState.usedLetters :+ letter,
          unusedLetters = validationState.unusedLetters.diff(Seq(letter)),
          invalidLetters = validationState.invalidLetters
        )
      } else if (validationState.unusedLetters.contains(blank)) {
        ValidationResult(
          usedLetters = validationState.usedLetters :+ Letter.blank,
          unusedLetters = validationState.unusedLetters.diff(Seq(blank)),
          invalidLetters = validationState.invalidLetters
        )
      } else {
        ValidationResult(
          usedLetters = validationState.usedLetters,
          unusedLetters = validationState.unusedLetters,
          invalidLetters = validationState.invalidLetters + letter
        )
      }
    }
  }
}
