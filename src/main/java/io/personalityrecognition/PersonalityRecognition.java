package io.personalityrecognition;

import io.personalityrecognition.util.DataShaper;
import io.personalityrecognition.util.PersonalityData;
import io.personalityrecognition.util.PersonalityDataWriter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
			
			HashMap<String, PersonalityData> train = new HashMap<>();
			HashMap<String, PersonalityData> test = new HashMap<>();
			Set<Map.Entry<String, PersonalityData>> entries = data.entrySet();
			int i = 0;
			
			for(Map.Entry<String, PersonalityData> row : entries) {
				if(i < 200)
					train.put(row.getKey(), row.getValue());
				else
					test.put(row.getKey(), row.getValue());
				i++;
			}
			
			PersonalityDataWriter.writeFile(alpha, train, "train.csv");
			PersonalityDataWriter.writeFile(alpha, test, "test.csv");
			
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
