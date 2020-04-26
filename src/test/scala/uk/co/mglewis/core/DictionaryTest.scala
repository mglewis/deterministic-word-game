package uk.co.mglewis.core

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.datamodel.Letter

class DictionaryTest extends AnyWordSpecLike with Matchers {

  "DictionaryTest" should {
    val dictionary = new Dictionary("resources/test/word_list.txt")

    "load the resource file as expected" in {
      dictionary.contains("BAT") should be (true)
    }

    "correctly identify some words are missing" in {
      dictionary.contains("CHIPS") should be (false)
    }

    "provide all playable words" in {
      val validationResults = dictionary.allValidWords(Letter.fromString("BATZIPA"))

      validationResults.map { r => Letter.asString(r.usedLetters) } should be (Set("BAT", "ZIP"))
    }
  }
}
