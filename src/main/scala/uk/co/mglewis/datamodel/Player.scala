package uk.co.mglewis.datamodel

case class Player(
  name: String,
  totalScore: Int,
  letters: Seq[Letter],
  lastAction: TurnEndingAction
)

object Player {
  def create(name: String, letters: Seq[Letter]) =
    Player(
      name = name,
      totalScore = 0,
      letters = letters,
      lastAction = Pass(Seq.empty)
    )
}

