package uk.co.mglewis.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.datamodel.Letter

class AvailableLetterValidationTest extends AnyWordSpecLike with Matchers {

  "AvailableLetterValidationTest" should {

    val availableLetters = Letter.fromString("AAABCDE")

    "pass validation when given something sensible" in {
      val result = AvailableLetterValidation.validate(
        usedLetters = Letter.fromString("ABCDE"),
        availableLetters = availableLetters
      )
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

  }
}
