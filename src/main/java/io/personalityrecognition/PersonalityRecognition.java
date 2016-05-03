package io.personalityrecognition;

import io.personalityrecognition.util.CSVMapper;
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
			TypeCounter counter = new TypeCounter("en-token.bin", "stopwordList.txt");
			List<Map.Entry<String, Integer>> counts = sortCounts(getTokenCounts(counter, "mypersonality_final.csv"));
			printTopNTokenCounts(1000, counts);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<Map.Entry<String, Integer>> sortCounts(Map<String, Integer> counts) {
		LinkedList<Map.Entry<String, Integer>> rows = new LinkedList<>(counts.entrySet());
		Comparator<Map.Entry<String, Integer>> comp = new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		};
		
		Collections.sort(rows, comp);
		
		return rows;
	}
	
	public static Map<String, Integer> getTokenCounts(TypeCounter counter, String filename) throws Exception {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		Map<String, Integer> counts = new HashMap<String, Integer>();
		
		for(Map<String, String> row : data) {
			addCounts(counts, counter.countTypes(row.get("STATUS")));
		}
		
		return counts;
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
	
	public static void addTokensToMap(String[] tokens, Map<String, Integer> map) {
		for(int i = 0; i < tokens.length; i++) {
			String word = tokens[i].toLowerCase();
			if(map.containsKey(word)) {
				map.put(word, map.get(word) + 1);
			} else {
				map.put(word, 1);
			}
		}
	}
	
	public static void addCounts(Map<String, Integer> target, Map<String, Integer> source) {
		for(String key : source.keySet()) {
			if(target.containsKey(key))
				target.put(key, target.get(key) + source.get(key));
			else
				target.put(key, source.get(key));
		}
	}
	
	public static String tokenizeString(String text, Tokenizer tokenizer) {
		String[] tokens = tokenizer.tokenize(text);
		String line = "";
		
		for(int i = 0; i < tokens.length; i++) {
			line += encapsulateToken(tokens[i]);
		}
		
		return line;
	}
	
	public static String getAllTextAsInputStream(String filename) throws Exception {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		String text = "";
		for(Map<String, String> row : data) {
			text += " " + row.get("STATUS");
		}
		return text;
	}
	
	public static String encapsulateToken(String s) {
		return "[" + s + "]";
	}
}
