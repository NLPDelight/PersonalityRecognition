package io.personalityrecognition;

import io.personalityrecognition.util.CSVMapper;

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
		File csv = new File("mypersonality_final.csv");
		try {
			List<Map<String, String>> data = CSVMapper.mapCSV(csv);
			InputStream text = new FileInputStream("en-token.bin");
			TokenizerModel model = new TokenizerModel(text);
			Tokenizer tokenizer = new TokenizerME(model);
			List<Map.Entry<String, Integer>> counts = sortCounts(getTokenCounts(tokenizer, "mypersonality_final.csv"));
			printTokenCounts(counts);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<Map.Entry<String, Integer>> sortCounts(Map<String, Integer> counts) {
		LinkedList<Map.Entry<String, Integer>> rows = new LinkedList<>(counts.entrySet());
		Comparator<Map.Entry<String, Integer>> comp = new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		};
		
		Collections.sort(rows, comp);
		
		return rows;
	}
	
	public static Map<String, Integer> getTokenCounts(Tokenizer tokenizer, String filename) throws Exception {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(filename));
		Map<String, Integer> counts = new HashMap<String, Integer>();
		
		for(Map<String, String> row : data) {
			String[] tokens = tokenizer.tokenize(row.get("STATUS"));
			addTokensToMap(tokens, counts);
		}
		
		return counts;
	}
	
	public static void printTokenCounts(List<Map.Entry<String, Integer>> counts) {
		for(Map.Entry<String, Integer> count : counts) {
			System.out.println(String.format("%s: %s", count.getKey(), count.getValue()));
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
