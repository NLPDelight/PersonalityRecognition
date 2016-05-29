package io.personalityrecognition;

import io.personalityrecognition.LogisticRegressionUtil.Observation;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataReader;
import io.personalityrecognition.util.SerializerUtil;
import java.io.FileInputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import opennlp.tools.ngram.NGramGenerator;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

public class LogisticRegressionTest {
	public void prepareDataSets() throws IOException {
		for (int i = 0; i < DATASETS.length; i++) {
			prepareSet(DATASETPATHS[i] + "_train.csv", DATASETS[i]);

			prepareTest(DATASETPATHS[i] + "_test.csv", DATASETS[i]);
		}
	}

	public Map<String, Map<String, Map<String, Double>>> testSuite(String dataset) throws Exception {
		if (!Files.exists(_fileStorePath)) {
			prepareDataSets();
		}

		Map<String, Map<String, Map<String, Double>>> resultsMap = new HashMap<>();

		for (String test : TESTS) {
			resultsMap.put(test, runTest(dataset, test));
		}

		return resultsMap;
	}

	public Map<String, Map<String, Double>> runTest(String datasetName, String datasetType) throws Exception {
		HashMap<String, Map<String, Double>> bigramMap =
			SerializerUtil.loadSerial(_fileStorePath.resolve(datasetName + "-" + datasetType));

		HashMap<String, Map<String, Map<String, Double>>> bigramTestMap = SerializerUtil.loadSerialTest(_fileStorePath.resolve(datasetName + "-test-" + datasetType));

		Map<String, OnlineLogisticRegression> olrMap = getOLRMap(bigramMap);

		LogisticRegressionUtil lRU = new LogisticRegressionUtil();

		Map<String, Map<String, Double>> resultMap = new HashMap<>();

		for (String type : TRAITS) {
			Map<String, Double> currentTypeResultMap = new HashMap<>();

			Map<String, Map<String, Double>> bigramTestType = bigramTestMap.get(type);

			Map<String, Map<String, Double>> otherTestType = new HashMap<>();

			for (String t : TRAITS) {
				if (!t.equals(type)) {
					for (Entry<String, Map<String, Double>> entry : bigramTestMap.get(t).entrySet()) {
						if (!bigramTestType.containsKey(entry.getKey())) {
							otherTestType.put(entry.getKey(), entry.getValue());
						}
					}
				}
			}


			double sum = 0;

			for (Map<String, Double> user : bigramTestType.values()) {

				double result = lRU.testModel(user, olrMap.get(type));

				if (result > 0) {
					sum += result;
				}
			}

			currentTypeResultMap.put("TP", sum/bigramTestType.size());
			currentTypeResultMap.put("FN", 1 - sum/bigramTestType.size());

			sum = 0;

			for (Map<String, Double> user : otherTestType.values()) {

				double result = lRU.testModel(user, olrMap.get(type));

				if (result > 0) {
					sum += result;
				}
			}

			currentTypeResultMap.put("FP", sum/bigramTestType.size());
			currentTypeResultMap.put("TN", 1 - sum/bigramTestType.size());

			resultMap.put(type, currentTypeResultMap);
		}

		return resultMap;
	}

	public Map<String, OnlineLogisticRegression> getOLRMap(HashMap<String, Map<String, Double>> trainingMap) {
		Map<String, OnlineLogisticRegression> olrMap = new HashMap<>();

		for (String type : TRAITS) {
			Map<String, Double> extMap = trainingMap.get(type);

			extMap = sortByValue(extMap);

			Map<String, Double> othermaps = new HashMap<>();

			for (String t : TRAITS) {
				if (!t.equals(type)) {
					for (Entry<String, Double> entry : trainingMap.get(t).entrySet()) {
						othermaps.put(entry.getKey(), entry.getValue());
					}
				}
			}

			othermaps = sortByValue(othermaps);

			Map<String, Double> trimmedMapA = trimMap(extMap);

			Map<String, Double> trimmedMapB = trimMap(othermaps);

			LogisticRegressionUtil lRU = new LogisticRegressionUtil();

			List<Observation> trainingData = lRU.parseInput(trimmedMapA, trimmedMapB);

			olrMap.put(type, lRU.testMethod(trainingData));
		}

		return olrMap;
	}

	public Map<String, Double> trimMap(Map<String, Double> map) {
		Map<String, Double> trim = new HashMap<>();

		int i = 0;

		for (Entry<String, Double> entry : map.entrySet()) {
			if (i < 300) {
				trim.put(entry.getKey(), entry.getValue());

				i++;
			} else {
				break;
			}
		}

		return trim;
	}

	public void prepareTest(String fileName, String setName) throws IOException {
		Map<String, PersonalityData> data
			= PersonalityDataReader.readPersonalityData(fileName);

		HashMap<String, Map<String, Map<String, Double>>> unigramMap = new HashMap<>();
		HashMap<String, Map<String, Map<String, Double>>> bigramMap = new HashMap<>();
		HashMap<String, Map<String, Map<String, Double>>> trigramMap = new HashMap<>();

		for (String type : TRAITS) {
			unigramMap.put(type, new HashMap<String, Map<String, Double>>());
			bigramMap.put(type, new HashMap<String, Map<String, Double>>());
			trigramMap.put(type, new HashMap<String, Map<String, Double>>());
		}

		for (PersonalityData pd : data.values()) {
			if (pd.isOpen()) {
				Map<String, Map<String, Double>> map = unigramMap.get(TRAITS[0]);

				map.put(pd.getUserId(), _getUnigramTestSet(pd.getPosts()));

				map = bigramMap.get(TRAITS[0]);

				map.put(pd.getUserId(), _getNGramTestSet(2, pd.getPosts()));

				map = trigramMap.get(TRAITS[0]);

				map.put(pd.getUserId(), _getNGramTestSet(3, pd.getPosts()));
			}
			if (pd.isConscientious()) {
				Map<String, Map<String, Double>> map = unigramMap.get(TRAITS[1]);

				map.put(pd.getUserId(), _getUnigramTestSet(pd.getPosts()));

				map = bigramMap.get(TRAITS[1]);

				map.put(pd.getUserId(), _getNGramTestSet(2, pd.getPosts()));

				map = trigramMap.get(TRAITS[1]);

				map.put(pd.getUserId(), _getNGramTestSet(3, pd.getPosts()));
			}
			if (pd.isExtraverted()) {
				Map<String, Map<String, Double>> map = unigramMap.get(TRAITS[2]);

				map.put(pd.getUserId(), _getUnigramTestSet(pd.getPosts()));

				map = bigramMap.get(TRAITS[2]);

				map.put(pd.getUserId(), _getNGramTestSet(2, pd.getPosts()));

				map = trigramMap.get(TRAITS[2]);

				map.put(pd.getUserId(), _getNGramTestSet(3, pd.getPosts()));
			}
			if (pd.isAgreeable()) {
				Map<String, Map<String, Double>> map = unigramMap.get(TRAITS[3]);

				map.put(pd.getUserId(), _getUnigramTestSet(pd.getPosts()));

				map = bigramMap.get(TRAITS[3]);

				map.put(pd.getUserId(), _getNGramTestSet(2, pd.getPosts()));

				map = trigramMap.get(TRAITS[3]);

				map.put(pd.getUserId(), _getNGramTestSet(3, pd.getPosts()));
			}
			if (pd.isNeurotic()) {
				Map<String, Map<String, Double>> map = unigramMap.get(TRAITS[4]);

				map.put(pd.getUserId(), _getUnigramTestSet(pd.getPosts()));

				map = bigramMap.get(TRAITS[4]);

				map.put(pd.getUserId(), _getNGramTestSet(2, pd.getPosts()));

				map = trigramMap.get(TRAITS[4]);

				map.put(pd.getUserId(), _getNGramTestSet(3, pd.getPosts()));
			}
		}

		SerializerUtil.storeSerialTest(unigramMap, _fileStorePath.resolve(setName + "-test-unigram"));
		SerializerUtil.storeSerialTest(bigramMap, _fileStorePath.resolve(setName + "-test-bigram"));
		SerializerUtil.storeSerialTest(trigramMap, _fileStorePath.resolve(setName + "-test-trigram"));
	}

	private Map<String, Double> _getUnigramTestSet(Set<String> posts) throws IOException {
		Tokenizer tokenizer = new TokenizerME(
			new TokenizerModel(new FileInputStream("en-token.bin")));

		String tokens[] = tokenizer.tokenize(posts.toString());

		List<String> stopwordsList = Files.readAllLines(
			Paths.get("stopwordList.txt"));

		Map<String, Double> wordMap = new HashMap<>();

		double totalWords = 0;
		double one = 1;
		for (String s : tokens) {
			if (!stopwordsList.contains(s)) {
				if (wordMap.containsKey(s)) {
					double i = wordMap.get(s);
					i++;
					wordMap.put(s, i);
				} else {
					wordMap.put(s, one);
				}
				totalWords++;
			}
		}

		for (Entry<String, Double> entry : wordMap.entrySet()) {
			double current = entry.getValue();

			double normal = current / totalWords;

			wordMap.put(entry.getKey(), normal);
		}

		wordMap = sortByValue(wordMap);

		return wordMap;
	}

	private Map<String, Double> _getNGramTestSet(int n, Set<String> posts) throws IOException {
		Tokenizer tokenizer = new TokenizerME(
			new TokenizerModel(new FileInputStream("en-token.bin")));

		Map<String, Double> ngrams = new HashMap<>();

		double totalWords = 0;
		double one = 1;

		for (String s : posts) {
			String s1 = s.replaceAll("[^a-zA-Z ]", "");
			String tokens2[] = tokenizer.tokenize(s);
			List<String> ngram = NGramGenerator.generate(
				Arrays.asList(tokens2), n, "-");

			for (String s2 : ngram) {
				if (ngrams.containsKey(s2)) {
					double i = ngrams.get(s2);
					i++;
					ngrams.put(s2, i);
				} else {
					ngrams.put(s2, one);
				}
				totalWords++;
			}
		}

		for (Entry<String, Double> entry : ngrams.entrySet()) {
			double current = entry.getValue();

			double normal = current / totalWords;

			ngrams.put(entry.getKey(), normal);
		}

		ngrams = sortByValue(ngrams);

		return ngrams;
	}

	public void prepareSet(String fileName, String fileType) throws IOException {
		_populateWordBank(fileName);

		if (!Files.exists(_fileStorePath)) {
			Files.createDirectory(_fileStorePath);
		}

		HashMap<String, Map<String, Double>> unigramMap = new HashMap<>();

		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		System.out.println("Current Date: " + ft.format(dNow));

		for (String type : TRAITS) {
			unigramMap.put(type, _getUnigrams(type));
		}

		SerializerUtil.storeSerial(unigramMap, _fileStorePath.resolve(fileType + "-unigram"));

		System.out.println("unigram finished at: " + ft.format(new Date()));

		HashMap<String, Map<String, Double>> bigramMap = new HashMap<>();

		for (String type : TRAITS) {
			bigramMap.put(type, _getNGrams(2, type));
		}

		SerializerUtil.storeSerial(bigramMap, _fileStorePath.resolve(fileType + "-bigram"));

		System.out.println("bigram finished at: " + ft.format(new Date()));

		HashMap<String, Map<String, Double>> trigram = new HashMap<>();

		for (String type : TRAITS) {
			trigram.put(type, _getNGrams(3, type));
		}

		SerializerUtil.storeSerial(trigram, _fileStorePath.resolve(fileType + "-trigram"));

		System.out.println("trigram finished at: " + ft.format(new Date()));
	}

	private Map<String, Double> _getNGrams(int n, String personalityType)
		throws IOException {

		List<String> list = _wordBank.get(personalityType);

		Tokenizer tokenizer = new TokenizerME(
			new TokenizerModel(new FileInputStream("en-token.bin")));

		Map<String, Double> ngrams = new HashMap<>();

		double totalGramCount = 0;

		double one = 1;

		for (String s : list) {
			String s1 = s.replaceAll("[^a-zA-Z ]", "");
			String tokens2[] = tokenizer.tokenize(s);
			List<String> ngram = NGramGenerator.generate(
				Arrays.asList(tokens2), n, "-");

			for (String s2 : ngram) {
				if (ngrams.containsKey(s2)) {
					Double i = ngrams.get(s2);
					i++;
					ngrams.put(s2, i);
				} else {
					ngrams.put(s2, one);
				}
				totalGramCount++;
			}
		}

		for (Entry<String, Double> entry : ngrams.entrySet()) {
			double current = entry.getValue();

			double normal = current / totalGramCount;

			ngrams.put(entry.getKey(), normal);
		}

		ngrams = sortByValue(ngrams);

		return ngrams;
	}

	private Map<String, Double> _getUnigrams(String personalityType)
		throws IOException {

		List<String> list = _wordBank.get(personalityType);

		Tokenizer tokenizer = new TokenizerME(
			new TokenizerModel(new FileInputStream("en-token.bin")));

		String tokens[] = tokenizer.tokenize(list.toString());

		List<String> stopwordsList = Files.readAllLines(
			Paths.get("stopwordList.txt"));

		Map<String, Double> wordMap = new LinkedHashMap<>();

		double totalWordsCount = 0;

		double one = 1;

		for (String s : tokens) {
			if (!stopwordsList.contains(s)) {
				if (wordMap.containsKey(s)) {
					double i = wordMap.get(s);
					i++;
					wordMap.put(s, i);
				} else {
					wordMap.put(s, one);
				}
				totalWordsCount++;
			}
		}

		for (Entry<String, Double> entry : wordMap.entrySet()) {
			double current = entry.getValue();

			double normal = current / totalWordsCount;

			wordMap.put(entry.getKey(), normal);
		}
		wordMap = sortByValue(wordMap);

		return wordMap;
	}

	private void _populateWordBank(String filePath) throws IOException {
		Map<String, PersonalityData> data
			= PersonalityDataReader.readPersonalityData(filePath);

		_wordBank = new HashMap<>();

		for (String type : TRAITS) {
			_wordBank.put(type, new ArrayList<String>());
		}
		for (PersonalityData p : data.values()) {
			if (p.isNeurotic()) {
				_appendSet(p, "Neuroticism");
			}

			if (p.isOpen()) {
				_appendSet(p, "Openness");
			}

			if (p.isConscientious()) {
				_appendSet(p, "Conscientiousness");
			}

			if (p.isAgreeable()) {
				_appendSet(p, "Agreeableness");
			}

			if (p.isExtraverted()) {
				_appendSet(p, "Extraversion");
			}
		}
	}

	private void _appendSet(PersonalityData p, String type) {
		List<String> list = _wordBank.get(type);

		Set<String> set = p.getPosts();

		for (String s : set) {
			list.add(s);
		}
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list
			= new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private Map<String, List<String>> _wordBank;
	private Path _fileStorePath = Paths.get("trainedData");
	private static final String[] DATASETPATHS = new String[]{"essay/essay", "my_personality/non_pca/my_personality"};
	private static final String[] DATASETS = new String[]{"essay", "my_personality"};
	private static final String[] TESTS = new String[]{"unigram", "bigram", "trigram"};
	private static final String[] TRAITS = new String[]{"Openness", "Conscientiousness", "Extraversion", "Agreeableness", "Neuroticism"};
}
