package uk.co.mglewis

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.core.Dictionary

class DictionaryTest extends AnyWordSpecLike with Matchers {

  "DictionaryTest" should {
    val dictionary = new Dictionary("resources/test/word_list.txt")

    "load the resource file as expected" in {
      dictionary.contains("BAT") should be(true)
    }

    "correctly identify some words are missing" in {
      dictionary.contains("CHIPS") should be(false)
    }
  }
}
