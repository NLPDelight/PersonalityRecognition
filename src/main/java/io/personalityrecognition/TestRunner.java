package io.personalityrecognition;

import static io.personalityrecognition.util.DatasetKeys.TRAIT_CLASSES;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neuroph.core.NeuralNetwork;

import io.personalityrecognition.util.PCADataReader;
import static io.personalityrecognition.util.PCADataReader.DEPENDENT_VARIABLES;
import static io.personalityrecognition.util.PCADataReader.INDEPENDENT_VARIABLES;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;
import io.personalityrecognition.util.TestResults;

public class TestRunner {

	public static final int POSSIBLE_OUTCOMES = 4;
	public static final int TRUE_POSITIVE = 0;
	public static final int FALSE_POSITIVE = 1;
	public static final int FALSE_NEGATIVE = 2;
	public static final int TRUE_NEGATIVE = 3;
	public static final String TP = "TP";
	public static final String FP = "FP";
	public static final String FN = "FN";
	public static final String TN = "TN";
	public static final String RECALL = "Recall";
	public static final String PRECISION = "Precision";
	public static final String ACCURACY = "Accuracy";
	public static final String SPECIFICITY = "Specificity";
	public static final String NEGATIVE_PREDICTIVE_VALUE = "Negative Predictive Value";
	public static final String F_MEASURE = "F Measure";

	private NeuralNetwork networkUnderTest;
	private String testFile;

	/**
	 * This is the constructor method
	 *
	 * @param NeuralNetwork, String
	 * @return
	 * @throws
	 */
	public TestRunner(NeuralNetwork networkUnderTest, String testFile) {
		this.networkUnderTest = networkUnderTest;
		this.testFile = testFile;
	}

	/**
	 * This is the constructor method with the neuralNetworkFile name
	 *
	 * @param String, String
	 * @return
	 * @throws
	 */
	public TestRunner(String neuralNetworkFile, String testFile) {
		this.networkUnderTest = NeuralNetwork.createFromFile(new File(neuralNetworkFile));
		this.testFile = testFile;
	}

	/**
	 * This method calls PCADataReader on the testFile and returns the interpreted results.
	 *
	 * @param
	 * @return TestResults[]
	 * @throws FileNotFoundException, UnsupportedEncodingException, IOException
	 */
	public TestResults[] runPCATest() throws FileNotFoundException, UnsupportedEncodingException, IOException {
		List<Map<String, double[]>> testData = PCADataReader.readPCAFile(testFile);
		double[][] results = testPCAData(testData);
		return interpretResults(addTraitKeys(results));
	}

	/**
	 * This method runs the word frequency test and returns the results.
	 *
	 * @param List<String>
	 * @return TestResults[]
	 * @throws
	 */
	public TestResults[] runWordFrequencyTest(List<String> inputFeatures)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> testData = PersonalityDataReader.readPersonalityData(testFile);
		return getTestResults(testData, inputFeatures);
	}

	/**
	 * This method calls word frequency test to get its results then returns processed results with interpret results.
	 *
	 * @param HashMap<String, PersonalityData>, List<String>
	 * @return TestResults[]
	 * @throws
	 */
	private TestResults[] getTestResults(HashMap<String, PersonalityData> testData,
			List<String> inputFeatures) {
		double[][] results = runWordFrequencyTest(testData, inputFeatures);
		return interpretResults(addTraitKeys(results));
	}

	/**
	 * This method takes in the confusionMatrices and process them into TestResults[]
	 *
	 * @param Map<String, double[]>
	 * @return TestResults[]
	 * @throws
	 */
	private TestResults[] interpretResults(Map<String, double[]> confusionMatrices) {
		TestResults[] results = new TestResults[confusionMatrices.size()];
		int i = 0;
		for(String trait : confusionMatrices.keySet()) {
			results[i] = confusionMatrixToResults(confusionMatrices.get(trait));
			results[i].setTrait(trait);
			++i;
		}
		return results;
	}

	/**
	 * This method sets the results by reading confusionMatrix data
	 *
	 * @param double[]
	 * @return TestResults
	 * @throws
	 */
	private TestResults confusionMatrixToResults(double[] confusionMatrix) {
		TestResults results = new TestResults()
			.setTruePositives(confusionMatrix[TRUE_POSITIVE])
			.setFalsePositives(confusionMatrix[FALSE_POSITIVE])
			.setTrueNegatives(confusionMatrix[TRUE_NEGATIVE])
			.setFalseNegatives(confusionMatrix[FALSE_NEGATIVE]);
		return results;
	}

	/**
	 * This method takes in test data and returns results to wordfrequency test.
	 *
	 * @param HashMap<String, PersonalityData>, List<String>
	 * @return double[][]
	 * @throws
	 */
	private double[][] runWordFrequencyTest(HashMap<String, PersonalityData> testData, List<String> inputFeatures) {
		double[][] results = new double[TRAIT_CLASSES.size()][POSSIBLE_OUTCOMES];
		for(Map.Entry<String, PersonalityData> row : testData.entrySet()) {
			int[] testResults = testWordFrequencyData(row.getValue(), inputFeatures);
			for(int i = 0; i < TRAIT_CLASSES.size(); i++) {
				results[i][testResults[i]] += 1;
			}
		}
		return results;
	}

	/**
	 * This method takes a row of data and tests it's word frequency.
	 *
	 * @param PersonalityData, List<String>
	 * @return int[]
	 * @throws
	 */
	private int[] testWordFrequencyData(PersonalityData row, List<String> inputFeatures) {
		double[] inputs = getWordFrequencyValues(row, inputFeatures);
		double[] outputs = row.getClassesAsNumericArray();
		return test(inputs, outputs);
	}

	/**
	 * This method takes a row of personalitydata and process it's value into double array.
	 *
	 * @param PersonalityData, List<String>
	 * @return double[]
	 * @throws
	 */
	private double[] getWordFrequencyValues(PersonalityData row, List<String> features) {
		Map<String, Double> frequencies = row.getWordFrequencies();
		double[] ret = new double[features.size()];
		for(int i = 0; i < features.size(); i++) {
			ret[i] = frequencies.get(features.get(i));
		}
		return ret;
	}

	/**
	 * This method adds traitkey to the unlabeledData
	 *
	 * @param double[][]
	 * @return Map<String, double[]>
	 * @throws
	 */
	private Map<String, double[]> addTraitKeys(double[][] unlabeledData) {
		Map<String, double[]> labeled = new HashMap<String, double[]>();
		for(int i = 0; i < unlabeledData.length; i++) {
			labeled.put(TRAIT_CLASSES.get(i), unlabeledData[i]);
		}
		return labeled;
	}

	/**
	 * This method tests PCAData and return scores in double matrix
	 *
	 * @param List<Map<String, double[]>>
	 * @return double[][]
	 * @throws
	 */
	private double[][] testPCAData(List<Map<String, double[]>> data) {
		double[][] scores = new double[TRAIT_CLASSES.size()][POSSIBLE_OUTCOMES];
		for(Map<String, double[]> row : data) {
			int[] testResults = testPCAData(row);
			for(int i = 0; i < 5; i++) {
				scores[i][testResults[i]] += 1;
			}
		}
		return scores;
	}

	/**
	 * This method calls test method on the prcoessed PCA data.
	 *
	 * @param Map<String, double[]>
	 * @return int[]
	 * @throws
	 */
	private int[] testPCAData(Map<String, double[]> testRow) {
		double[] traits = testRow.get(DEPENDENT_VARIABLES);
		double[] features = testRow.get(INDEPENDENT_VARIABLES);
		return test(features, traits);
	}

	/**
	 * This method runs the given test.
	 *
	 * @param double[], double[]
	 * @return int[]
	 * @throws
	 */
	private int[] test(double[] inputs, double[] expectedOutputs) {
		networkUnderTest.setInput(inputs);
		networkUnderTest.calculate();
		double[] results = networkUnderTest.getOutput();
		int[] comparison = new int[results.length];

		for(int i = 0; i < results.length; i++) {
			double predicted = Math.round(results[i]);
			int confusionMatrixValue = getConfusionMatrixValue(predicted, expectedOutputs[i]);
			comparison[i] += confusionMatrixValue;
		}

		return comparison;

	}

	/**
	 * This method returns confusion matrix value.
	 *
	 * @param double, double
	 * @return int
	 * @throws
	 */
	private int getConfusionMatrixValue(double predicted, double actual) {
		if(predicted > actual) {
			return FALSE_POSITIVE;
		} else if(actual > predicted) {
			return FALSE_NEGATIVE;
		} else {
			return actual > 0 ? TRUE_POSITIVE : TRUE_NEGATIVE;
		}
	}
}
