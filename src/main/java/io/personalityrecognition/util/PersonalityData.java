package io.personalityrecognition.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

	/**
	 * This class is the container class for each user from the raw dataset. We track each user by the list of variables declared below.
	 *
	 * @param
	 * @return
	 * @throws
	 */
public class PersonalityData {

	private String userId;
	private double neuroticScore;
	private double opennessScore;
	private double conscientiousnessScore;
	private double agreeablenessScore;
	private double extraversionScore;
	private boolean neuroticClass;
	private boolean opennessClass;
	private boolean conscientiousnessClass;
	private boolean agreeablenessClass;
	private boolean extraversionClass;
	private HashSet<String> posts;
	private HashMap<String, Integer> wordCounts;
	private HashMap<String, Double> wordFrequencies;
	private int tokenCount;

	public PersonalityData() {
		wordCounts = new HashMap<String, Integer>();
		wordFrequencies = new HashMap<String, Double>();
		posts = new HashSet<String>();
	}

	public PersonalityData(String userId) {
		wordCounts = new HashMap<String, Integer>();
		wordFrequencies = new HashMap<String, Double>();
		posts = new HashSet<String>();
		this.userId = userId;
	}

	public PersonalityData addToWordCount(String word, int count) {
		if(wordCounts.containsKey(word))
			wordCounts.put(word, wordCounts.get(word) + count);
		else
			wordCounts.put(word, count);
		tokenCount += count;
		return this;
	}

	public double[] getClassesAsNumericArray() {
		return new double[] { asNumber(extraversionClass), asNumber(opennessClass), asNumber(neuroticClass),
				asNumber(agreeablenessClass), asNumber(conscientiousnessClass)};
	}

	private double asNumber(boolean bool) {
		return bool ? 1 : 0;
	}

	public PersonalityData normalize() {
		for(String word : wordCounts.keySet()) {
			wordFrequencies.put(word, ((double) wordCounts.get(word)) / ((double) tokenCount));
		}
		return this;
	}

	public PersonalityData addWordFrequency(String word, double frequency) {
		if(wordFrequencies == null)
			wordFrequencies = new HashMap<String, Double>();
		wordFrequencies.put(word, frequency);
		return this;
	}

	private double booleanAsDouble(boolean bool) {
		return bool ? 1 : 0;
	}

	public PersonalityData addPost(String post) {
		this.posts.add(post);
		return this;
	}

	public Set<String> getPosts() {
		return posts;
	}

	public String getUserId() {
		return userId;
	}
	public PersonalityData setUserId(String userId) {
		this.userId = userId;
		return this;
	}
	public double getNeuroticScore() {
		return neuroticScore;
	}
	public PersonalityData setNeuroticScore(double neurotic_score) {
		this.neuroticScore = neurotic_score;
		return this;
	}
	public double getOpennessScore() {
		return opennessScore;
	}
	public PersonalityData setOpennessScore(double openness_score) {
		this.opennessScore = openness_score;
		return this;
	}
	public double getConscientiousnessScore() {
		return conscientiousnessScore;
	}
	public PersonalityData setConscientiousnessScore(double conscientiousness_score) {
		this.conscientiousnessScore = conscientiousness_score;
		return this;
	}
	public double getAgreeablenessScore() {
		return agreeablenessScore;
	}
	public PersonalityData setAgreeablenessScore(double agreeableness_score) {
		this.agreeablenessScore = agreeableness_score;
		return this;
	}
	public double getExtraversionScore() {
		return extraversionScore;
	}
	public PersonalityData setExtraversionScore(double extraversion_score) {
		this.extraversionScore = extraversion_score;
		return this;
	}
	public boolean isNeurotic() {
		return neuroticClass;
	}
	public PersonalityData isNeurotic(String neurotic_class) {
		this.neuroticClass = _yNChecker(neurotic_class);
		return this;
	}
	public boolean isOpen() {
		return opennessClass;
	}
	public PersonalityData isOpen(String openness_class) {
		this.opennessClass = _yNChecker(openness_class);
		return this;
	}
	public boolean isConscientious() {
		return conscientiousnessClass;
	}
	public PersonalityData isConscientious(String conscientiousness_class) {
		this.conscientiousnessClass = _yNChecker(conscientiousness_class);
		return this;
	}
	public boolean isAgreeable() {
		return agreeablenessClass;
	}
	public PersonalityData isAgreeable(String agreeableness_class) {
		this.agreeablenessClass = _yNChecker(agreeableness_class);
		return this;
	}
	public boolean isExtraverted() {
		return extraversionClass;
	}
	public PersonalityData isExtraverted(String extraversion_class) {
		this.extraversionClass = _yNChecker(extraversion_class);
		return this;
	}
	public HashMap<String, Integer> getWordCounts() {
		return wordCounts;
	}
	public PersonalityData setWordCounts(HashMap<String, Integer> wordCounts) {
		this.wordCounts = wordCounts;
		return this;
	}
	public HashMap<String, Double> getWordFrequencies() {
		return wordFrequencies;
	}
	public int getTokenCount() {
		return tokenCount;
	}
	public PersonalityData setTokenCount(int tokenCount) {
		this.tokenCount = tokenCount;
		return this;
	}

	private boolean _yNChecker(String input) {
		if (input.equals("y")) {
			return true;
		}
		return false;
	}

}
