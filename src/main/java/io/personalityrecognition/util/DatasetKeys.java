package io.personalityrecognition.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
	 * This class is a set of keys to use so we don't need to declare them in every class.
	 *
	 * @param
	 * @return
	 * @throws
	 */
public class DatasetKeys {

	public static final String EXTRAVERT_SCORE = "sEXT";
	public static final String OPENNESS_SCORE = "sOPN";
	public static final String NEUROTIC_SCORE = "sNEU";
	public static final String CONSCIENTIOUSNESS_SCORE = "sCON";
	public static final String AGREEABLENESS_SCORE = "sAGR";
	public static final String EXTRAVERT_CLASS = "cEXT";
	public static final String OPENNESS_CLASS = "cOPN";
	public static final String NEUROTIC_CLASS = "cNEU";
	public static final String CONSCIENTIOUSNESS_CLASS = "cCON";
	public static final String AGREEABLENESS_CLASS = "cAGR";
	public static final String DOES_NOT_HAVE_CLASS = "n";
	public static final String HAS_CLASS = "y";
	public static final String ID = "#AUTHID";
	public static final String LANGUAGE_MODEL = "en-token.bin";
	public static final String POSTS = "posts";
	public static final String STOPWORDS = "stopwordList.txt";
	public static final String TEXT = "TEXT";
	public static final List<String> TRAIT_CLASSES;
	public static final List<String> TRAIT_SCORES;

	static {
		TRAIT_CLASSES = new ArrayList<>(Arrays.asList(EXTRAVERT_CLASS, OPENNESS_CLASS, NEUROTIC_CLASS,
			CONSCIENTIOUSNESS_CLASS, AGREEABLENESS_CLASS));
		TRAIT_SCORES = new ArrayList<>(Arrays.asList(EXTRAVERT_SCORE, OPENNESS_SCORE, NEUROTIC_SCORE,
			CONSCIENTIOUSNESS_SCORE, AGREEABLENESS_SCORE));
	}
}
