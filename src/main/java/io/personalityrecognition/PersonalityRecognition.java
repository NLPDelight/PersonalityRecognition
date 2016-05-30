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
import java.util.Scanner;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.RBFNetwork;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.PerceptronLearning;
import org.neuroph.nnet.learning.RBFLearning;

public class PersonalityRecognition {

	public static void main(String[] args) {
		try {
		Scanner scan = new Scanner(System.in);
		System.out.println("Personality Recognition");
		System.out.println("Please select from the following options: ");
		System.out.println("1. Print All Results");
		System.out.println("2. Print Essay dataset Results");
		System.out.println("3. Print My-Personality dataset Results");
		System.out.println("4. Print results from a particular test");
		System.out.print("input: ");
		String s = scan.next();

		switch (s){
			case "1" :
				printAll();
				break;
			case "2" :
				printEssay();
				break;
			case "3" :
				printMyPersonality();
				break;
			case "4" :
				printSingle();
				break;
		}
		}
		catch (Exception e) {
			System.out.println("error");
		}
	}

	private static void printAll() {
		try {
			printMyPersonality();

			printEssay();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static void printEssay() throws Exception {
		TestRunner essay_perceptron = new TestRunner(ESSAY_PERCEPTRON_NN, ESSAY_TEST_DATA);
		TestRunner essay_multi = new TestRunner(ESSAY_MULTI_NN, ESSAY_TEST_DATA);

		System.out.println("STREAM OF CONSCIOUSNESS ESSAY RESULTS");

		getWordOrder(ESSAY_TEST_DATA);
		printResults("Single Perceptron", essay_perceptron.runWordFrequencyTest(WORDS));
		printResults("Multilayer Perceptron", essay_multi.runWordFrequencyTest(WORDS));
		printLogisticRegressionResults("essay");
	}

	private static void printMyPersonality() throws Exception {
		TestRunner fb_rbf = new TestRunner(RBF_PCA_NN, PCA_TEST_DATA);
		TestRunner fb_perceptron = new TestRunner(FB_PERCEPTRON_NN, FB_TEST_DATA);
		TestRunner fb_multi = new TestRunner(FB_MULTI_NN, FB_TEST_DATA);

		System.out.println("MY_PERSONALITY (FACEBOOK) DATA RESULTS");

		getWordOrder(FB_TEST_DATA);
		printResults("Single Perceptron", fb_perceptron.runWordFrequencyTest(WORDS));
		printResults("Multilayer Perceptron", fb_multi.runWordFrequencyTest(WORDS));
		printResults("RBF Network on PCA data", fb_rbf.runPCATest());
		printLogisticRegressionResults("my_personality");
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

	private static void printSingle() throws Exception {
		Scanner scan = new Scanner(System.in);
		System.out.println("Select Test Dataset: ");
		System.out.println("1. Essay");
		System.out.println("2. My Personality");
		System.out.print("input: ");
		String s = scan.next();

		if (s.equals("1")) {
			System.out.println("Select Test: ");
			System.out.println("1. Single Perceptron");
			System.out.println("2. Multilayer Perceptron");
			System.out.println("3. Logistic Regression");
			System.out.print("input: ");
			s = scan.next();

			TestRunner essay_perceptron = new TestRunner(ESSAY_PERCEPTRON_NN, ESSAY_TEST_DATA);
			TestRunner essay_multi = new TestRunner(ESSAY_MULTI_NN, ESSAY_TEST_DATA);
			getWordOrder(ESSAY_TEST_DATA);

			switch (s) {
				case "1" :
					printResults("Single Perceptron", essay_perceptron.runWordFrequencyTest(WORDS));
				case "2" :
					printResults("Multilayer Perceptron", essay_multi.runWordFrequencyTest(WORDS));
				case "3" :
					printLogisticRegressionResults("essay");
			}
		}

		if (s.equals("2")) {
			System.out.println("Select Test: ");
			System.out.println("1. Single Perceptron");
			System.out.println("2. Multilayer Perceptron");
			System.out.println("3. RBF Network on PCA data");
			System.out.println("4. Logistic Regression");
			System.out.print("input: ");
			s = scan.next();

			TestRunner fb_rbf = new TestRunner(RBF_PCA_NN, PCA_TEST_DATA);
			TestRunner fb_perceptron = new TestRunner(FB_PERCEPTRON_NN, FB_TEST_DATA);
			TestRunner fb_multi = new TestRunner(FB_MULTI_NN, FB_TEST_DATA);

			getWordOrder(FB_TEST_DATA);

			switch (s) {
				case "1" :
					printResults("Single Perceptron", fb_perceptron.runWordFrequencyTest(WORDS));
				case "2" :
					printResults("Multilayer Perceptron", fb_multi.runWordFrequencyTest(WORDS));
				case "3" :
					printResults("RBF Network on PCA data", fb_rbf.runPCATest());
				case "4" :
					printLogisticRegressionResults("my_personality");
			}
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
		for (TestResults result : results) {
			printHeaderLine(result.getTrait());
			printScoreLine("Accuracy", result.getAccuracy());
			printScoreLine("Precision", result.getPrecision());
			printScoreLine("Recall", result.getRecall());
			printScoreLine("F-Measure", result.getFMeasure());
			printScoreLine("Specificity", result.getSpecificity());
			printScoreLine("Negative Predictive Value", result.getNPV());
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
}
