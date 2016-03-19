package com.matt_wise.alphahex;

import org.apache.spark.ml.ann.FeedForwardTopology;
import org.apache.spark.ml.ann.TopologyModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.mllib.linalg.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a sloppy wrapper around the MultiLayerPerceptronClassificationModel
 * It provides:
 *  * serialization to/from disk
 *  * access to the underlying FeedForwardTopology model
 *      (which lets us get all the output values instead of just a prediction)
 */
public class SuperMLPC implements Serializable{
    private int[] layers;
    private Vector weights;
    private static final long serialVersionUID = 1L;

    public SuperMLPC(int[] layers, Vector weights) {
        this.layers = layers;
        this.weights = weights;
    }

    public SuperMLPC(MultilayerPerceptronClassificationModel model){
        this.layers = model.layers();
        this.weights = model.weights();
    }

    public TopologyModel getTopoModel(){
        return FeedForwardTopology.multiLayerPerceptron(layers, true).getInstance(weights);
    }

    public void saveToDisk(String path) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SuperMLPC loadFromDisk(String path){
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            return (SuperMLPC) obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static double decodePredictionVector(Vector prediction){
        double[] vector = prediction.toArray();
        int length = vector.length;
        int best = 0;
        for (int i=0; i<length; i++){
            if (vector[i] > vector[best]){
                best = i;
            }
        }
        return best;
    }



}
