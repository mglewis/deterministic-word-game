### Ever thought scrabble was ruined by too much randomness?

Well, good news! This silly app should solve your problem.

### How does it work?

Get points for spelling words out of your assigned letters. The longer the word and the 
rarer the letters the more points you will score. As you can see the queue of upcoming 
letters, good players will adopt a strategy of aggressive letter swapping and looking 
ahead to future turns.

You can play either on Telegram by messaging @conan_word_bot or on your command line.

### Running Locally

The application is a pretty standard sbt scala application. 

You can run the unit tests:

```
sbt test
```

And start up the game server:

```
sbt run
```

If you'd like to kick off a human vs computer match on your command line you can do so 
with the following:

```
sbt "runMain uk.co.mglewis.cli.HumanVsComputer"
```

This will give you a prompt to pick your word of choice

```
[info] running uk.co.mglewis.cli.HumanVsComputer

Matty: 0 pts
Letters: IM?NENE
Upcoming: DEGOWLE

> n?minee
Play N?MINEE scored 58

Conan: 0 pts
Letters: DNAETBO
Upcoming: ZAGNREI

Play BATONED scored 60

Matty: 58 pts
Letters: DEGOWLE
Upcoming: IBJUESS

> ...
```

You can also pitch the computer player against itself:

```
sbt "runMain uk.co.mglewis.cli.ComputerVsComputer"
```

### Attribution

Conan pixel art by Gwendal Uguen. Used for @conan_word_bot telegram avatar.
https://www.flickr.com/photos/gwendalcentrifugue/18166005906
