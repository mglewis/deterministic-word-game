package uk.co.mglewis.datamodel

import uk.co.mglewis.datamodel.Player.{ActionAndPoints, PlayerType}

case class Player(
  name: String,
  playerType: PlayerType,
  letters: Seq[Letter],
  actions: Seq[ActionAndPoints]
) {
  def totalScore: Points = actions.foldLeft(Points.zero)(_ + _.points)

  def lastAction: Option[TurnEndingAction] = actions.lastOption.map(_.action)

  def endOfTurnUpdate(
    newLetters: Seq[Letter],
    points: Points,
    action: TurnEndingAction
  ): Player = {
    Player(
      name = name,
      playerType = playerType,
      letters = newLetters,
      actions = actions :+ ActionAndPoints(action, points)
    )
  }
}

object Player {

  case class ActionAndPoints(action: TurnEndingAction, points: Points)

  sealed trait PlayerType
  case object Human extends PlayerType
  case object Computer extends PlayerType

  def create(
    name: String,
    playerType: PlayerType,
    letters: Seq[Letter]
  ): Player = {
    Player(
      name = name,
      playerType = playerType,
      letters = letters,
      actions = Seq.empty
    )
  }
}

