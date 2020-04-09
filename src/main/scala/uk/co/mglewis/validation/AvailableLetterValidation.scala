package uk.co.mglewis.validation

import uk.co.mglewis.Dictionary.Word
import uk.co.mglewis.datamodel.Letter

import scala.collection.mutable
import scala.collection.parallel.immutable.ParSet

object AvailableLetterValidation {

  case class ValidationResult(
    usedLetters: Seq[Letter],
    unusedLetters: Seq[Letter],
    invalidLetters: Set[Letter]
  ) {
    def isValid: Boolean = invalidLetters.isEmpty
  }

  def validate(
    word: Word,
    availableLetters: Seq[Letter]
  ): ValidationResult = validate(word.letters, availableLetters)

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
          usedLetters = validationState.usedLetters :+ blank,
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

  def validSubset(
    candidateWords: ParSet[Word],
    availableLetters: Seq[Letter]
  ): Set[ValidationResult] = {
    val availableLettersWithoutBlanks = availableLetters.filterNot(_ == Letter.blank)
    val availableChars = availableLettersWithoutBlanks.map(_.char)
    val availableCharMap = availableChars.groupBy(c => c).mapValues(_.size)

    val dedupedAvailableChars = availableChars.toSet.toSeq.sorted
    val availableCharCounts = dedupedAvailableChars.map(availableCharMap.getOrElse(_, 0))
    val blankLimit = availableLetters.count(_ == Letter.blank)

    val validWords = candidateWords.filter {
      fastValidateWord(_, dedupedAvailableChars, availableCharCounts, blankLimit)
    }.seq

    validWords.map { word =>
      validate(word, availableLetters)
    }.seq
  }

  /**
    * Example input:
    * candidateWord:        [C,H,E,E,S,E]
    * availableChars:       [A,C,E,H,S]
    * availableCharCounts:  [1,1,2,1,1]
    * blankLimit:           1
    */
  private def fastValidateWord(
    candidateWord: Word,
    availableChars: Seq[Char],
    availableCharCounts: Seq[Int],
    blankLimit: Int
  ): Boolean = {
    val mAvailableCharCounts = mutable.MutableList() ++ availableCharCounts
    var blankCount = 0

    for (char <- candidateWord.toCharSeq) {
      val i = availableChars.indexOf(char)

      if (i == -1 && blankCount < blankLimit) {
        blankCount += 1
      } else if (i == -1 || mAvailableCharCounts(i) == 0) {
        return false
      } else {
        mAvailableCharCounts(i) -= 1
      }
    }
    true
  }
}
