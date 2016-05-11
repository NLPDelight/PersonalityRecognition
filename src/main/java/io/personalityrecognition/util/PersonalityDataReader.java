package io.personalityrecognition.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PersonalityDataReader {

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
	private static final HashSet<String> TRAIT_CLASSES;
	private static final HashSet<String> TRAIT_SCORES;
	
	static {
		TRAIT_CLASSES = new HashSet<>(Arrays.asList(EXTRAVERT_CLASS, OPENNESS_CLASS, NEUROTIC_CLASS,
				CONSCIENTIOUSNESS_CLASS, AGREEABLENESS_CLASS));
		TRAIT_SCORES = new HashSet<>(Arrays.asList(EXTRAVERT_SCORE, OPENNESS_SCORE, NEUROTIC_SCORE,
				CONSCIENTIOUSNESS_SCORE, AGREEABLENESS_SCORE));
	}
	
	public static HashMap<String, PersonalityData> readPersonalityData(String filename) throws FileNotFoundException,
			UnsupportedEncodingException, IOException {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		HashMap<String, PersonalityData> table = new HashMap<>();
		for(Map<String, String> row : data) {
			String id = row.get(ID);
			PersonalityData user = extractPersonalityData(row);
			table.put(id, user);
		}
		return table;
	}
	
	private static PersonalityData extractPersonalityData(Map<String, String> row) {
		PersonalityData newUser = new PersonalityData(row.get(ID));

		newUser
			.isAgreeable(row.get(AGREEABLENESS_CLASS))
			.isOpen(row.get(OPENNESS_CLASS))
			.isExtraverted(row.get(EXTRAVERT_CLASS))
			.isNeurotic(row.get(NEUROTIC_CLASS))
			.isConscientious(row.get(CONSCIENTIOUSNESS_CLASS))
			.setAgreeablenessScore(Double.parseDouble(row.get(AGREEABLENESS_SCORE)))
			.setOpennessScore(Double.parseDouble(row.get(OPENNESS_SCORE)))
			.setExtraversionScore(Double.parseDouble(row.get(EXTRAVERT_SCORE)))
			.setNeuroticScore(Double.parseDouble(row.get(NEUROTIC_SCORE)))
			.setConscientiousnessScore(Double.parseDouble(row.get(CONSCIENTIOUSNESS_SCORE)));
		
		addWordFrequenciesToUser(row, newUser);
		
		return newUser;
	}
	
	private static void addWordFrequenciesToUser(Map<String, String> row, PersonalityData data) {
		Set<String> words = getWordColumns(row);
		for(String word : words) {
			data.addWordFrequency(word, Double.parseDouble(row.get(word)));
		}
	}
	
	private static Set<String> getWordColumns(Map<String, String> row) {
		Set<String> words = row.keySet();
		words.removeAll(TRAIT_CLASSES);
		words.removeAll(TRAIT_SCORES);
		words.remove(ID);
		return words;
		
	}

}
