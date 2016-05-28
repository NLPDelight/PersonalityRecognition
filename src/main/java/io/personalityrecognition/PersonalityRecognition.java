package io.personalityrecognition;

import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;
import io.personalityrecognition.util.TestResults;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.RBFNetwork;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.PerceptronLearning;
import org.neuroph.nnet.learning.RBFLearning;

public class PersonalityRecognition {

	private static final String[] TRAITS = new String[] { "Extraverion", "Neuroticism", "Openness", "Agreeableness", "Conscientiousness" };
	private static final int TRAIT_COUNT = 5;
	private static final double TRAINING_RATIO = .8;
	private static final String TRAINING_FILE = "essay_train.csv";
	private static final String TRAIT_PRINT_FORMAT = "    %s\n";
	private static final String PRINT_FORMAT = "        %-25s %10.5f\n";
	private static final String PCA_TEST_DATA = "my_personality/pca/pca_data_test.csv";
	private static final String FB_TEST_DATA = "my_personality/non_pca/my_personality_test.csv";
	private static final String ESSAY_TEST_DATA = "essay/essay_test.csv";
	private static final String RBF_PCA_NN = "my_personality/pca/facebook_pca_RBF.nnet";
	private static final String FB_PERCEPTRON_NN = "my_personality/non_pca/fb_single.nnet";
	private static final String FB_MULTI_NN = "my_personality/non_pca/fb_Multi.nnet";
	private static final String ESSAY_PERCEPTRON_NN = "essay/essay_single";
	private static final String ESSAY_MULTI_NN = "essay/essay_multilayer";

	private static List<String> WORDS;

	public static void main(String args[]) throws Exception {
		printAll();
	}

	private static void printAll() {
		try {
			TestRunner fb_rbf = new TestRunner(RBF_PCA_NN, PCA_TEST_DATA);
			TestRunner fb_perceptron = new TestRunner(FB_PERCEPTRON_NN, FB_TEST_DATA);
			TestRunner fb_multi = new TestRunner(FB_MULTI_NN, FB_TEST_DATA);
			TestRunner essay_perceptron = new TestRunner(ESSAY_PERCEPTRON_NN, ESSAY_TEST_DATA);
			TestRunner essay_multi = new TestRunner(ESSAY_MULTI_NN, ESSAY_TEST_DATA);

			System.out.println("MY_PERSONALITY (FACEBOOK) DATA RESULTS");

			getWordOrder(FB_TEST_DATA);
			printResults("Single Perceptron", fb_perceptron.runWordFrequencyTest(WORDS));
			printResults("Multilayer Perceptron", fb_multi.runWordFrequencyTest(WORDS));
			printResults("RBF Network on PCA data", fb_rbf.runPCATest());
			printLogisticRegressionResults("my_personality");

			System.out.println("STREAM OF CONSCIOUSNESS ESSAY RESULTS");

			getWordOrder(ESSAY_TEST_DATA);
			printResults("Single Perceptron", essay_perceptron.runWordFrequencyTest(WORDS));
			printResults("Multilayer Perceptron", essay_multi.runWordFrequencyTest(WORDS));
			printLogisticRegressionResults("essay");

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private static void printLogisticRegressionResults(String testName) throws Exception {
		LogisticRegressionTest m = new LogisticRegressionTest();

		Map<String, Map<String, Map<String, Double>>> allResults = m.testSuite(testName);

		Map<String, List<TestResults>> resultMap = new HashMap();

		for (Map.Entry<String, Map<String, Map<String, Double>>> results : allResults.entrySet()) {
			List<TestResults> resultList = new ArrayList<>();

			for (Map.Entry<String, Map<String, Double>> entry : results.getValue().entrySet()) {

				Map<String, Double> values = entry.getValue();

				resultList.add(
					new TestResults(
						entry.getKey(), results.getKey(), values.get("TP"),
						values.get("FP"), values.get("TN"), values.get("FN")));
			}
			resultMap.put(results.getKey(), resultList);
		}

		for (Entry<String, List<TestResults>> entry : resultMap.entrySet()) {
			List<TestResults> list = entry.getValue();
			printResults("Logistic Regression " + entry.getKey(), list.toArray(new TestResults[list.size()]));
		}
	}

	private static RBFNetwork newRBFNetwork(int ins, int hidden, int outs, int iterations) {
		RBFNetwork nn = new RBFNetwork(ins, hidden, outs);
		RBFLearning learningRule = (RBFLearning) nn.getLearningRule();
		learningRule.setLearningRate(.2);
		learningRule.setMaxIterations(iterations);
		nn.setLearningRule(learningRule);
		return nn;
	}

	private static Perceptron newPerceptronNetwork(int ins, int outs, int iterations) {
		Perceptron nn = new Perceptron(ins, outs);
		PerceptronLearning learningRule = (PerceptronLearning) nn.getLearningRule();
		learningRule.setLearningRate(.2);
		learningRule.setMaxIterations(iterations);
		nn.setLearningRule(learningRule);
		return nn;
	}

	private static MultiLayerPerceptron newMultiLayerPerceptronNetwork(int ins, int hidden,  int outs, int iterations) {
		MultiLayerPerceptron nn = new MultiLayerPerceptron(ins, hidden, outs);
		BackPropagation learningRule = (BackPropagation) nn.getLearningRule();
		learningRule.setLearningRate(.2);
		learningRule.setMaxIterations(iterations);
		nn.setLearningRule(learningRule);
		return nn;
	}

	private static void printResults(String header, TestResults[] results) {
		System.out.println(header);
		for(int i = 0; i < results.length; i++) {
			printHeaderLine(results[i].getTrait());
			printScoreLine("Accuracy", results[i].getAccuracy());
			printScoreLine("Precision", results[i].getPrecision());
			printScoreLine("Recall", results[i].getRecall());
			printScoreLine("F-Measure", results[i].getFMeasure());
			printScoreLine("Specificity", results[i].getSpecificity());
			printScoreLine("Negative Predictive Value", results[i].getNPV());
		}
	}

	private static void printScoreLine(String metric, double score) {
		System.out.format(PRINT_FORMAT, metric, score);
	}

	private static void printHeaderLine(String header) {
		System.out.format(TRAIT_PRINT_FORMAT, header);
	}

	private static void getWordOrder(String filename) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> map = PersonalityDataReader.readPersonalityData(filename);
		List<Map.Entry<String, PersonalityData>> list = new LinkedList<>(map.entrySet());
		PersonalityData firstRow = list.get(0).getValue();
		WORDS = new LinkedList<>(firstRow.getWordFrequencies().keySet());
		Collections.sort(WORDS);
	}
}
