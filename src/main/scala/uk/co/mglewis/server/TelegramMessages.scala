package uk.co.mglewis.server

import uk.co.mglewis.core.GameState
import uk.co.mglewis.datamodel.Player.{Computer, Human}
import uk.co.mglewis.datamodel.{Player, Points, Swap}

object TelegramMessages {

  object Instructions {
    def introduction(userName: String): String =
      s"""
         |Hi $userName
         |
       |Do you wish to challenge me? I am a tough opponent to beat.
         |
       |You will score more points if you can find longer words containing rarer letters.
         |
       |Just message me with the text 'START' so battle may commence!
     """.stripMargin

    def gameStart(state: GameState): String = {
      val playerLetters = state.activePlayer.letters.map(_.char).mkString
      val conanLetters = state.opposingPlayer.letters.map(_.char).mkString
      val upcoming️Letters = state.remainingLetters.map(_.char).mkString.take(7)
      val swapLetters = playerLetters.take(2) + playerLetters.takeRight(1)

      s"""
         |So it begins. As a benevolent barbarian I will let you move first.
         |
       |You have been dealt the following letters: $playerLetters
         |
       |I have been dealt the following letters: $conanLetters
         |
       |The next 7 available letters are $upcoming️Letters
         |
       |If you want to swap some of your letters send me a message in the format '-SWAP $swapLetters'
         |
       |If you can't think of any words just type '-PASS'
       """.stripMargin
    }
  }

  object PlayerReplies {
    def startOfTurn(player: Player): String = {
      s"""
         |It is your move.
         |
       |You have the following letters available ${player.letters.map(_.char).mkString}
     """.stripMargin
    }

    def helpfulMessage(state: GameState): String = {
      val playerLetters = state.activePlayer.letters.map(_.char).mkString
      val swapLetters = playerLetters.take(2) + playerLetters.takeRight(1)

      s"""
         |I'm having some difficulty understanding your gibberish.
         |
       |All you have to do is play a valid word from your letters $playerLetters
         |
       |Should you find that too difficult you can also swap some of your letters by typing '-SWAP $swapLetters'
         |
       |If you are a coward you can also pass your turn with '-PASS'. And end the game with '-QUIT'
     """.stripMargin
    }

    def turnPassed(state: GameState): String = {
      s"""
         |I hope you know what you're doing.
         |
       |You've passed your turn and thus scored 0 points.
         |
       |${totalScoreSummary(state)}
     """.stripMargin
    }

    def lettersSwapped(player: Player, swap: Swap): String = {
      s"""
         |You have chosen to swap ${swap.played.map(_.char).mkString}
         |
       |Your new set of letters is ${player.letters.map(_.char).mkString}
     """.stripMargin
    }

    def validWord(word: String, points: Points): String = {
      s"""
         |$word has scored you ${points.value}
     """.stripMargin
    }

    def invalidWord(word: String): String = {
      s"""
         |Hah! You really thought $word was in the tome of permissible words?
         |
       |Think again.
         |
       |You scored 0 this turn.
     """.stripMargin
    }
  }

  object ComputerReplies {
    def computerAction(state: GameState): String = {
      val lastAction = state.opposingPlayer.actions.lastOption.getOrElse(
        throw new RuntimeException("This method cannot be called unless the computer player has already made a move")
      )
      val actionText =  s"${lastAction.action.name} ${lastAction.action.played.map(_.char).mkString}".trim

      s"""
         |I have chosen to $actionText scoring me ${lastAction.points}
         |
       |${totalScoreSummary(state)}
     """.stripMargin
    }
  }

  def totalScoreSummary(state: GameState): String = {
    val (human, computer) = (state.activePlayer.playerType, state.opposingPlayer.playerType) match {
      case (Human, Computer) => (state.activePlayer, state.opposingPlayer)
      case (Computer, Human) => (state.opposingPlayer, state.activePlayer)
      case (_, _) => throw new RuntimeException("You are clearly not in a human vs computer contest!")
    }
    s"""
       |${human.name}: ${human.totalScore}
       |${computer.name}: ${computer.totalScore}
     """.stripMargin
  }

}
