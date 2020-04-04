package uk.co.mglewis.datamodel

case class Letter(
  char: Char,
  points: Int,
) {
  private def copy(i: Int): Seq[Letter] = {
    (1 to i).map { _ => this }
  }

  override def toString: String = s"$char[$points]"
}

object Letter {
  val startingLetters = Seq(
    Letter('A', 1).copy(9),
    Letter('B', 3).copy(2),
    Letter('C', 3).copy(2),
    Letter('D', 2).copy(4),
    Letter('E', 1).copy(12),
    Letter('F', 2).copy(2),
    Letter('G', 2).copy(3),
    Letter('H', 4).copy(2),
    Letter('I', 1).copy(9),
    Letter('J', 8).copy(1),
    Letter('K', 5).copy(1),
    Letter('L', 1).copy(4),
    Letter('M', 3).copy(2),
    Letter('N', 1).copy(6),
    Letter('O', 1).copy(8),
    Letter('P', 3).copy(2),
    Letter('Q', 10).copy(1),
    Letter('R', 1).copy(6),
    Letter('S', 1).copy(4),
    Letter('T', 1).copy(6),
    Letter('U', 1).copy(4),
    Letter('V', 4).copy(1),
    Letter('W', 4).copy(2),
    Letter('X', 8).copy(1),
    Letter('Y', 4).copy(2),
    Letter('Z', 10).copy(1),
  ).flatten

  val distinctLetters = startingLetters.toSet

  val validCharacters = distinctLetters.map(_.char)

  def fromString(string: String): Seq[Letter] = string.map(toLetter)

  private def toLetter(char: Char): Letter = {
    distinctLetters.find(_.char == char)
      .getOrElse(throw new RuntimeException(s"Character $char cannot be converted to a valid letter!"))
  }

  def validString(string: String): Boolean = string.map(validCharacters.contains).reduce(_ && _)
}





