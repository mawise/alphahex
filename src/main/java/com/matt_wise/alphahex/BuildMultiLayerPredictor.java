package com.matt_wise.alphahex;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.ann.FeedForwardTopology;
import org.apache.spark.ml.ann.TopologyModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Note: The MLPC doesn't yet support saving the model, see: https://github.com/apache/spark/pull/9854
 *
 * This
 */
public class BuildMultiLayerPredictor {
    public static void main(String[] args) {

        SparkConf conf = new SparkConf()
                .setAppName("BuildHexPredictor");
        try {
            conf.get("spark.master");
        } catch (NoSuchElementException e) {
            System.err.println("WARN: spark.master not set, using local");
            conf.setMaster("local[*]");
        }

        JavaSparkContext jsc = new JavaSparkContext(conf);
        SQLContext sql = new org.apache.spark.sql.SQLContext(jsc);

        List<String> numberOutput = new ArrayList<>();
        int iterations = 300;
        int layerSize = 150;
        String score = "1700";
        String dbfile = "/Users/matt/Documents/hex/" + score + "moves.csv";
        String saveModelPath = "/Users/matt/Documents/hex/" + score + "Topology.model";

        JavaRDD<LabeledPoint> dataRDD = jsc.textFile(dbfile)
                .flatMap(BuildMultiLayerPredictor::movesFromGame);

        DataFrame dataFrame = sql.createDataFrame(dataRDD, LabeledPoint.class);

// Split the data into train and test
        DataFrame[] splits = dataFrame.randomSplit(new double[]{0.99, 0.01}, 1234L);
        DataFrame train = splits[0];
        DataFrame test = splits[1];


        long startTime = System.currentTimeMillis();

        MultilayerPerceptronClassificationModel model = doTraining(train, layerSize, iterations, jsc, sql);

        SuperMLPC savableModel = new SuperMLPC(model);
        savableModel.saveToDisk(saveModelPath);

        // compute precision on the test set
        DataFrame result = model.transform(test);
        DataFrame predictionAndLabels = result.select("prediction", "label");
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setMetricName("precision");
        double precision = evaluator.evaluate(predictionAndLabels);
        // $example off
        numberOutput.add("minscore: " + score +
                ", iterations: " + iterations +
                ", layers: " + layerSize +
                ", precision: " + precision +
                ", time: " + (System.currentTimeMillis() - startTime));

        jsc.stop();

        for (String out : numberOutput) {
            System.out.println(out);
        }
    }

    public void saveMLPC(MultilayerPerceptronClassificationModel model, String path){
        int[] layers = model.layers();
        Vector weights = model.weights();
    }

    public static MultilayerPerceptronClassificationModel doTraining(
            DataFrame train,
            int hiddenLayerSize,
            int maxIters,
            JavaSparkContext jsc,
            SQLContext sql){

        // specify layers for the neural network:
        // input layer of size 121 (11x11 features), two intermediate
        // and output of size 122 (possible moves, 11x11 + SWAP)
        int[] layers = new int[]{122, hiddenLayerSize, 122};
        // create the trainer and set its parameters
        MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setBlockSize(512) // ? sample uses 128, source code recommends 10-1000
                .setSeed(1234L)
                .setMaxIter(maxIters);
        // train the model
        MultilayerPerceptronClassificationModel model = trainer.fit(train);
        return model;

    }

    public static List<LabeledPoint> movesFromGame(String movelist){
        HexBoard board = new HexBoard();
        List<LabeledPoint> out = new ArrayList<>();
        byte player = HexBoard.PLAYER_ONE;
        for (String m : movelist.split(",")){
            if (m.equals("SWAP")){
                out.add(new LabeledPoint(HexBoard.SWAP_INDEX, board.toVector()));
                out.add(new LabeledPoint(HexBoard.SWAP_INDEX, board.rotate().toVector()));

            } else {
                out.add(new LabeledPoint(
                        Integer.parseInt(HexBoard.indexOfMoveForPlayerOne(m, player)),
                        board.withPlayerOneToMove(player).toVector()));
                out.add(new LabeledPoint(
                        Integer.parseInt(HexBoard.indexOfMoveForPlayerOne(m, player)),
                        board.rotate().withPlayerOneToMove(player).toVector()));
                board.addMove(m, player);
                player = HexBoard.otherPlayer(player);
            }
        }
        return out;
    }
}
