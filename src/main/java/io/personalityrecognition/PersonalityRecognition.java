package io.personalityrecognition;

import io.personalityrecognition.util.CSVMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PersonalityRecognition {

	public static void main(String args[]) {
		File csv = new File("mypersonality_final.csv");
		try {
			List<Map<String, String>> data = CSVMapper.mapCSV(csv);
			Map<String, String> header = data.get(0);
			
			for(String key : header.keySet()) {
				System.out.println(key);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
