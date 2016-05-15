package io.personalityrecognition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neuroph.core.NeuralNetwork;

import io.personalityrecognition.util.PCADataReader;
import static io.personalityrecognition.util.PCADataReader.*;
import static io.personalityrecognition.util.DatasetKeys.*;

public class TestRunner {
	
	public static Map<String, double[]> runPCATest(String nnFile, String testFile)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		
		NeuralNetwork nn = NeuralNetwork.createFromFile(new File(nnFile));
		return runPCATest(nn, testFile);
	}
	
	public static Map<String, double[]> runPCATest(NeuralNetwork nn, String testFile)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		
		List<Map<String, double[]>> testData = PCADataReader.readPCAFile(testFile);
		double[][] results = testPCAData(nn, testData);
		return addTraitKeys(results);
	}
	
	public static Map<String, double[]> addTraitKeys(double[][] unlabeledData) {
		Map<String, double[]> labeled = new HashMap<String, double[]>();
		for(int i = 0; i < unlabeledData.length; i++) {
			labeled.put(TRAIT_CLASSES.get(i), unlabeledData[i]);
		}
		return labeled;
	}
	
	private static double[][] testPCAData(NeuralNetwork nn, List<Map<String, double[]>> data) {
		double[][] scores = new double[5][4];
		for(Map<String, double[]> row : data) {
			int[] testResults = testPCAData(nn, row);
			for(int i = 0; i < 5; i++) {
				scores[i][testResults[i]] += 1;
			}
		}
		return scores;
	}
	
	private static int[] testPCAData(NeuralNetwork nn, Map<String, double[]> testRow) {
		double[] traits = testRow.get(DEPENDENT_VARIABLES);
		double[] features = testRow.get(INDEPENDENT_VARIABLES);
		return test(nn, features, traits);
	}
	
	private static int[] test(NeuralNetwork nn, double[] inputs, double[] expectedOutputs) {
		nn.setInput(inputs);
		nn.calculate();
		double[] results = nn.getOutput();
		int[] comparison = new int[results.length];
		
		String line = "";
		for(int i = 0; i < results.length; i++) {
			double predicted = Math.round(results[i]);
			comparison[i] = (int) (predicted - expectedOutputs[i]) + 1;
			comparison[i] = comparison[i] == 1 ? (int)(comparison[i] + expectedOutputs[i]) :
				                                 comparison[i] > 0 ? comparison[i] + 1 :
				                                	                 comparison[i];
		}
		
		System.out.println(line);
		
		return comparison;
		
	}
}
