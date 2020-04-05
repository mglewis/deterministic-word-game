package uk.co.mglewis

import scala.io.Source

class Dictionary(
  filePath: String
) {

  private val dictionary: Set[String] = build()

  private def build(): Set[String] = {
    val file = Source.fromFile(filePath)
    val reader = file.bufferedReader
    val dictionary = Stream.continually(reader.readLine()).takeWhile(_ != null).toSet
    reader.close()
    file.close()

    dictionary
  }

  def contains(word: String): Boolean = {
    val pattern = word.toUpperCase.replaceAll("\\?", "[A-Z]")
    dictionary.exists(_.matches(pattern))
  }
}
