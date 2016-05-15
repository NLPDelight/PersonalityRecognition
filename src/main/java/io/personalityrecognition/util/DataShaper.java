package io.personalityrecognition.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mkobos.pca_transform.PCA;

import Jama.Matrix;
import static io.personalityrecognition.util.DatasetKeys.*;

public class DataShaper {

	private static final int WORD_COUNT = 1000;
	
	private TypeCounter typeCounter;
	private HashSet<String> acceptedTokens;
	private HashMap<String, PersonalityData> users;
	private String filename;

	public DataShaper(String filename) throws IOException {
		this.filename = filename;
		typeCounter = new TypeCounter(LANGUAGE_MODEL, STOPWORDS);
		users = new HashMap<String, PersonalityData>();
		acceptedTokens = new HashSet<String>();
	}

	public DataShaper shapeData() throws IOException {
		determineAcceptedTokens();
		aggregateDataByUser();
		calculateUserWordFrequencies();
		return this;
	}
	
	public List<String> getWordOrder() {
		List<Map.Entry<String, PersonalityData>> list = new LinkedList<>(users.entrySet());
		PersonalityData firstRow = list.get(0).getValue();
		LinkedList<String> words = new LinkedList<>(firstRow.getWordFrequencies().keySet());
		Collections.sort(words);
		return words;
	}

	public HashMap<String, PersonalityData> getUsers() {
		return users;
	}
	
	public HashSet<String> getAcceptedTokens() {
		return acceptedTokens;
	}

	private void calculateUserWordFrequencies() {
		for(String id : users.keySet()) {
			users.put(id, users.get(id).normalize());
		}
	}

	private void aggregateDataByUser() throws IOException {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		for(Map<String, String> row : data) {
			updateUserValues(row);
		}
	}

	private void updateUserValues(Map<String, String> row) {
		String id = row.get(ID);
		if(users.containsKey(id)) {
			addWordsToUser(row);
		} else {
			addNewUser(row);
		}
	}

	private void addNewUser(Map<String, String> row) {
		String id = row.get(ID);
		PersonalityData newUser = createNewUser(row);
		users.put(id, newUser);
		addWordsToUser(row);
	}

	private PersonalityData createNewUser(Map<String, String> row) {
		PersonalityData newUser = new PersonalityData(row.get(ID));

		newUser
			.isAgreeable(row.get(AGREEABLENESS_CLASS))
			.isOpen(row.get(OPENNESS_CLASS))
			.isExtraverted(row.get(EXTRAVERT_CLASS))
			.isNeurotic(row.get(NEUROTIC_CLASS))
			.isConscientious(row.get(CONSCIENTIOUSNESS_CLASS));
//			.setAgreeablenessScore(Double.parseDouble(row.get(AGREEABLENESS_SCORE)))
//			.setOpennessScore(Double.parseDouble(row.get(OPENNESS_SCORE)))
//			.setExtraversionScore(Double.parseDouble(row.get(EXTRAVERT_SCORE)))
//			.setNeuroticScore(Double.parseDouble(row.get(NEUROTIC_SCORE)))
//			.setConscientiousnessScore(Double.parseDouble(row.get(CONSCIENTIOUSNESS_SCORE)));

		return newUser;
	}

	private void addWordsToUser(Map<String, String> row) {
		String id = row.get(ID);
		String statusText = row.get(TEXT).trim();
		PersonalityData user = users.get(id);
		Map<String, Integer> counts = typeCounter.countTypesInSet(statusText, acceptedTokens);
		user.addPost(statusText);

		for(String word : counts.keySet()) {
			user.addToWordCount(word, counts.get(word));
		}

		users.put(id, user);
	}

	private void determineAcceptedTokens() throws IOException {
		Map<String, Integer> counts = getTokenCounts();
		List<Map.Entry<String, Integer>> sortedCounts = sortCounts(counts);
		acceptedTokens = takeTopNCounts(sortedCounts, WORD_COUNT);
	}

	private HashSet<String> takeTopNCounts(List<Map.Entry<String, Integer>> sortedCounts, int n) {
		HashSet<String> topN = new HashSet<>();
		for(int i = 0; i < n; i++) {
			topN.add(sortedCounts.get(i).getKey());
		}
		return topN;
	}

	private List<Map.Entry<String, Integer>> sortCounts(Map<String, Integer> counts) {
		LinkedList<Map.Entry<String, Integer>> rows = new LinkedList<>(counts.entrySet());
		Comparator<Map.Entry<String, Integer>> comp = new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		};

		Collections.sort(rows, comp);

		return rows;
	}

	private Map<String, Integer> getTokenCounts() throws IOException {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		Map<String, Integer> counts = new HashMap<String, Integer>();

		for(Map<String, String> row : data) {
			addCounts(counts, typeCounter.countTypes(row.get(TEXT)));
		}

		return counts;
	}

	public void addCounts(Map<String, Integer> target, Map<String, Integer> source) {
		for(String key : source.keySet()) {
			if(target.containsKey(key))
				target.put(key, target.get(key) + source.get(key));
			else
				target.put(key, source.get(key));
		}
	}


}
