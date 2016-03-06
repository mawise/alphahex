package com.matt_wise.alphahex;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.List;


/***
 * This didn't work.  It generated a file that looked right, but when reading the file
 * I got an error about the indexes needing to be in ascending order.  So now BuildPredictor
 * uses the raw CSV file of the games and generates LabeledPoints by directly constructing
 * a SparseVector
 */
class CsvGameToLibSvm {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf()
                .setAppName("ToLibSVM")
                .setMaster("local[*]");
        JavaSparkContext sc = new JavaSparkContext(conf);


        String score = "1700";
        String dbfile = "/Users/matt/Documents/hex/"+score+"moves.csv";
        String outputFile = "/Users/matt/Documents/hex/"+score+"training.libsvm";

        sc.textFile(dbfile)
        .flatMap(CsvGameToLibSvm::movesFromGame)
        .saveAsTextFile(outputFile);
    }

    /***
     * input is a comma-delimeted list of moves
     * output is a libsvm style set of board positions and next move
     *
     * @param movelist
     * @return
     */
    public static List<String> movesFromGame(String movelist){
        HexBoard board = new HexBoard();
        List<String> out = new ArrayList<>();
        byte player = HexBoard.PLAYER_ONE;
        for (String m : movelist.split(",")){
            if (m.equals("SWAP")){
                out.add(HexBoard.SWAP_INDEX + " " + board.toSparseLibSvmVector());
            } else {
                if (board.isEmpty()){
                    out.add(HexBoard.indexOfMoveForPlayerOne(m, player));
                } else {
                    out.add(
                            HexBoard.indexOfMoveForPlayerOne(m, player) + " " +
                                    board.withPlayerOneToMove(player).toSparseLibSvmVector());
                }
                board.addMove(m, player);
                player = HexBoard.otherPlayer(player);
            }
        }
        return out;
    }
}