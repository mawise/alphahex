package com.matt_wise.alphahex;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.ann.FeedForwardTopology;
import org.apache.spark.ml.ann.TopologyModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Note: MLPC doesn't support save/load yet.  So to play against the model, we need to build it
 */
public class UseMultiLayerPredictor {
    public static void main(String args[]){
        String modelPath = "src/main/resources/supermlpc.serialized";
        boolean computerGoesFirst = true;

        TopologyModel model = SuperMLPC.loadFromDisk(modelPath).getTopoModel();

        HexBoard board = new HexBoard();
        boolean play = true;

        Scanner in = new Scanner(System.in);

        if (computerGoesFirst){
            double moveIndex = SuperMLPC.decodePredictionVector(model.predict(board.toVector()));
            String move = HexBoard.indexToMove(moveIndex);
            board.addMove(move, HexBoard.PLAYER_ONE);
        }

        while (play){
            board.printBoard();
            System.out.println("You are blue, play is East West, 'STOP' to quit");
            System.out.print("Your move: ");
            String inputmove = in.next().trim();
            if (inputmove.equalsIgnoreCase("STOP")){
                play = false;
                continue;
            }
            board.addMove(inputmove, HexBoard.PLAYER_TWO);

            //Computer's turn
            double moveIndex = getLegalMove(model.predict(board.toVector()), board);
            String move = HexBoard.indexToMove(moveIndex);
            board.addMove(move, HexBoard.PLAYER_ONE);
            System.out.println("Computer moved at: " + move);
        }


    }

    public static int getLegalMove(Vector prediction, HexBoard board){
        double[] vector = prediction.toArray();
        int best = 0;
        for (int j=0; j<vector.length; j++){
            String move = HexBoard.indexToMove(j);
            if ((vector[j] > vector[best]) && board.isEmptyAt(move)){
                best = j;
            }
        }
        return best;
    }


}
