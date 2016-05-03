package io.personalityrecognition.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataShaper {

	private static final String LANGUAGE_MODEL = "en-token.bin";
	private static final String STOPWORDS = "stopwordList.txt";
	private static final String ID = "#AUTHID";
	private static final String TEXT = "STATUS";
	private static final String EXTRAVERT_SCORE = "sEXT";
	private static final String OPENNESS_SCORE = "sOPN";
	private static final String NEUROTIC_SCORE = "sNEU";
	private static final String CONSCIENTIOUSNESS_SCORE = "sCON";
	private static final String AGREEABLENESS_SCORE = "sAGR";
	private static final String EXTRAVERT_CLASS = "cEXT";
	private static final String OPENNESS_CLASS = "cOPN";
	private static final String NEUROTIC_CLASS = "cNEU";
	private static final String CONSCIENTIOUSNESS_CLASS = "cCON";
	private static final String AGREEABLENESS_CLASS = "cAGR";
	
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
	
	public HashMap<String, PersonalityData> getUsers() {
		return users;
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
			.setAgreeableness_class(row.get(AGREEABLENESS_CLASS))
			.setOpenness_class(row.get(OPENNESS_CLASS))
			.setExtraversion_class(row.get(EXTRAVERT_CLASS))
			.setNeurotic_class(row.get(NEUROTIC_CLASS))
			.setConscientiousness_class(row.get(CONSCIENTIOUSNESS_CLASS))
			.setAgreeableness_score(Double.parseDouble(row.get(AGREEABLENESS_SCORE)))
			.setOpenness_score(Double.parseDouble(row.get(OPENNESS_SCORE)))
			.setExtraversion_score(Double.parseDouble(row.get(EXTRAVERT_SCORE)))
			.setNeurotic_score(Double.parseDouble(row.get(NEUROTIC_SCORE)))
			.setConscientiousness_score(Double.parseDouble(row.get(CONSCIENTIOUSNESS_SCORE)));
		
		return newUser;
	}
	
	private void addWordsToUser(Map<String, String> row) {
		String id = row.get(ID);
		String statusText = row.get(TEXT);
		Map<String, Integer> counts = typeCounter.countTypesInSet(statusText, acceptedTokens);
		PersonalityData user = users.get(id);
		
		for(String word : counts.keySet()) {
			user.addToWordCount(word, counts.get(word));
		}
		
		users.put(id, user);
	}
	
	private void determineAcceptedTokens() throws IOException {
		Map<String, Integer> counts = getTokenCounts();
		List<Map.Entry<String, Integer>> sortedCounts = sortCounts(counts);
		acceptedTokens = takeTopNCounts(sortedCounts, 1000);
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
			addCounts(counts, typeCounter.countTypes(row.get("STATUS")));
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
