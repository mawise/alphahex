package com.matt_wise.alphahex;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 3/6/16.
 */
public class BuildPredictor {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf()
                .setAppName("BuildHexPredictor")
                .setMaster("local[*]");
        SparkContext sc = new SparkContext(conf);
        //JavaSparkContext jsc = new JavaSparkContext(conf);

        String score = "1500";
        String dbfile = "/Users/matt/Documents/hex/"+score+"moves.csv";
        String modelOutput = "/Users/matt/Documents/hex/"+score+"model";

        //No idea what the scala .textFile needs an int for, or what that 1 does
        //maybe it's number of partitions?  or number of files?
        JavaRDD<LabeledPoint> data = sc.textFile(dbfile, 1).toJavaRDD()
                .flatMap(BuildPredictor::movesFromGame);

        //JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sc, inputFile).toJavaRDD();

        // Split initial RDD into two... [60% training data, 40% testing data].
        JavaRDD<LabeledPoint>[] splits = data.randomSplit(new double[] {0.99, 0.01}, 11L);
        JavaRDD<LabeledPoint> training = splits[0].cache();
        JavaRDD<LabeledPoint> test = splits[1];

        // Run training algorithm to build the model.
        final LogisticRegressionModel model = new LogisticRegressionWithLBFGS()
                .setNumClasses(122)
                .run(training.rdd());

        // Compute raw scores on the test set.
        JavaRDD<Tuple2<Object, Object>> predictionAndLabels = test.map(p -> {
                    Double prediction = model.predict(p.features());
                    return new Tuple2<>(prediction, p.label());
                }
        );

        // Get evaluation metrics.
        MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels.rdd());
        double precision = metrics.precision();
        System.out.println("Precision = " + precision);
        model.save(sc, modelOutput);

    }

    public static List<LabeledPoint> movesFromGame(String movelist){
        HexBoard board = new HexBoard();
        List<LabeledPoint> out = new ArrayList<>();
        byte player = HexBoard.PLAYER_ONE;
        for (String m : movelist.split(",")){
            if (m.equals("SWAP")){
                out.add(new LabeledPoint(HexBoard.SWAP_INDEX, board.toVector()));
            } else {
                out.add(new LabeledPoint(
                        Integer.parseInt(HexBoard.indexOfMoveForPlayerOne(m, player)),
                        board.withPlayerOneToMove(player).toVector()));
                board.addMove(m, player);
                player = HexBoard.otherPlayer(player);
            }
        }
        return out;
    }
}
