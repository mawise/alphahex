# AlphaHex

Not really.  I enjoyed learning about [AlphaGo](https://deepmind.com/alpha-go.html), so I decided to try building a simple move predictor for my favorite game, [Hex](https://en.wikipedia.org/wiki/Hex_(board_game)).

I decided to use [Apache Spark](http://spark.apache.org/) because I have some experience with it and the Spark [machine learning libraries](http://spark.apache.org/docs/latest/mllib-guide.html) are pretty dead-simple to use.

## Status

You can train the model against a database of games.  The games don't need to be complete.
You can play against the model, the computer must go first and you cannot swap. (yet).
The model is predicting what a human player would do based on the data it has trained.  This means that a board state as input generates a move as output.  The model doesn't have any idea of the rules of Hex, so it doesn't know it cannot play in a cell that is occupied.  Unlike AlphaGo, the library I'm using only generates a move, it doesn't generate a probability distribution over moves, so there is no quick-and-dirty way to get the computer to choose a different cell, since it can't play in an occupied cell.  My game database existed in SQLite, so I have some code for converting that to a list of comma-separated moves.  That's the format you'll want to use.
