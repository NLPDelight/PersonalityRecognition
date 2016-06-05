package io.personalityrecognition.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.personalityrecognition.util.DatasetKeys.*;

/**
 * This class writes processed PersonalityDataMap to a file for easier access and avoid reprocessing the raw data.
 *
 * @param
 * @return
 * @throws
 */
public class PersonalityDataWriter{

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

	public static void writeFileAsNeurophDataSet(List<String> wordsToShow, Map<String, PersonalityData> data,
			String filename) throws IOException {
		BufferedWriter csvWriter = new BufferedWriter(new FileWriter(filename));
		for(String id : data.keySet()) {
			PersonalityData row = data.get(id);
			row.normalize();
			String csvRow = frequenciesToCSV(row, wordsToShow) + "," + join(row.getClassesAsNumericArray());
			csvWriter.append(csvRow + "\n");
		}
	}

	private static String getHeaderRow(List<String> wordsToShow) {
		return
				wrapInQuotes(ID) + "," +
				wrapInQuotes(EXTRAVERT_CLASS) + "," +
				wrapInQuotes(NEUROTIC_CLASS) + "," +
				wrapInQuotes(AGREEABLENESS_CLASS) + "," +
				wrapInQuotes(CONSCIENTIOUSNESS_CLASS) + "," +
				wrapInQuotes(OPENNESS_CLASS) + "," +
				wrapInQuotes(POSTS) + "," +
				wordsToCSV(wordsToShow);

	}

	private static String join(double[] array) {
		if(array.length == 0)
			return "";
		String str = "" + array[0];
		for(int i = 1; i < array.length; i++) {
			str += "," + array[i];
		}
		return str;
	}

	private static String wordsToCSV(List<String> words) {
		String csvString = "";
		for(int i = 0; i < words.size(); i++) {
			csvString += "," + wrapInQuotes(escapeIllegalTextCharacters(words.get(i)));
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
				getPosts(data);

		return csvString + frequenciesToCSV(data, wordsToShow);
	}

	private static String getPosts(PersonalityData data) {
		Set<String> posts = data.getPosts();

		StringBuilder sb = new StringBuilder();

		for (String s : posts) {
			s = s.replace("\"", "\"\"");
			sb.append(s);
		}

		return wrapInQuotes(sb.toString());
	}

	private static String escapeIllegalTextCharacters(String raw) {
		return raw.replace("\"", "\"\"");
	}

	private static String frequenciesToCSV(PersonalityData data, List<String> wordsToShow) {
		HashMap<String, Double> wordFrequencies = data.getWordFrequencies();
		String csvString = getWordFrequencyValue(wordFrequencies.get(0)).toString();
		for(int i = 1; i < wordsToShow.size(); i++) {
			double frequency = getWordFrequencyValue(wordFrequencies.get(wordsToShow.get(i)));
			csvString += "," + frequency;
		}
		return csvString;
	}

	private static Double getWordFrequencyValue(Double input) {
		return input == null ? 0 : input;
	}

	private static String personalityClassToString(boolean hasClass) {
		String textValue = hasClass ? HAS_CLASS : DOES_NOT_HAVE_CLASS;
		return wrapInQuotes(textValue);
	}


	private static String wrapInQuotes(String unquotedText) {
		return String.format("\"%s\"", unquotedText);
	}

}
