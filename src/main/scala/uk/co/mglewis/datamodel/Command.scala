package uk.co.mglewis.datamodel

sealed trait Command

sealed trait TurnEndingAction {
  val played: Seq[Letter]
  val unused: Seq[Letter]
}

case class Play(
  played: Seq[Letter],
  unused: Seq[Letter]
) extends Command with TurnEndingAction {
  def word = played.map(_.char).mkString
}

case class Pass(
  unused: Seq[Letter]
) extends Command with TurnEndingAction {
  val played = Seq.empty
}

case class Swap(
  played: Seq[Letter],
  unused: Seq[Letter]
) extends Command with TurnEndingAction

case class InvalidCommand(message: String) extends Command
