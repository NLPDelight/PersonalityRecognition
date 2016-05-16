package io.personalityrecognition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neuroph.core.NeuralNetwork;

import io.personalityrecognition.util.PCADataReader;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;
import static io.personalityrecognition.util.PCADataReader.*;
import static io.personalityrecognition.util.DatasetKeys.*;

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
	public static final String RECALL = "recall";
	public static final String PRECISION = "precision";
	public static final String ACCURACY = "accuracy";
	
	
	private NeuralNetwork networkUnderTest;
	private String testFile;
	
	public TestRunner(NeuralNetwork networkUnderTest, String testFile) {
		this.networkUnderTest = networkUnderTest;
		this.testFile = testFile;
	}
	
	public TestRunner(String neuralNetworkFile, String testFile) {
		this.networkUnderTest = NeuralNetwork.createFromFile(new File(neuralNetworkFile));
		this.testFile = testFile;
	}
	
	public Map<String, Map<String, Double>> runWordFrequencyTest(List<String> inputFeatures)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> testData = PersonalityDataReader.readPersonalityData(testFile);
		return getTestResults(testData, inputFeatures);
	}
	
	private Map<String, Map<String, Double>> getTestResults(HashMap<String, PersonalityData> testData,
			List<String> inputFeatures) {
		double[][] results = runWordFrequencyTest(testData, inputFeatures);
		return interpretResults(addTraitKeys(results));
	}
	
	private Map<String, Map<String, Double>> interpretResults(Map<String, double[]> confusionMatrices) {
		Map<String, Map<String, Double>> results = new HashMap<>();
		for(String trait : confusionMatrices.keySet()) {
			results.put(trait, confusionMatrixToResults(confusionMatrices.get(trait)));
		}
		return results;
	}
	
	private Map<String, Double> confusionMatrixToResults(double[] confusionMatrix) {
		Map<String, Double> results = new HashMap<>();
		results.put(TP, confusionMatrix[TRUE_POSITIVE]);
		results.put(FP, confusionMatrix[FALSE_POSITIVE]);
		results.put(FN, confusionMatrix[FALSE_NEGATIVE]);
		results.put(TN, confusionMatrix[TRUE_NEGATIVE]);
		results.put(ACCURACY, getAccuracy(confusionMatrix));
		results.put(PRECISION, getPrecision(confusionMatrix));
		results.put(RECALL, getRecall(confusionMatrix));
		return results;
	}
	
	private double getAccuracy(double[] confusionMatrix) {
		return (confusionMatrix[TRUE_POSITIVE] + confusionMatrix[TRUE_NEGATIVE]) / matrixTotal(confusionMatrix);
	}
	
	private double getRecall(double[] confusionMatrix) {
		return confusionMatrix[TRUE_POSITIVE] / (confusionMatrix[TRUE_POSITIVE] + confusionMatrix[FALSE_NEGATIVE]);
	}
	
	private double getPrecision(double[] confusionMatrix) {
		return confusionMatrix[TRUE_POSITIVE] / (confusionMatrix[TRUE_POSITIVE] + confusionMatrix[FALSE_POSITIVE]);
	}
	
	private double matrixTotal(double[] matrix) {
		double total = 0;
		for(int i = 0; i < matrix.length; i++)
			total += matrix[i];
		return total;
	}
		

	
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
	
	private int[] testWordFrequencyData(PersonalityData row, List<String> inputFeatures) {
		double[] inputs = getWordFrequencyValues(row, inputFeatures);
		double[] outputs = row.getClassesAsNumericArray();
		return test(inputs, outputs);
	}
	
	private double[] getWordFrequencyValues(PersonalityData row, List<String> features) {
		Map<String, Double> frequencies = row.getWordFrequencies();
		double[] ret = new double[features.size()];
		for(int i = 0; i < features.size(); i++) {
			ret[i] = frequencies.get(features.get(i));
		}
		return ret;
	}
	
	private Map<String, double[]> runPCATest() throws FileNotFoundException, UnsupportedEncodingException, IOException {
		List<Map<String, double[]>> testData = PCADataReader.readPCAFile(testFile);
		double[][] results = testPCAData(testData);
		return addTraitKeys(results);
	}
	
	private Map<String, double[]> addTraitKeys(double[][] unlabeledData) {
		Map<String, double[]> labeled = new HashMap<String, double[]>();
		for(int i = 0; i < unlabeledData.length; i++) {
			labeled.put(TRAIT_CLASSES.get(i), unlabeledData[i]);
		}
		return labeled;
	}
	
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
	
	private int[] testPCAData(Map<String, double[]> testRow) {
		double[] traits = testRow.get(DEPENDENT_VARIABLES);
		double[] features = testRow.get(INDEPENDENT_VARIABLES);
		return test(features, traits);
	}
	
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