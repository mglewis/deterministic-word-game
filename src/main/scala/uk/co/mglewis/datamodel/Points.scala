package uk.co.mglewis.datamodel

case class Points(
  value: Int
) extends Ordered[Points] {
  def +(that: Points): Points = {
    Points(value + that.value)
  }

  def compare(that: Points): Int = that.value.compareTo(value)

  override def toString: String = {
    s"$value"
  }
}

object Points {
  val zero = Points(0)

  def calculate(letters: Seq[Letter]): Points = {
    val bonus = if (letters.length == Letter.maxLetters) Points(50) else zero
    Points(letters.map(_.points).sum) + bonus
  }
}
