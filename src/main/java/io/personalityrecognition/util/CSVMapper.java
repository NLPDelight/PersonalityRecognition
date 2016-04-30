package io.personalityrecognition.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVMapper {

	private static final String CHAR_FORMAT = "UTF-8";

	/**
	 * Basic mapping of csv file to a list of rows. Each row is represented by a mapping column name to value. All
	 * values are strings. All rows are contained in a list.
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static List<Map<String, String>> mapCSV(File file)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		List<Map<String, String>> table = new ArrayList<>();
		CSVParser parser = getParser(file);
		
		for(CSVRecord tuple : parser)
			table.add(tuple.toMap());

		parser.close();
		return table;
	}
	
	/**
	 * Returns a parser object representation of the CSV file. Acts like an input stream that pumps out CSVRecord
	 * objects when iterated over. Good for custom aggregations. Also, feel free to add aggregation methods to this
	 * class, and use this function as a first step.
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static CSVParser getParser(File file) throws FileNotFoundException, UnsupportedEncodingException, IOException  {
		InputStream in = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(in, CHAR_FORMAT);
		CSVFormat format = CSVFormat.RFC4180.withHeader();
		return new CSVParser(reader, format);
	}
}