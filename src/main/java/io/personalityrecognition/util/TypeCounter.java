package io.personalityrecognition.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class TypeCounter {

	private Tokenizer tokenizer;
	private TokenizerModel model;
	private HashSet<String> stopwords;

	public TypeCounter() {
		tokenizer = SimpleTokenizer.INSTANCE;
		stopwords = new HashSet<String>();
	}

	public TypeCounter(String modelFileName) throws IOException {
		model = new TokenizerModel(new File(modelFileName));
		tokenizer = new TokenizerME(model);
		stopwords = new HashSet<String>();
	}

	public TypeCounter(String modelFileName, String stopwordFileName) throws IOException {
		model = new TokenizerModel(new File(modelFileName));
		tokenizer = new TokenizerME(model);
		stopwords = fileLinesToSet(stopwordFileName);
	}

	private HashSet<String> fileLinesToSet(String stopwordFileName) throws IOException {
		Path fileLocation = new File(stopwordFileName).toPath();
		List<String> stopwords = Files.readAllLines(fileLocation, StandardCharsets.UTF_8);
		return new HashSet<String>(stopwords);
	}

	public String[] tokenize(String line) {
		return tokenizer.tokenize(line);
	}

	public Map<String, Integer> countTypesInSet(String line, Set<String> words) {
		String[] tokens = tokenizer.tokenize(line);
		return countTypesInSet(tokens, words);
	}

	private Map<String, Integer> countTypesInSet(String[] tokens, Set<String> lexicon) {
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i].toLowerCase();
			if(lexicon.contains(token))
				addTokenToCounts(token, counts);
		}
		return counts;
	}

	public Map<String, Integer> countTypes(String line) {
		String[] tokens = tokenizer.tokenize(line);
		return countTypes(tokens);
	}

	private Map<String, Integer> countTypes(String[] tokens) {
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i].toLowerCase();
			if(!stopwords.contains(token))
				addTokenToCounts(token, counts);
		}
		return counts;
	}

	private void addTokenToCounts(String token, Map<String, Integer> counts) {
		if(counts.containsKey(token))
			counts.put(token, counts.get(token) + 1);
		else
			counts.put(token, 1);
	}

}
