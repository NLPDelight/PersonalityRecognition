package io.personalityrecognition.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PersonalityDataWriter{
	
	private static final String HAS_CLASS = "y";
	private static final String DOES_NOT_HAVE_CLASS = "n";
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
	private static final String COMBINED_TEXT = "Raw Text";
	
	public static void writeFile(List<String> wordsToShow, Map<String, PersonalityData> data, String filename) throws IOException {
		BufferedWriter csvWriter = new BufferedWriter(new FileWriter(filename));
		csvWriter.append(getHeaderRow(wordsToShow) + "\n");
		for(String id : data.keySet()) {
			PersonalityData row = data.get(id);
			row.normalize();
			String csvRow = toCSVString(wordsToShow, data.get(id));
			csvWriter.append(csvRow + "\n");
		}
		
		csvWriter.close();
	}
	
	private static String getHeaderRow(List<String> wordsToShow) {
		return 
				wrapInQuotes(ID) + "," +
				wrapInQuotes(EXTRAVERT_CLASS) + "," +
				wrapInQuotes(NEUROTIC_CLASS) + "," +
				wrapInQuotes(AGREEABLENESS_CLASS) + "," +
				wrapInQuotes(CONSCIENTIOUSNESS_CLASS) + "," +
				wrapInQuotes(OPENNESS_CLASS) + "," +
				wrapInQuotes(EXTRAVERT_SCORE) + "," +
				wrapInQuotes(NEUROTIC_SCORE) + "," +
				wrapInQuotes(AGREEABLENESS_SCORE) + "," +
				wrapInQuotes(CONSCIENTIOUSNESS_SCORE) + "," +
				wrapInQuotes(OPENNESS_SCORE) + "," +
				wrapInQuotes(COMBINED_TEXT) + "," +
				wordsToCSV(wordsToShow);
	}
	
	private static String wordsToCSV(List<String> words) {
		String csvString = "";
		for(int i = 0; i < words.size(); i++) {
			csvString += "," + wrapInQuotes(words.get(i));
		}
		return csvString;
	}

	private static String toCSVString(List<String> wordsToShow, PersonalityData data) {
		if(wordsToShow == null)
			wordsToShow = new LinkedList<String>(data.getWordCounts().keySet());
		String csvString = 
				wrapInQuotes(data.getUserId()) + "," +
				personalityClassToString(data.isExtraverted()) + "," +
				personalityClassToString(data.isNeurotic()) + "," +
				personalityClassToString(data.isAgreeable()) + "," +
				personalityClassToString(data.isConscientious()) + "," +
				personalityClassToString(data.isOpen()) + "," +
				data.getExtraversionScore() + "," +
				data.getNeuroticScore() + "," +
				data.getAgreeablenessScore() + "," +
				data.getConscientiousnessScore() + "," +
				data.getOpennessScore() + "," +
				wrapInQuotes(data.getCombinedText());
		return csvString + frequenciesToCSV(data, wordsToShow);
	}
	
	private static String frequenciesToCSV(PersonalityData data, List<String> wordsToShow) {
		HashMap<String, Double> wordFrequencies = data.getWordFrequencies();
		String csvString = "";
		for(int i = 0; i < wordsToShow.size(); i++) {
			double frequency = wordFrequencies.get(wordsToShow.get(i)) != null ?
							   wordFrequencies.get(wordsToShow.get(i)) : 0;
			csvString += "," + frequency;
		}
		return csvString;
	}
	
	private static String personalityClassToString(boolean hasClass) {
		String textValue = hasClass ? HAS_CLASS : DOES_NOT_HAVE_CLASS;
		return wrapInQuotes(textValue);
	}

	
	private static String wrapInQuotes(String unquotedText) {
		return String.format("\"%s\"", unquotedText);
	}

}
