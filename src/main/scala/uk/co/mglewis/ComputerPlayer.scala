package uk.co.mglewis

import uk.co.mglewis.datamodel.Player.ActionAndPoints
import uk.co.mglewis.datamodel.{Letter, Pass, Play, Points, Swap, TurnEndingAction}
import uk.co.mglewis.validation.AvailableLetterValidation

object ComputerPlayer {

  // Maximises currentTurnPoints + nextTurnPoints
  def chooseAction(
    playerLetters: Seq[Letter],
    remainingLetters: Seq[Letter],
    dictionary: Dictionary
  ): TurnEndingAction = {
    val currentTurnActions = allRelevantPlays(playerLetters, dictionary) ++ allPossibleSwaps(playerLetters)

    val allPossibleActionsWithOptimalFollowUpPlay = currentTurnActions.map { initialAction =>
      val newAvailableLetters = GameState.dealLettersToPlayer(
        initialAction.action,
        remainingLetters
      ).playerLetters

      val maybeBestFollowUpPlay = allRelevantPlays(newAvailableLetters, dictionary).headOption
      val followUpPoints = maybeBestFollowUpPlay.map(_.points).getOrElse(Points.zero)

      ActionAndPoints(initialAction.action, initialAction.points + followUpPoints)
    }

    val maybeOptimalAction = allPossibleActionsWithOptimalFollowUpPlay.sortBy(_.points).headOption
    maybeOptimalAction.map(_.action).getOrElse(Pass(playerLetters))
  }

  private def allRelevantPlays(
    availableLetters: Seq[Letter],
    dictionary: Dictionary
  ): Seq[ActionAndPoints] = {
    val allPossiblePlays = dictionary.wordsOrderedByPoints.par.flatMap { entry =>
      val validationResult = AvailableLetterValidation.validate(entry.word, availableLetters)
      if (validationResult.isValid) {
        val play = Play(played = validationResult.usedLetters, unused = validationResult.unusedLetters)
        Some(ActionAndPoints(play, Points.calculate(validationResult.usedLetters)))
      } else None
    }.seq

    discardEquivalentPlays(allPossiblePlays)
  }

  // It doesn't matter if we play [BAT] or [TAB] so we can eliminate one in the name of performance
  private def discardEquivalentPlays(
    plays: Seq[ActionAndPoints]
  ): Seq[ActionAndPoints] = {
    plays.foldLeft(Seq.empty[ActionAndPoints]) { (init, last) =>
      if (init.map(_.action.played.sorted).contains(last.action.played.sorted)) {
        init
      } else init :+ last
    }
  }

  private def allPossibleSwaps(
    availableLetters: Seq[Letter]
  ): Set[ActionAndPoints] = {
    case class LetterWthIndex(letter: Letter, index: Int)

    // we use LetterWithIndex to prevent duplicate letters being removed when we determine the subsets
    val letterSet = availableLetters.zipWithIndex.map(LetterWthIndex.tupled).toSet
    val letterSubsets = letterSet.subsets.filter(_.nonEmpty)

    val allSwaps = for {
      subset <- letterSubsets
      played = subset.toSeq.map(_.letter)
      unused = availableLetters.diff(played)
      swap = Swap(played = played, unused = unused)
    } yield ActionAndPoints(swap, Points(0))

    allSwaps.toSet
  }

}
