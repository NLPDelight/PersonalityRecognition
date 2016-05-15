package io.personalityrecognition;

import io.personalityrecognition.util.DataShaper;
import io.personalityrecognition.util.PCADataWriter;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;
import io.personalityrecognition.util.PersonalityDataWriter;
import Jama.Matrix;

import com.mkobos.pca_transform.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.RBFNetwork;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

public class PersonalityRecognition {
	
	private static final String[] TRAITS = new String[] { "Extraverion", "Neuroticism", "Openness", "Agreeableness", "Conscientiousness" };
	private static final int TRAIT_COUNT = 5;
	private static final double TRAINING_RATIO = .8;
	private static List<String> WORDS;

	public static void main(String args[]) {
		try {
//			DataShaper shaper = new DataShaper("essays.csv");
//			HashMap<String, PersonalityData> data = shaper.shapeData().getUsers();
//			int trainingSetCount = (int) (TRAINING_RATIO * data.size());
//			HashMap<String, PersonalityData> train = new HashMap<String, PersonalityData>();
//			HashMap<String, PersonalityData> test = new HashMap<String, PersonalityData>();
//			List<String> wordOrder = new LinkedList<>(shaper.getAcceptedTokens());
//			Collections.sort(wordOrder);
//			int count = 0;
//			
//			for(String id : data.keySet()) {
//				if(count < trainingSetCount)
//					train.put(id, data.get(id));
//				else
//					test.put(id, data.get(id));
//				count++;
//			}
//			
//			PersonalityDataWriter.writeFile(wordOrder, train, "essay_train.csv");
//			PersonalityDataWriter.writeFile(wordOrder, test, "essay_test.csv");
			
			PCADataWriter.writeDataAsPCA("essay_train.csv","essay_test.csv","pca_essay");
//			Map<String, double[]> results = TestRunner.runPCATest("Multi.nnet", "pca_data_test.csv");
//			for(String trait : results.keySet()) {
//				double[] numbers = results.get(trait);
//				String line = trait + ": " + numbers[0] + " " + numbers[1] + " " + numbers[2] + " " + numbers[3];
//				System.out.println(line);
//			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Matrix dataAsMatrix(String filename) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> data = PersonalityDataReader.readPersonalityData(filename);
		double[][] matrix = new double[data.size()][1000];
		int i = 0;
		for(String id : data.keySet()) {
			matrix[i] = frequenciesToArray(data.get(id).getWordFrequencies());
			i++;
		}
		return new Matrix(matrix);
	}
	
	public static double[] frequenciesToArray(HashMap<String, Double> data) {
		double[] arr = new double[WORDS.size()];
		for(int i = 0; i < WORDS.size(); i++) {
			arr[i] = data.get(WORDS.get(i));
		}
		return arr;
	}
	
	public static RBFNetwork newRBFNetwork() {
		return new RBFNetwork(1000, 200, 5);
	}
	
	public static double[][] basisPredictions(String testFileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		double[][] counts = basisFunction(testFileName);
		double[][] predictions = new double[TRAIT_COUNT][4];
		for(int i = 0; i < TRAIT_COUNT; i++) {
			int haves = (int) counts[i][1];
			int havenots = (int) counts[i][0];
			
			if(havenots > haves) {
				predictions[i][1] = havenots;
				predictions[i][3] = haves;
			} else {
				predictions[i][0] = havenots;
				predictions[i][2] = haves;
			}
		}
		return predictions;
	}
	
	public static double[][] basisFunction(String testFileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> data = PersonalityDataReader.readPersonalityData(testFileName);
		double[][] matrix = new double[TRAIT_COUNT][2];
		for(String id : data.keySet()) {
			PersonalityData user = data.get(id);
			double[] traits = user.getClassesAsNumericArray();
			for(int i = 0; i < TRAIT_COUNT; i++) {
				matrix[i][(int) traits[i]] += 1;
			}
		}
		return matrix;
	}
	
	public static double[][] test(NeuralNetwork nn, String testFileName) throws FileNotFoundException,
			UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> testData = PersonalityDataReader.readPersonalityData(testFileName);
		double[][] scores = new double[TRAIT_COUNT][4];
		double count = 0;
		for(String id : testData.keySet()) {
			PersonalityData user = testData.get(id);
			int[] testResults = test(nn, user);
			for(int i = 0; i < TRAIT_COUNT; i++) {
				scores[i][testResults[i]] += 1;
			}
			count++;
		}
		
		for(int i = 0; i < TRAIT_COUNT; i++) {
			for(int j = 0; j < 4; j++) {
				scores[i][j] /= count;
			}
		}
		
		return scores;
	}
	
	public static int[] test(NeuralNetwork nn, PersonalityData user) {
		double[] traits = user.getClassesAsNumericArray();
		double[] wordFrequencies = getWordFrequenciesAsArray(user);
		nn.setInput(wordFrequencies);
		nn.calculate();
		double[] results = nn.getOutput();
		int[] comparison = new int[results.length];
		
		String line = "";
		for(int i = 0; i < results.length; i++) {
			comparison[i] = (int) (results[i] - traits[i]) + 1;
			comparison[i] = comparison[i] == 1 ? (int)(comparison[i] + traits[i]) : comparison[i] > 0 ? comparison[i] + 1 : comparison[i];
		}
		
		System.out.println(line);
		
		return comparison;
	}
	
	public static void getWordOrder(String filename) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> map = PersonalityDataReader.readPersonalityData(filename);
		List<Map.Entry<String, PersonalityData>> list = new LinkedList<>(map.entrySet());
		PersonalityData firstRow = list.get(0).getValue();
		WORDS = new LinkedList<>(firstRow.getWordFrequencies().keySet());
		Collections.sort(WORDS);
	}
	
	public static void createTrainedNeuralNetwork(String trainingFile, String nnFile, int hiddenNodes) throws IOException {
		HashMap<String, PersonalityData> map = PersonalityDataReader.readPersonalityData(trainingFile);
		NeuralNetwork nn = hiddenNodes > 0 ? neuralNetworkWithNHiddenNodes(hiddenNodes) : newNN();
		DataSet trainingSet = trainingSet(map);
		nn.learn(trainingSet);
		nn.save(nnFile);
		
	}
	
	public static void trainNeuralNetwork(NeuralNetwork nn, String trainingFile, String nnFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> map = PersonalityDataReader.readPersonalityData(trainingFile);
		DataSet trainingSet = trainingSet(map);
		nn.learn(trainingSet);
		nn.save(nnFile);
	}
	
	public static NeuralNetwork neuralNetworkWithNHiddenNodes(int n) {
		Layer hiddenLayer = new Layer(n, new NeuronProperties(TransferFunctionType.SIGMOID, true));
		NeuralNetwork nn = newNN();
		nn.addLayer(hiddenLayer);
		return nn;
	}
	
	public static NeuralNetwork newNN() {
		NeuralNetwork nn = new Perceptron(WORDS.size(), TRAIT_COUNT);
		return nn;
	}
	
	public static DataSet trainingSet(Map<String, PersonalityData> trainingData) {
		DataSet trainingSet = new DataSet(WORDS.size(), TRAIT_COUNT);
		for(String id : trainingData.keySet()) {
			PersonalityData row = trainingData.get(id);
			double[] wordFrequencies = getWordFrequenciesAsArray(row);
			double[] traits = row.getClassesAsNumericArray();
			trainingSet.addRow(wordFrequencies, traits);
		}
		return trainingSet;
	}
	
	public static double[] getWordFrequenciesAsArray(PersonalityData row) {
		HashMap<String, Double> wordFrequencies = row.getWordFrequencies();
		double[] frequencies = new double[WORDS.size()];
		for(int i = 0; i < WORDS.size(); i++) {
			frequencies[i] = wordFrequencies.get(WORDS.get(i));
		}
		return frequencies;
	}
	
	public static void printTopNTokenCounts(int n, List<Map.Entry<String, Integer>> counts) {
		int numberOfTypes = counts.size();
		
		if(numberOfTypes <= n) {
			for(Map.Entry<String, Integer> count : counts) {
				System.out.println(String.format("%s: %s", count.getKey(), count.getValue()));
			}
		} else {
			for(int i = 0; i < n; i++) {
				Map.Entry<String, Integer> count = counts.get(i);
				System.out.println(String.format("%s: %s", count.getKey(), count.getValue()));
			}
		}
	}
	
	public static LinkedList<String> setToAlphabeticalList(Set<String> words) {
		LinkedList<String> orderedWords = new LinkedList<String>(words);
		Collections.sort(orderedWords);
		return orderedWords;
	}
	
	public static String encapsulateToken(String s) {
		return "[" + s + "]";
	}
}
