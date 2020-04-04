package uk.co.mglewis.datamodel

sealed trait Command

sealed trait TurnEndingAction

case class Play(
  played: Seq[Letter],
  unused: Seq[Letter]
) extends Command with TurnEndingAction {
  def word = played.map(_.char).mkString
}

case object Pass extends Command with TurnEndingAction

case class Swap(letters: Seq[Letter]) extends Command with TurnEndingAction

case class InvalidCommand(message: String) extends Command
