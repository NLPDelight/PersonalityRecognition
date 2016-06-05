package io.personalityrecognition.util;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mkobos.pca_transform.PCA;

import static io.personalityrecognition.util.DatasetKeys.*;
import Jama.Matrix;

// This class outputs processed PCA data into an output file.
public class PCADataWriter {

	private static List<String> WORD_ORDER;

	public static void writeDataAsPCA(String trainingFile, String testFile, String dataSetName)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		List<Map.Entry<String, PersonalityData>> trainingData = orderedData(trainingFile);
		List<Map.Entry<String, PersonalityData>> testData = orderedData(testFile);
		WORD_ORDER = getWordOrder(trainingData);
		PCA reducer = doPCA(trainingData);
		Matrix trainingComponents = transformDataToPCAMatrix(reducer, trainingData);
		Matrix testComponents = transformDataToPCAMatrix(reducer, testData);

		writePCAData(trainingComponents, trainingData, dataSetName + "_train.csv");
		writePCAData(testComponents, testData, dataSetName + "_test.csv");
	}

	private static List<Map.Entry<String, PersonalityData>> orderedData(String fileName)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		return orderSet(PersonalityDataReader.readPersonalityData(fileName).entrySet());
	}

	private static void writePCAData(Matrix dimensions, List<Map.Entry<String, PersonalityData>> data, String fileName)
			throws IOException {
		BufferedWriter csvWriter = new BufferedWriter(new FileWriter(fileName));
		//csvWriter.append(getHeaderRow(dimensions.getColumnDimension()));
		csvWriter.append(getCSVBody(dimensions, data));
		csvWriter.close();
	}

	private static String getCSVBody(Matrix dimensions, List<Map.Entry<String, PersonalityData>> data) {
		int rows = dimensions.getRowDimension();
		int cols = dimensions.getColumnDimension();
		String csvBody = "";
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				csvBody += dimensions.get(i, j) + ",";
			}
			csvBody += csvifyClasses(data.get(i));
			csvBody += "\n";
		}
		return csvBody;
	}

	private static String csvifyClasses(Map.Entry<String, PersonalityData> row) {
		PersonalityData user = row.getValue();
		return
				//user.getUserId() + "," +
				booleanToDouble(user.isExtraverted()) + "," +
				booleanToDouble(user.isNeurotic()) + "," +
				booleanToDouble(user.isAgreeable()) + "," +
				booleanToDouble(user.isConscientious()) + "," +
				booleanToDouble(user.isOpen());
	}

	private static double booleanToDouble(boolean bool) {
		return bool ? 1 : 0;
	}

	private static String getHeaderRow(int dimensions) {
		String header = wrapInQuotes(ID) + ",";
		for(String trait : TRAIT_CLASSES) {
			header += wrapInQuotes(trait) + ",";
		}

		int dimensionNumber = 1;
		for(; dimensionNumber < dimensions; dimensionNumber++) {
			header += wrapInQuotes("D_" + dimensionNumber) + ",";
		}

		header += wrapInQuotes("D_" + dimensionNumber) + "\n";
		return header;

	}

	private static Matrix transformDataToPCAMatrix(PCA reducer, List<Map.Entry<String, PersonalityData>> data) {
		return reducer.transform(dataAsMatrix(data), PCA.TransformationType.WHITENING);
	}

	private static PCA doPCA(List<Map.Entry<String, PersonalityData>> data) {
		return new PCA(dataAsMatrix(data));
	}

	private static Matrix dataAsMatrix(List<Map.Entry<String, PersonalityData>> data) {
		double[][] matrix = new double[data.size()][1000];
		int i = 0;
		for(Map.Entry<String, PersonalityData> id : data) {
			matrix[i] = frequenciesToArray(id.getValue().getWordFrequencies(), WORD_ORDER);
			i++;
		}
		return new Matrix(matrix);
	}

	public static double[] frequenciesToArray(HashMap<String, Double> data, List<String> wordOrder) {
		double[] arr = new double[wordOrder.size()];
		for(int i = 0; i < wordOrder.size(); i++) {
			arr[i] = data.get(wordOrder.get(i));
		}
		return arr;
	}

	public static <T> List<String> getRowOrder(HashMap<String, T> data) {
		return orderSet(data.keySet());
	}

	public static List<String> getWordOrder(List<Map.Entry<String, PersonalityData>> wordFrequencyData) {
		PersonalityData firstRow = wordFrequencyData.get(0).getValue();
		return orderSet(firstRow.getWordFrequencies().keySet());
	}

	public static <T> List<T> orderSet(Set<T> set) {
		return new LinkedList<T>(set);
	}

	private static String wrapInQuotes(String unquotedText) {
		return String.format("\"%s\"", unquotedText);
	}

}
