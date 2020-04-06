package uk.co.mglewis.datamodel

sealed trait Command

sealed trait TurnEndingAction extends Command {
  val played: Seq[Letter]
  val unused: Seq[Letter]
}

case class Play(
  played: Seq[Letter],
  unused: Seq[Letter]
) extends TurnEndingAction {
  def word = played.map(_.char).mkString
}

case class Pass(
  unused: Seq[Letter]
) extends TurnEndingAction {
  val played = Seq.empty
}

case class Swap(
  played: Seq[Letter],
  unused: Seq[Letter]
) extends TurnEndingAction

case class InvalidCommand(message: String) extends Command
