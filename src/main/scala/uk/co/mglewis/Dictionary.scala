package uk.co.mglewis

import uk.co.mglewis.Dictionary.LettersAndPoints
import uk.co.mglewis.datamodel.{Letter, Points}

import scala.io.Source

class Dictionary(
  filePath: String
) {

  private val dictionary: Set[String] = Dictionary.build(filePath)

  val dictionaryOrderedByPoints: Seq[LettersAndPoints] = Dictionary.buildWithPoints(dictionary)

  def contains(word: String): Boolean = {
    val pattern = word.toUpperCase.replaceAll("\\?", "[A-Z]")
    dictionary.exists(_.matches(pattern))
  }
}

object Dictionary {

  // TODO: eventually make this private
  case class LettersAndPoints(
    word: Seq[Letter],
    orderedLetters: Seq[Letter],
    points: Points
  )

  private def build(filePath: String): Set[String] = {
    val file = Source.fromFile(filePath)
    val reader = file.bufferedReader
    val dictionary = Stream.continually(reader.readLine()).takeWhile(_ != null).toSet
    reader.close()
    file.close()

    dictionary
  }

  private def buildWithPoints(dictionary: Set[String]): Seq[LettersAndPoints] = {
    dictionary.map { word =>
      val letters = Letter.fromString(word)
      val lettersInAlphabeticalOrder = letters.sorted
      val points = Points.calculate(lettersInAlphabeticalOrder)
      LettersAndPoints(letters, lettersInAlphabeticalOrder, points)
    }.toSeq.sortBy(_.points)
  }
}
