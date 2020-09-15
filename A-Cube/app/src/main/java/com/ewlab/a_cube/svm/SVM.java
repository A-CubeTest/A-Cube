package com.ewlab.a_cube.svm;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SVM {

    private static final String TAG = SVM.class.getName();


    private svm_parameter param;

    private HashMap<String, HashSet<float[]>> map;

    private ArrayList<String> keys;

    private int kFold = 10;

    private double precision = 0, recall = 0, f1 = 0;

    public SVM(HashMap<String, HashSet<float[]>> map){
        this.map = map;

        param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.gamma = 0.9;
        param.eps = 0.001;
        param.C = 50;
        //param.shrinking = 0;
        //param.probability = 1;
        param.nr_weight = map.keySet().size();
        int[] weight = new int[map.keySet().size()];
        for(int i = 0; i < weight.length; i++)
            weight[i] = i;

        double[] weightP = new double[map.keySet().size()];
        keys = new ArrayList<>(map.keySet());
        for(int i = 0; i < weightP.length; i++)
            weightP[i] = (1.0/map.get(keys.get(i)).size());

        param.weight_label = weight;
        param.weight = weightP;

    }

    // this method allows to do cross-validation
    // from data set, it creates the training and test set
    // and makes training and prediction
    // it calculates the precision, recall and f1 of the cross - validation.
    public svm_model runModel() {

        svm_model model = null;

        for (int count = 0; count < kFold; count++) {

            HashMap<String, List<float[]>> training = new HashMap<>();
            HashMap<String, List<float[]>> test = new HashMap<>();

            int trainingSize = 0, testSize = 0;

            for (String k : keys) {
                List<float[]> windows = new ArrayList<>(map.get(k));
                Collections.shuffle(windows);
                List<float[]> train = new ArrayList<>();
                train.addAll(windows.subList(0, (windows.size() - (windows.size() / 10)))); //cambiato valore di test. Adesso Ã¨ sempre 1/10 * |training|
                training.put(k, train);
                trainingSize += training.get(k).size();
                windows.removeAll(training.get(k));
                test.put(k, windows);
                testSize += test.get(k).size();
            }

            //preparo il modello con i valori del training
            svm_problem probTrain = new svm_problem();
            probTrain.y = new double[trainingSize];
            probTrain.l = trainingSize;
            probTrain.x = new svm_node[trainingSize][];

            int index = 0, label = 0;
            for (String ks : keys) {
                for (float[] f : training.get(ks)) {
                    probTrain.x[index] = new svm_node[f.length];
                    for (int i = 0; i < f.length; i++) {
                        svm_node node = new svm_node();
                        node.index = i;
                        node.value = f[i];
                        probTrain.x[index][i] = node;
                    }
                    probTrain.y[index++] = label;
                }
                label++;
            }

            //training
            Log.d(TAG, "TRAINING START");
            model = svm.svm_train(probTrain, param);
            Log.d(TAG, "TRAINING END");

            //test set
            index = 0;
            svm_node[][] nodes = new svm_node[testSize][];
            for (String ks : keys) {
                for (float[] f : test.get(ks)) {
                    nodes[index] = new svm_node[f.length];
                    for (int i = 0; i < f.length; i++) {
                        svm_node node = new svm_node();
                        node.index = i;
                        node.value = f[i];
                        nodes[index][i] = node;
                    }
                    index++;
                }
            }

            //predizione
            int[] predicted = new int[testSize], relevance = new int[testSize];
            Log.d(TAG, "PREDICTION START");
            index = 0;
            for (svm_node[] n : nodes) {
                predicted[index++] = (int) svm.svm_predict(model, n);
            }
            Log.d(TAG, "PREDICTION END");

            index = 0;
            label = 0;
            for (String s : keys) {
                for (int k = 0; k < (testSize / keys.size()); k++) {
                    relevance[index++] = label;
                }
                label++;
            }

            double precisionTemp = 0, recallTemp = 0, f1Temp = 0;
            for(int i = 0; i < keys.size(); i++){

                //precision-recall
                int tp = 0, fp = 0, fn = 0;
                for (index = 0; index < predicted.length; index++) {
                    if (predicted[index] == i && predicted[index] == relevance[index])
                        tp++;
                    else if(predicted[index] == i && predicted[index] != relevance[index])
                        fp++;
                    else if(predicted[index] != i && relevance[index] == i)
                        fn++;
                }

                double p = (double) tp / (tp + fp);
                double r = (double) tp / (tp + fn);
                double f1 = ((p * r) / (p + r)) * 2;
                precisionTemp += p;
                recallTemp += r;
                f1Temp += f1;

            }

            precision += precisionTemp / keys.size();
            recall += recallTemp / keys.size();
            f1 += f1Temp / keys.size();


        }

        return model;
    }

    public double getPrecision(){ return (precision/kFold); }

    public double getRecall(){ return (recall/kFold); }

    public double getF1(){ return (f1/kFold); }

    //this method allows to create the svm model and return it
    public svm_model getModel(){

        HashMap<String, List<float[]>> training = new HashMap<>();

        int trainingSize = 0;

        for (String k : keys) {
            List<float[]> windows = new ArrayList<>(map.get(k));
            training.put(k, windows);
            trainingSize += training.get(k).size();
        }

        //preparo il modello con i valori del training
        svm_problem probTrain = new svm_problem();
        probTrain.y = new double[trainingSize];
        probTrain.l = trainingSize;
        probTrain.x = new svm_node[trainingSize][];

        int index = 0, label = 0;
        for (String ks : keys) {
            for (float[] f : training.get(ks)) {
                probTrain.x[index] = new svm_node[f.length];
                for (int i = 0; i < f.length; i++) {
                    svm_node node = new svm_node();
                    node.index = i;
                    node.value = f[i];
                    probTrain.x[index][i] = node;
                }
                probTrain.y[index++] = label;
            }
            label++;
        }

        //training
        //setting probability = 1 (to calculate the probability) and shrinking = 0 (to make faster the model creation)
        param.probability = 1;
        param.shrinking = 0;

        Log.d(TAG, "TRAINING START");
        svm_model model = svm.svm_train(probTrain, param);
        Log.d(TAG, "TRAINING END");

        return model;
    }

}
