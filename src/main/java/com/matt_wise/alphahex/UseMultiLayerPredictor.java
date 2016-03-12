package com.matt_wise.alphahex;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Note: MLPC doesn't support save/load yet.  So to play against the model, we need to build it
 */
public class UseMultiLayerPredictor {
    public static void main(String args[]){
        String score = "1700";
        String dbfile = "/Users/matt/Documents/hex/"+score+"moves.csv";
        int hiddenNodes = 150;
        int maxIters = 300;
        boolean computerGoesFirst = true;

        SparkConf conf = new SparkConf()
                .setAppName("PlayHex");
        try {
            conf.get("spark.master");
        } catch (NoSuchElementException e){
            System.err.println("WARN: spark.master not set, using local");
            conf.setMaster("local[*]");
        }
        JavaSparkContext jsc = new JavaSparkContext(conf);
        SQLContext sql = new SQLContext(jsc);

        JavaRDD<LabeledPoint> dataRDD = jsc.textFile(dbfile)
                .flatMap(BuildMultiLayerPredictor::movesFromGame);
        DataFrame allData = sql.createDataFrame(dataRDD, LabeledPoint.class);

        MultilayerPerceptronClassificationModel model =
                BuildMultiLayerPredictor.doTraining(allData, hiddenNodes, maxIters, jsc, sql);

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
            } else if (inputmove.equals("RESTART") || inputmove.equals("RESET")){
                board = new HexBoard();
                double moveIndex = model.predict(board.toVector());
                String move = HexBoard.indexToMove(moveIndex);
                board.addMove(move, HexBoard.PLAYER_ONE);
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
