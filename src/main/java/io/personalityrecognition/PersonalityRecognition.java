package io.personalityrecognition;

import io.personalityrecognition.util.DataShaper;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;
import io.personalityrecognition.util.PersonalityDataWriter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.Perceptron;

public class PersonalityRecognition {
	
	private static final int TRAIT_COUNT = 5;
	private static List<String> WORDS;

	public static void main(String args[]) {
		try {
			HashMap<String, PersonalityData> map = PersonalityDataReader.readPersonalityData("train.csv");
			System.out.println("Size: " + map.size());
			List<Map.Entry<String, PersonalityData>> list = new LinkedList<>(map.entrySet());
			PersonalityData firstRow = list.get(0).getValue();
			System.out.println("ID: " + firstRow.getUserId());
			for(String word : firstRow.getWordFrequencies().keySet()) {
				System.out.println(word);
			}
			WORDS = new LinkedList<>(firstRow.getWordFrequencies().keySet());
			Collections.sort(WORDS);
			NeuralNetwork nn = newNN();
			DataSet trainingSet = trainingSet(map);
			nn.learn(trainingSet);
			nn.save("basic_perceptron.nnet");
		} catch(Exception e) {
			e.printStackTrace();
		}
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
