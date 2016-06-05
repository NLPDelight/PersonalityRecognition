package io.personalityrecognition.util;

import static io.personalityrecognition.util.DatasetKeys.AGREEABLENESS_CLASS;
import static io.personalityrecognition.util.DatasetKeys.CONSCIENTIOUSNESS_CLASS;
import static io.personalityrecognition.util.DatasetKeys.EXTRAVERT_CLASS;
import static io.personalityrecognition.util.DatasetKeys.ID;
import static io.personalityrecognition.util.DatasetKeys.LANGUAGE_MODEL;
import static io.personalityrecognition.util.DatasetKeys.NEUROTIC_CLASS;
import static io.personalityrecognition.util.DatasetKeys.OPENNESS_CLASS;
import static io.personalityrecognition.util.DatasetKeys.STOPWORDS;
import static io.personalityrecognition.util.DatasetKeys.TEXT;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class processes the CSV file into a processed dataset.
 *
 * @param
 * @return
 * @throws
 */
public class DataShaper {

	private static final int WORD_COUNT = 1000;

	private TypeCounter typeCounter;
	private HashSet<String> acceptedTokens;
	private HashMap<String, PersonalityData> users;
	private String filename;

	/**
	 * This method is the constructor
	 *
	 * @param String
	 * @return
	 * @throws
	 */
	public DataShaper(String filename) throws IOException {
		this.filename = filename;
		typeCounter = new TypeCounter(LANGUAGE_MODEL, STOPWORDS);
		users = new HashMap<String, PersonalityData>();
		acceptedTokens = new HashSet<String>();
	}

	/**
	 * This method calls the sub methods and returns the processed data.
	 *
	 * @param
	 * @return
	 * @throws
	 */
	public DataShaper shapeData() throws IOException {
		determineAcceptedTokens();
		aggregateDataByUser();
		calculateUserWordFrequencies();
		return this;
	}

	/**
	 * This method returns the words list in sorted order.
	 *
	 * @param
	 * @return List<String>
	 * @throws
	 */
	public List<String> getWordOrder() {
		List<Map.Entry<String, PersonalityData>> list = new LinkedList<>(users.entrySet());
		PersonalityData firstRow = list.get(0).getValue();
		LinkedList<String> words = new LinkedList<>(firstRow.getWordFrequencies().keySet());
		Collections.sort(words);
		return words;
	}

	/**
	 * This method is the getter for users
	 *
	 * @param
	 * @return HashMap<String, PersonalityData>
	 * @throws
	 */
	public HashMap<String, PersonalityData> getUsers() {
		return users;
	}

	/**
	 * This method is the getter for acceptedTokens
	 *
	 * @param
	 * @return HashSet<String>
	 * @throws
	 */
	public HashSet<String> getAcceptedTokens() {
		return acceptedTokens;
	}

	/**
	 * This method calculates user word frequencies
	 *
	 * @param
	 * @return
	 * @throws
	 */
	private void calculateUserWordFrequencies() {
		for(String id : users.keySet()) {
			users.put(id, users.get(id).normalize());
		}
	}

	/**
	 * This method processes each row of the raw dataset
	 *
	 * @param
	 * @return
	 * @throws IOException
	 */
	private void aggregateDataByUser() throws IOException {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		for(Map<String, String> row : data) {
			updateUserValues(row);
		}
	}

	/**
	 * This method takes a row in and determine if the user exists or create a new user.
	 *
	 * @param Map<String, String>
	 * @return
	 * @throws
	 */
	private void updateUserValues(Map<String, String> row) {
		String id = row.get(ID);
		if(users.containsKey(id)) {
			addWordsToUser(row);
		} else {
			addNewUser(row);
		}
	}

	/**
	 * This method processes the row into a new user.
	 *
	 * @param Map<String, String>
	 * @return
	 * @throws
	 */
	private void addNewUser(Map<String, String> row) {
		String id = row.get(ID);
		PersonalityData newUser = createNewUser(row);
		users.put(id, newUser);
		addWordsToUser(row);
	}

	//
	/**
	 * This method creates the new user and sets the user's personality traits.
	 *
	 * @param Map<String, String>
	 * @return PersonalityData
	 * @throws
	 */
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

	/**
	 * This method adds words to the user's word set.
	 *
	 * @param Map<String, String>
	 * @return
	 * @throws
	 */
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

	/**
	 * This method gets the token count and takes the top N tokens
	 *
	 * @param
	 * @return
	 * @throws
	 */
	private void determineAcceptedTokens() throws IOException {
		Map<String, Integer> counts = getTokenCounts();
		List<Map.Entry<String, Integer>> sortedCounts = sortCounts(counts);
		acceptedTokens = takeTopNCounts(sortedCounts, WORD_COUNT);
	}

	/**
	 * This method loop through to N
	 *
	 * @param List<Map.Entry<String, Integer>>
	 * @return HashSet<String>
	 * @throws
	 */
	private HashSet<String> takeTopNCounts(List<Map.Entry<String, Integer>> sortedCounts, int n) {
		HashSet<String> topN = new HashSet<>();
		for(int i = 0; i < n; i++) {
			topN.add(sortedCounts.get(i).getKey());
		}
		return topN;
	}

	/**
	 * This method compares and sorts into a list
	 *
	 * @param Map<String, Integer>
	 * @return List<Map.Entry<String, Integer>>
	 * @throws
	 */
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

	/**
	 * This method gets the token count
	 *
	 * @param
	 * @return Map<String, Integer>
	 * @throws
	 */
	private Map<String, Integer> getTokenCounts() throws IOException {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		Map<String, Integer> counts = new HashMap<String, Integer>();

		for(Map<String, String> row : data) {
			addCounts(counts, typeCounter.countTypes(row.get(TEXT)));
		}

		return counts;
	}

	/**
	 * This method adds the count to the target map
	 *
	 * @param Map<String, Integer>, Map<String, Integer>
	 * @return
	 * @throws
	 */
	public void addCounts(Map<String, Integer> target, Map<String, Integer> source) {
		for(String key : source.keySet()) {
			if(target.containsKey(key))
				target.put(key, target.get(key) + source.get(key));
			else
				target.put(key, source.get(key));
		}
	}


}
