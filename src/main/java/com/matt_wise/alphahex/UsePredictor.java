package com.matt_wise.alphahex;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionModel;

import java.util.Scanner;

/**
 * This leverages the first predictor, all relavent code has been moved
 * to the MultiLayerPredictor classes.
 */
public class UsePredictor {
    public static void main(String args[]){
        String score = "1500";
        boolean computerGoesFirst = true;
        String modelPath = "/Users/matt/Documents/hex/"+score+"model";

        SparkConf conf = new SparkConf()
                .setAppName("PlayHex")
                .setMaster("local[*]");
        SparkContext sc = new SparkContext(conf);

        LogisticRegressionModel model = LogisticRegressionModel.load(sc, modelPath);

        HexBoard board = new HexBoard();
        boolean play = true;

        Scanner in = new Scanner(System.in);

        if (computerGoesFirst){
            double moveIndex = model.predict(board.toVector());
            String move = HexBoard.indexToMove(moveIndex);
            board.addMove(move, HexBoard.PLAYER_ONE);
        }


        while (play){
            board.printBoard();
            System.out.println("You are blue, play is East West, 'STOP' to quit");
            System.out.print("Your move: ");
            String inputmove = in.next().trim();
            if (inputmove.equals("STOP")){
                play = false;
                continue;
            }
            board.addMove(inputmove, HexBoard.PLAYER_TWO);

            //Computer's turn
            double moveIndex = model.predict(board.toVector());
            String move = HexBoard.indexToMove(moveIndex);
            board.addMove(move, HexBoard.PLAYER_ONE);
            System.out.println("Computer moved at: " + move);
        }


    }
}
