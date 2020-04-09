package uk.co.mglewis.core

import uk.co.mglewis.core.Dictionary.Word
import uk.co.mglewis.datamodel.Letter
import uk.co.mglewis.validation.AvailableLetterValidation
import uk.co.mglewis.validation.AvailableLetterValidation.ValidationResult

import scala.collection.parallel.immutable.ParSet
import scala.io.Source

class Dictionary(
  filePath: String
) {
  val words: ParSet[Word] = build(filePath)

  def contains(word: String): Boolean = {
    val pattern = word.toUpperCase.replaceAll("\\?", "[A-Z]")
    words.exists(_.toString.matches(pattern))
  }

  def allValidWords(availableLetters: Seq[Letter]): Set[ValidationResult] = {
    AvailableLetterValidation.validSubset(words, availableLetters)
  }

  private def build(filePath: String): ParSet[Word] = {
    val file = Source.fromFile(filePath)
    val reader = file.bufferedReader
    val words = Stream.continually(reader.readLine()).takeWhile(_ != null).toSet
    reader.close()
    file.close()

    words.par.map { w =>
      Word(Letter.fromString(w))
    }
  }
}

object Dictionary {
  case class Word(
    letters: Seq[Letter]
  ) {
    val toCharSeq: Seq[Char] = letters.map(_.char)
    override val toString: String = toCharSeq.mkString
  }
}
