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

	/**
	 * This class outputs processed PCA data into an output file.
	 *
	 * @param
	 * @return
	 * @throws
	 */
public class PCADataWriter {

	private static List<String> WORD_ORDER;

	/**
	 * This method write the data into file
	 *
	 * @param String, String, String
	 * @return
	 * @throws FileNotFoundException, UnsupportedEncodingException, IOException
	 */
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

	/**
	 * This method calls orderset
	 *
	 * @param String
	 * @return List<Map.Entry<String, PersonalityData>>
	 * @throws FileNotFoundException, UnsupportedEncodingException, IOException
	 */
	private static List<Map.Entry<String, PersonalityData>> orderedData(String fileName)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		return orderSet(PersonalityDataReader.readPersonalityData(fileName).entrySet());
	}

	/**
	 * This method write PCAData to CSV
	 *
	 * @param Matrix, List<Map.Entry<String, PersonalityData>>, String
	 * @return
	 * @throws IOException
	 */
	private static void writePCAData(Matrix dimensions, List<Map.Entry<String, PersonalityData>> data, String fileName)
			throws IOException {
		BufferedWriter csvWriter = new BufferedWriter(new FileWriter(fileName));
		//csvWriter.append(getHeaderRow(dimensions.getColumnDimension()));
		csvWriter.append(getCSVBody(dimensions, data));
		csvWriter.close();
	}

	/**
	 * This method parses CSV
	 *
	 * @param Matrix, List<Map.Entry<String, PersonalityData>>
	 * @return String
	 * @throws
	 */
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

	/**
	 * This method converts boolean to double on all traits
	 *
	 * @param Map.Entry<String, PersonalityData>
	 * @return String
	 * @throws
	 */
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

	/**
	 * This method converts boolean to double
	 *
	 * @param boolean
	 * @return double
	 * @throws
	 */
	private static double booleanToDouble(boolean bool) {
		return bool ? 1 : 0;
	}

	/**
	 * This method gets the header row of CSV
	 *
	 * @param int
	 * @return String
	 * @throws
	 */
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

	/**
	 * This method calls PCA transform
	 *
	 * @param PCA, List<Map.Entry<String, PersonalityData>>
	 * @return Matrix
	 * @throws
	 */
	private static Matrix transformDataToPCAMatrix(PCA reducer, List<Map.Entry<String, PersonalityData>> data) {
		return reducer.transform(dataAsMatrix(data), PCA.TransformationType.WHITENING);
	}

	/**
	 * This method creates new PCA
	 *
	 * @param List<Map.Entry<String, PersonalityData>>
	 * @return PCA
	 * @throws
	 */
	private static PCA doPCA(List<Map.Entry<String, PersonalityData>> data) {
		return new PCA(dataAsMatrix(data));
	}

	/**
	 * This method converts data into matrix
	 *
	 * @param List<Map.Entry<String, PersonalityData>>
	 * @return Matrix
	 * @throws
	 */
	private static Matrix dataAsMatrix(List<Map.Entry<String, PersonalityData>> data) {
		double[][] matrix = new double[data.size()][1000];
		int i = 0;
		for(Map.Entry<String, PersonalityData> id : data) {
			matrix[i] = frequenciesToArray(id.getValue().getWordFrequencies(), WORD_ORDER);
			i++;
		}
		return new Matrix(matrix);
	}

	/**
	 * This method converts frequencies into double array
	 *
	 * @param HashMap<String, Double>, List<String>
	 * @return double[]
	 * @throws
	 */
	public static double[] frequenciesToArray(HashMap<String, Double> data, List<String> wordOrder) {
		double[] arr = new double[wordOrder.size()];
		for(int i = 0; i < wordOrder.size(); i++) {
			arr[i] = data.get(wordOrder.get(i));
		}
		return arr;
	}

	/**
	 * This method calls orderSet for data map
	 *
	 * @param HashMap<String, T>
	 * @return <T> List<String>
	 * @throws
	 */
	public static <T> List<String> getRowOrder(HashMap<String, T> data) {
		return orderSet(data.keySet());
	}

	/**
	 * This method calls orderSet for word frequencies
	 *
	 * @param List<Map.Entry<String, PersonalityData>>
	 * @return List<String>
	 * @throws
	 */
	public static List<String> getWordOrder(List<Map.Entry<String, PersonalityData>> wordFrequencyData) {
		PersonalityData firstRow = wordFrequencyData.get(0).getValue();
		return orderSet(firstRow.getWordFrequencies().keySet());
	}

	/**
	 * This method converts set to list
	 *
	 * @param Set<T>
	 * @return <T> List<T>
	 * @throws
	 */
	public static <T> List<T> orderSet(Set<T> set) {
		return new LinkedList<T>(set);
	}

	/**
	 * This method wraps string in quotes
	 *
	 * @param String
	 * @return String
	 * @throws
	 */
	private static String wrapInQuotes(String unquotedText) {
		return String.format("\"%s\"", unquotedText);
	}

}
