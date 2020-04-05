package uk.co.mglewis

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.co.mglewis.datamodel.Letter

class DictionaryTest extends AnyWordSpecLike with Matchers {

  "DictionaryTest" should {
    val dictionary = new Dictionary("resources/test/word_list.txt")

    "load the resource file as expected" in {
      dictionary.contains("BAT") should be(true)
    }

    "correctly identify some words are missing" in {
      dictionary.contains("CHIPS") should be(false)
    }

    "identify the highest scoring word as SAUSAGE" in {
      val topWord = dictionary.dictionaryOrderedByPoints.head.word
      topWord should be (Letter.fromString("SAUSAGE"))
    }
  }
}
