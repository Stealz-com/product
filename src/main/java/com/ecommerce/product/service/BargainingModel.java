package com.ecommerce.product.service;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.*;
import java.util.Random;

@Component
public class BargainingModel implements Serializable {

    private double[][] weights1; // Input to Hidden
    private double[][] weights2; // Hidden to Output
    private double[] bias1;
    private double[] bias2;

    private static final int INPUT_SIZE = 5;
    private static final int HIDDEN_SIZE = 8;
    private static final int OUTPUT_SIZE = 1;
    private static final double LEARNING_RATE = 0.05;
    private static final String MODEL_FILE = "bargaining_brain.bin";

    @PostConstruct
    public void init() {
        if (!loadModel()) {
            initializeWeights();
        }
    }

    private void initializeWeights() {
        Random rand = new Random();
        weights1 = new double[INPUT_SIZE][HIDDEN_SIZE];
        weights2 = new double[HIDDEN_SIZE][OUTPUT_SIZE];
        bias1 = new double[HIDDEN_SIZE];
        bias2 = new double[OUTPUT_SIZE];

        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < HIDDEN_SIZE; j++) {
                weights1[i][j] = rand.nextGaussian() * 0.1;
            }
        }
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            weights2[i][0] = rand.nextGaussian() * 0.1;
            bias1[i] = 0;
        }
        bias2[0] = 0;
    }

    public double predict(double[] input) {
        double[] hidden = forward(input);
        return sigmoid(dot(hidden, weights2)[0] + bias2[0]);
    }

    private double[] forward(double[] input) {
        double[] hidden = new double[HIDDEN_SIZE];
        for (int j = 0; j < HIDDEN_SIZE; j++) {
            double sum = bias1[j];
            for (int i = 0; i < INPUT_SIZE; i++) {
                sum += input[i] * weights1[i][j];
            }
            hidden[j] = sigmoid(sum);
        }
        return hidden;
    }

    public void train(double[][] inputs, double[][] labels) {
        for (int epoch = 0; epoch < 1000; epoch++) {
            for (int i = 0; i < inputs.length; i++) {
                trainSingle(inputs[i], labels[i][0]);
            }
        }
        saveModel();
    }

    private void trainSingle(double[] input, double label) {
        // Forward pass
        double[] hidden = forward(input);
        double output = predict(input);

        // Backpropagation
        double outputError = (label - output) * output * (1 - output);

        // Update Hidden -> Output weights
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            weights2[i][0] += LEARNING_RATE * outputError * hidden[i];
        }
        bias2[0] += LEARNING_RATE * outputError;

        // Hidden layer error
        for (int i = 0; i < HIDDEN_SIZE; i++) {
            double hiddenError = outputError * weights2[i][0] * hidden[i] * (1 - hidden[i]);
            for (int j = 0; j < INPUT_SIZE; j++) {
                weights1[j][i] += LEARNING_RATE * hiddenError * input[j];
            }
            bias1[i] += LEARNING_RATE * hiddenError;
        }
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double[] dot(double[] a, double[][] b) {
        double[] result = new double[b[0].length];
        for (int j = 0; j < b[0].length; j++) {
            for (int i = 0; i < a.length; i++) {
                result[j] += a[i] * b[i][j];
            }
        }
        return result;
    }

    private void saveModel() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MODEL_FILE))) {
            oos.writeObject(weights1);
            oos.writeObject(weights2);
            oos.writeObject(bias1);
            oos.writeObject(bias2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadModel() {
        File file = new File(MODEL_FILE);
        if (!file.exists())
            return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            weights1 = (double[][]) ois.readObject();
            weights2 = (double[][]) ois.readObject();
            bias1 = (double[]) ois.readObject();
            bias2 = (double[]) ois.readObject();
            return true;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }
}
