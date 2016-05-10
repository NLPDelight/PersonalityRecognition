package io.personalityrecognition;

import io.personalityrecognition.util.CSVMapper;
import io.personalityrecognition.util.DataShaper;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataWriter;
import io.personalityrecognition.util.TypeCounter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class PersonalityRecognition {

	public static void main(String args[]) {
		try {
			DataShaper shaper = new DataShaper("mypersonality_final.csv");
			HashMap<String, PersonalityData> data = shaper.shapeData().getUsers();
			HashSet<String> words = new HashSet<String>();
			HashSet<String> acceptedWords = shaper.getAcceptedTokens();
			for(Map.Entry<String, PersonalityData> row : data.entrySet()) {
				words.addAll(row.getValue().getWordCounts().keySet());
			}
			List<String> alpha = setToAlphabeticalList(words);
			System.out.println("In shaper: " + acceptedWords.size());
			System.out.println("In data: " + words.size());
			acceptedWords.removeAll(words);
			for(String word : acceptedWords) {
				System.out.println(word);
			}
			PersonalityDataWriter.writeFile(alpha, data, "massagedData.csv");
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void printTopNTokenCounts(int n, List<Map.Entry<String, Integer>> counts) {
		int numberOfTypes = counts.size();
		
		if(numberOfTypes <= n) {
			for(Map.Entry<String, Integer> count : counts) {
				System.out.println(String.format("%s: %s", count.getKey(), count.getValue()));
			}
		} else {
			for(int i = 0; i < n; i++) {
				Map.Entry<String, Integer> count = counts.get(i);
				System.out.println(String.format("%s: %s", count.getKey(), count.getValue()));
			}
		}
	}
	
	public static LinkedList<String> setToAlphabeticalList(Set<String> words) {
		LinkedList<String> orderedWords = new LinkedList<String>(words);
		Collections.sort(orderedWords);
		return orderedWords;
	}
	
	public static String encapsulateToken(String s) {
		return "[" + s + "]";
	}
}
