package uk.co.mglewis.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.Dictionary.Word
import uk.co.mglewis.datamodel.Letter

import scala.collection.parallel.immutable.ParSet

class AvailableLetterValidationTest extends AnyWordSpecLike with Matchers {

  "AvailableLetterValidationTest" should {

    val availableLetters = Letter.fromString("AAABCDE")

    "pass validation when given something sensible" in {
      val result = AvailableLetterValidation.validate(
        usedLetters = Letter.fromString("ABCDE"),
        availableLetters = availableLetters
      )
      result.usedLetters should be(Letter.fromString("ABCDE"))
      result.unusedLetters should be(Letter.fromString("AA"))
      result.isValid should be(true)
      result.invalidLetters should be(Set.empty)
    }

    "fail validation when given something crazy" in {
      val result = AvailableLetterValidation.validate(
        usedLetters = Letter.fromString("ABCZE"),
        availableLetters = availableLetters
      )
      result.isValid should be(false)
      result.invalidLetters should be(Letter.fromString("Z").toSet)
    }

    "use any ? letters as a last resort" in {
      val result = AvailableLetterValidation.validate(
        usedLetters = Letter.fromString("AAAZBC"),
        availableLetters = availableLetters.take(6) :+ Letter.blank
      )
      result.isValid should be(true)
    }

    "allow a sequence of used letters that contains blanks" in {
      val result = AvailableLetterValidation.validate(
        usedLetters = Letter.fromString("F?E?D"),
        availableLetters = Letter.fromString("A?CD?EF")
      )
      result.isValid should be(true)
    }

    "fail validation if using blanks when none are available" in {
      val result = AvailableLetterValidation.validate(
        usedLetters = Letter.fromString("A?"),
        availableLetters = availableLetters
      )
      result.isValid should be(false)
    }

    "return the subset of all valid words" in {
      val candidates = ParSet(
        Letter.fromString("BAT"),
        Letter.fromString("CAT"),
        Letter.fromString("MAT")
      ).map(Word)

      val actual = AvailableLetterValidation.validSubset(candidates, Letter.fromString("BCA?")).map(_.usedLetters)

      val expected = Set(
        Letter.fromString("BA?"),
        Letter.fromString("CA?")
      )

      actual should be (expected)
    }
  }
}
