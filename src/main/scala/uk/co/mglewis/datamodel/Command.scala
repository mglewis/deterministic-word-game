package uk.co.mglewis.datamodel

sealed trait Command

sealed trait TurnEndingAction extends Command {
  val played: Seq[Letter]
  val unused: Seq[Letter]

  def name: String
}

case class Play(
  played: Seq[Letter],
  unused: Seq[Letter]
) extends TurnEndingAction {
  def name = "Play"

  def word = played.map(_.char).mkString
}

case class Pass(
  unused: Seq[Letter]
) extends TurnEndingAction {
  def name = "Pass"

  val played = Seq.empty
}

case class Swap(
  played: Seq[Letter],
  unused: Seq[Letter]
) extends TurnEndingAction {
  def name = "Swap"
}

case class InvalidCommand(message: String) extends Command
