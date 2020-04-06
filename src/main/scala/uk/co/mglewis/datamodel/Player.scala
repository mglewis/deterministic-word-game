package uk.co.mglewis.datamodel

import uk.co.mglewis.datamodel.Player.PlayerType

case class Player(
  name: String,
  playerType: PlayerType,
  totalScore: Points,
  letters: Seq[Letter],
  lastAction: TurnEndingAction
) {
  def endOfTurnUpdate(
    pointsScored: Points,
    newLetters: Seq[Letter],
    action: TurnEndingAction
  ): Player = {
    Player(
      name = name,
      playerType = playerType,
      totalScore = totalScore + pointsScored,
      letters = newLetters,
      lastAction = action
    )
  }
}

object Player {

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
      totalScore = Points.zero,
      letters = letters,
      lastAction = Play(Seq.empty, Seq.empty)
    )
  }
}

