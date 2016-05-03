package io.personalityrecognition;

import io.personalityrecognition.util.CSVMapper;
import io.personalityrecognition.util.DataShaper;
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
			DataShaper shaper = new DataShaper("mypersonality_final.csv");
			shaper.shapeData();
			
			System.out.println("data shaped!");
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
	
	public static String encapsulateToken(String s) {
		return "[" + s + "]";
	}
}
