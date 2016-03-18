package com.matt_wise.alphahex;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.ann.FeedForwardTopology;
import org.apache.spark.ml.ann.TopologyModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by matt on 3/17/16.
 */
public class TestSuperMLPC {

    @Test
    public void superMlpcGivesSamePredictions() throws IOException {
        SparkConf conf = new SparkConf()
                .setAppName("JavaMultilayerPerceptronClassifierExample")
                .setMaster("local[*]");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        SQLContext jsql = new SQLContext(jsc);

        // $example on$
        // Load training data
        String path = "/opt/spark/data/mllib/sample_multiclass_classification_data.txt";
        DataFrame dataFrame = jsql.read().format("libsvm").load(path);

        dataFrame.printSchema();

        // Split the data into train and test
        DataFrame[] splits = dataFrame.randomSplit(new double[]{0.6, 0.4}, 1234L);
        DataFrame train = splits[0];
        DataFrame test = splits[1];
        // specify layers for the neural network:
        // input layer of size 4 (features), two intermediate of size 5 and 4
        // and output of size 3 (classes)
        int[] layers = new int[] {4, 4, 3};//{4, 5, 4, 3};
        // create the trainer and set its parameters
        MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setBlockSize(128)
                .setSeed(1234L)
                .setMaxIter(100);
        // train the model
        MultilayerPerceptronClassificationModel model = trainer.fit(train);

        SuperMLPC superMLPC = new SuperMLPC(model);
        TopologyModel topo = superMLPC.getTopoModel();

        //test save/load
        File tmpFile = File.createTempFile("saved-model", ".tmp");
        String tmpPath = tmpFile.getAbsolutePath();
        superMLPC.saveToDisk(tmpPath);

        SuperMLPC loadedMlpc = SuperMLPC.loadFromDisk(tmpPath);
        TopologyModel loadedTopo = loadedMlpc.getTopoModel();

        train.select("features")
                .toJavaRDD()
                .map(row -> (Vector) row.get(0))
                .collect()
                .stream()
                .forEach(v -> {
                    double topoResult = SuperMLPC.decodePredictionVector(topo.predict(v));
                    double modelResult = model.predict(v);
                    double loadedResult = SuperMLPC.decodePredictionVector(loadedTopo.predict(v));

                   // System.out.println("Comparing: " + topoResult + " and " + modelResult + " and " + loadedResult);
                    Assert.assertEquals(topoResult, modelResult);
                    Assert.assertEquals(modelResult, loadedResult);
                });
        jsc.stop();
    }
}
