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
    val currentTurnActions =
      allRelevantPlays(playerLetters, dictionary) ++ allPossibleSwaps(playerLetters, remainingLetters.size)

    val allPossibleActionsWithOptimalFollowUpPlay = currentTurnActions.map { initialAction =>
      val newAvailableLetters = GameState.dealLettersToPlayer(
        initialAction.action,
        remainingLetters
      ).playerLetters

      val maybeBestFollowUpPlay = allRelevantPlays(newAvailableLetters, dictionary).headOption
      val followUpPoints = maybeBestFollowUpPlay.map(_.points).getOrElse(Points.zero)

      ActionAndPoints(initialAction.action, initialAction.points + followUpPoints)
    }

    val maybeOptimalAction = allPossibleActionsWithOptimalFollowUpPlay.toSeq.sortBy(_.points).headOption
    maybeOptimalAction.map(_.action).getOrElse(Pass(playerLetters))
  }

  private def allRelevantPlays(
    availableLetters: Seq[Letter],
    dictionary: Dictionary
  ): Set[ActionAndPoints] = {
    val allPlayableWords = dictionary.allValidWords(availableLetters)

    val plays = allPlayableWords.map { validationResult =>
      val play = Play(played = validationResult.usedLetters, unused = validationResult.unusedLetters)
      ActionAndPoints(play, Points.calculate(validationResult.usedLetters))
    }

    discardEquivalentPlays(plays)
  }

  // It doesn't matter if we play [BAT] or [TAB] so we can eliminate one in the name of performance
  private def discardEquivalentPlays(
    plays: Set[ActionAndPoints]
  ): Set[ActionAndPoints] = {
    plays.foldLeft(Set.empty[ActionAndPoints]) { (init, last) =>
      if (init.map(_.action.played.sorted).contains(last.action.played.sorted)) {
        init
      } else init + last
    }
  }

  private def allPossibleSwaps(
    availableLetters: Seq[Letter],
    remainingLetters: Int
  ): Set[ActionAndPoints] = {
    case class LetterWithIndex(letter: Letter, index: Int)

    // We use LetterWithIndex to prevent duplicate letters being removed when we determine the subsets
    val letterSet = availableLetters.zipWithIndex.map(LetterWithIndex.tupled).toSet
    val letterSubsets = letterSet.subsets.filter(_.nonEmpty)

    val swaps = for {
      subset <- letterSubsets
      if subset.size <= remainingLetters
      played = subset.toSeq.map(_.letter).sorted
      unused = availableLetters.diff(played).sorted
      swap = Swap(played = played, unused = unused)
    } yield ActionAndPoints(swap, Points.zero)

    swaps.toSet
  }

}
