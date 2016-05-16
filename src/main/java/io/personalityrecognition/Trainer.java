package io.personalityrecognition;

import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;

import static io.personalityrecognition.util.DatasetKeys.*;

public class Trainer {
	
	public static void train(NeuralNetwork networkToTrain, String trainingDataFile, List<String> inputFeatures,
			String outputFile) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		train(networkToTrain, trainingDataFile, inputFeatures);
		networkToTrain.save(outputFile);
	}

	public static void train(NeuralNetwork networkToTrain, String trainingDataFile, List<String> inputFeatures)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		HashMap<String, PersonalityData> userData = PersonalityDataReader.readPersonalityData(trainingDataFile);
		DataSet trainingData = trainingSet(userData, inputFeatures);
		networkToTrain.learn(trainingData);
	}
	
	public static DataSet trainingSet(Map<String, PersonalityData> trainingData, List<String> inputFeatures) {
		DataSet trainingSet = new DataSet(inputFeatures.size(), TRAIT_CLASSES.size());
		for(String id : trainingData.keySet()) {
			PersonalityData row = trainingData.get(id);
			double[] wordFrequencies = getWordFrequenciesAsArray(row, inputFeatures);
			double[] traits = row.getClassesAsNumericArray();
			trainingSet.addRow(wordFrequencies, traits);
		}
		return trainingSet;
	}
	
	public static double[] getWordFrequenciesAsArray(PersonalityData row, List<String> inputFeatures) {
		HashMap<String, Double> wordFrequencies = row.getWordFrequencies();
		double[] frequencies = new double[inputFeatures.size()];
		for(int i = 0; i < inputFeatures.size(); i++) {
			frequencies[i] = wordFrequencies.get(inputFeatures.get(i));
		}
		return frequencies;
	}
}
