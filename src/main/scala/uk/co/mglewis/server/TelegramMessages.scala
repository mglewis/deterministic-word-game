package uk.co.mglewis.server

import uk.co.mglewis.core.GameState
import uk.co.mglewis.datamodel.Player.{Computer, Human}
import uk.co.mglewis.datamodel.{Player, Points, Swap}

object TelegramMessages {

  object Gifs {
    val swingingSword = "https://thumbs.gfycat.com/EnormousGreenLabradorretriever-mobile.mp4"
  }

  object Instructions {
    def introduction(userName: String): String =
      s"""
         |Greetings $userName
         |
         |Welcome to the ultimate word challenge! I am a very tough opponent to beat.
         |
         |You will score more points if you can find longer words containing rarer letters.
      """.stripMargin

    def gameStart(state: GameState): String = {
      val playerLetters = state.activePlayer.letters.map(_.char).mkString
      val conanLetters = state.opposingPlayer.letters.map(_.char).mkString
      val upcoming️Letters = state.remainingLetters.map(_.char).mkString.take(7)
      val swapLetters = playerLetters.take(2) + playerLetters.takeRight(1)

      s"""
         |As a benevolent barbarian I will let you move first.
         |
         |You start with these letters: $playerLetters
         |
         |My letters: $conanLetters
         |
         |The next 7 letters: $upcoming️Letters
         |
         |If you don't like your letters type '-SWAP $swapLetters'
         |
         |If you can't think of any words type '-PASS'
       """.stripMargin
    }

    def gameEnd(state: GameState): String = {
      s"""
         |And that is that. The game is over.
         |
         |${totalScoreSummary(state)}
         |
         |Type /start to battle once more.
       """.stripMargin
    }
  }

  object PlayerReplies {
    def startOfTurn(state: GameState): String = {
      val upcoming️Letters = state.remainingLetters.map(_.char).mkString.take(7)

      s"""
         |It is your move.
         |
         |Your letters: ${state.activePlayer.letters.map(_.char).mkString}
         |
         |The next ${upcoming️Letters.size} letters: $upcoming️Letters
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
    def notYourTurn: String = {
      s"""
         |It is my turn. I'm thinking!
       """.stripMargin
    }

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
