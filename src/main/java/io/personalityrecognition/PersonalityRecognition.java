package io.personalityrecognition;

import io.personalityrecognition.util.CSVMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
			
			for(int i = 0; i < 100; i++) {
				System.out.println(tokenizeString(data.get(i).get("STATUS"), tokenizer));
			}
		} catch(Exception e) {
			e.printStackTrace();
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
